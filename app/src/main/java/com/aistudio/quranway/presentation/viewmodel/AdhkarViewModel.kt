package com.aistudio.quranway.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.quranway.domain.model.Dhikr
import com.aistudio.quranway.domain.model.DailyAdhkar
import com.aistudio.quranway.domain.repository.AdhkarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AdhkarUiState(
    val dailyAdhkar: DailyAdhkar? = null,
    val allDhikr: List<Dhikr> = emptyList(),
    val selectedCategory: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val morningCompleted: Boolean = false,
    val eveningCompleted: Boolean = false,
    val searchResults: List<Dhikr> = emptyList()
)

@HiltViewModel
class AdhkarViewModel @Inject constructor(
    private val adhkarRepository: AdhkarRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AdhkarUiState())
    val uiState: StateFlow<AdhkarUiState> = _uiState.asStateFlow()
    
    init {
        loadDailyAdhkar()
        loadAllDhikr()
    }
    
    private fun loadDailyAdhkar() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val today = LocalDate.now().toString()
                val dailyAdhkar = adhkarRepository.getDailyAdhkar(today)
                _uiState.value = _uiState.value.copy(
                    dailyAdhkar = dailyAdhkar,
                    morningCompleted = dailyAdhkar.completedMorning,
                    eveningCompleted = dailyAdhkar.completedEvening,
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
    
    private fun loadAllDhikr() {
        viewModelScope.launch {
            try {
                val dhikr = adhkarRepository.getAllDhikr()
                _uiState.value = _uiState.value.copy(allDhikr = dhikr)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun markMorningComplete() {
        viewModelScope.launch {
            try {
                val today = LocalDate.now().toString()
                adhkarRepository.markMorningAsComplete(today)
                _uiState.value = _uiState.value.copy(morningCompleted = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun markEveningComplete() {
        viewModelScope.launch {
            try {
                val today = LocalDate.now().toString()
                adhkarRepository.markEveningAsComplete(today)
                _uiState.value = _uiState.value.copy(eveningCompleted = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun searchDhikr(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }
        
        viewModelScope.launch {
            try {
                val results = adhkarRepository.searchDhikr(query)
                _uiState.value = _uiState.value.copy(searchResults = results)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
