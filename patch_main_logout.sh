sed -i 's/onLogout = { showRoleSelection = true }/onLogout = { viewModel.disablePinLogin(); showRoleSelection = true }/g' app/src/main/java/com/example/MainActivity.kt
