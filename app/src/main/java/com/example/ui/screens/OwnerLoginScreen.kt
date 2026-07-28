package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.OskProfile
import com.example.ui.LoginUiState
import com.example.ui.OskViewModel

@Composable
fun OwnerLoginScreen(
    viewModel: OskViewModel,
    onBack: () -> Unit,
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val oskProfile by viewModel.oskProfile.collectAsState()
    val loginState by viewModel.loginState.collectAsState()
    val recoveryMessage by viewModel.recoveryMessage.collectAsState()

    var login by remember { mutableStateOf("") }
    var haslo by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var isRecoverMode by remember { mutableStateOf(false) }
    var recoveryAnswer by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    LaunchedEffect(loginState) {
        if (loginState is LoginUiState.Success) {
            viewModel.resetLoginState()
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState)
        ) {
            // Top Navigation Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("owner_login_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Powrót",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Logowanie Właściciela",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isRecoverMode) {
                // Main Login Section
                Text(
                    text = "Witaj ponownie!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Zaloguj się do swojego konta zarządcy szkoły OSK",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = login,
                            onValueChange = { login = it },
                            label = { Text("Login") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("owner_login_field")
                        )

                        OutlinedTextField(
                            value = haslo,
                            onValueChange = { haslo = it },
                            label = { Text("Hasło") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                            },
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, contentDescription = null)
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("owner_password_field")
                        )

                        if (loginState is LoginUiState.Error) {
                            Text(
                                text = (loginState as LoginUiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

                        Button(
                            onClick = { viewModel.loginOwner(login, haslo) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("owner_login_submit_button")
                        ) {
                            Text(
                                text = "Zaloguj się",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Recovery and Register Options
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextButton(
                        onClick = {
                            isRecoverMode = true
                            viewModel.clearRecoveryMessage()
                        },
                        modifier = Modifier.testTag("owner_recover_password_link")
                    ) {
                        Text(
                            text = "Zapomniałeś hasła? Odzyskaj je",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = TextDecoration.Underline,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 32.dp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Nie zarejestrowałeś jeszcze swojej szkoły?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Button(
                            onClick = onRegisterClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("owner_register_link_button")
                        ) {
                            Text(
                                text = "Rejestruj nową szkołę OSK",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            } else {
                // Recovery Section
                Text(
                    text = "Odzyskiwanie hasła",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (oskProfile == null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Brak zarejestrowanej szkoły w systemie! Zarejestruj najpierw szkołę OSK.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { isRecoverMode = false },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Powrót do logowania")
                    }
                } else {
                    val profile = oskProfile!!
                    Text(
                        text = "Odpowiedz na pytanie pomocnicze podane przy rejestracji konta.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Pytanie
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Twoje pytanie pomocnicze:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = profile.securityQuestion,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = recoveryAnswer,
                                onValueChange = { recoveryAnswer = it },
                                label = { Text("Twoja odpowiedź") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("recovery_answer_field")
                            )

                            if (recoveryMessage != null) {
                                val isSuccess = recoveryMessage!!.contains("Twoje hasło")
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSuccess) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = recoveryMessage!!,
                                        color = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.recoverPassword(recoveryAnswer) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("recovery_submit_button")
                            ) {
                                Text(
                                    text = "Zweryfikuj odpowiedź",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            TextButton(
                                onClick = {
                                    isRecoverMode = false
                                    recoveryAnswer = ""
                                    viewModel.clearRecoveryMessage()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Powrót do logowania",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // REQUIRED FOOTER
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0F172A)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "powered by ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "gK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}

