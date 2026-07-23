package com.aistudio.quranway.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.quranway.domain.model.Tafsir
import com.aistudio.quranway.domain.model.TafsirTheme
import com.aistudio.quranway.domain.repository.TafsirRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TafsirUiState(
    val tafsirs: List<Tafsir> = emptyList(),
    val themes: List<TafsirTheme> = emptyList(),
    val selectedTafsir: Tafsir? = null,
    val aiTafsir: String = "",
    val isLoading: Boolean = false,
    val isLoadingAI: Boolean = false,
    val error: String? = null,
    val selectedTheme: String = ""
)

@HiltViewModel
class TafsirViewModel @Inject constructor(
    private val tafsirRepository: TafsirRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TafsirUiState())
    val uiState: StateFlow<TafsirUiState> = _uiState.asStateFlow()
    
    init {
        loadThemes()
    }
    
    private fun loadThemes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val themes = tafsirRepository.getTafsirThemes()
                _uiState.value = _uiState.value.copy(
                    themes = themes,
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
    
    fun getTafsir(surahId: Int, verseNumber: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val tafsir = tafsirRepository.getTafsir(surahId, verseNumber)
                _uiState.value = _uiState.value.copy(
                    selectedTafsir = tafsir,
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
    
    fun getAITafsir(verseText: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingAI = true)
            try {
                val aiTafsir = tafsirRepository.getAITafsir(verseText)
                _uiState.value = _uiState.value.copy(
                    aiTafsir = aiTafsir,
                    isLoadingAI = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoadingAI = false
                )
            }
        }
    }
    
    fun selectTheme(theme: String) {
        _uiState.value = _uiState.value.copy(
            selectedTheme = theme,
            isLoading = true
        )
        viewModelScope.launch {
            try {
                val tafsirs = tafsirRepository.getTafsirsByTheme(theme)
                _uiState.value = _uiState.value.copy(
                    tafsirs = tafsirs,
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
