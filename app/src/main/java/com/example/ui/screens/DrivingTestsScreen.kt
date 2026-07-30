package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ExamResultEntity
import com.example.ui.components.EmptyState
import com.example.ui.viewmodel.OskViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

// Data model for questions parsed from assets/questions.json
data class Question(
    val id: Int,
    val question: String,
    val isBasic: Boolean,
    val options: List<String>,
    val correctAnswer: String,
    val points: Int,
    val category: String,
    val explanation: String
)

// Helper to load driving theory questions from questions.json asset
fun loadQuestionsFromAssets(context: Context): List<Question> {
    return try {
        val jsonString = context.assets.open("questions.json").bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(jsonString)
        val list = mutableListOf<Question>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val optionsJson = obj.getJSONArray("options")
            val options = List(optionsJson.length()) { optionsJson.getString(it) }
            list.add(
                Question(
                    id = obj.getInt("id"),
                    question = obj.getString("question"),
                    isBasic = obj.optBoolean("isBasic", true),
                    options = options,
                    correctAnswer = obj.getString("correctAnswer"),
                    points = obj.optInt("points", 3),
                    category = obj.optString("category", "B"),
                    explanation = obj.optString("explanation", "")
                )
            )
        }
        list
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

@Composable
fun DrivingTestsScreen(
    viewModel: OskViewModel,
    onMenuClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val examResults by viewModel.examResults.collectAsState()
    val userName by viewModel.userName.collectAsState()

    // Load questions on first launch
    val allQuestions = remember { loadQuestionsFromAssets(context) }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Egzamin, 1 = Nauka, 2 = Wyniki

    // Active exam variables
    var examQuestions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var currentExamIndex by remember { mutableStateOf(0) }
    var examSelectedOption by remember { mutableStateOf<String?>(null) }
    var examAnswers by remember { mutableStateOf<Map<Int, String>>(emptyMap()) } // Map of QuestionId -> ChosenOption
    var timeLeftSeconds by remember { mutableStateOf(0) }
    var isExamActive by remember { mutableStateOf(false) }
    var showExamResultSummary by remember { mutableStateOf<ExamSummaryData?>(null) }

    // Active learning variables
    var currentLearningIndex by remember { mutableStateOf(0) }
    var learningSelectedOption by remember { mutableStateOf<String?>(null) }
    var showLearningFeedback by remember { mutableStateOf(false) }

    // Timer coroutine
    LaunchedEffect(isExamActive, timeLeftSeconds) {
        if (isExamActive && timeLeftSeconds > 0) {
            delay(1000L)
            timeLeftSeconds--
            if (timeLeftSeconds == 0) {
                // Auto-submit exam when timer expires
                val pointsEarned = calculateExamPoints(examQuestions, examAnswers)
                val totalMaxPoints = examQuestions.sumOf { it.points }
                val passed = pointsEarned >= (totalMaxPoints * 0.85).toInt() // 85% to pass
                
                viewModel.saveExamResult(
                    score = pointsEarned,
                    maxPoints = totalMaxPoints,
                    isPassed = passed
                )
                
                showExamResultSummary = ExamSummaryData(
                    score = pointsEarned,
                    maxPoints = totalMaxPoints,
                    isPassed = passed,
                    answers = examAnswers,
                    questions = examQuestions
                )
                isExamActive = false
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF0B0F19), // Cyber Deep Background
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Screen Title Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Testy na Prawo Jazdy",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Oficjalna baza pytań egzaminacyjnych • Kategoria B",
                    fontSize = 12.sp,
                    color = Color(0xFF38BDF8)
                )
            }

            // Material 3 Custom Navigation Tabs with Neon border
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF131B2E),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFFA855F7),
                        height = 3.dp
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { if (!isExamActive) selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Egzamin", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    modifier = Modifier.testTag("tab_exam")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { if (!isExamActive) selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Szybka Nauka", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    modifier = Modifier.testTag("tab_learn")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { if (!isExamActive) selectedTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Moje Wyniki", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    modifier = Modifier.testTag("tab_history")
                )
            }

            // Main Content Body based on Active Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (isExamActive) {
                    // Active Exam UI running (renders over other options to prevent navigation loss)
                    ExamRunningLayout(
                        questions = examQuestions,
                        currentIndex = currentExamIndex,
                        selectedOption = examSelectedOption,
                        onOptionSelected = { examSelectedOption = it },
                        timeLeftSeconds = timeLeftSeconds,
                        onNextClick = {
                            // Record answer
                            examSelectedOption?.let { opt ->
                                val question = examQuestions[currentExamIndex]
                                examAnswers = examAnswers + (question.id to opt)
                            }
                            
                            if (currentExamIndex < examQuestions.lastIndex) {
                                currentExamIndex++
                                // Pre-fill with existing selection if they backtracked (or simple progression)
                                examSelectedOption = examAnswers[examQuestions[currentExamIndex].id]
                            } else {
                                // Last question completed -> Submit Exam!
                                val pointsEarned = calculateExamPoints(examQuestions, examAnswers)
                                val totalMaxPoints = examQuestions.sumOf { it.points }
                                val passed = pointsEarned >= (totalMaxPoints * 0.85).toInt()
                                
                                viewModel.saveExamResult(
                                    score = pointsEarned,
                                    maxPoints = totalMaxPoints,
                                    isPassed = passed
                                )
                                
                                showExamResultSummary = ExamSummaryData(
                                    score = pointsEarned,
                                    maxPoints = totalMaxPoints,
                                    isPassed = passed,
                                    answers = examAnswers,
                                    questions = examQuestions
                                )
                                isExamActive = false
                            }
                        },
                        onPrevClick = {
                            if (currentExamIndex > 0) {
                                currentExamIndex--
                                examSelectedOption = examAnswers[examQuestions[currentExamIndex].id]
                            }
                        },
                        onTerminateClick = {
                            isExamActive = false
                        }
                    )
                } else {
                    when (selectedTab) {
                        0 -> {
                            // Exam Start Screen or Result Summary
                            val summary = showExamResultSummary
                            if (summary != null) {
                                ExamResultSummaryLayout(
                                    summary = summary,
                                    onRestartExam = {
                                        showExamResultSummary = null
                                        if (allQuestions.isNotEmpty()) {
                                            examQuestions = allQuestions.shuffled().take(20)
                                            currentExamIndex = 0
                                            examSelectedOption = null
                                            examAnswers = emptyMap()
                                            timeLeftSeconds = 20 * 60 // 20 minutes
                                            isExamActive = true
                                        }
                                    },
                                    onCloseSummary = {
                                        showExamResultSummary = null
                                    }
                                )
                            } else {
                                ExamStartLayout(
                                    allQuestionsCount = allQuestions.size,
                                    onStartExamClick = {
                                        if (allQuestions.isNotEmpty()) {
                                            examQuestions = allQuestions.shuffled().take(20)
                                            currentExamIndex = 0
                                            examSelectedOption = null
                                            examAnswers = emptyMap()
                                            timeLeftSeconds = 20 * 60 // 20 minutes
                                            isExamActive = true
                                        }
                                    }
                                )
                            }
                        }

                        1 -> {
                            // Learning / Practice Mode
                            if (allQuestions.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Color(0xFFA855F7))
                                }
                            } else {
                                StudyModeLayout(
                                    questions = allQuestions,
                                    currentIndex = currentLearningIndex,
                                    selectedOption = learningSelectedOption,
                                    showFeedback = showLearningFeedback,
                                    onOptionSelected = {
                                        if (!showLearningFeedback) {
                                            learningSelectedOption = it
                                            showLearningFeedback = true
                                        }
                                    },
                                    onNextClick = {
                                        if (currentLearningIndex < allQuestions.lastIndex) {
                                            currentLearningIndex++
                                            learningSelectedOption = null
                                            showLearningFeedback = false
                                        }
                                    },
                                    onPrevClick = {
                                        if (currentLearningIndex > 0) {
                                            currentLearningIndex--
                                            learningSelectedOption = null
                                            showLearningFeedback = false
                                        }
                                    }
                                )
                            }
                        }

                        2 -> {
                            // Exam History & Stats
                            StatsAndHistoryLayout(
                                examResults = examResults,
                                onDeleteResult = { id -> viewModel.deleteExamResult(id) }
                            )
                        }
                    }
                }
            }

            // Elegant, polished bottom information banner for students
            if (!isExamActive) {
                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.6f),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "To jest wyłącznie poglądowy zbiór pytań podstawowych (bez multimediów: wideo i zdjęć). Pełną, oficjalną wersję bazy pytań egzaminacyjnych bez problemu znajdziesz w internecie lub otrzymasz bezpośrednio od swojego instruktora.",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// Data holder for exam summary representation
