package com.example.data.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class RecitationState(
    val isRecording: Boolean = false,
    val isPlaying: Boolean = false,
    val durationSeconds: Int = 0,
    val currentAmplitude: Float = 0f,
    val recordedFilePath: String? = null,
    val statusMessage: String = ""
)

class RecitationRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var outputFile: File? = null
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _recitationState = MutableStateFlow(RecitationState())
    val recitationState: StateFlow<RecitationState> = _recitationState.asStateFlow()

    fun startRecording(): Boolean {
        return try {
            stopPlaying()
            val file = File(context.cacheDir, "recitation_${System.currentTimeMillis()}.m4a")
            outputFile = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder

            _recitationState.value = RecitationState(
                isRecording = true,
                durationSeconds = 0,
                recordedFilePath = file.absolutePath,
                statusMessage = "جاري تسجيل التلاوة بصوتك..."
            )

            startAmplitudeTimer()
            true
        } catch (e: Exception) {
            Log.e("RecitationRecorder", "Failed to start recording", e)
            _recitationState.value = RecitationState(
                isRecording = false,
                statusMessage = "تعذر بدء التسجيل: ${e.localizedMessage}"
            )
            false
        }
    }

    private fun startAmplitudeTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            var seconds = 0
            while (_recitationState.value.isRecording) {
                delay(200)
                seconds += 1
                val amp = try {
                    val maxAmp = mediaRecorder?.maxAmplitude ?: 0
                    (maxAmp / 32767f).coerceIn(0.1f, 1.0f)
                } catch (e: Exception) {
                    0.2f
                }
                _recitationState.value = _recitationState.value.copy(
                    durationSeconds = seconds / 5,
                    currentAmplitude = amp
                )
            }
        }
    }

    fun stopRecording(): String? {
        return try {
            timerJob?.cancel()
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            val path = outputFile?.absolutePath
            _recitationState.value = _recitationState.value.copy(
                isRecording = false,
                currentAmplitude = 0f,
                recordedFilePath = path,
                statusMessage = "تم حفظ التسجيل بنجاح، يمكنك الاستماع الآن أو تحليله بالتجويد"
            )
            path
        } catch (e: Exception) {
            Log.e("RecitationRecorder", "Failed to stop recording", e)
            _recitationState.value = _recitationState.value.copy(
                isRecording = false,
                statusMessage = "حدث خطأ أثناء إنهاء التسجيل"
            )
            null
        }
    }

    fun startPlaying() {
        val path = _recitationState.value.recordedFilePath ?: return
        try {
            stopPlaying()
            val player = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                start()
                setOnCompletionListener {
                    _recitationState.value = _recitationState.value.copy(isPlaying = false)
                }
            }
            mediaPlayer = player
            _recitationState.value = _recitationState.value.copy(isPlaying = true)
        } catch (e: Exception) {
            Log.e("RecitationRecorder", "Failed to play recitation", e)
        }
    }

    fun stopPlaying() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        _recitationState.value = _recitationState.value.copy(isPlaying = false)
    }

    fun cleanup() {
        timerJob?.cancel()
        try {
            mediaRecorder?.release()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // ignore
        }
    }
}
