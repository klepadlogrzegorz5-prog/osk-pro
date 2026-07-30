package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.DocumentEntity
import com.example.ui.components.EmptyState
import com.example.ui.components.ScheduleExamModal
import com.example.ui.viewmodel.OskViewModel
import com.example.ui.viewmodel.UserRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class GroupingMode(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    NONE("Brak", Icons.Default.ClearAll),
    CATEGORY("Kategoria", Icons.Default.Category),
    TYPE("Typ (PKK / Zdjęcie)", Icons.Default.FilterList)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    viewModel: OskViewModel,
    onMenuClick: () -> Unit
) {
    val documents by viewModel.studentDocuments.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val userName by viewModel.userName.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedGrouping by remember { mutableStateOf(GroupingMode.NONE) }
    var documentToDelete by remember { mutableStateOf<DocumentEntity?>(null) }
    var examToSchedule by remember { mutableStateOf<DocumentEntity?>(null) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Filter documents based on query
    val filteredDocuments = remember(documents, searchQuery, currentRole, userName) {
        documents.filter { doc ->
            // If student role, restrict view to only their own documents to preserve privacy, 
            // but let Managers and Instructors see all.
            val matchesRole = if (currentRole == UserRole.STUDENT) {
                doc.fullName.lowercase().contains(userName.split(" ").first().lowercase()) ||
                userName.lowercase().contains(doc.fullName.lowercase())
            } else {
                true
            }

            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                doc.fullName.lowercase().contains(searchQuery.lowercase()) ||
                doc.phone.contains(searchQuery) ||
                (doc.pkkNumber != null && doc.pkkNumber.contains(searchQuery)) ||
                doc.category.lowercase().contains(searchQuery.lowercase())
            }

            matchesRole && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "DOKUMENTY",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick, modifier = Modifier.testTag("menu_button")) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0B0F19)
                )
            )
        },
        containerColor = Color(0xFF0B0F19)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Screen Header Description
            Text(
                text = "Zgromadzone profile PKK oraz przesłane zdjęcia dokumentów tożsamości kandydatów na kierowców.",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Search and Grouping Controls Panel
            Surface(
                color = Color(0xFF131B2E),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Szukaj po imieniu, telefonie lub PKK...", color = Color.White.copy(alpha = 0.4f)) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF38BDF8)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, null, tint = Color.White.copy(alpha = 0.6f))
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("document_search_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Grouping Mode title
                    Text(
                        text = "GRUPOWANIE DOKUMENTÓW:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFA855F7),
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Grouping selection chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        GroupingMode.entries.forEach { mode ->
                            val isSelected = selectedGrouping == mode
                            val bgChipColor by animateColorAsState(
                                targetValue = if (isSelected) Color(0xFF8B5CF6).copy(alpha = 0.35f) else Color(0xFF0F172A),
                                label = "ChipBg"
                            )

                            Surface(
                                color = bgChipColor,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFA855F7) else Color.White.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedGrouping = mode }
                                    .testTag("grouping_mode_${mode.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = mode.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = mode.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Results List
            if (filteredDocuments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        title = "Brak dokumentów",
                        description = "Nie odnaleziono żadnych dokumentów spełniających kryteria.",
                        icon = Icons.Default.FolderOpen
                    )
                }
            } else {
                // Determine layout based on grouping mode
                when (selectedGrouping) {
                    GroupingMode.NONE -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .testTag("documents_list_no_grouping")
                        ) {
                            items(filteredDocuments) { doc ->
                                DocumentCard(
                                    doc = doc,
                                    showScheduleExamButton = (currentRole == UserRole.MANAGER || currentRole == UserRole.INSTRUCTOR),
                                    onScheduleExam = { examToSchedule = doc },
                                    onDownload = {
                                        Toast.makeText(context, "⬇️ Pobieranie dokumentu: ${doc.fullName} (${doc.category})...", Toast.LENGTH_SHORT).show()
                                    },
                                    onDelete = { documentToDelete = doc },
                                    onCopyPkk = { pkk ->
                                        clipboardManager.setText(AnnotatedString(pkk))
                                        Toast.makeText(context, "📋 Skopiowano PKK do schowka!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                    GroupingMode.CATEGORY -> {
                        val groupedByCategory = filteredDocuments.groupBy { it.category }
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .testTag("documents_list_grouped_category")
                        ) {
                            groupedByCategory.entries.sortedBy { it.key }.forEach { (category, docsList) ->
                                item {
                                    CategoryGroupHeader(category = category, count = docsList.size)
                                }
                                items(docsList) { doc ->
                                    DocumentCard(
                                        doc = doc,
                                        showScheduleExamButton = (currentRole == UserRole.MANAGER || currentRole == UserRole.INSTRUCTOR),
                                        onScheduleExam = { examToSchedule = doc },
                                        onDownload = {
                                            Toast.makeText(context, "⬇️ Pobieranie dokumentu: ${doc.fullName} (${doc.category})...", Toast.LENGTH_SHORT).show()
                                        },
                                        onDelete = { documentToDelete = doc },
                                        onCopyPkk = { pkk ->
                                            clipboardManager.setText(AnnotatedString(pkk))
                                            Toast.makeText(context, "📋 Skopiowano PKK do schowka!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                    GroupingMode.TYPE -> {
                        val groupedByType = filteredDocuments.groupBy {
                            when {
                                !it.pkkNumber.isNullOrBlank() && !it.photoPath.isNullOrBlank() -> "Profil PKK oraz Zdjęcie"
                                !it.pkkNumber.isNullOrBlank() -> "Tylko Profil PKK"
                                else -> "Tylko Zdjęcie Dokumentu"
                            }
                        }
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .testTag("documents_list_grouped_type")
                        ) {
                            groupedByType.entries.sortedBy { it.key }.forEach { (typeLabel, docsList) ->
                                item {
                                    TypeGroupHeader(typeLabel = typeLabel, count = docsList.size)
                                }
                                items(docsList) { doc ->
                                    DocumentCard(
                                        doc = doc,
                                        showScheduleExamButton = (currentRole == UserRole.MANAGER || currentRole == UserRole.INSTRUCTOR),
                                        onScheduleExam = { examToSchedule = doc },
                                        onDownload = {
                                            Toast.makeText(context, "⬇️ Pobieranie dokumentu: ${doc.fullName} (${doc.category})...", Toast.LENGTH_SHORT).show()
                                        },
                                        onDelete = { documentToDelete = doc },
                                        onCopyPkk = { pkk ->
                                            clipboardManager.setText(AnnotatedString(pkk))
                                            Toast.makeText(context, "📋 Skopiowano PKK do schowka!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Deletion confirmation dialog
    if (documentToDelete != null) {
        AlertDialog(
            onDismissRequest = { documentToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Usuń Dokument", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Text(
                    text = "Czy na pewno chcesz permanentnie usunąć dokumenty przypisane do kursanta ${documentToDelete!!.fullName}? Operacja ta jest nieodwracalna.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDocument(documentToDelete!!.id)
                        Toast.makeText(context, "🗑️ Pomyślnie usunięto dokument kursanta ${documentToDelete!!.fullName}.", Toast.LENGTH_SHORT).show()
                        documentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Usuń", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { documentToDelete = null }
                ) {
                    Text("Anuluj", color = Color.White.copy(alpha = 0.6f))
                }
            },
            containerColor = Color(0xFF131B2E),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Schedule Exam dialog
    if (examToSchedule != null) {
        ScheduleExamModal(
            prefilledStudentName = examToSchedule!!.fullName,
            prefilledPkkNumber = examToSchedule!!.pkkNumber ?: "",
            hasPhotoInDocs = !examToSchedule!!.photoPath.isNullOrBlank(),
            onDismiss = { examToSchedule = null },
            onConfirm = { studentName, date, time, examType, pkkNumber, pkkStatus, hasPhoto, isConfirmed ->
                viewModel.scheduleExam(
                    studentName = studentName,
                    date = date,
                    time = time,
                    examType = examType,
                    pkkNumber = pkkNumber,
                    pkkStatus = pkkStatus,
                    hasPhoto = hasPhoto,
                    isConfirmed = isConfirmed
                )
                examToSchedule = null
                Toast.makeText(context, "📅 Zaplanowano egzamin dla: $studentName!", Toast.LENGTH_LONG).show()
            }
        )
    }
}

@Composable
fun CategoryGroupHeader(category: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = Color(0xFFEF4444).copy(alpha = 0.2f),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = category,
                color = Color(0xFFEF4444),
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color.White.copy(alpha = 0.15f)
        )

        Text(
            text = "Liczba: $count",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun TypeGroupHeader(typeLabel: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = Color(0xFF38BDF8).copy(alpha = 0.2f),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = typeLabel,
                color = Color(0xFF38BDF8),
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color.White.copy(alpha = 0.15f)
        )

        Text(
            text = "Liczba: $count",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun DocumentCard(
    doc: DocumentEntity,
    showScheduleExamButton: Boolean = false,
    onScheduleExam: () -> Unit = {},
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onCopyPkk: (String) -> Unit
) {
    val dateStr = remember(doc.createdAt) {
        try {
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            sdf.format(Date(doc.createdAt))
        } catch (e: Exception) {
            "Brak daty"
        }
    }

    Surface(
        color = Color(0xFF131B2E).copy(alpha = 0.85f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, Color(0xFF8B5CF6).copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("document_card_${doc.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Name + Category badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, Color(0xFFA855F7), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = doc.fullName.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = doc.fullName,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Tel: ${doc.phone}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                // Category Badge (e.g. Kat. B)
                Surface(
                    color = Color(0xFFEC4899).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFEC4899))
                ) {
                    Text(
                        text = doc.category,
                        color = Color(0xFFEC4899),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // PKK Info Block (If available)
            if (!doc.pkkNumber.isNullOrBlank()) {
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "PROFIL PKK:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                            Text(
                                text = doc.pkkNumber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                        }

                        IconButton(
                            onClick = { onCopyPkk(doc.pkkNumber) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Kopiuj PKK",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Image Preview Block (If photo is uploaded)
            if (!doc.photoPath.isNullOrBlank()) {
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .padding(bottom = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF0F172A), Color(0xFF131B2E))
                                )
                            )
                    ) {
                        // Simulated Card Watermark/Grid Background
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top Row: Card header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "KARTA PKK / DOWÓD KANDYDATA",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF10B981)
                                    )
                                }

                                Text(
                                    text = "OSK-PRO VERIFIED",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981).copy(alpha = 0.7f)
                                )
                            }

                            // Middle: Personal visual representation
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Avatar mockup
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF1E293B))
                                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.3f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = doc.fullName.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Kategoria: ${doc.category} | PESEL: Zweryfikowany",
                                        fontSize = 8.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = "Plik: ${doc.photoPath}",
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }

                            // Bottom row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = "AUTENTYKACJA BEZPIECZNA",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.3f)
                                )

                                Surface(
                                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, Color(0xFF10B981))
                                ) {
                                    Text(
                                        text = " ZDJĘCIE OBECNE ",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showScheduleExamButton) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onScheduleExam,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("btn_schedule_exam_for_${doc.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.AssignmentTurnedIn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ustal termin egzaminu",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.White.copy(alpha = 0.1f)
            )

            // Footer info & actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timestamp
                Text(
                    text = "Dodano: $dateStr",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f)
                )

                // Actions: Download and Delete
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Download
                    IconButton(
                        onClick = onDownload,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF38BDF8).copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, Color(0xFF38BDF8), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Pobierz",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Delete
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFEF4444).copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, Color(0xFFEF4444), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Usuń",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
