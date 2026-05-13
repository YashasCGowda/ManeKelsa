package com.manekelsa.app.presentation.screens.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.manekelsa.app.data.local.BangaloreAreas
import com.manekelsa.app.domain.model.*
import com.manekelsa.app.domain.repository.WorkerRepository
import com.manekelsa.app.presentation.components.*
import com.manekelsa.app.presentation.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val worker: Worker? = null,
    val isLoading: Boolean = false,
    val showJobDialog: Boolean = false,
    val showRatingDialog: Boolean = false,
    val showLeaveDialog: Boolean = false,
    val snackMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(private val repo: WorkerRepository) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    fun load(id: String) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        val w = repo.getWorkerById(id)
        _state.update { it.copy(worker = w, isLoading = false) }
    }

    fun showJobDialog() = _state.update { it.copy(showJobDialog = true) }
    fun dismissJobDialog() = _state.update { it.copy(showJobDialog = false) }
    fun showRatingDialog() = _state.update { it.copy(showRatingDialog = true) }
    fun dismissRatingDialog() = _state.update { it.copy(showRatingDialog = false) }
    fun showLeaveDialog() = _state.update { it.copy(showLeaveDialog = true) }
    fun dismissLeaveDialog() = _state.update { it.copy(showLeaveDialog = false) }
    fun clearSnack() = _state.update { it.copy(snackMessage = null) }

    fun sendJobRequest(title: String, desc: String, location: String, pay: Double) = viewModelScope.launch {
        val w = _state.value.worker ?: return@launch
        repo.sendJobRequest(JobRequest(workerId = w.id, title = title, description = desc, location = location, payPerDay = pay))
        _state.update { it.copy(showJobDialog = false, snackMessage = "Job request sent to ${w.name}!") }
    }

    fun submitRating(rating: Float, comment: String) {
        _state.update { it.copy(showRatingDialog = false, snackMessage = "Rating submitted! ⭐".repeat(rating.toInt())) }
    }

    fun submitLeaveRequest(context: Context, reason: String, startDate: String, endDate: String) = viewModelScope.launch {
        val worker = _state.value.worker ?: return@launch
        val request = LeaveRequest(
            workerId = worker.id,
            workerName = worker.name,
            workerEmail = worker.email,
            reason = reason,
            startDate = startDate,
            endDate = endDate
        )
        repo.requestLeave(request)
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("admin@manekelsa.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Leave Request - ${worker.name}")
            putExtra(Intent.EXTRA_TEXT, "Reason: $reason\nDates: $startDate to $endDate")
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Send Email"))
            _state.update { it.copy(showLeaveDialog = false, snackMessage = "Request submitted and Email opened") }
        } catch (e: Exception) {
            _state.update { it.copy(showLeaveDialog = false, snackMessage = "Request saved") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    workerId: String, 
    onBack: () -> Unit, 
    onVerifyClick: (String) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackHost = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(workerId) { viewModel.load(workerId) }
    LaunchedEffect(state.snackMessage) {
        state.snackMessage?.let { snackHost.showSnackbar(it); viewModel.clearSnack() }
    }

    if (state.showJobDialog) JobDialog(
        workerName = state.worker?.name ?: "",
        dailyRate = state.worker?.dailyRate ?: 0.0,
        onDismiss = viewModel::dismissJobDialog,
        onSubmit = viewModel::sendJobRequest
    )

    if (state.showRatingDialog) RatingDialog(
        workerName = state.worker?.name ?: "",
        onDismiss = viewModel::dismissRatingDialog,
        onSubmit = viewModel::submitRating
    )

    if (state.showLeaveDialog) LeaveRequestDialog(
        onDismiss = viewModel::dismissLeaveDialog,
        onSubmit = { reason, start, end -> viewModel.submitLeaveRequest(context, reason, start, end) }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackHost) },
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                title = { Text(state.worker?.name ?: "Profile") },
                actions = {
                    IconButton(onClick = viewModel::showLeaveDialog) { Icon(Icons.Outlined.Email, "Request Leave") }
                    IconButton(onClick = {}) { Icon(Icons.Outlined.Share, null) }
                }
            )
        },
        bottomBar = {
            state.worker?.let { w ->
                Surface(shadowElevation = 8.dp) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = viewModel::showRatingDialog, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Outlined.Star, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp)); Text("Rate")
                        }
                        Button(
                            onClick = viewModel::showJobDialog,
                            enabled = w.isAvailable && !w.isBlocked,
                            modifier = Modifier.weight(2f),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Saffron)
                        ) {
                            Icon(Icons.Filled.Work, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (w.isBlocked) "Blocked" else if (w.isAvailable) "Hire Now" else "Not Available", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AppColors.Saffron) }
        } else state.worker?.let { w ->
            Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState())) {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(90.dp).clip(CircleShape).background(AppColors.Saffron.copy(0.15f)), contentAlignment = Alignment.Center) {
                            Text(w.name.take(2).uppercase(), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = AppColors.Saffron)
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(w.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                            if (w.identityVerified) Icon(Icons.Filled.Verified, null, tint = AppColors.Verified, modifier = Modifier.size(20.dp))
                            if (w.isBlocked) Icon(Icons.Filled.Block, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                        }
                        Text(w.primarySkill(), color = AppColors.Saffron, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                            if (w.kycStatus != KycStatus.NOT_STARTED) StatusBadge(label = "KYC: ${w.kycStatus.name}", color = if(w.kycStatus == KycStatus.VERIFIED) AppColors.Available else Color.Red)
                            if (w.isBlocked) StatusBadge(label = "TERMINATED", color = Color.Red)
                        }

                        if (w.role == UserRole.WORKER && w.kycStatus != KycStatus.VERIFIED) {
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { onVerifyClick(w.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Verified)
                            ) {
                                Icon(Icons.Default.CameraAlt, null)
                                Spacer(Modifier.width(8.dp)); Text("Verify Identity")
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            StatColumn("Rating", w.toDisplayRating())
                            StatColumn("Jobs", "${w.completedJobs}")
                            StatColumn("Exp", "${w.experience}yr")
                            StatColumn("Rate", "₹${w.dailyRate.toInt()}/d")
                        }
                    }
                }

                SectionHeader("Earnings & Wallet")
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Wallet Balance", style = MaterialTheme.typography.labelSmall, color = AppColors.MidGray)
                            Text("₹${w.walletBalance.toInt()}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = AppColors.Saffron)
                        }
                        Button(onClick = {}, shape = RoundedCornerShape(12.dp)) { Text("Withdraw") }
                    }
                }

                Spacer(Modifier.height(12.dp))
                SectionHeader("Payroll & Attendance")
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PayrollRow("Base Salary", "₹${w.baseSalary.toInt()}")
                        PayrollRow("Absent Days", "${w.absentDays} days", if(w.absentDays > 0) Color.Red else AppColors.Available)
                        PayrollRow("Deductions", "-₹${w.deductions.toInt()}", Color.Red)
                        HorizontalDivider()
                        PayrollRow("Final Salary", "₹${w.calculateFinalSalary().toInt()}", AppColors.Teal, true)
                    }
                }

                SectionHeader("Contact Info")
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ContactItem(Icons.Outlined.Phone, w.phone)
                        ContactItem(Icons.Outlined.Email, w.email)
                    }
                }

                SectionHeader("Skills & Expertise")
                FlowRow(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    w.skills.forEach { SkillChip(it) }
                }
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun PayrollRow(label: String, value: String, color: Color = AppColors.Charcoal, isBold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = AppColors.MidGray)
        Text(value, fontWeight = if(isBold) FontWeight.ExtraBold else FontWeight.Bold, color = color)
    }
}

