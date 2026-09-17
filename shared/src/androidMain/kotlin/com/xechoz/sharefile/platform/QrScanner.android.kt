package com.xechoz.sharefile.platform

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview as CameraPreviewUseCase
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.ReaderException
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors

@Composable
actual fun PlatformQrScanner(
    onResult: (String) -> Unit,
    modifier: Modifier,
    resetToken: Int,
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        if (hasPermission) {
            CameraPreview(onResult = onResult, resetToken = resetToken)
        } else {
            Text(
                "Camera permission is required to scan QR codes",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun CameraPreview(onResult: (String) -> Unit, resetToken: Int) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val reader = remember {
        MultiFormatReader().apply {
            setHints(mapOf(DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)))
        }
    }
    var handled by remember { mutableStateOf(false) }

    LaunchedEffect(resetToken) {
        handled = false
    }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val providerFuture = ProcessCameraProvider.getInstance(ctx)
            providerFuture.addListener({
                val provider = providerFuture.get()
                val preview = CameraPreviewUseCase.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setResolutionSelector(
                        ResolutionSelector.Builder()
                            .setResolutionStrategy(
                                ResolutionStrategy(
                                    Size(1280, 720),
                                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER,
                                )
                            )
                            .build()
                    )
                    .build()
                analysis.setAnalyzer(executor) { imageProxy ->
                    processImage(imageProxy, reader) { value ->
                        if (!handled) {
                            handled = true
                            onResult(value)
                        }
                    }
                }
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis,
                )
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
    )
}

private fun processImage(
    imageProxy: ImageProxy,
    reader: MultiFormatReader,
    onFound: (String) -> Unit,
) {
    try {
        val plane = imageProxy.planes[0]
        if (plane.pixelStride != 1) return
        val width = imageProxy.width
        val height = imageProxy.height
        val rowStride = plane.rowStride
        val buffer = plane.buffer
        val data = ByteArray(rowStride * height)
        buffer.rewind()
        buffer.get(data, 0, minOf(data.size, buffer.remaining()))
        val source = PlanarYUVLuminanceSource(
            data,
            rowStride,
            height,
            0,
            0,
            width,
            height,
            false,
        )
        val result = try {
            reader.decode(BinaryBitmap(HybridBinarizer(source)))
        } catch (_: ReaderException) {
            null
        }
        result?.text?.let(onFound)
    } finally {
        imageProxy.close()
    }
}
