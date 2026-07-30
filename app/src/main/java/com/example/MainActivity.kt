package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.OskProTheme
import com.example.ui.viewmodel.OskViewModel
import com.example.ui.viewmodel.UserRole
import kotlinx.coroutines.launch

enum class ActiveDialog {
    ADD_VEHICLE,
    GEN_KEY,
    BOOK_LESSON,
    ADD_FINANCE,
    REPORT_SICK_LEAVE,
    STUDENT_RESERVATION,
    NEW_INSTRUCTOR,
    SCHEDULE_EXAM
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: OskViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            OskProTheme(darkTheme = isDarkMode) {
                OskProApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OskProApp(viewModel: OskViewModel) {
    val showSplash by viewModel.showSplash.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val companyName by viewModel.companyName.collectAsStateWithLifecycle()
    val companyNip by viewModel.companyNip.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()

    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val userKeys by viewModel.userKeys.collectAsStateWithLifecycle()
    val lessons by viewModel.lessons.collectAsStateWithLifecycle()
    val finances by viewModel.finances.collectAsStateWithLifecycle()
    val sickLeaves by viewModel.sickLeaves.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val reservations by viewModel.reservations.collectAsStateWithLifecycle()
    val totalBalancePln by viewModel.totalBalancePln.collectAsStateWithLifecycle()
    val vehicleReports by viewModel.vehicleReports.collectAsStateWithLifecycle()
    val examResults by viewModel.examResults.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val examReservations by viewModel.examReservations.collectAsStateWithLifecycle()
    val categoryPrices by viewModel.categoryPrices.collectAsStateWithLifecycle()
    val fixedCosts by viewModel.fixedCosts.collectAsStateWithLifecycle()
    val isManagerInitialized by viewModel.isManagerInitialized.collectAsStateWithLifecycle()

    val unreadCount = remember(notifications, currentRole) {
        notifications.filter { !it.isRead && when (currentRole) {
            UserRole.STUDENT -> it.targetGroup == "KURSANT" || it.targetGroup == "ALL"
            UserRole.INSTRUCTOR -> it.targetGroup == "INSTRUKTOR" || it.targetGroup == "ALL"
            UserRole.MANAGER -> true
        }}.size
    }

    var showRoleSelection by remember { mutableStateOf(true) }
    var pendingRole by remember { mutableStateOf<UserRole?>(null) }
    var showKeyAuth by remember { mutableStateOf(false) }
    var currentRoute by remember { mutableStateOf(ScreenRoute.DASHBOARD) }
    var activeDialog by remember { mutableStateOf<ActiveDialog?>(null) }
    var prefilledExamStudentName by remember { mutableStateOf("") }
    var prefilledExamPkkNumber by remember { mutableStateOf("") }
    var reportingVehicle by remember { mutableStateOf<com.example.data.db.VehicleEntity?>(null) }

    val savedPin by viewModel.savedPin.collectAsStateWithLifecycle()
    var showPinAuth by remember(savedPin) { mutableStateOf(savedPin != null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    if (showSplash) {
        SplashScreen(onDismiss = { viewModel.dismissSplash() })
    } else if (showPinAuth && showRoleSelection) {
        PinAuthScreen(
            onPinSuccess = {
                showPinAuth = false
                showRoleSelection = false
            },
            onCancel = {
                viewModel.disablePinLogin()
                showPinAuth = false
            },
            validatePin = { pin -> viewModel.validatePin(pin) }
        )
    } else if (showRoleSelection) {
        RoleSelectionScreen(
            onRoleSelected = { selectedRole ->
                if (selectedRole == UserRole.MANAGER) {
                    viewModel.setRole(selectedRole)
                    currentRoute = ScreenRoute.DASHBOARD
                    showRoleSelection = false
                } else {
                    pendingRole = selectedRole
                    showKeyAuth = true
                    showRoleSelection = false
                }
            }
        )
    } else if (showKeyAuth && pendingRole != null) {
        KeyAuthScreen(
            role = pendingRole!!,
            onAuthSuccess = { name ->
                viewModel.setRole(pendingRole!!)
                viewModel.setUserName(name)
                currentRoute = ScreenRoute.DASHBOARD
                showKeyAuth = false
            },
            onCancel = {
                showKeyAuth = false
                showRoleSelection = true
            },
            validateKey = { name, key -> viewModel.validateUserKey(name, key, pendingRole!!) }
        )
    } else if (currentRole == UserRole.MANAGER && !isManagerInitialized) {
        ManagerOnboardingScreen(
            onComplete = { name, nip, phone ->
                viewModel.completeManagerOnboarding(name, nip, phone)
            }
        )
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawerContent(
                    currentRoute = currentRoute,
                    currentRole = currentRole,
                    companyName = companyName,
                    companyNip = companyNip,
                    userName = userName,
                    onRoleSelect = { role ->
                        viewModel.setRole(role)
                        // Reset route to dashboard on role change if current route is inaccessible
                        if (!getRoutesForRole(role).contains(currentRoute)) {
                            currentRoute = ScreenRoute.DASHBOARD
                        }
                    },
                    onRouteSelect = { route -> currentRoute = route },
                    onCloseDrawer = { coroutineScope.launch { drawerState.close() } },
                    onLogout = { viewModel.disablePinLogin(); showRoleSelection = true }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    OskTopBar(
                        currentRoute = currentRoute,
                        currentRole = currentRole,
                        isDarkMode = isDarkMode,
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                        onToggleDarkMode = { viewModel.toggleDarkMode() },
                        onNotificationsClick = { currentRoute = ScreenRoute.NOTIFICATIONS },
                        unreadCount = unreadCount
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentRoute) {
                        ScreenRoute.DASHBOARD -> DashboardScreen(
                            currentRole = currentRole,
                            companyName = companyName,
                            companyNip = companyNip,
                            userName = userName,
                            vehicleCount = vehicles.size,
                            lessonCount = lessons.size,
                            balancePln = totalBalancePln,
                            keyCount = userKeys.size,
                            sickLeaveCount = sickLeaves.size,
                            onNavigate = { currentRoute = it },
                            onAddVehicleClick = { activeDialog = ActiveDialog.ADD_VEHICLE },
                            onBookLessonClick = { activeDialog = ActiveDialog.BOOK_LESSON },
                            onAddFinanceClick = { activeDialog = ActiveDialog.ADD_FINANCE },
                            onGenerateKeyClick = { activeDialog = ActiveDialog.GEN_KEY },
                            onStudentReservationClick = { activeDialog = ActiveDialog.STUDENT_RESERVATION }
                        )

                        ScreenRoute.NOTIFICATIONS -> NotificationsScreen(
                            currentRole = currentRole,
                            notifications = notifications,
                            onSendNotification = { target, title, msg -> viewModel.sendNotification(target, title, msg) },
                            onMarkAsRead = { id -> viewModel.markNotificationAsRead(id) },
                            onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() },
                            onDeleteNotification = { id -> viewModel.deleteNotification(id) },
                            onClearAll = { viewModel.clearAllNotifications() }
                        )

                        ScreenRoute.EXAMS -> ExamReservationsScreen(
                            currentRole = currentRole,
                            examReservations = examReservations,
                            onScheduleExam = { studentName, date, time, examType, pkkNumber, pkkStatus, hasPhoto, isConfirmed ->
                                viewModel.scheduleExam(studentName, date, time, examType, pkkNumber, pkkStatus, hasPhoto, isConfirmed)
                            },
                            onUpdatePkk = { id, pkkNumber, isConfirmed, pkkStatus ->
                                viewModel.updateExamPkk(id, pkkNumber, isConfirmed, pkkStatus)
                            },
                            onDeleteExam = { id -> viewModel.deleteExamReservation(id) }
                        )

                        ScreenRoute.RESERVATIONS -> ReservationsScreen(
                            currentRole = currentRole,
                            userName = userName,
                            reservations = reservations,
                            onOpenReservationModal = { activeDialog = ActiveDialog.STUDENT_RESERVATION },
                            onUpdateStatus = { id, status -> viewModel.updateReservationStatus(id, status) },
                            onProposeAlternatives = { id, alts, note -> viewModel.proposeReservationAlternatives(id, alts, note) },
                            onAcceptAlternative = { id, date, time -> viewModel.acceptReservationAlternative(id, date, time) },
                            onDeleteReservation = { id -> viewModel.deleteReservation(id) },
                            onScheduleExam = { name, pkk ->
                                prefilledExamStudentName = name
                                prefilledExamPkkNumber = pkk
                                activeDialog = ActiveDialog.SCHEDULE_EXAM
                            }
                        )

                        ScreenRoute.DOCUMENTS -> DocumentsScreen(
                            viewModel = viewModel,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        ScreenRoute.FLEET -> FleetScreen(
                            vehicles = vehicles,
                            userRole = currentRole.label,
                            onAddVehicleClick = { activeDialog = ActiveDialog.ADD_VEHICLE },
                            onDeleteVehicle = { id -> viewModel.deleteVehicle(id) },
                            onReportClick = { vehicle -> reportingVehicle = vehicle }
                        )

                        ScreenRoute.REPORTS -> ReportsScreen(
                            vehicles = vehicles,
                            reports = vehicleReports,
                            onDeleteReport = { id -> viewModel.deleteVehicleReport(id) },
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        ScreenRoute.SCHEDULE -> ScheduleScreen(
                            viewModel = viewModel,
                            currentRole = currentRole,
                            userName = userName,
                            lessons = lessons,
                            onBookLessonClick = { activeDialog = ActiveDialog.BOOK_LESSON },
                            onUpdateLessonStatus = { id, status -> viewModel.updateLessonStatus(id, status) },
                            onDeleteLesson = { id -> viewModel.deleteLesson(id) }
                        )

                        ScreenRoute.FINANCE -> FinanceScreen(
                            finances = finances,
                            totalBalancePln = totalBalancePln,
                            fixedCosts = fixedCosts,
                            categoryPrices = categoryPrices,
                            reservations = reservations,
                            onAddFinanceClick = { activeDialog = ActiveDialog.ADD_FINANCE },
                            onDeleteFinance = { id -> viewModel.deleteFinance(id) }
                        )

                        ScreenRoute.USER_KEYS -> AccessKeysScreen(
                            userKeys = userKeys,
                            onGenerateKeyClick = { activeDialog = ActiveDialog.GEN_KEY },
                            onDeleteKey = { id -> viewModel.deleteUserKey(id) }
                        )

                        ScreenRoute.SICK_LEAVE -> SickLeaveScreen(
                            sickLeaves = sickLeaves,
                            onReportSickLeaveClick = { activeDialog = ActiveDialog.REPORT_SICK_LEAVE },
                            onUpdateStatus = { id, status -> viewModel.updateSickLeaveStatus(id, status) }
                        )

                        ScreenRoute.CHAT -> ChatScreen(
                            messages = chatMessages,
                            userKeys = userKeys,
                            currentUserRole = currentRole,
                            currentUserName = userName,
                            onSendMessage = { text, channelId -> viewModel.sendChatMessage(text, channelId) },
                            onUpdateBlockStatus = { id, isBlocked -> viewModel.updateUserBlockStatus(id, isBlocked) },
                            onUpdateRemoveStatus = { id, isRemoved -> viewModel.updateUserRemoveStatus(id, isRemoved) }
                        )

                        ScreenRoute.DRIVING_TESTS -> DrivingTestsScreen(
                            viewModel = viewModel,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        ScreenRoute.OPTIONS -> OptionsScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        // Active Dialog Handler
        when (activeDialog) {
            ActiveDialog.ADD_VEHICLE -> AddVehicleDialog(
                onDismiss = { activeDialog = null },
                onConfirm = { brand, model, plate, fuel, status, inspection, insurance, instructor ->
                    viewModel.addVehicle(brand, model, plate, fuel, status, inspection, insurance, instructor)
                }
            )

            ActiveDialog.GEN_KEY -> GenerateKeyDialog(
                onDismiss = { activeDialog = null },
                onConfirm = { role, assignedName ->
                    viewModel.generateUserKey(role, assignedName)
                }
            )

            ActiveDialog.BOOK_LESSON -> BookLessonDialog(
                defaultStudent = if (currentRole == com.example.ui.viewmodel.UserRole.STUDENT) userName else "",
                defaultInstructor = if (currentRole == com.example.ui.viewmodel.UserRole.INSTRUCTOR) userName else "",
                availableVehicles = vehicles.map { "${it.brand} ${it.model} (${it.registrationPlate})" },
                onDismiss = { activeDialog = null },
                onConfirm = { student, instructor, vehicle, date, time, hours, notes ->
                    viewModel.bookLesson(student, instructor, vehicle, date, time, hours, notes)
                }
            )

            ActiveDialog.ADD_FINANCE -> AddFinanceDialog(
                onDismiss = { activeDialog = null },
                onConfirm = { title, amount, type, category, date ->
                    viewModel.addFinance(title, amount, type, category, date)
                }
            )

            ActiveDialog.REPORT_SICK_LEAVE -> ReportSickLeaveDialog(
                instructorName = userName,
                onDismiss = { activeDialog = null },
                onConfirm = { instructor, startDate, endDate, reason ->
                    viewModel.reportSickLeave(instructor, startDate, endDate, reason)
                }
            )

            ActiveDialog.STUDENT_RESERVATION -> StudentReservationModal(
                defaultStudentName = if (currentRole == com.example.ui.viewmodel.UserRole.STUDENT) userName else "",
                categoryPrices = categoryPrices,
                onDismiss = { activeDialog = null },
                onConfirm = { fullName, dob, pesel, phone, email, date, slots, category, pkkNumber, documentPhotoPath ->
                    viewModel.submitReservation(fullName, dob, pesel, phone, email, date, slots, category, pkkNumber, documentPhotoPath)
                }
            )

            ActiveDialog.NEW_INSTRUCTOR -> NewInstructorDialog(
                onDismiss = { activeDialog = null },
                onConfirm = { name, phone, email, notes, categories ->
                    // Actually, we can generate a key automatically here, or save to ViewModel
                    viewModel.generateUserKey(com.example.ui.viewmodel.UserRole.INSTRUCTOR.label, name)
                }
            )

            ActiveDialog.SCHEDULE_EXAM -> ScheduleExamModal(
                prefilledStudentName = prefilledExamStudentName,
                prefilledPkkNumber = prefilledExamPkkNumber,
                onDismiss = {
                    activeDialog = null
                    prefilledExamStudentName = ""
                    prefilledExamPkkNumber = ""
                },
                onConfirm = { studentName, date, time, examType, pkkNumber, pkkStatus, hasPhoto, isConfirmed ->
                    viewModel.scheduleExam(studentName, date, time, examType, pkkNumber, pkkStatus, hasPhoto, isConfirmed)
                    activeDialog = null
                    prefilledExamStudentName = ""
                    prefilledExamPkkNumber = ""
                }
            )

            null -> {}
        }

        reportingVehicle?.let { vehicle ->
            VehicleReportDialog(
                vehicle = vehicle,
                onDismiss = { reportingVehicle = null },
                onConfirm = { startOdo, refuelOdo, fuelQty, pricePerL, expenseName, expenseCost, endOdo ->
                    viewModel.submitVehicleReport(
                        vehicleId = vehicle.id,
                        vehiclePlate = vehicle.registrationPlate,
                        startOdometer = startOdo,
                        refuelOdometer = refuelOdo,
                        fuelQuantity = fuelQty,
                        pricePerLiter = pricePerL,
                        extraExpenseName = expenseName,
                        extraExpenseCost = expenseCost,
                        endOdometer = endOdo
                    )
                }
            )
        }
    }
}
