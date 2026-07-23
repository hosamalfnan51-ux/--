package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.audio.RecitationRecorder
import com.example.data.audio.RecitationState

@Composable
fun RecitationRecorderCard(
    verseText: String,
    isEnglish: Boolean,
    onEvaluateRecitation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recorder = remember { RecitationRecorder(context) }
    val state by recorder.recitationState.collectAsState()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            recorder.startRecording()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            recorder.cleanup()
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(primaryColor.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Mic",
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isEnglish) "Recitation Recording & AI Tajweed" else "تسجيل التلاوة ومعلم التجويد",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                        Text(
                            text = if (isEnglish) "Record your voice to check pronunciation" else "سجل صوتك بالقراءة للتحليل الصوتي المباشر",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                if (state.isRecording) {
                    Surface(
                        color = Color.Red.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "LIVE ●",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Audio Waveform Canvas Visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                val amp = state.currentAmplitude
                val barCount = 28

                Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    val width = size.width
                    val height = size.height
                    val step = width / barCount

                    for (i in 0 until barCount) {
                        val x = i * step + step / 2
                        val randomFactor = (i * 7 % 10) / 10f
                        val barHeight = if (state.isRecording) {
                            ((amp * 0.7f + randomFactor * 0.3f) * height).coerceIn(8f, height)
                        } else if (state.isPlaying) {
                            ((0.4f + randomFactor * 0.5f) * height).coerceIn(10f, height)
                        } else {
                            8f
                        }

                        val yStart = (height - barHeight) / 2
                        val yEnd = yStart + barHeight

                        drawLine(
                            color = if (state.isRecording) Color.Red else primaryColor,
                            start = Offset(x, yStart),
                            end = Offset(x, yEnd),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                val mins = state.durationSeconds / 60
                val secs = state.durationSeconds % 60
                val timeFormatted = String.format("%02d:%02d", mins, secs)

                Text(
                    text = timeFormatted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Record Button
                Button(
                    onClick = {
                        if (!hasMicPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            if (state.isRecording) {
                                recorder.stopRecording()
                            } else {
                                recorder.startRecording()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isRecording) Color.Red else primaryColor
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(
                        imageVector = if (state.isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Record Action",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.isRecording) (if (isEnglish) "Stop" else "إيقاف")
                        else (if (isEnglish) "Start Recording" else "بدء التسجيل")
                    )
                }

                // Playback Button (if recording exists)
                if (state.recordedFilePath != null && !state.isRecording) {
                    OutlinedButton(
                        onClick = {
                            if (state.isPlaying) {
                                recorder.stopPlaying()
                            } else {
                                recorder.startPlaying()
                            }
                        },
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "Play Action",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.isPlaying) (if (isEnglish) "Stop" else "إيقاف الاستماع")
                            else (if (isEnglish) "Play Voice" else "استمع لتسجيلك")
                        )
                    }
                }
            }

            if (state.statusMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.statusMessage,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            if (state.recordedFilePath != null && !state.isRecording) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onEvaluateRecitation,
                    colors = ButtonDefaults.buttonColors(containerColor = secondaryColor),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Evaluate", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEnglish) "Analyze Recitation with AI Coach" else "تحليل التلاوة والتجويد بالذكاء الاصطناعي ✨",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
