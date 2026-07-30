package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.db.CategoryPriceEntity
import com.example.data.db.FinanceEntity
import com.example.data.db.FixedCostConfigEntity
import com.example.data.db.ReservationEntity
import com.example.ui.components.EmptyState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    finances: List<FinanceEntity>,
    totalBalancePln: Double,
    fixedCosts: List<FixedCostConfigEntity>,
    categoryPrices: List<CategoryPriceEntity>,
    reservations: List<ReservationEntity>,
    onAddFinanceClick: () -> Unit,
    onDeleteFinance: (Long) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var activeTab by remember { mutableStateOf(0) } // 0: BILANS & STATS, 1: HISTORIA TRANSAKCJI
    var showReportDialog by remember { mutableStateOf(false) }
    var searchLedgerQuery by remember { mutableStateOf("") }

    // Calculate current month boundaries
    val calendar = Calendar.getInstance()
    val currentMonthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale("pl")) ?: "Bieżący miesiąc"
    val yearStr = calendar.get(Calendar.YEAR).toString()
    
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfMonthMs = calendar.timeInMillis

    // --- FINANCIAL METRICS COMPUTATION ---
    // 1. Enrolled Count this month (Approved reservations or simply all new reservations in current month)
    val enrolledThisMonth = remember(reservations, startOfMonthMs) {
        reservations.filter { it.createdAt >= startOfMonthMs }
    }
    val enrolledCount = enrolledThisMonth.size

    // 2. Expected course revenues based on category price configurations
    val expectedRevenues = remember(enrolledThisMonth, categoryPrices) {
        enrolledThisMonth.sumOf { student ->
            categoryPrices.find { it.category == student.category }?.priceCoursePln ?: 3200.0
        }
    }

    // 3. Paid Amount (Course fees collected / General Income)
    val totalIncome = remember(finances) {
        finances.filter { it.type == "INCOME" }.sumOf { it.amountPln }
    }

    // 4. Outstanding Amount (Amount still expected but not yet paid)
    val outstandingAmount = remember(expectedRevenues, totalIncome) {
        maxOf(0.0, expectedRevenues - totalIncome)
    }

    // 5. Fixed costs from config
    val totalFixedCosts = remember(fixedCosts) {
        fixedCosts.sumOf { it.amountPln }
    }

    // 6. General expenses
    val totalExpenses = remember(finances) {
        finances.filter { it.type == "EXPENSE" }.sumOf { it.amountPln }
    }

    // 7. Profit / Loss calculation taking into account Revenues, Expenses, and Fixed Costs
    val totalCostsAndExpenses = totalExpenses + totalFixedCosts
    val netProfit = totalIncome - totalCostsAndExpenses
    val isNetProfitPositive = netProfit >= 0.0

    Scaffold(
        containerColor = Color(0xFF0B0F19),
        floatingActionButton = {
            if (activeTab == 1) {
                FloatingActionButton(
                    onClick = onAddFinanceClick,
                    containerColor = Color(0xFFEF4444),
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_finance_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Dodaj Transakcję")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Header Screen Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FINANSE & ROZLICZENIA",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Statystyki zysków, koszty stałe i bilans kasy OSK",
                        fontSize = 11.sp,
                        color = Color(0xFF38BDF8)
                    )
                }

                Button(
                    onClick = { showReportDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generuj Raport", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation Tabs (Bilans i Koszty vs Księga Transakcji)
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color(0xFF111827),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]), // wait, TabRowDefaults.tabIndicatorOffset or just standard TabRowDefaults.Indicator
                        color = Color(0xFFEF4444)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Bilans & Statystyki", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Księga Transakcji (${finances.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            if (activeTab == 0) {
                // --- TAB 0: BILANS & MONTHLY STATS ---
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Balance Hero Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF0F172A), Color(0xFF0F766E))
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "CAŁKOWITY BILANS KASY OSK",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.7f),
                                    letterSpacing = 0.5.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = String.format("%.2f zł", totalBalancePln),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(Color(0xFF10B981), CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Przychody", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                        }
                                        Text(String.format("+%.2f zł", totalIncome), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF34D399))
                                    }

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(Color(0xFFEF4444), CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Wydatki", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                        }
                                        Text(String.format("-%.2f zł", totalExpenses), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFF87171))
                                    }

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(Color(0xFFF59E0B), CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Koszty Stałe", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                        }
                                        Text(String.format("-%.2f zł", totalFixedCosts), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFBBF24))
                                    }
                                }
                            }
                        }
                    }

                    // Monthly Statistics Panel
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.05f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "STATYSTYKI MIESIĘCZNE: ${currentMonthName.uppercase()} $yearStr",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF8B5CF6),
                                    letterSpacing = 0.8.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                // Item 1: Enrolled Count
                                StatRow(
                                    label = "Zapisane osoby w tym miesiącu:",
                                    value = "$enrolledCount osób",
                                    icon = Icons.Default.People,
                                    iconColor = Color(0xFF38BDF8)
                                )

                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 10.dp))

                                // Item 2: Expected Revenues
                                StatRow(
                                    label = "Oczekiwane wpłaty za kursy:",
                                    value = String.format("%.2f zł", expectedRevenues),
                                    icon = Icons.Default.QueryStats,
                                    iconColor = Color(0xFFA855F7)
                                )

                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 10.dp))

                                // Item 3: Paid amount
                                StatRow(
                                    label = "Rzeczywiście wpłacono (Przychód):",
                                    value = String.format("%.2f zł", totalIncome),
                                    icon = Icons.Default.CheckCircle,
                                    iconColor = Color(0xFF10B981)
                                )

                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 10.dp))

                                // Item 4: Outstanding amount
                                StatRow(
                                    label = "Zaległa kwota (Należności):",
                                    value = String.format("%.2f zł", outstandingAmount),
                                    valueColor = if (outstandingAmount > 0.0) Color(0xFFF59E0B) else Color.White,
                                    icon = Icons.Default.Warning,
                                    iconColor = Color(0xFFF59E0B)
                                )

                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 10.dp))

                                // Item 5: Fixed Costs
                                StatRow(
                                    label = "Koszty stałe szkoły (ZUS, biuro, itp):",
                                    value = String.format("%.2f zł", totalFixedCosts),
                                    icon = Icons.Default.AccountBalance,
                                    iconColor = Color(0xFFF87171)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // PROFIT AND LOSS ACCORDING TO FORMULA (Zysk/Strata Netto)
                                Surface(
                                    color = if (isNetProfitPositive) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f),
                                    border = BorderStroke(1.2.dp, if (isNetProfitPositive) Color(0xFF10B981) else Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "WYNIK FINANSOWY NETTO",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (isNetProfitPositive) Color(0xFF34D399) else Color(0xFFFCA5A5),
                                                letterSpacing = 0.5.sp
                                            )
                                            Text(
                                                text = "Przychody - Koszty Stałe - Wydatki",
                                                fontSize = 10.sp,
                                                color = Color.White.copy(alpha = 0.5f)
                                            )
                                        }

                                        Text(
                                            text = String.format("%s%.2f zł", if (isNetProfitPositive) "+" else "", netProfit),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isNetProfitPositive) Color(0xFF10B981) else Color(0xFFEF4444)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Configured Fixed Costs Breakdown
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.05f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ZESTAWIENIE KOSZTÓW STAŁYCH",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFF59E0B),
                                        letterSpacing = 0.8.sp
                                    )
                                    
                                    Surface(
                                        color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "${fixedCosts.size} pozycji",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF59E0B),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (fixedCosts.isEmpty()) {
                                    Text(
                                        text = "Brak skonfigurowanych kosztów stałych. Dodaj je w karcie 'Opcje'.",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    fixedCosts.forEach { cost ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 5.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Remove, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(text = cost.name, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                                            }
                                            Text(
                                                text = String.format("%.2f zł", cost.amountPln),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // --- TAB 1: TRANSACTION LEDGER LIST ---
                // Search Field inside ledger
                OutlinedTextField(
                    value = searchLedgerQuery,
                    onValueChange = { searchLedgerQuery = it },
                    placeholder = { Text("Szukaj transakcji...", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFFEF4444)) },
                    trailingIcon = {
                        if (searchLedgerQuery.isNotEmpty()) {
                            IconButton(onClick = { searchLedgerQuery = "" }) {
                                Icon(Icons.Default.Clear, null, tint = Color.White)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFEF4444),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = Color(0xFF111827),
                        unfocusedContainerColor = Color(0xFF111827)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("ledger_search_field")
                )

                val filteredLedger = remember(finances, searchLedgerQuery) {
                    finances.filter {
                        it.title.contains(searchLedgerQuery, ignoreCase = true) ||
                        it.category.contains(searchLedgerQuery, ignoreCase = true)
                    }
                }

                if (filteredLedger.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        EmptyState(
                            icon = Icons.Default.AccountBalanceWallet,
                            title = "Brak pasujących wpisów",
                            description = "Nie znaleziono żadnych wpisów pasujących do frazy \"$searchLedgerQuery\".",
                            testTagPrefix = "finance_search"
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredLedger, key = { it.id }) { item ->
                            FinanceRowCard(
                                item = item,
                                onDelete = { onDeleteFinance(item.id) }
                            )
                        }
                    }
                }
            }
        }

        // --- EXPORT & GENERATE REPORT MODAL DIALOG ---
        if (showReportDialog) {
            val reportContent = remember(
                currentMonthName,
                yearStr,
                enrolledCount,
                expectedRevenues,
                totalIncome,
                outstandingAmount,
                totalFixedCosts,
                totalExpenses,
                netProfit,
                fixedCosts
            ) {
                buildString {
                    appendLine("=============================================")
                    appendLine("      RAPORT FINANSOWY OSK MENEDŻER          ")
                    appendLine("      Okres: $currentMonthName $yearStr     ")
                    appendLine("=============================================")
                    appendLine("Zapisane osoby w tym miesiącu:  $enrolledCount")
                    appendLine("Oczekiwane wpłaty za kursy:     ${String.format("%.2f", expectedRevenues)} zł")
                    appendLine("Suma wpłacona (Rzeczywista):    ${String.format("%.2f", totalIncome)} zł")
                    appendLine("Suma zaległa (Należności):      ${String.format("%.2f", outstandingAmount)} zł")
                    appendLine("---------------------------------------------")
                    appendLine("KOSZTY STAŁE SZKOŁY JAZDY:")
                    if (fixedCosts.isEmpty()) {
                        appendLine(" - Brak pozycji")
                    } else {
                        fixedCosts.forEach { cost ->
                            appendLine(" - ${cost.name}: ${String.format("%.2f", cost.amountPln)} zł")
                        }
                    }
                    appendLine("SUMA KOSZTÓW STAŁYCH:           ${String.format("%.2f", totalFixedCosts)} zł")
                    appendLine("SUMA INNYCH WYDATKÓW (zmienne):  ${String.format("%.2f", totalExpenses)} zł")
                    appendLine("=============================================")
                    appendLine("WYNIK FINANSOWY NETTO:")
                    appendLine("NETTO:                          ${String.format("%.2f", netProfit)} zł")
                    appendLine("Status:                         ${if (netProfit >= 0) "ZYSK" else "STRATA"}")
                    appendLine("=============================================")
                    appendLine("Wygenerowano automatycznie w aplikacji OSK Menedżer")
                    appendLine("Data generowania: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
                }
            }

            Dialog(onDismissRequest = { showReportDialog = false }) {
                Surface(
                    color = Color(0xFF111827),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .wrapContentHeight()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Assessment, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("RAPORT FINANSOWY OSK", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                        Text(
                            text = "Poniżej znajduje się zestawienie zysków i strat gotowe do skopiowania lub pobrania w formacie pliku tekstowego / PDF na Twoje urządzenie.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )

                        // Scrollable Report Area
                        Surface(
                            color = Color(0xFF0B0F19),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            Box(modifier = Modifier.padding(8.dp)) {
                                LazyColumn {
                                    item {
                                        Text(
                                            text = reportContent,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            color = Color(0xFF34D399)
                                        )
                                    }
                                }
                            }
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(reportContent))
                                    Toast.makeText(context, "📋 Raport skopiowano do schowka!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Skopiuj", color = Color.White, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    // Simulate downloading file PDF/Text to Device
                                    Toast.makeText(context, "⬇️ Pobieranie pliku 'raport_finansowy_${currentMonthName}_${yearStr}.pdf'...", Toast.LENGTH_LONG).show()
                                    showReportDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Icon(Icons.Default.GetApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pobierz PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        TextButton(
                            onClick = { showReportDialog = false },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Zamknij", color = Color.White.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    valueColor: Color = Color.White,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                color = iconColor.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun FinanceRowCard(
    item: FinanceEntity,
    onDelete: () -> Unit
) {
    val isIncome = item.type == "INCOME"
    val color = if (isIncome) Color(0xFF10B981) else Color(0xFFEF4444)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = color.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${item.category} • ${item.date}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = String.format("%s%.2f zł", if (isIncome) "+" else "-", item.amountPln),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Usuń", tint = Color(0xFFEF4444).copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
