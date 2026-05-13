package com.manekelsa.app.presentation.screens.jobs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.manekelsa.app.domain.model.JobRequest
import com.manekelsa.app.domain.model.JobStatus
import com.manekelsa.app.domain.repository.WorkerRepository
import com.manekelsa.app.presentation.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobsViewModel @Inject constructor(private val repo: WorkerRepository) : ViewModel() {
    private val _jobs = MutableStateFlow<List<JobRequest>>(emptyList())
    val jobs: StateFlow<List<JobRequest>> = _jobs.asStateFlow()
    private val _snack = MutableStateFlow<String?>(null)
    val snack: StateFlow<String?> = _snack.asStateFlow()

    init { viewModelScope.launch { repo.getJobs().collect { _jobs.value = it } } }

    fun accept(id: String) = viewModelScope.launch {
        repo.updateJobStatus(id, JobStatus.ACCEPTED)
        _snack.value = "Job Accepted ✅"
    }
    fun reject(id: String) = viewModelScope.launch {
        repo.updateJobStatus(id, JobStatus.REJECTED)
        _snack.value = "Job Declined"
    }
    fun complete(id: String) = viewModelScope.launch {
        repo.updateJobStatus(id, JobStatus.COMPLETED)
        _snack.value = "Job Marked Complete 🎉"
    }
    fun clearSnack() = _snack.update { null }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobsScreen(onBack: () -> Unit, viewModel: JobsViewModel = hiltViewModel()) {
    val jobs by viewModel.jobs.collectAsStateWithLifecycle()
    val snack by viewModel.snack.collectAsStateWithLifecycle()
    val snackHost = remember { SnackbarHostState() }
    var tab by remember { mutableIntStateOf(0) }

    LaunchedEffect(snack) { snack?.let { snackHost.showSnackbar(it); viewModel.clearSnack() } }

    val completed = jobs.filter { it.status == JobStatus.COMPLETED }
    val remaining = jobs.filter { it.status == JobStatus.PENDING || it.status == JobStatus.ACCEPTED }
    val notDoing = jobs.filter { it.status == JobStatus.REJECTED || it.status == JobStatus.CANCELLED }

    val filtered = when (tab) {
        0 -> remaining
        1 -> completed
        2 -> notDoing
        else -> jobs
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackHost) },
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                title = { Text("Job List", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Summary Row
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryCard("Remaining", remaining.size, AppColors.Saffron, Modifier.weight(1f))
                SummaryCard("Completed", completed.size, AppColors.Teal, Modifier.weight(1f))
                SummaryCard("Not Doing", notDoing.size, AppColors.Unavailable, Modifier.weight(1f))
            }

            TabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.background, contentColor = AppColors.Saffron) {
                listOf("Remaining", "Completed", "Not Doing").forEachIndexed { i, label ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(label) })
                }
            }
            if (filtered.isEmpty()) {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Outlined.WorkOff, null, modifier = Modifier.size(60.dp), tint = AppColors.MidGray)
                    Spacer(Modifier.height(8.dp))
                    Text("No jobs in this category", fontWeight = FontWeight.Bold)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filtered, key = { it.id }) { job ->
                        JobCard(job = job,
                            onAccept = { viewModel.accept(job.id) },
                            onReject = { viewModel.reject(job.id) },
                            onComplete = { viewModel.complete(job.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(0.1f))) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(count.toString(), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = AppColors.MidGray)
        }
    }
}

@Composable
private fun JobCard(job: JobRequest, onAccept: () -> Unit, onReject: () -> Unit, onComplete: () -> Unit) {
    val statusColor = when (job.status) {
        JobStatus.PENDING -> Color(0xFFF57F17)
        JobStatus.ACCEPTED -> AppColors.Available
        JobStatus.COMPLETED -> AppColors.Teal
        JobStatus.REJECTED, JobStatus.CANCELLED -> AppColors.Unavailable
    }
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(job.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(50), color = statusColor.copy(0.12f)) {
                    Text(job.status.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }
            if (job.description.isNotBlank()) Text(job.description, style = MaterialTheme.typography.bodySmall, color = AppColors.MidGray, maxLines = 2)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, null, tint = AppColors.MidGray, modifier = Modifier.size(14.dp))
                Text(job.location, style = MaterialTheme.typography.bodySmall, color = AppColors.MidGray)
            }
            HorizontalDivider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("₹${job.payPerDay.toInt()}/day", fontWeight = FontWeight.Bold, color = AppColors.Saffron)
                when (job.status) {
                    JobStatus.PENDING -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onReject, modifier = Modifier.height(34.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Unavailable)) { Text("Decline") }
                        Button(onClick = onAccept, modifier = Modifier.height(34.dp), colors = ButtonDefaults.buttonColors(containerColor = AppColors.Available)) { Text("Accept") }
                    }
                    JobStatus.ACCEPTED -> Button(onClick = onComplete, modifier = Modifier.height(34.dp), colors = ButtonDefaults.buttonColors(containerColor = AppColors.Teal)) {
                        Icon(Icons.Filled.Check, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Done")
                    }
                    else -> {}
                }
            }
        }
    }
}
