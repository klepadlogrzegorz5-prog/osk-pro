package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.*
import com.example.ui.viewmodel.OskViewModel
import com.example.ui.viewmodel.UserRole

@Composable
fun OptionsScreen(
    viewModel: OskViewModel
) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val savedPin by viewModel.savedPin.collectAsStateWithLifecycle()
    var showPinSetupDialog by remember { mutableStateOf(false) }
    val categoryPrices by viewModel.categoryPrices.collectAsStateWithLifecycle()
    val fixedCosts by viewModel.fixedCosts.collectAsStateWithLifecycle()
    val instructorRates by viewModel.instructorRates.collectAsStateWithLifecycle()
    val instructorDaysOff by viewModel.instructorDaysOff.collectAsStateWithLifecycle()
    val syncStatusMessage by viewModel.syncStatusMessage.collectAsStateWithLifecycle()
    val userKeys by viewModel.userKeys.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    val instructorsList = remember(userKeys) {
        val dbInstructors = userKeys.filter { it.role == "Instruktor" }.map { it.assignedName }
        if (dbInstructors.isEmpty()) {
            listOf("Piotr Nowak (Instruktor)", "Tomasz Zieliński")
        } else {
            dbInstructors
        }
    }

    var activeDialogType by remember { mutableStateOf<ConfigDialogType?>(null) }
    var showClearSimulationConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF0B0F19)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Opcje & Ustawienia",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Skonfiguruj parametry operacyjne i finansowe OSK",
                    fontSize = 13.sp,
                    color = Color(0xFF38BDF8)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                OptionsSectionHeader("KONTO I BEZPIECZEŃSTWO")
                OptionsCard(
                    title = if (savedPin != null) "Wyłącz logowanie PIN" else "Włącz logowanie PIN",
                    description = if (savedPin != null) "Aktualnie logujesz się 4-cyfrowym PIN-em." else "Skonfiguruj 4-cyfrowy PIN, aby ominąć ekran wyboru ról i szybciej się logować.",
                    icon = Icons.Default.Lock,
                    iconColor = Color(0xFF10B981),
                    onClick = { 
                        if (savedPin != null) {
                            viewModel.disablePinLogin()
                            android.widget.Toast.makeText(context, "Logowanie PIN zostało wyłączone.", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            showPinSetupDialog = true
                        }
                    }
                )
            }

            if (currentRole == UserRole.INSTRUCTOR) {
                item {
                    OptionsSectionHeader("MOJE USTAWIENIA (INSTRUKTOR)")
                    OptionsCard(
                        title = "Preferencje Harmonogramu",
                        description = "Ustaw domyślne godziny pracy (np. 08:00 - 16:00). Blokuje automatyczne rezerwacje przez kursantów poza tymi godzinami.",
                        icon = Icons.Default.Schedule,
                        iconColor = Color(0xFF8B5CF6),
                        onClick = { activeDialogType = ConfigDialogType.INSTRUCTOR_SCHEDULE }
                    )
                    OptionsCard(
                        title = "Powiadomienia o Nowych Jazdach",
                        description = "Otrzymuj natychmiastowe powiadomienia PUSH gdy kursant zrezerwuje z Tobą jazdę.",
                        icon = Icons.Default.NotificationAdd,
                        iconColor = Color(0xFF10B981),
                        onClick = { activeDialogType = ConfigDialogType.INSTRUCTOR_NOTIFICATIONS }
                    )
                    OptionsCard(
                        title = "Domyślny Pojazd (Raporty)",
                        description = "Przypisz do siebie domyślny pojazd, który z automatu pojawi się przy raportowaniu stanu paliwa.",
                        icon = Icons.Default.DirectionsCar,
                        iconColor = Color(0xFFF59E0B),
                        onClick = { activeDialogType = ConfigDialogType.INSTRUCTOR_VEHICLE }
                    )
                }
            }
            if (currentRole == UserRole.MANAGER) {
                item {
                    OptionsSectionHeader("KONFIGURACJA SYSTEMOWA (ZARZĄDCA)")
                    
                    OptionsCard(
                        title = "Cennik Kategorii OSK",
                        description = "Ustal stawki za kurs lub za godzinę dla kategorii A, B, C, D.",
                        icon = Icons.Default.Payments,
                        iconColor = Color(0xFF10B981),
                        onClick = { activeDialogType = ConfigDialogType.PRICING }
                    )
                    
                    OptionsCard(
                        title = "Koszty Stałe Szkoły",
                        description = "Wprowadź stałe koszty (czynsz, media, biuro) do raportów kasy.",
                        icon = Icons.Default.AccountBalance,
                        iconColor = Color(0xFFEF4444),
                        onClick = { activeDialogType = ConfigDialogType.FIXED_COSTS }
                    )

                    OptionsCard(
                        title = "Dni Wolne Instruktorów",
                        description = "Zarządzaj urlopami. System zablokuje rezerwacje w te dni.",
                        icon = Icons.Default.EventBusy,
                        iconColor = Color(0xFFF59E0B),
                        onClick = { activeDialogType = ConfigDialogType.DAYS_OFF }
                    )

                    OptionsCard(
                        title = "Stawki Instruktorów (Wynagrodzenie)",
                        description = "Wprowadź stawkę godzinową instruktorów do zestawień finansowych.",
                        icon = Icons.Default.Engineering,
                        iconColor = Color(0xFF8B5CF6),
                        onClick = { activeDialogType = ConfigDialogType.PAY_RATES }
                    )
                    
                    OptionsCard(
                        title = "Wyczyść Symulację (Tylko Manager)",
                        description = "Usuwa testowe wpisy, przygotowuje system do komercyjnego działania.",
                        icon = Icons.Default.DeleteForever,
                        iconColor = Color(0xFFEF4444),
                        onClick = { showClearSimulationConfirm = true }
                    )
                }
            }
                
            item {
                OptionsSectionHeader("INTEGRACJE CHMURY FIREBASE & MOST KODU")
                
                if (syncStatusMessage.isNotEmpty()) {
                    Text(
                        text = syncStatusMessage,
                        color = if (syncStatusMessage.contains("ONLINE")) Color(0xFF10B981) else Color(0xFFF59E0B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                OptionsCard(
                    title = "Test Synchronizacji Firebase & Kodu Dostępu",
                    description = "Sprawdź połączenie z chmurą Firebase oraz poprawność działania Mostu Kodu Dostępu i powiadomień PUSH na tym urządzeniu.",
                    icon = Icons.Default.CloudSync,
                    iconColor = Color(0xFF38BDF8),
                    onClick = { viewModel.testFirebaseSync() }
                )
            }

            item {
                OptionsSectionHeader("KONTO I PREFERENCJE")
                OptionsCard(
                    title = "Ustawienia Powiadomień",
                    description = "Zarządzaj powiadomieniami PUSH i alertami.",
                    icon = Icons.Default.Notifications,
                    iconColor = Color(0xFF94A3B8),
                    onClick = {}
                )
                OptionsCard(
                    title = "Zmień Język Aplikacji",
                    description = "Obecny: Polski",
                    icon = Icons.Default.Language,
                    iconColor = Color(0xFF38BDF8),
                    onClick = {}
                )
            }
        }
    }

    // Modal dialogues for each configuration
    when (activeDialogType) {
        ConfigDialogType.PRICING -> PricingConfigDialog(
            prices = categoryPrices,
            onSavePrice = { cat, coursePln, hourPln, active ->
                viewModel.addCategoryPrice(cat, coursePln, hourPln, active)
            },
            onDeletePrice = { cat -> viewModel.deleteCategoryPrice(cat) },
            onDismiss = { activeDialogType = null }
        )
        ConfigDialogType.FIXED_COSTS -> FixedCostsConfigDialog(
            costs = fixedCosts,
            onAddCost = { name, amount -> viewModel.addFixedCost(name, amount) },
            onDeleteCost = { id -> viewModel.deleteFixedCost(id) },
            onDismiss = { activeDialogType = null }
        )
        ConfigDialogType.DAYS_OFF -> DaysOffConfigDialog(
            daysOff = instructorDaysOff,
            instructors = instructorsList,
            onAddDayOff = { inst, date -> viewModel.addInstructorDayOff(inst, date) },
            onDeleteDayOff = { id -> viewModel.deleteInstructorDayOff(id) },
            onDismiss = { activeDialogType = null }
        )
        ConfigDialogType.PAY_RATES -> PayRatesConfigDialog(
            rates = instructorRates,
            instructors = instructorsList,
            onAddRate = { inst, rate -> viewModel.addInstructorRate(inst, rate) },
            onDeleteRate = { inst -> viewModel.deleteInstructorRate(inst) },
            onDismiss = { activeDialogType = null }
        )
        ConfigDialogType.INSTRUCTOR_SCHEDULE -> InstructorScheduleDialog(onDismiss = { activeDialogType = null })
        ConfigDialogType.INSTRUCTOR_NOTIFICATIONS -> InstructorNotificationsDialog(onDismiss = { activeDialogType = null })
        ConfigDialogType.INSTRUCTOR_VEHICLE -> InstructorVehicleDialog(onDismiss = { activeDialogType = null })
        null -> {}
    }

    if (showPinSetupDialog) {
        PinSetupDialog(
            onSavePin = { pin ->
                viewModel.enablePinLogin(pin)
                showPinSetupDialog = false
                android.widget.Toast.makeText(context, "Logowanie PIN zostało włączone.", android.widget.Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showPinSetupDialog = false }
        )
    }
}

enum class ConfigDialogType {
    PRICING, FIXED_COSTS, DAYS_OFF, PAY_RATES, INSTRUCTOR_SCHEDULE, INSTRUCTOR_NOTIFICATIONS, INSTRUCTOR_VEHICLE
}

@Composable
fun OptionsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        color = Color.White.copy(alpha = 0.5f),
        modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
    )
}

