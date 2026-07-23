package com.aistudio.quranway.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.quranway.domain.model.PrayerTime
import com.aistudio.quranway.domain.model.Qibla
import com.aistudio.quranway.domain.repository.PrayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrayerUiState(
    val prayerTime: PrayerTime? = null,
    val qibla: Qibla? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val cityName: String = ""
)

@HiltViewModel
class PrayerViewModel @Inject constructor(
    private val prayerRepository: PrayerRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PrayerUiState())
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()
    
    fun fetchPrayerTimes(latitude: Double, longitude: Double, cityName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                latitude = latitude,
                longitude = longitude,
                cityName = cityName
            )
            try {
                val today = android.text.format.DateFormat.format("yyyy-MM-dd", System.currentTimeMillis()).toString()
                val prayerTime = prayerRepository.getPrayerTimes(latitude, longitude, today)
                val qibla = prayerRepository.getQiblaDirection(latitude, longitude)
                
                _uiState.value = _uiState.value.copy(
                    prayerTime = prayerTime,
                    qibla = qibla,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
}
