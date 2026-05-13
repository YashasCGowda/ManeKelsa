package com.manekelsa.app.presentation.screens.profile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

sealed class VoiceIntent {
    object RequestLeave : VoiceIntent()
    object CheckWallet : VoiceIntent()
    object CheckPayroll : VoiceIntent()
    data class Unknown(val text: String) : VoiceIntent()
}

@HiltViewModel
class VoiceAssistantViewModel @Inject constructor() : ViewModel() {
    private val _intents = MutableSharedFlow<VoiceIntent>()
    val intents = _intents.asSharedFlow()

    suspend fun processSpeech(text: String) {
        val command = text.lowercase()
        when {
            command.contains("leave") || command.contains("absent") -> {
                _intents.emit(VoiceIntent.RequestLeave)
            }
            command.contains("wallet") || command.contains("money") || command.contains("balance") -> {
                _intents.emit(VoiceIntent.CheckWallet)
            }
            command.contains("salary") || command.contains("payroll") || command.contains("pay") -> {
                _intents.emit(VoiceIntent.CheckPayroll)
            }
            else -> {
                _intents.emit(VoiceIntent.Unknown(text))
            }
        }
    }
}
