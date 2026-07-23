package com.aistudio.quranway.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.quranway.domain.model.Dua
import com.aistudio.quranway.domain.model.DuaCategory
import com.aistudio.quranway.domain.repository.DuaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DuaUiState(
    val duas: List<Dua> = emptyList(),
    val categories: List<DuaCategory> = emptyList(),
    val selectedCategory: String = "",
    val favorites: List<Dua> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val searchResults: List<Dua> = emptyList()
)

@HiltViewModel
class DuaViewModel @Inject constructor(
    private val duaRepository: DuaRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DuaUiState())
    val uiState: StateFlow<DuaUiState> = _uiState.asStateFlow()
    
    init {
        loadCategories()
        loadFavorites()
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val categories = duaRepository.getDuaCategories()
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
                val duas = duaRepository.getDuasByCategory(category)
                _uiState.value = _uiState.value.copy(
                    duas = duas,
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
    
    fun searchDuas(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }
        
        viewModelScope.launch {
            try {
                val results = duaRepository.searchDuas(query)
                _uiState.value = _uiState.value.copy(searchResults = results)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun addToFavorites(duaId: Int) {
        viewModelScope.launch {
            try {
                duaRepository.addToFavorites(duaId)
                loadFavorites()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    private fun loadFavorites() {
        viewModelScope.launch {
            duaRepository.getFavoriteDuas().collect { favorites ->
                _uiState.value = _uiState.value.copy(favorites = favorites)
            }
        }
    }
}
