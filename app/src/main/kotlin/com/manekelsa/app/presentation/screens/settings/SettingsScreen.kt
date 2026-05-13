package com.manekelsa.app.presentation.screens.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.manekelsa.app.domain.repository.WorkerRepository
import com.manekelsa.app.presentation.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val email: String = "",
    val darkMode: Boolean = false,
    val notifications: Boolean = true,
    val loggedOut: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repo: WorkerRepository) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init { _state.update { it.copy(email = repo.getCurrentUserEmail()) } }

    fun toggleDarkMode() = _state.update { it.copy(darkMode = !it.darkMode) }
    fun toggleNotifications() = _state.update { it.copy(notifications = !it.notifications) }
    fun logout() = viewModelScope.launch {
        repo.signOut()
        _state.update { it.copy(loggedOut = true) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    onAdminClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showLogout by remember { mutableStateOf(false) }

    LaunchedEffect(state.loggedOut) { if (state.loggedOut) onLoggedOut() }

    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            title = { Text("Logout", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = { showLogout = false; viewModel.logout() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Logout") }
            },
            dismissButton = { TextButton(onClick = { showLogout = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                title = { Text("Settings", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // Profile card
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = AppColors.Saffron.copy(0.08f))) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(AppColors.Saffron), contentAlignment = Alignment.Center) {
                        Text(if (state.email.isNotBlank()) state.email.first().uppercase() else "U",
                            style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Logged In", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text(state.email.ifBlank { "Guest User" }, style = MaterialTheme.typography.bodySmall, color = AppColors.MidGray)
                    }
                }
            }

            // Preferences
            SettingGroup("Preferences") {
                SettingToggle(Icons.Outlined.DarkMode, "Dark Mode", state.darkMode, viewModel::toggleDarkMode)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingToggle(Icons.Outlined.Notifications, "Notifications", state.notifications, viewModel::toggleNotifications)
            }

            // Administrative (Visible for all in demo, usually gated by role)
            SettingGroup("Administrative") {
                ListItem(
                    headlineContent = { Text("Admin Dashboard", fontWeight = FontWeight.Medium) },
                    supportingContent = { Text("Manage areas, attendance & payroll", style = MaterialTheme.typography.labelSmall) },
                    leadingContent = { Icon(Icons.Outlined.AdminPanelSettings, null, tint = AppColors.Saffron) },
                    trailingContent = { Icon(Icons.Filled.ChevronRight, null, tint = AppColors.MidGray) },
                    modifier = Modifier.clickable(onClick = onAdminClick)
                )
            }

            // App info
            SettingGroup("About") {
                SettingItem(Icons.Outlined.Info, "Version", "1.0.0")
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingItem(Icons.Outlined.LocationOn, "Region", "Bengaluru, Karnataka")
            }

            // Logout
            Button(
                onClick = { showLogout = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Outlined.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Logout", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, style = MaterialTheme.typography.labelMedium, color = AppColors.MidGray, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 4.dp))
    Card(shape = RoundedCornerShape(16.dp)) { Column { content() } }
}

@Composable
private fun SettingToggle(icon: ImageVector, title: String, checked: Boolean, onToggle: () -> Unit) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        leadingContent = { Icon(icon, null, tint = AppColors.Saffron) },
        trailingContent = { Switch(checked = checked, onCheckedChange = { onToggle() }, colors = SwitchDefaults.colors(checkedThumbColor = AppColors.Saffron, checkedTrackColor = AppColors.Saffron.copy(0.3f))) }
    )
}

@Composable
private fun SettingItem(icon: ImageVector, title: String, value: String) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        leadingContent = { Icon(icon, null, tint = AppColors.Saffron) },
        trailingContent = { Text(value, style = MaterialTheme.typography.bodySmall, color = AppColors.MidGray) }
    )
}
