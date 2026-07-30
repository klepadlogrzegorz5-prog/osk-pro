with open("app/src/main/java/com/example/ui/screens/OptionsScreen.kt", "r") as f:
    content = f.read()

target = """                // Price Field
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it.filter { char -> char.isDigit() } },
                    label = { Text("Cena (PLN)", color = Color.White.copy(alpha = 0.7f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Price Type choices"""

replacement = """                // Price Fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceCourseInput,
                        onValueChange = { priceCourseInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Cena za kurs (PLN)", color = Color.White.copy(alpha = 0.7f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = priceHourInput,
                        onValueChange = { priceHourInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Cena za 1h (PLN)", color = Color.White.copy(alpha = 0.7f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Price Type choices"""

content = content.replace(target, replacement)

# We need to remove the Price Type choices block, and also modify the button to use both inputs
target_choices = """                // Price Type choices
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val courseSelected = priceType == "COURSE"
                    val hourSelected = priceType == "HOUR"
                    
                    Surface(
                        color = if (courseSelected) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF1E293B),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (courseSelected) Color(0xFF10B981) else Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { priceType = "COURSE" }
                    ) {
                        Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                            Text("Za cały kurs", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        color = if (hourSelected) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF1E293B),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (hourSelected) Color(0xFF10B981) else Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { priceType = "HOUR" }
                    ) {
                        Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                            Text("Za 1 godzinę", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }"""

content = content.replace(target_choices, "")

target_button = """                Button(
                    onClick = {
                        val parsedVal = priceInput.toDoubleOrNull() ?: 0.0
                        onSavePrice(selectedCat, parsedVal, priceType, isActive)
                    },"""

replacement_button = """                Button(
                    onClick = {
                        val parsedCourse = priceCourseInput.toDoubleOrNull() ?: 0.0
                        val parsedHour = priceHourInput.toDoubleOrNull() ?: 0.0
                        onSavePrice(selectedCat, parsedCourse, parsedHour, isActive)
                    },"""

content = content.replace(target_button, replacement_button)

with open("app/src/main/java/com/example/ui/screens/OptionsScreen.kt", "w") as f:
    f.write(content)
