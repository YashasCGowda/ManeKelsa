package com.manekelsa.app.presentation.screens.admin

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.manekelsa.app.data.local.BangaloreAreas
import com.manekelsa.app.domain.model.*
import com.manekelsa.app.domain.repository.WorkerRepository
import com.manekelsa.app.presentation.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(private val repo: WorkerRepository) : ViewModel() {
    private val _workers = MutableStateFlow<List<Worker>>(emptyList())
    val workers: StateFlow<List<Worker>> = _workers.asStateFlow()

    private val _leaveRequests = MutableStateFlow<List<LeaveRequest>>(emptyList())
    val leaveRequests: StateFlow<List<LeaveRequest>> = _leaveRequests.asStateFlow()

    init {
        viewModelScope.launch { repo.getAllWorkers().collect { _workers.value = it } }
        viewModelScope.launch { repo.getLeaveRequests().collect { _leaveRequests.value = it } }
    }

    fun updateArea(id: String, area: String) = viewModelScope.launch { repo.updateWorkerArea(id, area) }
    fun updateAttendance(id: String, absent: Int, deductions: Double) = viewModelScope.launch { repo.updateAttendance(id, absent, deductions) }
    fun updateKyc(id: String, status: KycStatus) = viewModelScope.launch { repo.updateKycStatus(id, status) }

    fun approveLeave(request: LeaveRequest, context: Context) = viewModelScope.launch {
        repo.updateLeaveStatus(request.id, LeaveStatus.APPROVED)
        sendEmail(context, request.workerEmail, "Leave Approved", "Your leave for ${request.startDate} is approved.")
    }

    fun rejectLeaveWithPenalty(request: LeaveRequest, penalty: Double, context: Context) = viewModelScope.launch {
        repo.updateLeaveStatus(request.id, LeaveStatus.REJECTED)
        repo.getWorkerById(request.workerId)?.let {
            repo.updateAttendance(it.id, it.absentDays + 1, it.deductions + penalty)
        }
        sendEmail(context, request.workerEmail, "Leave Rejected & Penalty", "Your leave is rejected. A penalty of ₹$penalty is applied.")
    }

    fun blockWorker(worker: Worker, penalty: Double, context: Context) = viewModelScope.launch {
        repo.blockWorkerWithPenalty(worker.id, penalty)
        sendEmail(context, worker.email, "Account Blocked", "Your account is blocked. Penalty applied: ₹$penalty.")
    }

    fun sendEmail(context: Context, email: String, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        try { context.startActivity(Intent.createChooser(intent, "Admin Notification")) } catch (e: Exception) {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaAssignmentScreen(onBack: () -> Unit, viewModel: AdminViewModel = hiltViewModel()) {
    val workers by viewModel.workers.collectAsStateWithLifecycle()
    val leaveRequests by viewModel.leaveRequests.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var tabIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Admin Dashboard") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = tabIndex, contentColor = AppColors.Saffron) {
                Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Workers") })
                Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Leaves (${leaveRequests.count { it.status == LeaveStatus.PENDING }})") })
            }

            if (tabIndex == 0) {
                OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, label = { Text("Search Worker") }, modifier = Modifier.fillMaxWidth().padding(16.dp), leadingIcon = { Icon(Icons.Default.Search, null) })
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(workers.filter { it.name.contains(searchQuery, true) }, key = { it.id }) { worker ->
                        WorkerAdminCard(worker, { viewModel.updateArea(worker.id, it) }, { a, d -> viewModel.updateAttendance(worker.id, a, d) }, { viewModel.updateKyc(worker.id, it) }, { p -> viewModel.blockWorker(worker, p, context) }, { viewModel.sendEmail(context, worker.email, "Admin Message", "Hello ${worker.name},") })
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(leaveRequests, key = { it.id }) { request ->
                        LeaveRequestCard(request, { viewModel.approveLeave(request, context) }, { p -> viewModel.rejectLeaveWithPenalty(request, p, context) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkerAdminCard(worker: Worker, onArea: (String) -> Unit, onPay: (Int, Double) -> Unit, onKyc: (KycStatus) -> Unit, onBlock: (Double) -> Unit, onEmailClick: () -> Unit) {
    var areaExpanded by remember { mutableStateOf(false) }; var kycExpanded by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var abs by remember { mutableStateOf(worker.absentDays.toString()) }; var ded by remember { mutableStateOf(worker.deductions.toInt().toString()) }

    if (showBlockDialog) {
        var p by remember { mutableStateOf("1000") }
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            title = { Text("Block Worker") },
            text = { OutlinedTextField(value = p, onValueChange = { p = it }, label = { Text("Penalty Amount (₹)") }) },
            confirmButton = { Button(onClick = { onBlock(p.toDoubleOrNull() ?: 0.0); showBlockDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Block & Cut Salary") } }
        )
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(worker.name, fontWeight = FontWeight.Bold); Text(worker.primarySkill(), color = AppColors.Saffron)
                }
                IconButton(onClick = onEmailClick) { Icon(Icons.Outlined.Email, null, tint = AppColors.Saffron) }
                if (worker.isBlocked) Surface(color = Color.Red.copy(0.1f), shape = RoundedCornerShape(50)) { Text("BLOCKED", color = Color.Red, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(8.dp, 4.dp)) }
                else IconButton(onClick = { kycExpanded = true }) { Icon(Icons.Default.VerifiedUser, null, tint = if(worker.identityVerified) AppColors.Available else Color.Gray) }
            }
            DropdownMenu(expanded = kycExpanded, onDismissRequest = { kycExpanded = false }) {
                KycStatus.entries.forEach { status -> DropdownMenuItem(text = { Text(status.name) }, onClick = { onKyc(status); kycExpanded = false }) }
            }
            ExposedDropdownMenuBox(expanded = areaExpanded, onExpandedChange = { areaExpanded = it }) {
                OutlinedTextField(value = worker.assignedArea.ifBlank { "Unassigned" }, onValueChange = {}, readOnly = true, label = { Text("Assigned Area") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(areaExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                ExposedDropdownMenu(expanded = areaExpanded, onDismissRequest = { areaExpanded = false }) {
                    BangaloreAreas.allAreas.forEach { a -> DropdownMenuItem(text = { Text(a) }, onClick = { onArea(a); areaExpanded = false }) }
                }
            }
            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = abs, onValueChange = { abs = it }, label = { Text("Absents") }, modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(value = ded, onValueChange = { ded = it }, label = { Text("Deductions") }, modifier = Modifier.weight(1f), singleLine = true)
            }
            Row(modifier = Modifier.padding(top = 12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onPay(abs.toIntOrNull() ?: 0, ded.toDoubleOrNull() ?: 0.0) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = AppColors.Teal)) { Text("Update Pay") }
                if (!worker.isBlocked) OutlinedButton(onClick = { showBlockDialog = true }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)) { Text("Terminate") }
            }
        }
    }
}

@Composable
fun LeaveRequestCard(request: LeaveRequest, onApprove: () -> Unit, onReject: (Double) -> Unit) {
    var showRejectDialog by remember { mutableStateOf(false) }
    if (showRejectDialog) {
        var p by remember { mutableStateOf("500") }
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject Leave") },
            text = { OutlinedTextField(value = p, onValueChange = { p = it }, label = { Text("Salary Cut (₹)") }) },
            confirmButton = { Button(onClick = { onReject(p.toDoubleOrNull() ?: 0.0); showRejectDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Reject & Cut Salary") } }
        )
    }
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(request.workerName, fontWeight = FontWeight.Bold)
            Text("${request.startDate} to ${request.endDate}", style = MaterialTheme.typography.bodySmall)
            Text(request.reason)
            if (request.status == LeaveStatus.PENDING) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showRejectDialog = true }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) { Text("Reject & Penalty") }
                    Button(onClick = onApprove, colors = ButtonDefaults.buttonColors(containerColor = AppColors.Available)) { Text("Approve") }
                }
            }
        }
    }
}
