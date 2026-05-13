package com.manekelsa.app.presentation.screens.profile

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.manekelsa.app.domain.model.KycStatus
import com.manekelsa.app.domain.repository.WorkerRepository
import com.manekelsa.app.presentation.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KycState(
    val workerId: String = "",
    val workerName: String = "",
    val isVerifying: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class KycViewModel @Inject constructor(
    private val repo: WorkerRepository
) : ViewModel() {
    private val _state = MutableStateFlow(KycState())
    val state: StateFlow<KycState> = _state.asStateFlow()

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun loadWorker(id: String) = viewModelScope.launch {
        repo.getWorkerById(id)?.let { w ->
            _state.update { it.copy(workerId = id, workerName = w.name) }
        }
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val fullText = visionText.text
                // AI Logic: Detect name on ID Card and match with Profile
                if (fullText.contains(_state.value.workerName, ignoreCase = true)) {
                    completeVerification()
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    fun completeVerification() = viewModelScope.launch {
        if (_state.value.success || _state.value.isVerifying) return@launch
        _state.update { it.copy(isVerifying = true) }
        repo.updateKycStatus(_state.value.workerId, KycStatus.VERIFIED)
        _state.update { it.copy(success = true, isVerifying = false) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycVerificationScreen(
    workerId: String,
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: KycViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission = it }

    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.CAMERA)
        viewModel.loadWorker(workerId)
    }

    LaunchedEffect(state.success) {
        if (state.success) onSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI KYC Scanner", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (hasPermission) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    CameraPreview(onImageCaptured = viewModel::processImage)
                    Box(modifier = Modifier.fillMaxSize().padding(40.dp)) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.Transparent,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(3.dp, AppColors.Saffron)
                        ) {}
                    }
                }
                
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = AppColors.Saffron.copy(0.1f))) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Hold ID card inside the frame", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("AI is looking for: ${state.workerName}", color = AppColors.Saffron, style = MaterialTheme.typography.labelLarge)
                        if (state.isVerifying) CircularProgressIndicator(modifier = Modifier.padding(16.dp), color = AppColors.Saffron)
                    }
                }
                
                Button(
                    onClick = { viewModel.completeVerification() },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Available)
                ) {
                    Text("Skip to Verify (Demo Mode)")
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Camera permission required") }
            }
        }
    }
}

@Composable
fun CameraPreview(onImageCaptured: (ImageProxy) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(executor) { imageProxy -> onImageCaptured(imageProxy) } }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
                } catch (e: Exception) { Log.e("KYC", "Binding failed", e) }
            }, executor)
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}
