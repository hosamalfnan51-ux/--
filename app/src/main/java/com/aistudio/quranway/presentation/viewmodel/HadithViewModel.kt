package com.aistudio.quranway.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.quranway.domain.model.Hadith
import com.aistudio.quranway.domain.model.HadithCategory
import com.aistudio.quranway.domain.repository.HadithRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HadithUiState(
    val hadiths: List<Hadith> = emptyList(),
    val categories: List<HadithCategory> = emptyList(),
    val selectedCategory: String = "",
    val favorites: List<Hadith> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val searchResults: List<Hadith> = emptyList()
)

@HiltViewModel
class HadithViewModel @Inject constructor(
    private val hadithRepository: HadithRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HadithUiState())
    val uiState: StateFlow<HadithUiState> = _uiState.asStateFlow()
    
    init {
        loadCategories()
        loadFavorites()
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val categories = hadithRepository.getHadithCategories()
                _uiState.value = _uiState.value.copy(
                    categories = categories,
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
    
    fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            isLoading = true
        )
        viewModelScope.launch {
            try {
                val hadiths = hadithRepository.getHadithsByCategory(category)
                _uiState.value = _uiState.value.copy(
                    hadiths = hadiths,
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
    
    fun searchHadiths(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }
        
        viewModelScope.launch {
            try {
                val results = hadithRepository.searchHadiths(query)
                _uiState.value = _uiState.value.copy(searchResults = results)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun addToFavorites(hadithId: Int) {
        viewModelScope.launch {
            try {
                hadithRepository.addToFavorites(hadithId)
                loadFavorites()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    private fun loadFavorites() {
        viewModelScope.launch {
            hadithRepository.getFavoriteHadiths().collect { favorites ->
                _uiState.value = _uiState.value.copy(favorites = favorites)
            }
        }
    }
}
