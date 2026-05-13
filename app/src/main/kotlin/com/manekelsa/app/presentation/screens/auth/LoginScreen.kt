package com.manekelsa.app.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.manekelsa.app.domain.model.UserRole
import com.manekelsa.app.domain.repository.WorkerRepository
import com.manekelsa.app.presentation.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val role: UserRole = UserRole.CLIENT,
    val isSignUp: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showPassword: Boolean = false,
    val success: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: WorkerRepository
) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun setEmail(v: String) = _state.update { it.copy(email = v, error = null) }
    fun setPassword(v: String) = _state.update { it.copy(password = v, error = null) }
    fun setName(v: String) = _state.update { it.copy(name = v, error = null) }
    fun setRole(v: UserRole) = _state.update { it.copy(role = v, error = null) }
    fun toggleMode() = _state.update { it.copy(isSignUp = !it.isSignUp, error = null) }
    fun togglePassword() = _state.update { it.copy(showPassword = !it.showPassword) }
    fun onNavigated() = _state.update { it.copy(success = false) }

    fun authenticate() {
        val s = _state.value
        if (s.email.isBlank()) { _state.update { it.copy(error = "Enter email") }; return }
        if (s.password.length < 6) { _state.update { it.copy(error = "Password min 6 characters") }; return }
        if (s.isSignUp && s.name.isBlank()) { _state.update { it.copy(error = "Enter your name") }; return }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = if (s.isSignUp)
                repository.signUp(s.email, s.password, s.name, s.role)
            else
                repository.signIn(s.email, s.password)

            result.fold(
                onSuccess = { _state.update { it.copy(isLoading = false, success = true) } },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.message ?: "Failed. Try again.") } }
            )
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, viewModel: LoginViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.success) {
        if (state.success) { viewModel.onNavigated(); onLoginSuccess() }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(AppColors.Saffron.copy(0.08f), Color.White, Color.White))
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(72.dp))

            // Logo
            Box(
                modifier = Modifier.size(90.dp).background(AppColors.Saffron, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Home, null, tint = Color.White, modifier = Modifier.size(52.dp))
            }

            Spacer(Modifier.height(16.dp))
            Text("Mane-Kelsa", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = AppColors.Charcoal)
            Text("ಮನೆ-ಕೆಲಸ", fontSize = 18.sp, color = AppColors.Saffron, fontWeight = FontWeight.Medium)
            Text("Find Skilled Workers Near You", style = MaterialTheme.typography.bodyMedium, color = AppColors.MidGray)

            Spacer(Modifier.height(36.dp))

            // Login / Signup toggle
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
                    .padding(4.dp)
            ) {
                listOf("Login" to false, "Sign Up" to true).forEach { (label, mode) ->
                    val selected = state.isSignUp == mode
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50),
                        color = if (selected) AppColors.Saffron else Color.Transparent,
                        onClick = { if (!selected) viewModel.toggleMode() }
                    ) {
                        Text(
                            label,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) Color.White else AppColors.MidGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (state.isSignUp) {
                OutlinedTextField(
                    value = state.name, onValueChange = viewModel::setName,
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Outlined.Person, null) },
                    shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Spacer(Modifier.height(12.dp))

                // Role Selection
                Text("Register as:", style = MaterialTheme.typography.labelMedium, color = AppColors.MidGray, modifier = Modifier.align(Alignment.Start).padding(start = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UserRole.entries.filter { it != UserRole.ADMIN }.forEach { role ->
                        val selected = state.role == role
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setRole(role) },
                            label = { Text(role.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppColors.Saffron.copy(0.12f),
                                selectedLabelColor = AppColors.Saffron,
                                selectedLeadingIconColor = AppColors.Saffron
                            ),
                            leadingIcon = {
                                if (selected) Icon(Icons.Filled.Check, null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            OutlinedTextField(
                value = state.email, onValueChange = viewModel::setEmail,
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Outlined.Email, null) },
                shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.password, onValueChange = viewModel::setPassword,
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = viewModel::togglePassword) {
                        Icon(if (state.showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
                    }
                },
                visualTransformation = if (state.showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            state.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = viewModel::authenticate,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Saffron)
            ) {
                if (state.isLoading) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                else Text(if (state.isSignUp) "Create Account" else "Login", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
