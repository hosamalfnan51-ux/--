package com.aistudio.quranway.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.quranway.domain.model.Surah
import com.aistudio.quranway.domain.model.Verse
import com.aistudio.quranway.domain.model.VerseBookmark
import com.aistudio.quranway.domain.repository.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MushafUiState(
    val surahs: List<Surah> = emptyList(),
    val selectedSurah: Surah? = null,
    val verses: List<Verse> = emptyList(),
    val bookmarks: List<VerseBookmark> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val searchResults: List<Verse> = emptyList(),
    val isNightMode: Boolean = false,
    val textSize: Float = 18f,
    val selectedNarration: String = "حفص"
)

@HiltViewModel
class MushafViewModel @Inject constructor(
    private val quranRepository: QuranRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MushafUiState())
    val uiState: StateFlow<MushafUiState> = _uiState.asStateFlow()
    
    init {
        loadSurahs()
        loadBookmarks()
    }
    
    private fun loadSurahs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val surahs = quranRepository.getAllSurahs()
                _uiState.value = _uiState.value.copy(
                    surahs = surahs,
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
    
    fun selectSurah(surah: Surah) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedSurah = surah,
                isLoading = true
            )
            try {
                val verses = quranRepository.getVerses(surah.id)
                _uiState.value = _uiState.value.copy(
                    verses = verses,
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
    
    fun searchVerses(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }
        
        viewModelScope.launch {
            try {
                val results = quranRepository.searchVerses(query)
                _uiState.value = _uiState.value.copy(searchResults = results)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun addBookmark(verse: Verse) {
        viewModelScope.launch {
            try {
                val bookmark = VerseBookmark(
                    id = 0,
                    verseId = verse.id,
                    surahId = uiState.value.selectedSurah?.id ?: 0,
                    verseNumber = verse.verseNumber
                )
                quranRepository.addBookmark(bookmark)
                loadBookmarks()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun removeBookmark(bookmarkId: Int) {
        viewModelScope.launch {
            try {
                quranRepository.removeBookmark(bookmarkId)
                loadBookmarks()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    private fun loadBookmarks() {
        viewModelScope.launch {
            quranRepository.getBookmarks().collect { bookmarks ->
                _uiState.value = _uiState.value.copy(bookmarks = bookmarks)
            }
        }
    }
    
    fun toggleNightMode() {
        _uiState.value = _uiState.value.copy(
            isNightMode = !_uiState.value.isNightMode
        )
    }
    
    fun setTextSize(size: Float) {
        _uiState.value = _uiState.value.copy(textSize = size)
    }
    
    fun setNarration(narration: String) {
        _uiState.value = _uiState.value.copy(selectedNarration = narration)
    }
}
