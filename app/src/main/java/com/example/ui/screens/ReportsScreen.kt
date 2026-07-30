package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.VehicleEntity
import com.example.data.db.VehicleReportEntity
import com.example.ui.components.EmptyState
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Typeface
import androidx.core.content.FileProvider
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    vehicles: List<VehicleEntity>,
    reports: List<VehicleReportEntity>,
    onDeleteReport: (Long) -> Unit,
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Raporty dzienne, 1: Analiza 15-dniowa
    var showConsolidatedMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // We can rely on the OskTopBar, but let's draw screen tabs inside the body
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(4.dp)
            ) {
                TabButton(
                    text = "Dzienniki Pojazdów",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "Analizy 15-dniowe",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action row with section title and consolidated export menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedTab == 0) "Dzienniki Pojazdów" else "Analizy Floty Pojazdów",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (reports.isNotEmpty()) {
                    Box {
                        Button(
                            onClick = { showConsolidatedMenu = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("export_consolidated_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Eksportuj zbiorczo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        DropdownMenu(
                            expanded = showConsolidatedMenu,
                            onDismissRequest = { showConsolidatedMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Pobierz zestawienie jako PDF") },
                                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showConsolidatedMenu = false
                                    exportAllReportsAsPdf(context, reports, vehicles)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Pobierz zestawienie jako TXT") },
                                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showConsolidatedMenu = false
                                    exportAllReportsAsTxt(context, reports, vehicles)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Udostępnij jako tekst") },
                                leadingIcon = { Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showConsolidatedMenu = false
                                    shareAllReportsAsText(context, reports, vehicles)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                DailyReportsTab(
                    vehicles = vehicles,
                    reports = reports,
                    onDeleteReport = onDeleteReport
                )
            } else {
                AnalysisTab(
                    vehicles = vehicles,
                    reports = reports
                )
            }
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DailyReportsTab(
    vehicles: List<VehicleEntity>,
    reports: List<VehicleReportEntity>,
    onDeleteReport: (Long) -> Unit
) {
    if (reports.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Assignment,
            title = "Brak raportów eksploatacji",
            description = "Żaden z instruktorów nie przesłał dzisiaj dziennego raportu z przebiegu ani tankowania pojazdu.",
            testTagPrefix = "daily_reports"
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(reports, key = { it.id }) { report ->
                val vehicle = vehicles.find { it.id == report.vehicleId }
                val vehicleLabel = if (vehicle != null) "${vehicle.brand} ${vehicle.model}" else "Pojazd"
                
                DailyReportCard(
                    report = report,
                    vehicleLabel = vehicleLabel,
                    onDelete = { onDeleteReport(report.id) }
                )
            }
        }
    }
}

@Composable
private fun DailyReportCard(
    report: VehicleReportEntity,
    vehicleLabel: String,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(report.timestamp))
    var showCardMenu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = report.vehiclePlate,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = vehicleLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    Box {
                        IconButton(
                            onClick = { showCardMenu = true },
                            modifier = Modifier.size(32.dp).testTag("share_report_${report.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Udostępnij raport",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showCardMenu,
                            onDismissRequest = { showCardMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Pobierz jako PDF") },
                                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showCardMenu = false
                                    exportReportAsPdf(context, report, vehicleLabel)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Pobierz jako TXT") },
                                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showCardMenu = false
                                    exportReportAsTxt(context, report, vehicleLabel)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Udostępnij jako tekst") },
                                leadingIcon = { Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showCardMenu = false
                                    shareReportText(context, report, vehicleLabel)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).testTag("delete_report_${report.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Usuń raport",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(10.dp))

            // Mileage Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Przebieg dzienny:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${report.startOdometer} km ➔ ${report.endOdometer} km  (${report.endOdometer - report.startOdometer} km)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Tankowanie if present
            if (report.fuelQuantity != null && report.fuelQuantity > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFF59E0B).copy(alpha = 0.08f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalGasStation,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tankowanie (przy ${report.refuelOdometer ?: report.endOdometer} km):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706)
                                )
                            }

                            val totalPrice = (report.fuelQuantity * (report.pricePerLiter ?: 0.0))
                            Text(
                                text = String.format("%.2f PLN", totalPrice),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFB45309)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Paliwo: ${report.fuelQuantity} litrów @ ${report.pricePerLiter} PLN/l",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Calculated fuel consumption
                            val distance = report.endOdometer - report.startOdometer
                            if (distance > 0) {
                                val consumption = (report.fuelQuantity / distance) * 100
                                Text(
                                    text = String.format("Spalanie: %.1f l/100km", consumption),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Extra expenses if present
            if (!report.extraExpenseName.isNullOrBlank() && report.extraExpenseCost != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Dodatkowy koszt: ${report.extraExpenseName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = String.format("%.2f PLN", report.extraExpenseCost),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisTab(
    vehicles: List<VehicleEntity>,
    reports: List<VehicleReportEntity>
) {
    if (reports.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Analytics,
            title = "Brak danych analitycznych",
            description = "Prześlij przynajmniej jeden dzienny raport eksploatacji, aby uruchomić automatyczną analizę 15-dniową floty.",
            testTagPrefix = "analysis_empty"
        )
        return
    }

    // 15-day Buckets: Split reports into current 15 days (0 - 15 days ago) and previous 15 days (16 - 30 days ago)
    val msInDay = 24 * 3600 * 1000L
    val ms15Days = 15 * msInDay
    val now = System.currentTimeMillis()

    val current15DayReports = reports.filter { now - it.timestamp <= ms15Days }
    val previous15DayReports = reports.filter { (now - it.timestamp > ms15Days) && (now - it.timestamp <= 2 * ms15Days) }

    // Group reports by vehicle plate
    val plates = reports.map { it.vehiclePlate }.distinct()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            AnalyticsHeader(
                currentCount = current15DayReports.size,
                previousCount = previous15DayReports.size
            )
        }

        // Fleet summary
        item {
            FleetSummaryCard(
                reports = current15DayReports,
                prevReports = previous15DayReports
            )
        }

        // Section Title
        item {
            Text(
                text = "Indywidualna Analiza Pojazdów (Co 15 dni)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Car-by-car reports
        items(plates) { plate ->
            val carReports = current15DayReports.filter { it.vehiclePlate == plate }
            val carPrevReports = previous15DayReports.filter { it.vehiclePlate == plate }
            val vehicle = vehicles.find { it.registrationPlate == plate }
            val brandModel = if (vehicle != null) "${vehicle.brand} ${vehicle.model}" else "Auto"

            IndividualVehicleAnalysisCard(
                plate = plate,
                brandModel = brandModel,
                reports = carReports,
                prevReports = carPrevReports
            )
        }
    }
}

@Composable
private fun AnalyticsHeader(currentCount: Int, previousCount: Int) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Timeline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "System Automatycznej Analizy Floty",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Analizator co 15 dni przetwarza koszty paliwa, dodatkowe wydatki oraz wskaźniki średniego spalania floty.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FleetSummaryCard(
    reports: List<VehicleReportEntity>,
    prevReports: List<VehicleReportEntity>
) {
    // Current period stats
    val totalDist = reports.sumOf { it.endOdometer - it.startOdometer }
    val fuelCost = reports.sumOf { (it.fuelQuantity ?: 0.0) * (it.pricePerLiter ?: 0.0) }
    val extraCost = reports.sumOf { it.extraExpenseCost ?: 0.0 }
    val totalCost = fuelCost + extraCost

    // Previous period stats
    val prevDist = prevReports.sumOf { it.endOdometer - it.startOdometer }
    val prevFuelCost = prevReports.sumOf { (it.fuelQuantity ?: 0.0) * (it.pricePerLiter ?: 0.0) }
    val prevExtraCost = prevReports.sumOf { it.extraExpenseCost ?: 0.0 }
    val prevTotalCost = prevFuelCost + prevExtraCost

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PODSUMOWANIE GRUPOWE FLOTY (Ost. 15 dni)",
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // Total Costs
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(text = "Suma wydatków", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = String.format("%.2f PLN", totalCost),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ComparisonIndicator(current = totalCost, previous = prevTotalCost, suffix = "PLN")
                }

                // Total Distance
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Przejechany dystans", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = String.format("%.0f km", totalDist),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ComparisonIndicator(current = totalDist, previous = prevDist, suffix = "km")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(14.dp))

            // Cost breakdowns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Koszty paliwa:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = String.format("%.2f PLN", fuelCost), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Koszty eksploatacji (myjnia, żarówki itp.):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = String.format("%.2f PLN", extraCost), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Average Fleet Fuel consumption
            val totalFuelQuantity = reports.sumOf { it.fuelQuantity ?: 0.0 }
            val fuelReportingDistance = reports.filter { it.fuelQuantity != null && it.fuelQuantity > 0 }.sumOf { it.endOdometer - it.startOdometer }
            if (fuelReportingDistance > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Średnie spalanie całej floty:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = String.format("%.1f l/100km", (totalFuelQuantity / fuelReportingDistance) * 100),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun IndividualVehicleAnalysisCard(
    plate: String,
    brandModel: String,
    reports: List<VehicleReportEntity>,
    prevReports: List<VehicleReportEntity>
) {
    val distance = reports.sumOf { it.endOdometer - it.startOdometer }
    val fuelQty = reports.sumOf { it.fuelQuantity ?: 0.0 }
    val fuelCost = reports.sumOf { (it.fuelQuantity ?: 0.0) * (it.pricePerLiter ?: 0.0) }
    val extraCost = reports.sumOf { it.extraExpenseCost ?: 0.0 }
    val totalCost = fuelCost + extraCost

    // Consumption logic
    val fuelDist = reports.filter { it.fuelQuantity != null && it.fuelQuantity > 0 }.sumOf { it.endOdometer - it.startOdometer }
    val avgConsumption = if (fuelDist > 0) (fuelQty / fuelDist) * 100 else 0.0

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = plate,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = brandModel,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = String.format("%.0f km", distance),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(10.dp))

            // Financial stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Koszty paliwa", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = String.format("%.2f PLN", fuelCost), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Inne koszty", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = String.format("%.2f PLN", extraCost), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Suma kosztów", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = String.format("%.2f PLN", totalCost), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
            }

            if (avgConsumption > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Średnie spalanie pojazdu:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%.1f l/100km", avgConsumption),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = if (avgConsumption > 8.5) Color(0xFFEF4444) else Color(0xFF10B981)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            // AI Diagnostic / Feedback Insight based on data
            Surface(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp).padding(top = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = getAnalysisInsight(plate, avgConsumption, extraCost, distance),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 13.sp
                    )
                }
            }
        }
    }
}

private fun getAnalysisInsight(plate: String, consumption: Double, extraCost: Double, distance: Double): String {
    if (distance == 0.0) return "Brak odnotowanych jazd dla pojazdu $plate w tym okresie."
    val sb = StringBuilder()
    sb.append("Analiza 15-dniowa: Auto przejechało $distance km. ")
    if (consumption > 0) {
        if (consumption > 8.2) {
            sb.append("Średnie spalanie ($consumption l/100km) przekracza normę fabryczną. Zalecana weryfikacja techniki jazdy lub filtra powietrza.")
        } else {
            sb.append("Spalanie na optymalnym poziomie ($consumption l/100km). Styl jazdy instruktora jest ekonomiczny.")
        }
    } else {
        sb.append("Brak odnotowanego tankowania - niemożliwe obliczenie spalania.")
    }
    if (extraCost > 50.0) {
        sb.append(" Dodatkowe koszty ($extraCost zł) podnoszą ogólny koszt utrzymania kilometra.")
    } else {
        sb.append(" Koszty eksploatacyjne w normie.")
    }
    return sb.toString()
}

@Composable
private fun ComparisonIndicator(current: Double, previous: Double, suffix: String) {
    if (previous == 0.0) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.TrendingFlat,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = "Pierwszy okres", fontSize = 10.sp, color = Color.Gray)
        }
        return
    }

    val pct = ((current - previous) / previous) * 100
    val isIncrease = pct > 0
    val color = if (isIncrease) Color(0xFFEF4444) else Color(0xFF10B981)
    val icon = if (isIncrease) Icons.Default.TrendingUp else Icons.Default.TrendingDown
    val label = if (isIncrease) String.format("+%.1f%% wzkrost", pct) else String.format("%.1f%% spadek", pct)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(text = label, fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
    }
}

private fun shareFile(context: Context, file: File, mimeType: String, chooserTitle: String) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "com.aistudio.oskpro.system.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    } catch (e: Exception) {
        Toast.makeText(context, "Błąd udostępniania: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun shareReportText(context: Context, report: VehicleReportEntity, vehicleLabel: String) {
    val sdf = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date(report.timestamp))
    
    val textContent = buildString {
        appendLine("🚗 *RAPORT POJAZDU OSK* 🚗")
        appendLine("Rejestracja: ${report.vehiclePlate}")
        appendLine("Model: $vehicleLabel")
        appendLine("Data: $dateStr")
        appendLine("-------------------------")
        appendLine("Przebieg: ${report.startOdometer} km ➔ ${report.endOdometer} km (${report.endOdometer - report.startOdometer} km)")
        if (report.fuelQuantity != null && report.fuelQuantity > 0) {
            appendLine("⛽ *Tankowanie:*")
            appendLine("  • Ilość: ${report.fuelQuantity} litrów")
            appendLine("  • Cena: ${report.pricePerLiter ?: 0.0} PLN/l")
            val total = report.fuelQuantity * (report.pricePerLiter ?: 0.0)
            appendLine(String.format("  • Koszt paliwa: %.2f PLN", total))
        }
        if (!report.extraExpenseName.isNullOrBlank() && report.extraExpenseCost != null) {
            appendLine("🔧 *Dodatkowe koszty:*")
            appendLine("  • ${report.extraExpenseName}: ${report.extraExpenseCost} PLN")
        }
        appendLine("-------------------------")
        appendLine("Wygenerowano z aplikacji OSK Menedżer.")
    }

    try {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, textContent)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Udostępnij raport jako tekst"))
    } catch (e: Exception) {
        Toast.makeText(context, "Nie można udostępnić: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun exportReportAsTxt(context: Context, report: VehicleReportEntity, vehicleLabel: String) {
    val sdf = SimpleDateFormat("dd.MM.yyyy_HHmm", Locale.getDefault())
    val dateStr = sdf.format(Date(report.timestamp))
    
    val textContent = buildString {
        appendLine("========================================")
        appendLine("RAPORT EKSPLOATACJI POJAZDU - OSK MENEDŻER")
        appendLine("========================================")
        appendLine("Pojazd: $vehicleLabel")
        appendLine("Nr rejestracyjny: ${report.vehiclePlate}")
        appendLine("Data raportu: ${SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault()).format(Date(report.timestamp))}")
        appendLine("----------------------------------------")
        appendLine("Przebieg początkowy: ${report.startOdometer} km")
        appendLine("Przebieg końcowy: ${report.endOdometer} km")
        appendLine("Dzienny dystans: ${report.endOdometer - report.startOdometer} km")
        appendLine("----------------------------------------")
        if (report.fuelQuantity != null && report.fuelQuantity > 0) {
            appendLine("INFORMACJE O TANKOWANIU:")
            appendLine("  Przebieg przy tankowaniu: ${report.refuelOdometer ?: report.endOdometer} km")
            appendLine("  Ilość paliwa: ${report.fuelQuantity} litrów")
            appendLine("  Cena za litr: ${report.pricePerLiter ?: 0.0} PLN")
            val total = report.fuelQuantity * (report.pricePerLiter ?: 0.0)
            appendLine(String.format("  Koszt łączny: %.2f PLN", total))
            val distance = report.endOdometer - report.startOdometer
            if (distance > 0) {
                appendLine(String.format("  Średnie spalanie: %.1f l/100km", (report.fuelQuantity / distance) * 100))
            }
            appendLine("----------------------------------------")
        }
        if (!report.extraExpenseName.isNullOrBlank() && report.extraExpenseCost != null) {
            appendLine("DODATKOWY KOSZT:")
            appendLine("  Nazwa kosztu: ${report.extraExpenseName}")
            appendLine(String.format("  Kwota: %.2f PLN", report.extraExpenseCost))
            appendLine("----------------------------------------")
        }
        appendLine("Wygenerowano automatycznie w aplikacji OSK Menedżer.")
    }

    try {
        val file = File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS), "raport_${report.vehiclePlate}_$dateStr.txt")
        file.writeText(textContent)
        Toast.makeText(context, "Zapisano TXT w pobranych: ${file.name}", Toast.LENGTH_LONG).show()
        shareFile(context, file, "text/plain", "Otwórz lub udostępnij plik TXT")
    } catch (e: Exception) {
        Toast.makeText(context, "Błąd eksportu TXT: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun exportReportAsPdf(context: Context, report: VehicleReportEntity, vehicleLabel: String) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    
    val paintTitle = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 18f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val paintSubtitle = Paint().apply {
        color = android.graphics.Color.DKGRAY
        textSize = 13f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val paintText = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 11f
    }
    val paintBold = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 11f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val paintLine = Paint().apply {
        color = android.graphics.Color.LTGRAY
        strokeWidth = 1f
    }

    var y = 50f
    canvas.drawText("RAPORT EKSPLOATACJI POJAZDU - OSK", 40f, y, paintTitle)
    y += 30f
    
    canvas.drawLine(40f, y, 555f, y, paintLine)
    y += 25f
    
    canvas.drawText("Dane Pojazdu:", 40f, y, paintSubtitle)
    y += 20f
    canvas.drawText("Pojazd: $vehicleLabel", 40f, y, paintText)
    y += 18f
    canvas.drawText("Numer rejestracyjny: ${report.vehiclePlate}", 40f, y, paintText)
    y += 18f
    val sdf = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault())
    canvas.drawText("Data: ${sdf.format(Date(report.timestamp))}", 40f, y, paintText)
    y += 25f
    
    canvas.drawLine(40f, y, 555f, y, paintLine)
    y += 25f
    
    canvas.drawText("Szczegóły przebiegu:", 40f, y, paintSubtitle)
    y += 20f
    
    canvas.drawText("Przebieg początkowy:", 40f, y, paintText)
    canvas.drawText("${report.startOdometer} km", 250f, y, paintBold)
    y += 18f
    
    canvas.drawText("Przebieg końcowy:", 40f, y, paintText)
    canvas.drawText("${report.endOdometer} km", 250f, y, paintBold)
    y += 18f
    
    val distance = report.endOdometer - report.startOdometer
    canvas.drawText("Dzienny dystans:", 40f, y, paintText)
    canvas.drawText("$distance km", 250f, y, paintBold)
    y += 25f
    
    if (report.fuelQuantity != null && report.fuelQuantity > 0) {
        canvas.drawLine(40f, y, 555f, y, paintLine)
        y += 25f
        canvas.drawText("Informacje o Tankowaniu:", 40f, y, paintSubtitle)
        y += 20f
        
        canvas.drawText("Ilość paliwa:", 40f, y, paintText)
        canvas.drawText("${report.fuelQuantity} litrów", 250f, y, paintBold)
        y += 18f
        
        canvas.drawText("Cena za litr:", 40f, y, paintText)
        canvas.drawText(String.format("%.2f PLN", report.pricePerLiter ?: 0.0), 250f, y, paintBold)
        y += 18f
        
        val totalFuelCost = report.fuelQuantity * (report.pricePerLiter ?: 0.0)
        canvas.drawText("Koszt tankowania:", 40f, y, paintText)
        canvas.drawText(String.format("%.2f PLN", totalFuelCost), 250f, y, paintBold)
        y += 18f
        
        if (distance > 0) {
            val consumption = (report.fuelQuantity / distance) * 100
            canvas.drawText("Średnie spalanie:", 40f, y, paintText)
            canvas.drawText(String.format("%.1f l/100km", consumption), 250f, y, paintBold)
            y += 18f
        }
        y += 10f
    }
    
    if (!report.extraExpenseName.isNullOrBlank() && report.extraExpenseCost != null) {
        canvas.drawLine(40f, y, 555f, y, paintLine)
        y += 25f
        canvas.drawText("Dodatkowe Koszty:", 40f, y, paintSubtitle)
        y += 20f
        
        canvas.drawText("Wpis:", 40f, y, paintText)
        canvas.drawText(report.extraExpenseName, 250f, y, paintBold)
        y += 18f
        
        canvas.drawText("Koszt:", 40f, y, paintText)
        canvas.drawText(String.format("%.2f PLN", report.extraExpenseCost), 250f, y, paintBold)
        y += 18f
    }
    
    y = 780f
    canvas.drawLine(40f, y, 555f, y, paintLine)
    y += 15f
    canvas.drawText("Generowano automatycznie przez aplikację OSK Menedżer. Strona 1/1", 40f, y, paintText)
    
    pdfDocument.finishPage(page)
    
    try {
        val file = File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS), "raport_${report.vehiclePlate}_${System.currentTimeMillis()}.pdf")
        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        pdfDocument.close()
        fos.close()
        
        Toast.makeText(context, "Zapisano PDF w pobranych: ${file.name}", Toast.LENGTH_LONG).show()
        shareFile(context, file, "application/pdf", "Otwórz lub udostępnij plik PDF")
    } catch (e: Exception) {
        pdfDocument.close()
        Toast.makeText(context, "Błąd generowania PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun shareAllReportsAsText(context: Context, reports: List<VehicleReportEntity>, vehicles: List<VehicleEntity>) {
    val textContent = buildString {
        appendLine("📊 *ZBIORCZY RAPORT FLOTY OSK* 📊")
        val sdf = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault())
        appendLine("Wygenerowano: ${sdf.format(Date())}")
        appendLine("Liczba raportów: ${reports.size}")
        appendLine("----------------------------------------")
        
        val totalDist = reports.sumOf { it.endOdometer - it.startOdometer }
        val fuelCost = reports.sumOf { (it.fuelQuantity ?: 0.0) * (it.pricePerLiter ?: 0.0) }
        val extraCost = reports.sumOf { it.extraExpenseCost ?: 0.0 }
        
        appendLine("*Statystyki ogólne:*")
        appendLine("  • Dystans całkowity: $totalDist km")
        appendLine(String.format("  • Wydatki paliwo: %.2f PLN", fuelCost))
        appendLine(String.format("  • Inne wydatki: %.2f PLN", extraCost))
        appendLine(String.format("  • Suma wszystkich: %.2f PLN", fuelCost + extraCost))
        
        appendLine("----------------------------------------")
        appendLine("*Spis pojazdów w raportach:*")
        
        reports.take(10).forEachIndexed { i, r ->
            val v = vehicles.find { it.id == r.vehicleId }
            val label = if (v != null) "${v.brand} ${v.model}" else "Pojazd"
            appendLine("${i+1}. ${r.vehiclePlate} ($label) ➔ Przebieg: ${r.startOdometer} - ${r.endOdometer} km (${r.endOdometer - r.startOdometer} km)")
        }
        if (reports.size > 10) {
            appendLine("...oraz ${reports.size - 10} innych raportów.")
        }
        appendLine("----------------------------------------")
        appendLine("Wygenerowano z aplikacji OSK Menedżer.")
    }

    try {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, textContent)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Udostępnij zbiorczy raport floty"))
    } catch (e: Exception) {
        Toast.makeText(context, "Nie można udostępnić: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun exportAllReportsAsTxt(context: Context, reports: List<VehicleReportEntity>, vehicles: List<VehicleEntity>) {
    val textContent = buildString {
        appendLine("========================================")
        appendLine("ZBIORCZE ZESTAWIENIE EKSPLOATACJI FLOTY OSK")
        appendLine("========================================")
        val sdf = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault())
        appendLine("Generowano dnia: ${sdf.format(Date())}")
        appendLine("Liczba raportów w okresie: ${reports.size}")
        appendLine("----------------------------------------")
        
        val totalDist = reports.sumOf { it.endOdometer - it.startOdometer }
        val fuelCost = reports.sumOf { (it.fuelQuantity ?: 0.0) * (it.pricePerLiter ?: 0.0) }
        val extraCost = reports.sumOf { it.extraExpenseCost ?: 0.0 }
        val totalCost = fuelCost + extraCost
        
        appendLine("ŁĄCZNE STATYSTYKI FLOTY:")
        appendLine("  Suma przejechanych kilometrów: $totalDist km")
        appendLine(String.format("  Suma wydatków na paliwo: %.2f PLN", fuelCost))
        appendLine(String.format("  Suma innych kosztów: %.2f PLN", extraCost))
        appendLine(String.format("  Suma wszystkich kosztów floty: %.2f PLN", totalCost))
        
        val totalFuelQuantity = reports.sumOf { it.fuelQuantity ?: 0.0 }
        val fuelReportingDistance = reports.filter { it.fuelQuantity != null && it.fuelQuantity > 0 }.sumOf { it.endOdometer - it.startOdometer }
        if (fuelReportingDistance > 0) {
            appendLine(String.format("  Średnie spalanie całej floty: %.1f l/100km", (totalFuelQuantity / fuelReportingDistance) * 100))
        }
        appendLine("========================================")
        appendLine("SZCZEGÓŁOWY SPIS RAPORTÓW:")
        appendLine("----------------------------------------")
        
        reports.forEachIndexed { index, report ->
            val v = vehicles.find { it.id == report.vehicleId }
            val label = if (v != null) "${v.brand} ${v.model}" else "Auto"
            appendLine("${index + 1}. [${sdf.format(Date(report.timestamp))}] ${report.vehiclePlate} ($label)")
            appendLine("   Przebieg: ${report.startOdometer} km -> ${report.endOdometer} km (${report.endOdometer - report.startOdometer} km)")
            if (report.fuelQuantity != null && report.fuelQuantity > 0) {
                appendLine(String.format("   Tankowanie: %.1f litrów @ %.2f PLN/l (Koszt: %.2f PLN)", report.fuelQuantity, report.pricePerLiter ?: 0.0, report.fuelQuantity * (report.pricePerLiter ?: 0.0)))
            }
            if (!report.extraExpenseName.isNullOrBlank() && report.extraExpenseCost != null) {
                appendLine(String.format("   Dodatkowy koszt: %s (%.2f PLN)", report.extraExpenseName, report.extraExpenseCost))
            }
            appendLine("----------------------------------------")
        }
        appendLine("Generowano automatycznie przez OSK Menedżer.")
    }
    
    try {
        val file = File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS), "zestawienie_floty_${System.currentTimeMillis()}.txt")
        file.writeText(textContent)
        Toast.makeText(context, "Zapisano zbiorczy raport TXT: ${file.name}", Toast.LENGTH_LONG).show()
        shareFile(context, file, "text/plain", "Otwórz lub udostępnij zbiorcze zestawienie floty")
    } catch (e: Exception) {
        Toast.makeText(context, "Błąd eksportu TXT: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun exportAllReportsAsPdf(context: Context, reports: List<VehicleReportEntity>, vehicles: List<VehicleEntity>) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    
    val paintTitle = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 18f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val paintSubtitle = Paint().apply {
        color = android.graphics.Color.DKGRAY
        textSize = 12f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val paintText = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 9f
    }
    val paintBold = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 9f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val paintLine = Paint().apply {
        color = android.graphics.Color.LTGRAY
        strokeWidth = 1f
    }

    var y = 40f
    canvas.drawText("ZBIORCZE ZESTAWIENIE FLOTY OSK", 40f, y, paintTitle)
    y += 25f
    
    canvas.drawLine(40f, y, 555f, y, paintLine)
    y += 20f
    
    val sdf = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault())
    canvas.drawText("Data sporządzenia: ${sdf.format(Date())}", 40f, y, paintText)
    canvas.drawText("Liczba raportów floty: ${reports.size}", 300f, y, paintText)
    y += 25f
    
    canvas.drawText("1. Podsumowanie finansowe i przebiegów floty", 40f, y, paintSubtitle)
    y += 20f
    
    val totalDist = reports.sumOf { it.endOdometer - it.startOdometer }
    val fuelCost = reports.sumOf { (it.fuelQuantity ?: 0.0) * (it.pricePerLiter ?: 0.0) }
    val extraCost = reports.sumOf { it.extraExpenseCost ?: 0.0 }
    val totalCost = fuelCost + extraCost

    canvas.drawText("Łączny dystans floty:", 40f, y, paintText)
    canvas.drawText("$totalDist km", 250f, y, paintBold)
    y += 15f
    
    canvas.drawText("Koszty paliwa floty:", 40f, y, paintText)
    canvas.drawText(String.format("%.2f PLN", fuelCost), 250f, y, paintBold)
    y += 15f
    
    canvas.drawText("Dodatkowe wydatki:", 40f, y, paintText)
    canvas.drawText(String.format("%.2f PLN", extraCost), 250f, y, paintBold)
    y += 15f
    
    canvas.drawText("SUMA WSZYSTKICH KOSZTÓW:", 40f, y, paintText)
    canvas.drawText(String.format("%.2f PLN", totalCost), 250f, y, paintBold)
    y += 20f
    
    val totalFuelQuantity = reports.sumOf { it.fuelQuantity ?: 0.0 }
    val fuelReportingDistance = reports.filter { it.fuelQuantity != null && it.fuelQuantity > 0 }.sumOf { it.endOdometer - it.startOdometer }
    if (fuelReportingDistance > 0) {
        val avgConsumption = (totalFuelQuantity / fuelReportingDistance) * 100
        canvas.drawText("Średnie spalanie grupowe:", 40f, y, paintText)
        canvas.drawText(String.format("%.1f l/100km", avgConsumption), 250f, y, paintBold)
        y += 20f
    }
    
    canvas.drawLine(40f, y, 555f, y, paintLine)
    y += 20f
    
    canvas.drawText("2. Wykaz raportów dziennych (skrócony)", 40f, y, paintSubtitle)
    y += 20f
    
    canvas.drawText("Pojazd / Rejestracja", 40f, y, paintBold)
    canvas.drawText("Przebieg", 220f, y, paintBold)
    canvas.drawText("Dystans", 320f, y, paintBold)
    canvas.drawText("Koszty", 420f, y, paintBold)
    y += 15f
    canvas.drawLine(40f, y, 555f, y, paintLine)
    y += 15f
    
    reports.take(15).forEach { r ->
        if (y > 750f) return@forEach
        val v = vehicles.find { it.id == r.vehicleId }
        val name = if (v != null) "${v.brand} ${v.model}" else "Pojazd"
        val label = "${r.vehiclePlate} ($name)"
        
        canvas.drawText(if (label.length > 25) label.substring(0, 22) + "..." else label, 40f, y, paintText)
        canvas.drawText("${r.startOdometer} - ${r.endOdometer} km", 220f, y, paintText)
        canvas.drawText("${r.endOdometer - r.startOdometer} km", 320f, y, paintText)
        
        val fuelC = (r.fuelQuantity ?: 0.0) * (r.pricePerLiter ?: 0.0)
        val extraC = r.extraExpenseCost ?: 0.0
        canvas.drawText(String.format("%.2f PLN", fuelC + extraC), 420f, y, paintText)
        y += 18f
    }
    
    y = 780f
    canvas.drawLine(40f, y, 555f, y, paintLine)
    y += 15f
    canvas.drawText("Generowano automatycznie przez aplikację OSK Menedżer. Strona 1/1", 40f, y, paintText)
    
    pdfDocument.finishPage(page)
    
    try {
        val file = File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS), "zbiorczy_raport_floty_${System.currentTimeMillis()}.pdf")
        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        pdfDocument.close()
        fos.close()
        
        Toast.makeText(context, "Zapisano zbiorczy raport PDF: ${file.name}", Toast.LENGTH_LONG).show()
        shareFile(context, file, "application/pdf", "Otwórz lub udostępnij zbiorczy raport PDF")
    } catch (e: Exception) {
        pdfDocument.close()
        Toast.makeText(context, "Błąd generowania PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

