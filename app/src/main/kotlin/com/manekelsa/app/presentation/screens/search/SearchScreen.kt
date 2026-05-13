package com.manekelsa.app.presentation.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.manekelsa.app.domain.model.Worker
import com.manekelsa.app.domain.repository.WorkerRepository
import com.manekelsa.app.presentation.components.WorkerCard
import com.manekelsa.app.presentation.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchState(
    val query: String = "",
    val results: List<Worker> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(private val repo: WorkerRepository) : ViewModel() {
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()
    private var searchJob: Job? = null

    fun onQuery(q: String) {
        _state.update { it.copy(query = q) }
        searchJob?.cancel()
        if (q.isBlank()) {
            _state.update { it.copy(results = emptyList()) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            _state.update { it.copy(isLoading = true) }
            repo.searchWorkers(q).collect { results ->
                _state.update { it.copy(results = results, isLoading = false) }
            }
        }
    }

    fun clear() = _state.update { it.copy(query = "", results = emptyList()) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onWorkerClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focus = remember { FocusRequester() }

    LaunchedEffect(Unit) { focus.requestFocus() }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                },
                title = {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::onQuery,
                        placeholder = { Text("Search workers, skills…") },
                        modifier = Modifier.fillMaxWidth().focusRequester(focus),
                        shape = RoundedCornerShape(50),
                        singleLine = true,
                        trailingIcon = {
                            if (state.query.isNotBlank()) {
                                IconButton(onClick = viewModel::clear) {
                                    Icon(Icons.Filled.Clear, null)
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                    )
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when {
                state.isLoading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Saffron)
                }

                state.query.isBlank() -> Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        "Popular Searches",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(12.dp))

                    listOf("Plumber", "Electrician", "Carpenter", "Cleaner", "Painter", "Cook").forEach { skill ->
                        ListItem(
                            headlineContent = { Text(skill) },
                            leadingContent = {
                                Icon(Icons.Outlined.Search, null, tint = AppColors.MidGray)
                            },
                            modifier = Modifier.clickable(
                                onClick = {
                                    viewModel.onQuery(skill)  // This will trigger search
                                }
                            )
                        )
                    }
                }

                state.results.isEmpty() -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Outlined.SearchOff,
                        null,
                        modifier = Modifier.size(60.dp),
                        tint = AppColors.MidGray
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No workers found for \"${state.query}\"",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Try different keywords",
                        color = AppColors.MidGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                else -> {
                    Text(
                        "${state.results.size} results",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.MidGray,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.results, key = { it.id }) { w ->
                            WorkerCard(worker = w, onClick = { onWorkerClick(w.id) })
                        }
                    }
                }
            }
        }
    }
}