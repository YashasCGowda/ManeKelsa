package com.manekelsa.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manekelsa.app.domain.model.Worker
import com.manekelsa.app.domain.repository.WorkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val workers: List<Worker> = emptyList(),
    val isLoading: Boolean = false,
    val selectedCategory: String = "All"
)

sealed class HomeEffect {
    object NavigateToJobs : HomeEffect()
    object NavigateToSettings : HomeEffect()
    data class ShowToast(val message: String) : HomeEffect()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: WorkerRepository
) : ViewModel() {

    private val _state = MutableStateFlow<HomeState>(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<HomeEffect>()
    val effects: SharedFlow<HomeEffect> = _effects.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val recommendedWorkers: StateFlow<List<Worker>> = _state
        .map { homeState: HomeState -> homeState.selectedCategory }
        .distinctUntilChanged()
        .flatMapLatest { category: String ->
            repo.getRecommendedWorkers(category)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repo.getAllWorkers().collect { workersList ->
                _state.update { it.copy(workers = workersList, isLoading = false) }
            }
        }
    }

    fun selectCategory(cat: String) {
        _state.update { it.copy(selectedCategory = cat) }
    }

    fun filteredWorkers(): List<Worker> {
        val s = _state.value
        val filtered = if (s.selectedCategory == "All") s.workers
        else s.workers.filter { w ->
            w.skills.any { skill -> skill.contains(s.selectedCategory, ignoreCase = true) }
        }
        return filtered.filter { worker -> !worker.isBlocked }
    }

    fun handleVoiceCommand(text: String) = viewModelScope.launch {
        val cmd = text.lowercase()
        when {
            cmd.contains("job") || cmd.contains("work") -> _effects.emit(HomeEffect.NavigateToJobs)
            cmd.contains("setting") || cmd.contains("admin") -> _effects.emit(HomeEffect.NavigateToSettings)
            else -> _effects.emit(HomeEffect.ShowToast("Searching for: $text"))
        }
    }
}
