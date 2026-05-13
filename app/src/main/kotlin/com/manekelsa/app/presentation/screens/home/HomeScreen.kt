package com.manekelsa.app.presentation.screens.home

import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manekelsa.app.domain.model.Worker
import com.manekelsa.app.presentation.components.*
import com.manekelsa.app.presentation.theme.AppColors

val CATEGORIES = listOf("All", "Plumber", "Electrician", "Carpenter", "Cleaner", "Painter", "Driver", "Cook", "Mason", "Gardener")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onWorkerClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onNavigateToJobs: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val recommended by viewModel.recommendedWorkers.collectAsStateWithLifecycle()
    val filtered = viewModel.filteredWorkers()
    val context = LocalContext.current

    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        data?.firstOrNull()?.let { viewModel.handleVoiceCommand(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToJobs -> onNavigateToJobs()
                is HomeEffect.NavigateToSettings -> onNavigateToSettings()
                is HomeEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mane-Kelsa", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, null, tint = AppColors.Saffron, modifier = Modifier.size(12.dp))
                            Text("Bengaluru, KA", style = MaterialTheme.typography.labelSmall, color = AppColors.MidGray)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) { Icon(Icons.Outlined.Search, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Try saying 'Jobs' or 'Settings'")
                    }
                    voiceLauncher.launch(intent)
                },
                containerColor = AppColors.Saffron,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Mic, "AI Voice Assistant")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(130.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.horizontalGradient(listOf(AppColors.Saffron, AppColors.SaffronLight)))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxHeight()) {
                        Text("Find Skilled Workers", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White)
                        Text("${state.workers.size} workers in Bengaluru", color = Color.White.copy(0.9f))
                        Text("Plumbers · Electricians · Carpenters & more", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.7f))
                    }
                }
            }

            if (recommended.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.AutoAwesome, null, tint = AppColors.Saffron, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("AI Recommended", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                        }
                        
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(recommended, key = { it.id }) { worker ->
                                RecommendedWorkerCard(
                                    worker = worker,
                                    onClick = { onWorkerClick(worker.id) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatItem("Total", "${state.workers.size}", Icons.Outlined.People, Modifier.weight(1f))
                    StatItem("Available", "${state.workers.count { w -> w.isAvailable && !w.isBlocked }}", Icons.Filled.CheckCircle, Modifier.weight(1f))
                    StatItem("Verified", "${state.workers.count { w -> w.identityVerified }}", Icons.Filled.Verified, Modifier.weight(1f))
                }
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CATEGORIES) { cat ->
                        val sel = state.selectedCategory == cat
                        FilterChip(
                            selected = sel,
                            onClick = { viewModel.selectCategory(cat) },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppColors.Saffron,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (state.isLoading) {
                items(4) { ShimmerCard() }
            } else {
                items(filtered, key = { it.id }) { worker ->
                    WorkerCard(
                        worker = worker,
                        onClick = { onWorkerClick(worker.id) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            if (!state.isLoading && filtered.isEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.SearchOff, null, modifier = Modifier.size(64.dp), tint = AppColors.MidGray)
                        Spacer(Modifier.height(8.dp))
                        Text("No workers found", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendedWorkerCard(worker: Worker, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Saffron.copy(0.05f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Saffron.copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AppColors.Saffron.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(worker.name.take(2).uppercase(), fontWeight = FontWeight.ExtraBold, color = AppColors.Saffron)
            }
            Spacer(Modifier.height(8.dp))
            Text(worker.name, fontWeight = FontWeight.Bold, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
            Text(worker.primarySkill(), color = AppColors.Saffron, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Star, null, tint = AppColors.StarYellow, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text(worker.toDisplayRating(), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = AppColors.Saffron, modifier = Modifier.size(20.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
            Text(label, style = MaterialTheme.typography.labelSmall, color = AppColors.MidGray)
        }
    }
}

@Composable
private fun ShimmerCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {}
}
