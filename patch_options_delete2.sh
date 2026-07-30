sed -i 's/onClick = { viewModel.clearSimulationData() }/onClick = { showClearSimulationConfirm = true }/g' app/src/main/java/com/example/ui/screens/OptionsScreen.kt
