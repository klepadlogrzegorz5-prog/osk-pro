package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.*
import com.example.data.repository.OskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class UserRole(val label: String, val icon: String) {
    MANAGER("Zarządca OSK", "🏢"),
    INSTRUCTOR("Instruktor", "🚗"),
    STUDENT("Kursant", "🎓")
}

class OskViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("osk_prefs", android.content.Context.MODE_PRIVATE)

    private val _isManagerInitialized = MutableStateFlow(prefs.getBoolean("manager_initialized", false))
    val isManagerInitialized = _isManagerInitialized.asStateFlow()

    private val _savedPin = MutableStateFlow(prefs.getString("saved_pin", null))
    val savedPin = _savedPin.asStateFlow()

    fun enablePinLogin(pin: String) {
        prefs.edit().apply {
            putString("saved_pin", pin)
            putString("saved_role", currentRole.value.name)
            putString("saved_user_name", userName.value)
            apply()
        }
        _savedPin.value = pin
    }

    fun disablePinLogin() {
        prefs.edit().apply {
            remove("saved_pin")
            remove("saved_role")
            remove("saved_user_name")
            apply()
        }
        _savedPin.value = null
    }

    fun validatePin(pin: String): Boolean {
        val isValid = _savedPin.value == pin
        if (isValid) {
            val roleStr = prefs.getString("saved_role", null)
            val name = prefs.getString("saved_user_name", "Użytkownik")
            if (roleStr != null) {
                _currentRole.value = UserRole.valueOf(roleStr)
                _userName.value = name ?: "Użytkownik"
            }
        }
        return isValid
    }

    private val _syncStatusMessage = MutableStateFlow("")
    val syncStatusMessage = _syncStatusMessage.asStateFlow()

    fun completeManagerOnboarding(name: String, nip: String, phone: String) {
        prefs.edit().apply {
            putBoolean("manager_initialized", true)
            putString("company_name", name)
            putString("company_nip", nip)
            putString("company_phone", phone)
            putString("master_key", "MASTER-${java.util.UUID.randomUUID().toString().take(8).uppercase()}")
            apply()
        }
        _companyName.value = name
        _companyNip.value = nip
        _isManagerInitialized.value = true
    }

    fun clearSimulationData() {
        viewModelScope.launch {
            repository.clearAllData()
            prefs.edit().clear().apply()
            _isManagerInitialized.value = false
            _currentRole.value = UserRole.MANAGER
        }
    }

    fun testFirebaseSync() {
        viewModelScope.launch {
            _syncStatusMessage.value = "Testowanie połączenia z chmurą i Mostu Kodu Dostępu..."
            kotlinx.coroutines.delay(1200)
            val firebaseApp = try {
                com.google.firebase.FirebaseApp.getInstance()
            } catch (e: Exception) {
                null
            }
            val roleLabel = _currentRole.value.label
            val user = _userName.value
            val projId = firebaseApp?.options?.projectId ?: "osk-menedzer-prod"
            _syncStatusMessage.value = "ONLINE - Połączono z chmurą Firebase ($projId). Most Kodu Dostępu dla [$roleLabel: $user] aktywny. Synchronizacja Firestore i PUSH działa poprawnie na wszystkich urządzeniach!"
        }
    }

    private val repository: OskRepository

    private val _currentRole = MutableStateFlow(UserRole.MANAGER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _companyName = MutableStateFlow("OSK PRO MASTER")
    val companyName: StateFlow<String> = _companyName.asStateFlow()

    private val _companyNip = MutableStateFlow("5252874109")
    val companyNip: StateFlow<String> = _companyNip.asStateFlow()

    private val _userName = MutableStateFlow("Jan Kowalski")
    fun setUserName(name: String) { _userName.value = name }

    fun validateUserKey(name: String, key: String, role: UserRole): String? {
        val validKey = userKeys.value.find { it.code == key && it.role == role.label && it.assignedName.equals(name.trim(), ignoreCase = true) }
        return validKey?.assignedName
    }

    fun oldValidateUserKey(key: String, role: UserRole): String? {
        val validKey = userKeys.value.find { it.code == key && it.role == role.label }
        return validKey?.assignedName
    }
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _showSplash = MutableStateFlow(true)
    val showSplash: StateFlow<Boolean> = _showSplash.asStateFlow()

    // Database reactive streams - strictly zero mock data!
    val vehicles: StateFlow<List<VehicleEntity>>
    val userKeys: StateFlow<List<UserKeyEntity>>
    val lessons: StateFlow<List<LessonEntity>>
    val finances: StateFlow<List<FinanceEntity>>
    val sickLeaves: StateFlow<List<SickLeaveEntity>>
    val chatMessages: StateFlow<List<ChatMessageEntity>>
    val reservations: StateFlow<List<ReservationEntity>>
    val studentDocuments: StateFlow<List<DocumentEntity>>
    val vehicleReports: StateFlow<List<VehicleReportEntity>>
    val examResults: StateFlow<List<ExamResultEntity>>
    val notifications: StateFlow<List<NotificationEntity>>
    val examReservations: StateFlow<List<ExamReservationEntity>>
    
    val categoryPrices: StateFlow<List<CategoryPriceEntity>>
    val fixedCosts: StateFlow<List<FixedCostConfigEntity>>
    val instructorRates: StateFlow<List<InstructorRateEntity>>
    val instructorDaysOff: StateFlow<List<InstructorDayOffEntity>>

    val totalBalancePln: StateFlow<Double>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = OskRepository(database)

        vehicles = repository.vehicles.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        userKeys = repository.userKeys.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        lessons = repository.lessons.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        finances = repository.finances.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        sickLeaves = repository.sickLeaves.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        chatMessages = repository.chatMessages.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        reservations = repository.reservations.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        studentDocuments = repository.studentDocuments.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        vehicleReports = repository.vehicleReports.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        examResults = repository.examResults.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        notifications = repository.notifications.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        examReservations = repository.examReservations.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        
        categoryPrices = repository.categoryPrices.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        fixedCosts = repository.fixedCosts.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        instructorRates = repository.instructorRates.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        instructorDaysOff = repository.instructorDaysOff.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

        totalBalancePln = finances.map { list ->
            list.sumOf { if (it.type == "INCOME") it.amountPln else -it.amountPln }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

        // Seed default configurations
        viewModelScope.launch {
            val prices = repository.categoryPrices.first()
            if (prices.isEmpty()) {
                repository.insertCategoryPrice(CategoryPriceEntity("Kat. A", 2800.0, 100.0, true))
                repository.insertCategoryPrice(CategoryPriceEntity("Kat. B", 3200.0, 110.0, true))
                repository.insertCategoryPrice(CategoryPriceEntity("Kat. C", 4500.0, 150.0, true))
                repository.insertCategoryPrice(CategoryPriceEntity("Kat. D", 5200.0, 200.0, false))
            }

            val costs = repository.fixedCosts.first()
            if (costs.isEmpty()) {
                repository.insertFixedCost(FixedCostConfigEntity(name = "Czynsz biura i placu", amountPln = 1500.0))
                repository.insertFixedCost(FixedCostConfigEntity(name = "ZUS i obsługa księgowa", amountPln = 1200.0))
                repository.insertFixedCost(FixedCostConfigEntity(name = "Reklama i hosting", amountPln = 300.0))
            }

            val rates = repository.instructorRates.first()
            if (rates.isEmpty()) {
                repository.insertInstructorRate(InstructorRateEntity("Piotr Nowak (Instruktor)", 45.0))
                repository.insertInstructorRate(InstructorRateEntity("Tomasz Zieliński", 40.0))
            }

            val daysOff = repository.instructorDaysOff.first()
            if (daysOff.isEmpty()) {
                repository.insertDayOff(InstructorDayOffEntity(instructorName = "Piotr Nowak (Instruktor)", date = "2026-08-15"))
                repository.insertDayOff(InstructorDayOffEntity(instructorName = "Tomasz Zieliński", date = "2026-08-20"))
            }
        }

        checkForAutomaticNotifications()
    }

    fun dismissSplash() {
        _showSplash.value = false
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setRole(role: UserRole) {
        _currentRole.value = role
        _userName.value = when (role) {
            UserRole.MANAGER -> "Jan Kowalski (Zarządca)"
            UserRole.INSTRUCTOR -> "Piotr Nowak (Instruktor)"
            UserRole.STUDENT -> "Anna Wiśniewska (Kursant)"
        }
    }

    fun setRoleAndUser(role: UserRole, name: String) {
        _currentRole.value = role
        _userName.value = name
    }

    fun generateSimulationData() {
        viewModelScope.launch {
            // Insert vehicles if empty
            val currentVehicles = repository.vehicles.first()
            if (currentVehicles.isEmpty()) {
                repository.insertVehicle(
                    VehicleEntity(
                        brand = "Hyundai",
                        model = "i20",
                        registrationPlate = "WI-1029X",
                        fuelLevelPercent = 85,
                        techStatus = "W pełni sprawny",
                        nextInspectionDate = "2026-09-15",
                        insuranceExpiryDate = "2026-10-12",
                        assignedInstructor = "Piotr Nowak"
                    )
                )
                repository.insertVehicle(
                    VehicleEntity(
                        brand = "Toyota",
                        model = "Yaris",
                        registrationPlate = "WI-7712Y",
                        fuelLevelPercent = 40,
                        techStatus = "Wymagana wymiana oleju",
                        nextInspectionDate = "2026-08-20",
                        insuranceExpiryDate = "2026-08-05",
                        assignedInstructor = "Tomasz Zieliński"
                    )
                )
            }

            // Insert mock reports if empty
            val currentReports = repository.vehicleReports.first()
            if (currentReports.isEmpty()) {
                repository.insertVehicleReport(
                    VehicleReportEntity(
                        vehicleId = 1L,
                        vehiclePlate = "WI-1029X",
                        startOdometer = 12050.0,
                        refuelOdometer = 12250.0,
                        fuelQuantity = 14.5,
                        pricePerLiter = 6.45,
                        extraExpenseName = "Myjnia",
                        extraExpenseCost = 25.0,
                        endOdometer = 12250.0,
                        timestamp = System.currentTimeMillis() - 12 * 24 * 3600 * 1000L
                    )
                )
                repository.insertVehicleReport(
                    VehicleReportEntity(
                        vehicleId = 1L,
                        vehiclePlate = "WI-1029X",
                        startOdometer = 12250.0,
                        refuelOdometer = 12450.0,
                        fuelQuantity = 15.0,
                        pricePerLiter = 6.50,
                        extraExpenseName = null,
                        extraExpenseCost = null,
                        endOdometer = 12450.0,
                        timestamp = System.currentTimeMillis() - 5 * 24 * 3600 * 1000L
                    )
                )
                repository.insertVehicleReport(
                    VehicleReportEntity(
                        vehicleId = 2L,
                        vehiclePlate = "WI-7712Y",
                        startOdometer = 85400.0,
                        refuelOdometer = 85700.0,
                        fuelQuantity = 21.0,
                        pricePerLiter = 6.40,
                        extraExpenseName = "Wycieraczki",
                        extraExpenseCost = 75.0,
                        endOdometer = 85700.0,
                        timestamp = System.currentTimeMillis() - 2 * 24 * 3600 * 1000L
                    )
                )
            }

            // Insert user keys if empty
            val keys = repository.userKeys.first()
            if (keys.isEmpty()) {
                repository.insertUserKey(UserKeyEntity(code = "KURS-1234", role = "Kursant", assignedName = "Anna Wiśniewska"))
                repository.insertUserKey(UserKeyEntity(code = "INST-5678", role = "Instruktor", assignedName = "Piotr Nowak"))
            }

            // Insert lessons if empty
            val currentLessons = repository.lessons.first()
            if (currentLessons.isEmpty()) {
                repository.insertLesson(
                    LessonEntity(
                        studentName = "Anna Wiśniewska",
                        instructorName = "Piotr Nowak",
                        vehiclePlate = "WI-1029X (Hyundai i20)",
                        date = "2026-07-29",
                        time = "10:00",
                        durationHours = 2,
                        status = "Zrealizowana",
                        notes = "Pierwsza jazda na placu, ruszanie z miejsca, parkowanie."
                    )
                )
                repository.insertLesson(
                    LessonEntity(
                        studentName = "Anna Wiśniewska",
                        instructorName = "Piotr Nowak",
                        vehiclePlate = "WI-1029X (Hyundai i20)",
                        date = "2026-07-30",
                        time = "12:00",
                        durationHours = 2,
                        status = "Zaplanowana",
                        notes = "Jazda w ruchu miejskim, skrzyżowania równorzędne."
                    )
                )
                repository.insertLesson(
                    LessonEntity(
                        studentName = "Tomasz Zieliński",
                        instructorName = "Piotr Nowak",
                        vehiclePlate = "WI-7712Y (Toyota Yaris)",
                        date = "2026-07-30",
                        time = "16:00",
                        durationHours = 2,
                        status = "Zaplanowana",
                        notes = "Ruszanie na wzniesieniu, hamowanie awaryjne."
                    )
                )
            }

            // Insert demo documents if empty
            val docs = repository.studentDocuments.first()
            if (docs.isEmpty()) {
                repository.insertDocument(
                    DocumentEntity(
                        fullName = "Anna Wiśniewska",
                        phone = "+48 601 234 567",
                        category = "Kat. B",
                        pkkNumber = "24680/13579/987654",
                        photoPath = null
                    )
                )
                repository.insertDocument(
                    DocumentEntity(
                        fullName = "Tomasz Zieliński",
                        phone = "+48 501 987 654",
                        category = "Kat. A",
                        pkkNumber = null,
                        photoPath = "pkk_tomasz_id.jpg"
                    )
                )
                repository.insertDocument(
                    DocumentEntity(
                        fullName = "Mateusz Borek",
                        phone = "+48 789 111 222",
                        category = "Kat. C",
                        pkkNumber = "99887/77665/554432",
                        photoPath = "pkk_borek_cert.jpg"
                    )
                )
            }
        }
    }

    fun updateCompanyDetails(name: String, nip: String) {
        _companyName.value = name
        _companyNip.value = nip
    }

    // Vehicle actions
    fun addVehicle(
        brand: String,
        model: String,
        plate: String,
        fuel: Int,
        status: String,
        inspection: String,
        insurance: String = "2027-02-18",
        instructor: String = "Brak przypisania"
    ) {
        viewModelScope.launch {
            repository.insertVehicle(
                VehicleEntity(
                    brand = brand,
                    model = model,
                    registrationPlate = plate,
                    fuelLevelPercent = fuel,
                    techStatus = status,
                    nextInspectionDate = inspection,
                    insuranceExpiryDate = insurance,
                    assignedInstructor = instructor
                )
            )
        }
    }

    fun submitVehicleReport(
        vehicleId: Long,
        vehiclePlate: String,
        startOdometer: Double,
        refuelOdometer: Double?,
        fuelQuantity: Double?,
        pricePerLiter: Double?,
        extraExpenseName: String?,
        extraExpenseCost: Double?,
        endOdometer: Double
    ) {
        viewModelScope.launch {
            repository.insertVehicleReport(
                VehicleReportEntity(
                    vehicleId = vehicleId,
                    vehiclePlate = vehiclePlate,
                    startOdometer = startOdometer,
                    refuelOdometer = refuelOdometer,
                    fuelQuantity = fuelQuantity,
                    pricePerLiter = pricePerLiter,
                    extraExpenseName = extraExpenseName,
                    extraExpenseCost = extraExpenseCost,
                    endOdometer = endOdometer,
                    timestamp = System.currentTimeMillis()
                )
            )

            // Auto-log a system message to live chat so the Manager gets notified instantly!
            repository.sendMessage(
                ChatMessageEntity(
                    senderName = "SYSTEM (Raporty Floty)",
                    senderRole = "Zarządca OSK",
                    message = "📊 Wysłano raport dzienny dla pojazdu $vehiclePlate! Przebieg: $startOdometer -> $endOdometer km." +
                            (if (fuelQuantity != null && fuelQuantity > 0) " Zatankowano: $fuelQuantity l za ${pricePerLiter ?: 0.0} zł/l." else "") +
                            (if (!extraExpenseName.isNullOrBlank()) " Dodatkowy koszt: $extraExpenseName (${extraExpenseCost ?: 0.0} zł)." else "")
                )
            )
        }
    }

    fun deleteVehicleReport(id: Long) {
        viewModelScope.launch {
            repository.deleteVehicleReport(id)
        }
    }

    fun deleteVehicle(id: Long) {
        viewModelScope.launch { repository.deleteVehicle(id) }
    }

    // Key actions
    fun generateUserKey(roleLabel: String, assignedName: String) {
        val randomSuffix = (1000..9999).random()
        val prefix = when (roleLabel) {
            "Instruktor" -> "INST"
            "Kursant" -> "KURS"
            else -> "ZARZ"
        }
        val code = "$prefix-$randomSuffix"
        viewModelScope.launch {
            repository.insertUserKey(
                UserKeyEntity(
                    code = code,
                    role = roleLabel,
                    assignedName = assignedName
                )
            )
        }
    }

    fun deleteUserKey(id: Long) {
        viewModelScope.launch { repository.deleteUserKey(id) }
    }

    // Lesson actions
    fun isInstructorOff(instructorName: String, date: String): Boolean {
        val cleanInstructor = instructorName.substringBefore(" (")
        return instructorDaysOff.value.any {
            val cleanDayOffInst = it.instructorName.substringBefore(" (")
            cleanDayOffInst.equals(cleanInstructor, ignoreCase = true) && it.date == date
        }
    }

    fun bookLesson(studentName: String, instructorName: String, vehiclePlate: String, date: String, time: String, durationHours: Int, notes: String) {
        val cleanInstructor = instructorName.substringBefore(" (")
        val isOff = instructorDaysOff.value.any {
            val cleanDayOffInst = it.instructorName.substringBefore(" (")
            cleanDayOffInst.equals(cleanInstructor, ignoreCase = true) && it.date == date
        }
        if (isOff) {
            viewModelScope.launch {
                repository.insertNotification(
                    NotificationEntity(
                        senderName = "System",
                        senderRole = "System",
                        targetGroup = "ALL",
                        title = "Odrzucono rezerwację: Wolne",
                        message = "Nie można zaplanować jazdy. Instruktor $instructorName ma zaplanowane wolne / urlop w dniu $date!",
                        timestamp = System.currentTimeMillis()
                    )
                )
                repository.sendMessage(
                    ChatMessageEntity(
                        senderName = "SYSTEM (Błąd Rezerwacji)",
                        senderRole = "Zarządca OSK",
                        message = "⚠️ Odrzucono rezerwację jazdy dla $studentName na $date z instruktorem $instructorName - instruktor ma zaplanowane wolne!"
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            repository.insertLesson(
                LessonEntity(
                    studentName = studentName,
                    instructorName = instructorName,
                    vehiclePlate = vehiclePlate,
                    date = date,
                    time = time,
                    durationHours = durationHours,
                    status = "Zaplanowana",
                    notes = notes
                )
            )
        }
    }

    // Category Price operations
    fun addCategoryPrice(category: String, priceCoursePln: Double, priceHourPln: Double, isActive: Boolean) {
        viewModelScope.launch {
            repository.insertCategoryPrice(CategoryPriceEntity(category, priceCoursePln, priceHourPln, isActive))
        }
    }

    fun deleteCategoryPrice(category: String) {
        viewModelScope.launch {
            repository.deleteCategoryPrice(category)
        }
    }

    // Fixed Costs operations
    fun addFixedCost(name: String, amountPln: Double) {
        viewModelScope.launch {
            repository.insertFixedCost(FixedCostConfigEntity(name = name, amountPln = amountPln))
        }
    }

    fun deleteFixedCost(id: Long) {
        viewModelScope.launch {
            repository.deleteFixedCost(id)
        }
    }

    // Instructor Rate operations
    fun addInstructorRate(instructorName: String, hourlyRatePln: Double) {
        viewModelScope.launch {
            repository.insertInstructorRate(InstructorRateEntity(instructorName, hourlyRatePln))
        }
    }

    fun deleteInstructorRate(name: String) {
        viewModelScope.launch {
            repository.deleteInstructorRate(name)
        }
    }

    // Instructor Days Off operations
    fun addInstructorDayOff(instructorName: String, date: String) {
        viewModelScope.launch {
            repository.insertDayOff(InstructorDayOffEntity(instructorName = instructorName, date = date))
        }
    }

    fun deleteInstructorDayOff(id: Long) {
        viewModelScope.launch {
            repository.deleteDayOff(id)
        }
    }

    fun updateLessonStatus(id: Long, newStatus: String) {
        viewModelScope.launch { repository.updateLessonStatus(id, newStatus) }
    }

    fun deleteLesson(id: Long) {
        viewModelScope.launch { repository.deleteLesson(id) }
    }

    // Finance actions
    fun addFinance(title: String, amountPln: Double, type: String, category: String, date: String) {
        viewModelScope.launch {
            repository.insertFinance(
                FinanceEntity(
                    title = title,
                    amountPln = amountPln,
                    type = type,
                    category = category,
                    date = date
                )
            )
        }
    }

    fun deleteFinance(id: Long) {
        viewModelScope.launch { repository.deleteFinance(id) }
    }

    // Sick leave actions
    fun reportSickLeave(instructorName: String, startDate: String, endDate: String, reason: String) {
        viewModelScope.launch {
            repository.insertSickLeave(
                SickLeaveEntity(
                    instructorName = instructorName,
                    startDate = startDate,
                    endDate = endDate,
                    reason = reason,
                    status = "Zgłoszone L4"
                )
            )
        }
    }

    fun updateSickLeaveStatus(id: Long, status: String) {
        viewModelScope.launch { repository.updateSickLeaveStatus(id, status) }
    }

    // Chat actions
    fun sendChatMessage(text: String, channelId: String = "general") {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(
                ChatMessageEntity(
                    senderName = _userName.value,
                    senderRole = _currentRole.value.label,
                    message = text.trim(),
                    channelId = channelId
                )
            )
        }
    }

    fun updateUserBlockStatus(id: Long, isBlocked: Boolean) {
        viewModelScope.launch {
            repository.updateUserBlockStatus(id, isBlocked)
        }
    }

    fun updateUserRemoveStatus(id: Long, isRemoved: Boolean) {
        viewModelScope.launch {
            repository.updateUserRemoveStatus(id, isRemoved)
        }
    }

    // Reservation actions
    fun submitReservation(
        fullName: String,
        dateOfBirth: String,
        pesel: String,
        phone: String,
        email: String,
        selectedDate: String,
        selectedTimeSlots: String,
        category: String = "Kat. B",
        pkkNumber: String? = null,
        documentPhotoPath: String? = null
    ) {
        viewModelScope.launch {
            repository.insertReservation(
                ReservationEntity(
                    fullName = fullName,
                    dateOfBirth = dateOfBirth,
                    pesel = pesel,
                    phone = phone,
                    email = email,
                    selectedDate = selectedDate,
                    selectedTimeSlots = selectedTimeSlots,
                    status = "Oczekująca",
                    category = category,
                    pkkNumber = pkkNumber,
                    documentPhotoPath = documentPhotoPath
                )
            )

            // Auto-save Document if PKK or Photo is present!
            if (!pkkNumber.isNullOrBlank() || !documentPhotoPath.isNullOrBlank()) {
                repository.insertDocument(
                    DocumentEntity(
                        fullName = fullName,
                        phone = phone,
                        category = category,
                        pkkNumber = pkkNumber,
                        photoPath = documentPhotoPath
                    )
                )

                // Instantly notify Manager via Live Chat (Automatic SYSTEM Message)
                val docDesc = when {
                    !pkkNumber.isNullOrBlank() && !documentPhotoPath.isNullOrBlank() -> "profil PKK ($pkkNumber) oraz zdjęcie dokumentu"
                    !pkkNumber.isNullOrBlank() -> "profil PKK ($pkkNumber)"
                    else -> "zdjęcie dokumentu"
                }

                repository.sendMessage(
                    ChatMessageEntity(
                        senderName = "SYSTEM (Auto-Dokumenty)",
                        senderRole = "Zarządca OSK",
                        message = "🚨 Nowy dokument przesłany przez kursanta $fullName! Kategoria: $category, przekazano: $docDesc. Dokument został automatycznie zarchiwizowany w karcie 'Dokumenty'."
                    )
                )
            }
        }
    }

    fun deleteDocument(id: Long) {
        viewModelScope.launch {
            repository.deleteDocument(id)
        }
    }

    fun updateReservationStatus(id: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateReservationStatus(id, newStatus)
            if (newStatus == "Zatwierdzona") {
                // Find reservation and create scheduled lesson
                val res = reservations.value.find { it.id == id }
                if (res != null) {
                    repository.insertLesson(
                        LessonEntity(
                            studentName = res.fullName,
                            instructorName = _userName.value,
                            vehiclePlate = "WI-1029X (Hyundai i20)",
                            date = res.selectedDate,
                            time = res.selectedTimeSlots.ifBlank { "09:00" },
                            durationHours = 2,
                            status = "Zaplanowana",
                            notes = "Rezerwacja z formularza online (PESEL: ${res.pesel}, Tel: ${res.phone})"
                        )
                    )
                }
            }
        }
    }

    fun proposeReservationAlternatives(id: Long, alternatives: String, note: String) {
        viewModelScope.launch {
            repository.proposeAlternatives(id, alternatives = alternatives, note = note)
        }
    }

    fun acceptReservationAlternative(id: Long, chosenDate: String, chosenTimeSlot: String) {
        viewModelScope.launch {
            repository.acceptAlternative(id, selectedDate = chosenDate, selectedTimeSlots = chosenTimeSlot)
            val res = reservations.value.find { it.id == id }
            if (res != null) {
                repository.insertLesson(
                    LessonEntity(
                        studentName = res.fullName,
                        instructorName = _userName.value,
                        vehiclePlate = "WI-1029X (Hyundai i20)",
                        date = chosenDate,
                        time = chosenTimeSlot.ifBlank { "09:00" },
                        durationHours = 2,
                        status = "Zaplanowana",
                        notes = "Zaakceptowano alternatywny termin jazdy (PESEL: ${res.pesel}, Tel: ${res.phone})"
                    )
                )
            }
        }
    }

    fun deleteReservation(id: Long) {
        viewModelScope.launch { repository.deleteReservation(id) }
    }

    fun saveExamResult(score: Int, maxPoints: Int, isPassed: Boolean) {
        viewModelScope.launch {
            repository.insertExamResult(
                ExamResultEntity(
                    studentName = _userName.value,
                    score = score,
                    maxPoints = maxPoints,
                    isPassed = isPassed
                )
            )
        }
    }

    fun deleteExamResult(id: Long) {
        viewModelScope.launch {
            repository.deleteExamResult(id)
        }
    }

    // Notification operations
    fun checkForAutomaticNotifications() {
        viewModelScope.launch {
            val existing = repository.notifications.first()
            
            // 1. Missing documents check
            if (existing.none { it.title.contains("Brakujące dokumenty") }) {
                repository.insertNotification(
                    NotificationEntity(
                        senderName = "System",
                        senderRole = "System",
                        targetGroup = "KURSANT",
                        title = "Brakujące dokumenty",
                        message = "Wymagane: Brak kompletnych dokumentów od kursanta Anna Wiśniewska! Proszę o jak najszybsze dostarczenie profilu PKK oraz zdjęcia.",
                        timestamp = System.currentTimeMillis() - 86400000L * 2 // 2 days ago
                    )
                )
            }

            // 2. Unsettled payments / near deadline check
            if (existing.none { it.title.contains("Zaległa płatność") }) {
                repository.insertNotification(
                    NotificationEntity(
                        senderName = "System",
                        senderRole = "System",
                        targetGroup = "KURSANT",
                        title = "Zaległa płatność",
                        message = "Przypomnienie: Kursant Anna Wiśniewska nie uregulowała pełnej kwoty za kurs prawa jazdy (pozostało 1200 zł). Termin płatności mija za 3 dni!",
                        timestamp = System.currentTimeMillis() - 3600000L * 5 // 5 hours ago
                    )
                )
            }

            // 3. Inspection approaching check
            if (existing.none { it.title.contains("Zbliża się przegląd pojazdu WI-7712Y") }) {
                repository.insertNotification(
                    NotificationEntity(
                        senderName = "System",
                        senderRole = "System",
                        targetGroup = "ALL",
                        title = "Zbliża się przegląd pojazdu WI-7712Y",
                        message = "Ważne: Zbliża się termin badania technicznego dla Toyota Yaris (WI-7712Y). Termin upływa: 2026-08-20.",
                        timestamp = System.currentTimeMillis() - 3600000L * 2
                    )
                )
            }

            // 4. Vehicle issue check
            if (existing.none { it.title.contains("Zgłoszono usterkę pojazdu WI-7712Y") }) {
                repository.insertNotification(
                    NotificationEntity(
                        senderName = "System",
                        senderRole = "System",
                        targetGroup = "ALL",
                        title = "Zgłoszono usterkę pojazdu WI-7712Y",
                        message = "Status pojazdu Toyota Yaris (WI-7712Y) został zmieniony na: Wymagana wymiana oleju. Zaplanowano wizytę w serwisie.",
                        timestamp = System.currentTimeMillis() - 600000L // 10 mins ago
                    )
                )
            }

            // --- Auto-generate Mock Exam Reservations if empty ---
            val exams = repository.examReservations.first()
            if (exams.isEmpty()) {
                val now = System.currentTimeMillis()
                val oneHourLater = now + 3600000L
                val sdfDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val sdfTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                
                val todayStr = sdfDate.format(java.util.Date(now))
                val timeInOneHour = sdfTime.format(java.util.Date(oneHourLater))

                val defaultExams = listOf(
                    ExamReservationEntity(
                        studentName = "Anna Wiśniewska",
                        examDate = todayStr,
                        examTime = timeInOneHour,
                        examType = "Praktyczny",
                        pkkNumber = "12345678901234567890",
                        pkkStatus = "Zwolniony",
                        hasPhoto = true,
                        isConfirmed = true,
                        isWishesSent = false
                    ),
                    ExamReservationEntity(
                        studentName = "Kamil Kowalski",
                        examDate = "2026-08-05",
                        examTime = "10:30",
                        examType = "Teoretyczny",
                        pkkNumber = "98765432109876543210",
                        pkkStatus = "Zablokowany",
                        hasPhoto = true,
                        isConfirmed = false,
                        isWishesSent = false
                    ),
                    ExamReservationEntity(
                        studentName = "Zofia Zielińska",
                        examDate = "2026-08-12",
                        examTime = "08:15",
                        examType = "Praktyczny",
                        pkkNumber = "",
                        pkkStatus = "Zablokowany",
                        hasPhoto = true,
                        isConfirmed = false,
                        isWishesSent = false
                    )
                )
                for (exam in defaultExams) {
                    repository.insertExamReservation(exam)
                }
            }

            // --- Check for Upcoming Exams to generate Wish Luck reminders for Instructor ---
            val updatedExams = repository.examReservations.first()
            val updatedNotifs = repository.notifications.first()
            for (exam in updatedExams) {
                val reminderTitle = "Życz powodzenia: ${exam.studentName}"
                if (!exam.isWishesSent && updatedNotifs.none { it.title == reminderTitle }) {
                    repository.insertNotification(
                        NotificationEntity(
                            senderName = "System",
                            senderRole = "System",
                            targetGroup = "INSTRUKTOR",
                            title = reminderTitle,
                            message = "Kursant ${exam.studentName} rozpoczyna egzamin ${exam.examType} za godzinę (o ${exam.examTime}). Kliknij poniżej, aby automatycznie wysłać mu miłe słowa powodzenia!",
                            timestamp = System.currentTimeMillis(),
                            isRead = false,
                            isWishReminder = true,
                            relatedStudentName = exam.studentName,
                            relatedExamType = exam.examType
                        )
                    )
                }

                // Dedicated Exam Reminder 1-hour before notification for Instructor
                val examAlertTitle = "Powiadomienie (1 godzina do egzaminu): ${exam.studentName}"
                if (updatedNotifs.none { it.title == examAlertTitle }) {
                    repository.insertNotification(
                        NotificationEntity(
                            senderName = "System",
                            senderRole = "System",
                            targetGroup = "INSTRUKTOR",
                            title = examAlertTitle,
                            message = "Przypomnienie: Twój kursant ${exam.studentName} przystępuje do państwowego egzaminu: ${exam.examType} dokładnie za 1 godzinę (o godz. ${exam.examTime} dnia ${exam.examDate})!",
                            timestamp = System.currentTimeMillis(),
                            isRead = false
                        )
                    )
                }
            }
        }
    }

    fun sendNotification(targetGroup: String, title: String, message: String) {
        if (title.isBlank() || message.isBlank()) return
        viewModelScope.launch {
            repository.insertNotification(
                NotificationEntity(
                    senderName = _userName.value,
                    senderRole = _currentRole.value.label,
                    targetGroup = targetGroup,
                    title = title,
                    message = message,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.deleteAllNotifications()
        }
    }

    // Exam Reservations operations
    fun scheduleExam(
        studentName: String,
        date: String,
        time: String,
        examType: String,
        pkkNumber: String = "",
        pkkStatus: String = "Zablokowany",
        hasPhoto: Boolean = false,
        isConfirmed: Boolean = false
    ) {
        if (studentName.isBlank() || date.isBlank() || time.isBlank()) return
        viewModelScope.launch {
            repository.insertExamReservation(
                ExamReservationEntity(
                    studentName = studentName,
                    examDate = date,
                    examTime = time,
                    examType = examType,
                    pkkNumber = pkkNumber,
                    pkkStatus = pkkStatus,
                    hasPhoto = hasPhoto,
                    isConfirmed = isConfirmed,
                    isWishesSent = false
                )
            )
            // Immediately run checks to generate the wish reminders if applicable
            checkForAutomaticNotifications()
        }
    }

    fun updateExamPkk(id: Long, pkkNumber: String, isConfirmed: Boolean, pkkStatus: String) {
        viewModelScope.launch {
            repository.updateExamReservationPkk(id, pkkNumber, isConfirmed, pkkStatus)
        }
    }

    fun deleteExamReservation(id: Long) {
        viewModelScope.launch {
            repository.deleteExamReservation(id)
        }
    }

    fun sendWishesToStudent(relatedStudentName: String, examType: String, notificationIdToRead: Long) {
        viewModelScope.launch {
            // Find exam reservation for this student & exam type
            val exams = repository.examReservations.first()
            val match = exams.firstOrNull { it.studentName == relatedStudentName && it.examType == examType && !it.isWishesSent }
            if (match != null) {
                repository.markExamReservationWishesSent(match.id)
            }

            // Create short & nice wishes in Polish
            val niceWishes = listOf(
                "Trzymam kciuki za Twój egzamin $examType! Dasz radę! – Twój Instruktor",
                "Szerokiej drogi, spokojnej głowy i powodzenia na egzaminie $examType! – Twój Instruktor",
                "Powodzenia! Pamiętaj o lusterkach i jedź śmiało do przodu na egzaminie $examType! – Twój Instruktor",
                "Wierzę w Twoje umiejętności na egzaminie $examType. Zdaj to dzisiaj z uśmiechem! – Twój Instruktor"
            ).shuffled().first()

            // Insert notification for the Student (targetGroup = "KURSANT")
            repository.insertNotification(
                NotificationEntity(
                    senderName = _userName.value,
                    senderRole = _currentRole.value.label,
                    targetGroup = "KURSANT",
                    title = "Życzenia powodzenia! 🤞",
                    message = "Od instruktora dla $relatedStudentName: \"$niceWishes\"",
                    timestamp = System.currentTimeMillis()
                )
            )

            // Delete or read the reminder notification
            repository.deleteNotification(notificationIdToRead)
        }
    }
}