@Composable
fun OptionsCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xFF131B2E),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    color = iconColor.copy(alpha = 0.15f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, iconColor.copy(alpha = 0.3f)),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = description, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                }
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
        }
    }
}

// ---------------- CONFIGURATION DIALOGS ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingConfigDialog(
    prices: List<CategoryPriceEntity>,
    onSavePrice: (category: String, coursePrice: Double, hourPrice: Double, active: Boolean) -> Unit,
    onDeletePrice: (category: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCat by remember { mutableStateOf("Kat. B") }
    var priceCourseInput by remember { mutableStateOf("3200") }
    var priceHourInput by remember { mutableStateOf("100") }
    
    var isActive by remember { mutableStateOf(true) }

    val categories = listOf("Kat. A", "Kat. B", "Kat. C", "Kat. D")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payments, null, tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Current price list overview
                Text("ZAPISANE STAWKI:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))
                
                prices.forEach { pr ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(pr.category, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = if (pr.isActive) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        if (pr.isActive) "Aktywna" else "Pominięta",
                                        fontSize = 10.sp,
                                        color = if (pr.isActive) Color(0xFF34D399) else Color(0xFFF87171),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                "Kurs: ${String.format("%.0f zł", pr.priceCoursePln)} | 1h: ${String.format("%.0f zł", pr.priceHourPln)}",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { onDeletePrice(pr.category) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Text("DODAJ LUB EDYTUJ KATEGORIĘ:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))

                // Dropdown of Category
                Column {
                    Text("Kategoria", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.forEach { cat ->
                            val isSel = selectedCat == cat
                            Surface(
                                color = if (isSel) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF1E293B),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isSel) Color(0xFF10B981) else Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedCat = cat }
                            ) {
                                Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                                    Text(cat, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Price Fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceCourseInput,
                        onValueChange = { priceCourseInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Cena za kurs (PLN)", color = Color.White.copy(alpha = 0.7f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = priceHourInput,
                        onValueChange = { priceHourInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Cena za 1h (PLN)", color = Color.White.copy(alpha = 0.7f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }



                // Is active / skip switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Prowadź szkolenia w tej kategorii", color = Color.White, fontSize = 14.sp)
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF10B981),
                            checkedTrackColor = Color(0xFF10B981).copy(alpha = 0.4f)
                        )
                    )
                }

                Button(
                    onClick = {
                        val parsedCourse = priceCourseInput.toDoubleOrNull() ?: 0.0
                        val parsedHour = priceHourInput.toDoubleOrNull() ?: 0.0
                        onSavePrice(selectedCat, parsedCourse, parsedHour, isActive)
                        priceCourseInput = ""
                        priceHourInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Zapisz w Cenniku", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedCostsConfigDialog(
    costs: List<FixedCostConfigEntity>,
    onAddCost: (String, Double) -> Unit,
    onDeleteCost: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalance, null, tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Text("AKTUALNE KOSZTY STAŁE:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))

                if (costs.isEmpty()) {
                    Text("Brak wpisanych kosztów stałych.", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    costs.forEach { c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(c.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("Miesięczny koszt stały", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    String.format("%.2f zł", c.amountPln),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF87171),
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                IconButton(onClick = { onDeleteCost(c.id) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Text("DODAJ NOWY KOSZT STAŁY:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Nazwa kosztu (np. Czynsz placu)", color = Color.White.copy(alpha = 0.7f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFEF4444),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Kwota (PLN)", color = Color.White.copy(alpha = 0.7f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFEF4444),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (nameInput.isNotBlank() && amountInput.isNotBlank()) {
                            val amt = amountInput.toDoubleOrNull() ?: 0.0
                            onAddCost(nameInput, amt)
                            nameInput = ""
                            amountInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Zapisz Koszt", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaysOffConfigDialog(
    daysOff: List<InstructorDayOffEntity>,
    instructors: List<String>,
    onAddDayOff: (String, String) -> Unit,
    onDeleteDayOff: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedInst by remember { mutableStateOf(instructors.firstOrNull() ?: "Piotr Nowak (Instruktor)") }
    var dateInput by remember { mutableStateOf("2026-08-16") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EventBusy, null, tint = Color(0xFFF59E0B))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Text("ZABLOKOWANE TERMINY WOLNEGO:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))

                if (daysOff.isEmpty()) {
                    Text("Brak zarejestrowanych dni wolnych.", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                } else {
                    daysOff.forEach { d ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(d.instructorName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(d.date, fontSize = 12.sp, color = Color(0xFFF59E0B))
                                }
                            }
                            IconButton(onClick = { onDeleteDayOff(d.id) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Text("ZAREJESTRUJ NOWE WOLNE:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))

                // Selector of instructor
                Column {
                    Text("Wybierz Instruktora", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        instructors.forEach { inst ->
                            val isSel = selectedInst == inst
                            Surface(
                                color = if (isSel) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color(0xFF1E293B),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isSel) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedInst = inst }
                            ) {
                                Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                                    Text(inst.substringBefore(" ("), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Date input
                OutlinedTextField(
                    value = dateInput,
                    onValueChange = { dateInput = it },
                    label = { Text("Data wolnego (format YYYY-MM-DD)", color = Color.White.copy(alpha = 0.7f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFF59E0B),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick buttons for dates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickDates = listOf("2026-08-15", "2026-08-20", "2026-08-25")
                    quickDates.forEach { qDate ->
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { dateInput = qDate }
                        ) {
                            Box(modifier = Modifier.padding(6.dp), contentAlignment = Alignment.Center) {
                                Text(qDate.substring(5), color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        if (dateInput.isNotBlank()) {
                            onAddDayOff(selectedInst, dateInput)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Zarejestruj Wolne", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayRatesConfigDialog(
    rates: List<InstructorRateEntity>,
    instructors: List<String>,
    onAddRate: (String, Double) -> Unit,
    onDeleteRate: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedInst by remember { mutableStateOf(instructors.firstOrNull() ?: "Piotr Nowak (Instruktor)") }
    var rateInput by remember { mutableStateOf("45") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Engineering, null, tint = Color(0xFF8B5CF6))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Text("SKONFIGUROWANE STAWKI GODZINOWE:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))

                if (rates.isEmpty()) {
                    Text("Brak stawek. Instruktorzy mają stawkę domyślną (40 zł/h).", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                } else {
                    rates.forEach { r ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(r.instructorName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("Wynagrodzenie za godzinę jazdy", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    String.format("%.2f zł/h", r.hourlyRatePln),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA78BFA),
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                IconButton(onClick = { onDeleteRate(r.instructorName) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Text("USTAW STAWKĘ INSTRUKTORA:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))

                // Selector of instructor
                Column {
                    Text("Wybierz Instruktora", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        instructors.forEach { inst ->
                            val isSel = selectedInst == inst
                            Surface(
                                color = if (isSel) Color(0xFF8B5CF6).copy(alpha = 0.2f) else Color(0xFF1E293B),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isSel) Color(0xFF8B5CF6) else Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedInst = inst }
                            ) {
                                Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                                    Text(inst.substringBefore(" ("), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Rate input
                OutlinedTextField(
                    value = rateInput,
                    onValueChange = { rateInput = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Stawka godzinowa (PLN/h)", color = Color.White.copy(alpha = 0.7f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (rateInput.isNotBlank()) {
                            val rateVal = rateInput.toDoubleOrNull() ?: 0.0
                            onAddRate(selectedInst, rateVal)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Zapisz Stawkę", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSetupDialog(
    onSavePin: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Wpisz 4-cyfrowy PIN, którego będziosz używać do szybkiego logowania na to urządzenie.", fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { 
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            pinInput = it
                            error = ""
                        }
                    },
                    label = { Text("Nowy PIN (4 cyfry)", color = Color.White.copy(alpha = 0.7f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (error.isNotEmpty()) {
                    Text(error, color = Color(0xFFEF4444), fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Anuluj", color = Color.White.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (pinInput.length == 4) {
                                onSavePin(pinInput)
                            } else {
                                error = "PIN musi mieć dokładnie 4 cyfry."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Zapisz PIN", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorScheduleDialog(onDismiss: () -> Unit) {
    var startHour by remember { mutableStateOf("08:00") }
    var endHour by remember { mutableStateOf("16:00") }
    val context = androidx.compose.ui.platform.LocalContext.current

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(color = Color(0xFF0F172A), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth(0.92f).padding(16.dp)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Ustaw domyślne godziny pracy", fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = startHour, onValueChange = { startHour = it },
                        label = { Text("Od", color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFF8B5CF6), unfocusedBorderColor = Color.White.copy(alpha = 0.15f), focusedContainerColor = Color(0xFF1E293B), unfocusedContainerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = endHour, onValueChange = { endHour = it },
                        label = { Text("Do", color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFF8B5CF6), unfocusedBorderColor = Color.White.copy(alpha = 0.15f), focusedContainerColor = Color(0xFF1E293B), unfocusedContainerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Anuluj", color = Color.White.copy(alpha = 0.7f)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { 
                        android.widget.Toast.makeText(context, "Zapisano godziny: $startHour - $endHour", android.widget.Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))) {
                        Text("Zapisz", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun InstructorNotificationsDialog(onDismiss: () -> Unit) {
    var enabled by remember { mutableStateOf(true) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(color = Color(0xFF0F172A), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth(0.92f).padding(16.dp)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Powiadomienia o nowych jazdach", color = Color.White, fontSize = 14.sp)
                    Switch(
                        checked = enabled, 
                        onCheckedChange = { enabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF10B981))
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Anuluj", color = Color.White.copy(alpha = 0.7f)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { 
                        val state = if(enabled) "włączone" else "wyłączone"
                        android.widget.Toast.makeText(context, "Powiadomienia $state", android.widget.Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))) {
                        Text("Zapisz", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorVehicleDialog(onDismiss: () -> Unit) {
    var selectedVehicle by remember { mutableStateOf("Brak domyślnego") }
    val vehiclesList = listOf("Brak domyślnego", "Toyota Yaris (B)", "Hyundai i20 (B)", "Kia Rio (B)")
    var expanded by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(color = Color(0xFF0F172A), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth(0.92f).padding(16.dp)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Wybierz pojazd do raportowania", fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedVehicle,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pojazd", color = Color.White.copy(alpha = 0.7f)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFF59E0B), unfocusedBorderColor = Color.White.copy(alpha = 0.15f), focusedContainerColor = Color(0xFF1E293B), unfocusedContainerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color(0xFF1E293B))
                    ) {
                        vehiclesList.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = Color.White) },
                                onClick = {
                                    selectedVehicle = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Anuluj", color = Color.White.copy(alpha = 0.7f)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { 
                        android.widget.Toast.makeText(context, "Domyślny pojazd: $selectedVehicle", android.widget.Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))) {
                        Text("Zapisz", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