data class ExamSummaryData(
    val score: Int,
    val maxPoints: Int,
    val isPassed: Boolean,
    val answers: Map<Int, String>,
    val questions: List<Question>
)

// Helper to calculate total points obtained
fun calculateExamPoints(questions: List<Question>, answers: Map<Int, String>): Int {
    var points = 0
    questions.forEach { question ->
        val userAnswer = answers[question.id]
        if (userAnswer != null && userAnswer.equals(question.correctAnswer, ignoreCase = true)) {
            points += question.points
        }
    }
    return points
}

// Layout for starting a brand-new practice state-exam
@Composable
fun ExamStartLayout(
    allQuestionsCount: Int,
    onStartExamClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(androidx.compose.foundation.rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing Amber Badge
        Surface(
            color = Color(0xFFFFD600).copy(alpha = 0.12f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, Color(0xFFFFD600).copy(alpha = 0.8f)),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = Color(0xFFFFD600),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Próbny Egzamin Państwowy",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD600),
                    letterSpacing = 0.8.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large Cyberpunk Card with start stats
        Surface(
            color = Color(0xFF131B2E),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.25f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ROZPOCZNIJ SYMULACJĘ",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Zmierz się z losowym zestawem pytań wygenerowanym zgodnie z oficjalnym arkuszem egzaminu państwowego.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Stats breakdown cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ExamStatItem("20", "Pytania", Icons.Default.HelpOutline, Color(0xFFA855F7))
                    ExamStatItem("20 min", "Limit czasu", Icons.Default.Timer, Color(0xFFFFD600))
                    ExamStatItem("85%", "Zaliczenie", Icons.Default.CheckCircle, Color(0xFF10B981))
                }

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dostępnych pytań w bazie: $allQuestionsCount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStartExamClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8B5CF6)
            ),
            border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.8f)),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp)
                .testTag("start_exam_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "URUCHOM EGZAMIN",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun ExamStatItem(value: String, label: String, icon: ImageVector, iconColor: Color) {
    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = Modifier
            .width(96.dp)
            .padding(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Active Exam Screen running with ticking timer and forward/backward navigation
@Composable
fun ExamRunningLayout(
    questions: List<Question>,
    currentIndex: Int,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    timeLeftSeconds: Int,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit,
    onTerminateClick: () -> Unit
) {
    val question = questions.getOrNull(currentIndex) ?: return
    val minutes = timeLeftSeconds / 60
    val seconds = timeLeftSeconds % 60
    val timerString = String.format("%02d:%02d", minutes, seconds)
    val timerColor = if (timeLeftSeconds < 120) Color(0xFFEF4444) else Color(0xFFFFD600)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
    ) {
        // Upper status bar: Progress + Timer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Pytanie ${currentIndex + 1} z ${questions.size}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Kategoria ${question.category} • Punkty: ${question.points}",
                    fontSize = 11.sp,
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Bold
                )
            }

            // High-fidelity Neon Timer box
            Surface(
                color = timerColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, timerColor.copy(alpha = 0.8f)),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Pozostały czas",
                        tint = timerColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = timerString,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = timerColor
                    )
                }
            }
        }

        // Beautiful custom gradient progress bar indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E293B))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (currentIndex + 1).toFloat() / questions.size)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF8B5CF6), Color(0xFF38BDF8))
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Large Premium Question box
        Surface(
            color = Color(0xFF131B2E),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, Color(0xFFA855F7).copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = question.question,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    lineHeight = 22.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Options listing
        Text(
            text = "WYBIERZ JEDNĄ ODPOWIEDŹ:",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = Color.White.copy(alpha = 0.4f),
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        question.options.forEach { option ->
            val isSelected = selectedOption == option
            val optionIndex = question.options.indexOf(option)
            val optionLetter = when (optionIndex) {
                0 -> "A"
                1 -> "B"
                2 -> "C"
                else -> ""
            }

            Surface(
                color = if (isSelected) Color(0xFF8B5CF6).copy(alpha = 0.12f) else Color(0xFF131B2E).copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) Color(0xFFA855F7) else Color.White.copy(alpha = 0.08f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable { onOptionSelected(option) }
                    .testTag("option_${option.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Modern Option Badge
                    Surface(
                        color = if (isSelected) Color(0xFFA855F7) else Color(0xFF1E293B),
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (question.options.size == 2) (if (optionIndex == 0) "TAK" else "NIE") else optionLetter,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                fontSize = if (question.options.size == 2) 9.sp else 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = option,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.weight(1f)
                    )

                    // Compact checkmark for selected
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Wybrane",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Control Navigation Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Button
            TextButton(
                onClick = onPrevClick,
                enabled = currentIndex > 0,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF38BDF8)
                )
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Wstecz", fontWeight = FontWeight.Bold)
            }

            // Abandon Button (red cancel)
            IconButton(
                onClick = onTerminateClick,
                modifier = Modifier
                    .background(Color(0xFFEF4444).copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), CircleShape)
                    .size(42.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Anuluj egzamin", tint = Color(0xFFEF4444))
            }

            // Next / Finish Button
            Button(
                onClick = onNextClick,
                enabled = selectedOption != null,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFA855F7),
                    disabledContainerColor = Color.White.copy(alpha = 0.08f)
                ),
                modifier = Modifier.testTag("next_exam_question_button")
            ) {
                Text(
                    text = if (currentIndex == questions.lastIndex) "ZAKOŃCZ" else "DALEJ",
                    fontWeight = FontWeight.ExtraBold,
                    color = if (selectedOption != null) Color.White else Color.White.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (currentIndex == questions.lastIndex) Icons.Default.Check else Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (selectedOption != null) Color.White else Color.White.copy(alpha = 0.3f)
                )
            }
        }
    }
}