@Composable
private fun ContactItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AppColors.Saffron, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp)); Text(text, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StatusBadge(label: String, color: Color) {
    Surface(shape = RoundedCornerShape(50), color = color.copy(0.1f)) {
        Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = AppColors.Saffron)
        Text(label, style = MaterialTheme.typography.labelSmall, color = AppColors.MidGray)
    }
}

@Composable
private fun LeaveRequestDialog(onDismiss: () -> Unit, onSubmit: (String, String, String) -> Unit) {
    var reason by remember { mutableStateOf("") }; var start by remember { mutableStateOf("") }; var end by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Request Leave", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = start, onValueChange = { start = it }, label = { Text("Start Date") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text("End Date") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(onClick = { onSubmit(reason, start, end) }, enabled = reason.isNotBlank(), modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = AppColors.Saffron)) { Text("Send") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JobDialog(workerName: String, dailyRate: Double, onDismiss: () -> Unit, onSubmit: (String, String, String, Double) -> Unit) {
    var title by remember { mutableStateOf("") }; var desc by remember { mutableStateOf("") }; var loc by remember { mutableStateOf("") }
    var pay by remember { mutableStateOf(if (dailyRate > 0) dailyRate.toInt().toString() else "") }
    var areaExpanded by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Hire $workerName", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Job Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                ExposedDropdownMenuBox(expanded = areaExpanded, onExpandedChange = { areaExpanded = it }) {
                    OutlinedTextField(value = loc, onValueChange = { loc = it }, label = { Text("Area") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = areaExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = areaExpanded, onDismissRequest = { areaExpanded = false }) {
                        BangaloreAreas.allAreas.filter { it.contains(loc, ignoreCase = true) }.forEach { area ->
                            DropdownMenuItem(text = { Text(area) }, onClick = { loc = area; areaExpanded = false })
                        }
                    }
                }
                OutlinedTextField(value = pay, onValueChange = { pay = it }, label = { Text("Pay per Day") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(onClick = { onSubmit(title, desc, loc, pay.toDoubleOrNull() ?: 0.0) }, enabled = title.isNotBlank() && loc.isNotBlank(), modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = AppColors.Saffron)) { Text("Send") }
                }
            }
        }
    }
}

@Composable
private fun RatingDialog(workerName: String, onDismiss: () -> Unit, onSubmit: (Float, String) -> Unit) {
    var rating by remember { mutableStateOf(0f) }; var comment by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Rate $workerName", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                RatingBar(rating = rating, starSize = 36, onRatingChange = { rating = it })
                OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("Review") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(onClick = { onSubmit(rating, comment) }, enabled = rating > 0, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = AppColors.Saffron)) { Text("Submit") }
                }
            }
        }
    }
}
