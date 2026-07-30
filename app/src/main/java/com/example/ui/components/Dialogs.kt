package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.VehicleEntity

@Composable
fun AddVehicleDialog(
    onDismiss: () -> Unit,
    onConfirm: (brand: String, model: String, plate: String, fuel: Int, status: String, inspection: String, insurance: String, instructor: String) -> Unit
) {
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var fuelStr by remember { mutableStateOf("100") }
    var status by remember { mutableStateOf("W pełni sprawny") }
    var inspection by remember { mutableStateOf("2026-12-31") }
    var insurance by remember { mutableStateOf("2027-02-18") }
    var instructor by remember { mutableStateOf("Piotr Nowak") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj Nowy Pojazd do Floty", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Marka (np. Toyota, Hyundai)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vehicle_brand_input")
                )
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Model (np. Yaris, i20)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vehicle_model_input")
                )
                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it.uppercase() },
                    label = { Text("Numer Rejestracyjny (np. WI 7728A)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vehicle_plate_input")
                )
                OutlinedTextField(
                    value = fuelStr,
                    onValueChange = { fuelStr = it },
                    label = { Text("Poziom Paliwa (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = status,
                    onValueChange = { status = it },
                    label = { Text("Stan Techniczny") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = inspection,
                    onValueChange = { inspection = it },
                    label = { Text("Data Następnego Przeglądu (RRRR-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = insurance,
                    onValueChange = { insurance = it },
                    label = { Text("Data Końca Ubezpieczenia (RRRR-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = instructor,
                    onValueChange = { instructor = it },
                    label = { Text("Przypisany Instruktor") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (brand.isNotBlank() && model.isNotBlank() && plate.isNotBlank()) {
                        onConfirm(
                            brand.trim(),
                            model.trim(),
                            plate.trim(),
                            fuelStr.toIntOrNull() ?: 100,
                            status,
                            inspection,
                            insurance,
                            instructor
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("submit_vehicle_button")
            ) {
                Text("Dodaj Pojazd")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anuluj") }
        }
    )
}

@Composable
fun GenerateKeyDialog(
    onDismiss: () -> Unit,
    onConfirm: (role: String, assignedName: String) -> Unit
) {
    var selectedRole by remember { mutableStateOf("Instruktor") }
    var assignedName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wygeneruj Kod Dostępu", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Wybierz rolę użytkownika:", fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedRole == "Instruktor",
                        onClick = { selectedRole = "Instruktor" },
                        label = { Text("Instruktor") }
                    )
                    FilterChip(
                        selected = selectedRole == "Kursant",
                        onClick = { selectedRole = "Kursant" },
                        label = { Text("Kursant") }
                    )
                }
                OutlinedTextField(
                    value = assignedName,
                    onValueChange = { assignedName = it },
                    label = { Text("Imię i Nazwisko odbiorcy") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("key_name_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (assignedName.isNotBlank()) {
                        onConfirm(selectedRole, assignedName.trim())
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("submit_key_button")
            ) {
                Text("Generuj Kod")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

@Composable
fun BookLessonDialog(
    defaultStudent: String = "",
    defaultInstructor: String = "",
    availableVehicles: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (student: String, instructor: String, vehicle: String, date: String, time: String, hours: Int, notes: String) -> Unit
) {
    var studentName by remember { mutableStateOf(defaultStudent) }
    var instructorName by remember { mutableStateOf(defaultInstructor) }
    var selectedVehicle by remember { mutableStateOf(availableVehicles.firstOrNull() ?: "Toyota Yaris (WI 7712)") }
    var date by remember { mutableStateOf("2026-07-30") }
    var time by remember { mutableStateOf("14:00") }
    var hoursStr by remember { mutableStateOf("2") }
    var notes by remember { mutableStateOf("Jazda w ruchu miejskim + plac manewrowy") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zaplanuj Jazdę Szkoleniową", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = studentName,
                    onValueChange = { studentName = it },
                    label = { Text("Imię i Nazwisko Kursanta") },
                    enabled = defaultStudent.isBlank(),
                    modifier = Modifier.fillMaxWidth().testTag("lesson_student_input")
                )
                OutlinedTextField(
                    value = instructorName,
                    onValueChange = { instructorName = it },
                    label = { Text("Imię i Nazwisko Instruktora") },
                    enabled = defaultInstructor.isBlank(),
                    modifier = Modifier.fillMaxWidth().testTag("lesson_instructor_input")
                )
                OutlinedTextField(
                    value = selectedVehicle,
                    onValueChange = { selectedVehicle = it },
                    label = { Text("Pojazd (Rejestracja)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Data") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Godzina") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = hoursStr,
                    onValueChange = { hoursStr = it },
                    label = { Text("Liczba godzin") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Uwagi / Program zajęć") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (studentName.isNotBlank() && instructorName.isNotBlank()) {
                        onConfirm(
                            studentName.trim(),
                            instructorName.trim(),
                            selectedVehicle.trim(),
                            date,
                            time,
                            hoursStr.toIntOrNull() ?: 2,
                            notes
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("submit_lesson_button")
            ) {
                Text("Zapisz Jazdę")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

@Composable
fun AddFinanceDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, type: String, category: String, date: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("INCOME") }
    var category by remember { mutableStateOf("Czesne kursanta") }
    var date by remember { mutableStateOf("2026-07-29") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj Operację Finansową", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "INCOME",
                        onClick = { type = "INCOME" },
                        label = { Text("📈 Przychód (+)") }
                    )
                    FilterChip(
                        selected = type == "EXPENSE",
                        onClick = { type = "EXPENSE" },
                        label = { Text("📉 Wydatek (-)") }
                    )
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Opis transakcji") },
                    modifier = Modifier.fillMaxWidth().testTag("finance_title_input")
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Kwota PLN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("finance_amount_input")
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategoria (Paliwo, Czesne, Serwis)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Data") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0) {
                        onConfirm(title.trim(), amt, type, category, date)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("submit_finance_button")
            ) {
                Text("Zapisz Transakcję")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

@Composable
fun ReportSickLeaveDialog(
    instructorName: String,
    onDismiss: () -> Unit,
    onConfirm: (instructor: String, startDate: String, endDate: String, reason: String) -> Unit
) {
    var instructor by remember { mutableStateOf(instructorName) }
    var startDate by remember { mutableStateOf("2026-08-01") }
    var endDate by remember { mutableStateOf("2026-08-05") }
    var reason by remember { mutableStateOf("Zwolnienie lekarskie L4 - grypa") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zgłoszenie Zwolnienia L4", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = instructor,
                    onValueChange = { instructor = it },
                    label = { Text("Imię i Nazwisko Instruktora") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Data Od") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("Data Do") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Powód / Uwagi") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (instructor.isNotBlank()) {
                        onConfirm(instructor.trim(), startDate, endDate, reason.trim())
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("submit_sick_leave_button")
            ) {
                Text("Zgłoś L4")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

@Composable
fun VehicleReportDialog(
    vehicle: VehicleEntity,
    onDismiss: () -> Unit,
    onConfirm: (
        startOdo: Double,
        refuelOdo: Double?,
        fuelQty: Double?,
        pricePerL: Double?,
        expenseName: String?,
        expenseCost: Double?,
        endOdo: Double
    ) -> Unit
) {
    var startOdoStr by remember { mutableStateOf("") }
    var endOdoStr by remember { mutableStateOf("") }

    var isRefueled by remember { mutableStateOf(false) }
    var refuelOdoStr by remember { mutableStateOf("") }
    var fuelQtyStr by remember { mutableStateOf("") }
    var pricePerLStr by remember { mutableStateOf("") }

    var hasExtraExpenses by remember { mutableStateOf(false) }
    var expenseName by remember { mutableStateOf("") }
    var expenseCostStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Dzienny Raport Eksploatacji",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${vehicle.brand} ${vehicle.model} (${vehicle.registrationPlate})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Wpisz poniższe dane z dzisiejszego dnia roboczego. Raport zostanie zapisany w bazie zarządcy OSK.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                OutlinedTextField(
                    value = startOdoStr,
                    onValueChange = { startOdoStr = it },
                    label = { Text("Przebieg na początku dnia (km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("report_start_odo_input")
                )

                // Tankowanie section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Czy auto było tankowane?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Switch(
                        checked = isRefueled,
                        onCheckedChange = { isRefueled = it },
                        modifier = Modifier.testTag("report_refuel_switch")
                    )
                }

                if (isRefueled) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "DANE TANKOWANIA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedTextField(
                                value = refuelOdoStr,
                                onValueChange = { refuelOdoStr = it },
                                label = { Text("Przebieg w momencie tankowania (km)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("report_refuel_odo_input")
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = fuelQtyStr,
                                    onValueChange = { fuelQtyStr = it },
                                    label = { Text("Ilość (litry)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("report_fuel_qty_input")
                                )

                                OutlinedTextField(
                                    value = pricePerLStr,
                                    onValueChange = { pricePerLStr = it },
                                    label = { Text("Cena/litr (PLN)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("report_fuel_price_input")
                                )
                            }
                        }
                    }
                }

                // Koszty utrzymania (Extra expenses) section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Dodatkowe koszty utrzymania?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Switch(
                        checked = hasExtraExpenses,
                        onCheckedChange = { hasExtraExpenses = it },
                        modifier = Modifier.testTag("report_extra_switch")
                    )
                }

                if (hasExtraExpenses) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "DODATKOWY KOSZT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedTextField(
                                value = expenseName,
                                onValueChange = { expenseName = it },
                                label = { Text("Na co poniosłeś koszt? (np. myjnia, żarówka)") },
                                modifier = Modifier.fillMaxWidth().testTag("report_expense_name_input")
                            )

                            OutlinedTextField(
                                value = expenseCostStr,
                                onValueChange = { expenseCostStr = it },
                                label = { Text("Cena (PLN)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("report_expense_cost_input")
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = endOdoStr,
                    onValueChange = { endOdoStr = it },
                    label = { Text("Przebieg na koniec dnia (km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("report_end_odo_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val startOdo = startOdoStr.toDoubleOrNull()
                    val endOdo = endOdoStr.toDoubleOrNull()

                    if (startOdo != null && endOdo != null && endOdo >= startOdo) {
                        val refuelOdo = if (isRefueled) refuelOdoStr.toDoubleOrNull() else null
                        val fuelQty = if (isRefueled) fuelQtyStr.toDoubleOrNull() else null
                        val pricePerL = if (isRefueled) pricePerLStr.toDoubleOrNull() else null

                        val name = if (hasExtraExpenses) expenseName.trim().ifBlank { null } else null
                        val cost = if (hasExtraExpenses) expenseCostStr.toDoubleOrNull() else null

                        onConfirm(
                            startOdo,
                            refuelOdo,
                            fuelQty,
                            pricePerL,
                            name,
                            cost,
                            endOdo
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("submit_vehicle_report_button")
            ) {
                Text("Raportuj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anuluj") }
        }
    )
}
