sed -i 's/OutlinedTextField(/Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {\n                    OutlinedTextField(/g' app/src/main/java/com/example/ui/screens/OptionsScreen.kt
sed -i 's/modifier = Modifier.fillMaxWidth()/modifier = Modifier.weight(1f)/g' app/src/main/java/com/example/ui/screens/OptionsScreen.kt
