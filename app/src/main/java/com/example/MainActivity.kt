package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.OskViewModel
import com.example.ui.RegisterOskUiState
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DrivingSchoolApp()
                }
            }
        }
    }
}

@Composable
fun DrivingSchoolApp() {
    val viewModel: OskViewModel = viewModel()
    val activeSession by viewModel.activeSession.collectAsState()
    val oskProfile by viewModel.oskProfile.collectAsState()

    // Internal navigation state
    // Screens: "WELCOME", "OWNER_LOGIN", "OWNER_REG_STEP_1", "OWNER_REG_STEP_2", "CODE_LOGIN_INSTRUKTOR", "CODE_LOGIN_KURSANT", "DASHBOARD"
    var currentScreen by remember { mutableStateOf("WELCOME") }

    // Sync screen with session if it changes
    LaunchedEffect(activeSession) {
        if (activeSession != null) {
            currentScreen = "DASHBOARD"
        } else {
            currentScreen = "WELCOME"
        }
    }

    Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
        when (screen) {
            "WELCOME" -> {
                WelcomeRoleSelectionScreen(
                    onBypassLogin = { role, name ->
                        viewModel.bypassLogin(role, name)
                    },
                    onRoleSelected = { role ->
                        when (role) {
                            "OWNER" -> {
                                if (oskProfile == null) {
                                    viewModel.resetRegisterState()
                                    currentScreen = "OWNER_REG_STEP_1"
                                } else {
                                    viewModel.resetLoginState()
                                    currentScreen = "OWNER_LOGIN"
                                }
                            }
                            "INSTRUKTOR" -> {
                                viewModel.resetLoginState()
                                currentScreen = "CODE_LOGIN_INSTRUKTOR"
                            }
                            "KURSANT" -> {
                                viewModel.resetLoginState()
                                currentScreen = "CODE_LOGIN_KURSANT"
                            }
                        }
                    }
                )
            }
            "OWNER_LOGIN" -> {
                OwnerLoginScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "WELCOME" },
                    onRegisterClick = {
                        viewModel.resetRegisterState()
                        currentScreen = "OWNER_REG_STEP_1"
                    },
                    onLoginSuccess = {
                        currentScreen = "DASHBOARD"
                    }
                )
            }
            "OWNER_REG_STEP_1" -> {
                OwnerRegisterStep1Screen(
                    viewModel = viewModel,
                    onBack = {
                        if (oskProfile == null) {
                            currentScreen = "WELCOME"
                        } else {
                            currentScreen = "OWNER_LOGIN"
                        }
                    },
                    onNext = {
                        currentScreen = "OWNER_REG_STEP_2"
                    }
                )
            }
            "OWNER_REG_STEP_2" -> {
                OwnerRegisterStep2Screen(
                    viewModel = viewModel,
                    onBack = {
                        currentScreen = "OWNER_REG_STEP_1"
                    },
                    onRegisterSuccess = {
                        currentScreen = "DASHBOARD"
                    }
                )
            }
            "CODE_LOGIN_INSTRUKTOR" -> {
                AccessCodeLoginScreen(
                    viewModel = viewModel,
                    role = "INSTRUKTOR",
                    onBack = { currentScreen = "WELCOME" },
                    onLoginSuccess = { currentScreen = "DASHBOARD" }
                )
            }
            "CODE_LOGIN_KURSANT" -> {
                AccessCodeLoginScreen(
                    viewModel = viewModel,
                    role = "KURSANT",
                    onBack = { currentScreen = "WELCOME" },
                    onLoginSuccess = { currentScreen = "DASHBOARD" }
                )
            }
            "DASHBOARD" -> {
                val session = activeSession
                if (session != null) {
                    when (session.role) {
                        "OWNER" -> {
                            val profile = oskProfile
                            if (profile != null) {
                                OwnerDashboard(
                                    viewModel = viewModel,
                                    profile = profile,
                                    onLogout = { viewModel.logout() }
                                )
                            } else {
                                // Fallback
                                TextButton(onClick = { viewModel.logout() }) {
                                    Text("Błąd profilu. Kliknij, aby wylogować.")
                                }
                            }
                        }
                        "INSTRUKTOR" -> {
                            InstructorDashboard(
                                viewModel = viewModel,
                                session = session,
                                profile = oskProfile,
                                onLogout = { viewModel.logout() }
                            )
                        }
                        "KURSANT" -> {
                            val registrations by viewModel.studentRegistrations.collectAsState()
                            val isRegistered = session.codeUsed == "BYPASS" || registrations.any { it.accessCode == session.codeUsed }
                            if (isRegistered) {
                                StudentDashboard(
                                    viewModel = viewModel,
                                    session = session,
                                    profile = oskProfile,
                                    onLogout = { viewModel.logout() }
                                )
                            } else {
                                StudentRegistrationFormScreen(
                                    viewModel = viewModel,
                                    session = session,
                                    onBack = { viewModel.logout() }
                                )
                            }
                        }
                    }
                } else {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
