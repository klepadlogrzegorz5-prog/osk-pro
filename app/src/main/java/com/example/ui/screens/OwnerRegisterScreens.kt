package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.OskViewModel
import com.example.ui.RegisterOskUiState

@Composable
fun OwnerRegisterStep1Screen(
    viewModel: OskViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var companyName by remember { mutableStateOf(viewModel.tempCompanyName) }
    var nip by remember { mutableStateOf(viewModel.tempNip) }
    var phoneNumber by remember { mutableStateOf(viewModel.tempPhoneNumber) }
    var email by remember { mutableStateOf(viewModel.tempEmail) }
    var city by remember { mutableStateOf(viewModel.tempCity) }
    var street by remember { mutableStateOf(viewModel.tempStreet) }
    var ownerName by remember { mutableStateOf(viewModel.tempOwnerName) }

    val registerState by viewModel.registerState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(registerState) {
        if (registerState is RegisterOskUiState.Step1Completed) {
            onNext()
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
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Powrót", tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Rejestracja Szkoły OSK",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Krok 1 z 2: Informacje o OSK",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
                LinearProgressIndicator(
                    progress = { 0.5f },
                    modifier = Modifier
                        .width(100.dp)
                        .height(8.dp),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Zarejestruj swój ośrodek",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Wprowadź dane identyfikacyjne oraz adresowe swojej szkoły nauki jazdy.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("Pełna nazwa firmy (OSK)") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Business, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_company_name")
                    )

                    OutlinedTextField(
                        value = nip,
                        onValueChange = { nip = it },
                        label = { Text("NIP firmy (10 cyfr)") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Assignment, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_nip")
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Numer telefonu") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_phone")
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Adres e-mail szkoły") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_email")
                    )

                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("Miasto") },
                        leadingIcon = { Icon(imageVector = Icons.Default.LocationOn, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_city")
                    )

                    OutlinedTextField(
                        value = street,
                        onValueChange = { street = it },
                        label = { Text("Ulica i numer") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Home, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_street")
                    )

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Imię i nazwisko właściciela") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_owner_name")
                    )

                    if (registerState is RegisterOskUiState.Error) {
                        Text(
                            text = (registerState as RegisterOskUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.completeStep1(companyName, nip, phoneNumber, email, city, street, ownerName)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("reg_step1_submit")
                    ) {
                        Text(
                            text = "Zarejestruj OSK",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerRegisterStep2Screen(
    viewModel: OskViewModel,
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var login by remember { mutableStateOf("") }
    var haslo by remember { mutableStateOf("") }
    var powtorzHaslo by remember { mutableStateOf("") }

    var selectedQuestion by remember { mutableStateOf("Jakie było Twoje pierwsze zwierzę?") }
    var securityAnswer by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val registerState by viewModel.registerState.collectAsState()
    val scrollState = rememberScrollState()

    val securityQuestions = listOf(
        "Jakie było Twoje pierwsze zwierzę?",
        "Jak nazywała się Twoja pierwsza szkoła?",
        "Jakie jest Twoje ulubione miasto?",
        "Jaka była marka Twojego pierwszego samochodu?"
    )

    LaunchedEffect(registerState) {
        if (registerState is RegisterOskUiState.Success) {
            onRegisterSuccess()
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
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Powrót", tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Konfiguracja Zabezpieczeń",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Krok 2 z 2: Dane konta",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier
                        .width(100.dp)
                        .height(8.dp),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Utwórz konto zarządcy",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Zabezpiecz system przed niepowołanym dostępem oraz ustaw pytanie pomocnicze do odzyskiwania hasła.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                    OutlinedTextField(
                        value = login,
                        onValueChange = { login = it },
                        label = { Text("Unikalny Login") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_login")
                    )

                    OutlinedTextField(
                        value = haslo,
                        onValueChange = { haslo = it },
                        label = { Text("Hasło") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
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
                            .testTag("reg_password")
                    )

                    OutlinedTextField(
                        value = powtorzHaslo,
                        onValueChange = { powtorzHaslo = it },
                        label = { Text("Powtórz Hasło") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            val image = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(imageVector = image, contentDescription = null)
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_password_repeat")
                    )

                    // Dropdown for Security Question
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedQuestion,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pytanie pomocnicze") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            securityQuestions.forEach { question ->
                                DropdownMenuItem(
                                    text = { Text(question) },
                                    onClick = {
                                        selectedQuestion = question
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = securityAnswer,
                        onValueChange = { securityAnswer = it },
                        label = { Text("Odpowiedź na pytanie") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Help, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_security_answer")
                    )

                    if (registerState is RegisterOskUiState.Error) {
                        Text(
                            text = (registerState as RegisterOskUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.completeStep2(
                                login = login,
                                haslo = haslo,
                                powtorzHaslo = powtorzHaslo,
                                pytanie = selectedQuestion,
                                odpowiedz = securityAnswer
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("reg_step2_submit")
                    ) {
                        Text(
                            text = "Dokończ rejestrację i utwórz OSK",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