// Layout to display after the exam completes (either pass or fail)
@Composable
fun ExamResultSummaryLayout(
    summary: ExamSummaryData,
    onRestartExam: () -> Unit,
    onCloseSummary: () -> Unit
) {
    val percent = ((summary.score.toFloat() / summary.maxPoints.toFloat()) * 100).toInt()
    val statusColor = if (summary.isPassed) Color(0xFF10B981) else Color(0xFFEF4444)
    val statusText = if (summary.isPassed) "EGZAMIN ZALICZONY!" else "EGZAMIN NIEZALICZONY!"
    val statusDesc = if (summary.isPassed) "Gratulacje! Twoja wiedza teoretyczna stoi na najwyższym poziomie." else "Niestety zabrakło trochę punktów. Przeanalizuj błędy i spróbuj ponownie!"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(androidx.compose.foundation.rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status Badge
        Surface(
            color = statusColor.copy(alpha = 0.12f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, statusColor.copy(alpha = 0.8f)),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (summary.isPassed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = statusText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = statusColor,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Performance breakdown details with elegant circular chart dial
        Surface(
            color = Color(0xFF131B2E),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TWÓJ WYNIK:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Beautiful interactive circular score dial
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(130.dp)
                        .padding(4.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        color = Color.White.copy(alpha = 0.06f),
                        strokeWidth = 8.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                    CircularProgressIndicator(
                        progress = { summary.score.toFloat() / summary.maxPoints.toFloat() },
                        color = statusColor,
                        strokeWidth = 8.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${summary.score}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "/ ${summary.maxPoints} pkt",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Procent poprawnych odpowiedzi: $percent%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = statusDesc,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Wrong Questions Analysis Header
        Text(
            text = "PRZEGLĄD PYTAŃ I WYJAŚNIENIA:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = Color.White.copy(alpha = 0.5f),
            letterSpacing = 0.8.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp)
        )

        // Render each question of the exam with highlights of correctness
        summary.questions.forEachIndexed { idx, q ->
            val userAnswer = summary.answers[q.id]
            val isCorrect = userAnswer != null && userAnswer.equals(q.correctAnswer, ignoreCase = true)
            val outlineColor = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)

            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pytanie ${idx + 1}",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )

                        Surface(
                            color = outlineColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.8f))
                        ) {
                            Text(
                                text = if (isCorrect) "POPRAWNA" else "BŁĘDNA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = outlineColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = q.question,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Twoja odpowiedź: ${userAnswer ?: "Brak odpowiedzi"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)
                    )

                    if (!isCorrect) {
                        Text(
                            text = "Poprawna odpowiedź: ${q.correctAnswer}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }

                    if (q.explanation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = Color(0xFF131B2E),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.25f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Wyjaśnienie i podstawa prawna:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF8B5CF6),
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = q.explanation,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Reset Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCloseSummary,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("WYJDŹ", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Button(
                onClick = onRestartExam,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5CF6)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("SPRÓBUJ ZNOWU", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Layout for general study practice database mode
@Composable
fun StudyModeLayout(
    questions: List<Question>,
    currentIndex: Int,
    selectedOption: String?,
    showFeedback: Boolean,
    onOptionSelected: (String) -> Unit,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit
) {
    val question = questions.getOrNull(currentIndex) ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Nauka: Pytanie ${currentIndex + 1} z ${questions.size}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Kategoria ${question.category} • Waga pytania: ${question.points} pkt",
                    fontSize = 11.sp,
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Bold
                )
            }

            // Quick reset badge
            Surface(
                color = Color(0xFFA855F7).copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.6f))
            ) {
                Text(
                    text = "TRYB NAUKI",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFA855F7),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Large Premium Question box
        Surface(
            color = Color(0xFF131B2E),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = question.question,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    lineHeight = 22.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Options listing
        Text(
            text = "KLIKNIJ ODPOWIEDŹ, ABY SPRAWDZIĆ:",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = Color.White.copy(alpha = 0.4f),
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        question.options.forEach { option ->
            val isSelected = selectedOption == option
            val isCorrect = option.equals(question.correctAnswer, ignoreCase = true)
            val optionIndex = question.options.indexOf(option)
            val optionLetter = when (optionIndex) {
                0 -> "A"
                1 -> "B"
                2 -> "C"
                else -> ""
            }

            // Dynamic card colors depending on click and truth
            val (bgColor, borderCol, badgeColor) = when {
                showFeedback && isCorrect -> Triple(Color(0xFF10B981).copy(alpha = 0.12f), Color(0xFF10B981), Color(0xFF10B981))
                showFeedback && isSelected && !isCorrect -> Triple(Color(0xFFEF4444).copy(alpha = 0.12f), Color(0xFFEF4444), Color(0xFFEF4444))
                isSelected -> Triple(Color(0xFF8B5CF6).copy(alpha = 0.12f), Color(0xFF8B5CF6), Color(0xFF8B5CF6))
                else -> Triple(Color(0xFF131B2E).copy(alpha = 0.6f), Color.White.copy(alpha = 0.08f), Color(0xFF1E293B))
            }

            Surface(
                color = bgColor,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = if (isSelected || (showFeedback && isCorrect)) 2.dp else 1.dp,
                    color = borderCol
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable { onOptionSelected(option) }
                    .testTag("learn_option_${option.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Modern Option Badge
                    Surface(
                        color = badgeColor,
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (question.options.size == 2) (if (optionIndex == 0) "TAK" else "NIE") else optionLetter,
                                color = if (badgeColor == Color(0xFF1E293B)) Color.White.copy(alpha = 0.7f) else Color.White,
                                fontSize = if (question.options.size == 2) 9.sp else 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = option,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected || (showFeedback && isCorrect)) FontWeight.Bold else FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    // Interactive Status Icon
                    if (showFeedback) {
                        if (isCorrect) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Prawidłowa", tint = Color(0xFF10B981))
                        } else if (isSelected) {
                            Icon(Icons.Default.Cancel, contentDescription = "Błędna", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }

        // Expanded explanation block when feedback is triggered
        if (showFeedback && question.explanation.isNotBlank()) {
            Spacer(modifier = Modifier.height(18.dp))
            Surface(
                color = Color(0xFF131B2E),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, Color(0xFF8B5CF6).copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WYJAŚNIENIE I PODSTAWA PRAWNA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF8B5CF6),
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = question.explanation,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Control Navigation Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onPrevClick,
                enabled = currentIndex > 0,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF38BDF8)
                )
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Poprzednie")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Poprzednie", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onNextClick,
                enabled = currentIndex < questions.lastIndex,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5CF6),
                    disabledContainerColor = Color.White.copy(alpha = 0.08f)
                ),
                modifier = Modifier.testTag("learn_next_question_button")
            ) {
                Text("NASTĘPNE", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// Stats overview & list of historical attempts stored in Room
@Composable
fun StatsAndHistoryLayout(
    examResults: List<ExamResultEntity>,
    onDeleteResult: (Long) -> Unit
) {
    if (examResults.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Assessment,
            title = "Brak zarejestrowanych wyników",
            description = "Nie ukończyłeś jeszcze żadnego próbnego egzaminu. Przejdź do pierwszej zakładki i sprawdź swoje siły!",
            testTagPrefix = "driving_tests_stats"
        )
    } else {
        val totalExams = examResults.size
        val passedExams = examResults.count { it.isPassed }
        val passRatePercent = ((passedExams.toFloat() / totalExams.toFloat()) * 100).toInt()
        val avgScore = examResults.map { it.score }.average().toInt()
        val bestScore = examResults.maxOf { it.score }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // General performance widgets
            item {
                Text(
                    text = "MOJE STATYSTYKI:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // 2x2 Grid of polished stat cards
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            StatCard("${passRatePercent}%", "Zdawalność", Icons.Default.TrendingUp, Color(0xFF10B981))
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            StatCard("$totalExams", "Egzaminy", Icons.Default.Assessment, Color(0xFF8B5CF6))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            StatCard("$avgScore pkt", "Średni wynik", Icons.Default.MenuBook, Color(0xFF38BDF8))
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            StatCard("$bestScore", "Najlepszy wynik", Icons.Default.Star, Color(0xFFFFD600))
                        }
                    }
                }
            }

            // Historic Attempts Header
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "HISTORIA PRÓB EGZAMINACYJNYCH:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // List of exam items
            items(examResults) { result ->
                val dateStr = try {
                    val sdf = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault())
                    sdf.format(Date(result.examDate))
                } catch (e: Exception) {
                    "Nieznana data"
                }

                val statusColor = if (result.isPassed) Color(0xFF10B981) else Color(0xFFEF4444)
                val statusLabel = if (result.isPassed) "ZALICZONY" else "NIEZALICZONY"

                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            // Status Dot Badge
                            Surface(
                                color = statusColor.copy(alpha = 0.12f),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.8f)),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (result.isPassed) Icons.Default.Check else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = statusColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "$statusLabel • ${result.score}/${result.maxPoints} pkt",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = dateStr,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }

                        // Delete single history item
                        IconButton(
                            onClick = { onDeleteResult(result.id) },
                            modifier = Modifier.testTag("delete_exam_result_${result.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Usuń wynik",
                                tint = Color.White.copy(alpha = 0.35f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(value: String, label: String, icon: ImageVector, color: Color) {
    Surface(
        color = Color(0xFF131B2E),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = color.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text(text = label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
            }
        }
    }
}
