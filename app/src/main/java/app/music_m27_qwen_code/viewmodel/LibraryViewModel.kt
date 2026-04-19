package app.music_m27_qwen_code.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.music_m27_qwen_code.data.model.Playlist
import app.music_m27_qwen_code.data.model.Song
import app.music_m27_qwen_code.data.repository.MusicRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LibraryUiState(
    val allSongs: List<Song> = emptyList(),
    val recentSongs: List<Song> = emptyList(),
    val favorites: List<Song> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val searchResults: List<Song> = emptyList(),
    val isScanning: Boolean = false,
    val searchQuery: String = "",
    val selectedTab: HomeTab = HomeTab.RECOMMEND
)

enum class HomeTab { RECOMMEND, PLAYLIST, ARTIST, ALBUM }

class LibraryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private var repository: MusicRepository? = null

    fun initialize(context: Context) {
        repository = MusicRepository(context)
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository?.getAllSongs()?.collect { songs ->
                _uiState.update { it.copy(allSongs = songs) }
            }
        }
        viewModelScope.launch {
            repository?.getRecentlyPlayed()?.collect { songs ->
                _uiState.update { it.copy(recentSongs = songs) }
            }
        }
        viewModelScope.launch {
            repository?.getAllFavorites()?.collect { songs ->
                _uiState.update { it.copy(favorites = songs) }
            }
        }
        viewModelScope.launch {
            repository?.getAllPlaylists()?.collect { playlists ->
                _uiState.update { it.copy(playlists = playlists) }
            }
        }
    }

    fun scanMusic() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }
            try {
                repository?.scanAndSaveSongs()
            } finally {
                _uiState.update { it.copy(isScanning = false) }
            }
        }
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }
        viewModelScope.launch {
            repository?.searchSongs(query)?.collect { results ->
                _uiState.update { it.copy(searchResults = results) }
            }
        }
    }

    fun selectTab(tab: HomeTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository?.createPlaylist(name)
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository?.deletePlaylist(playlist)
        }
    }
}
