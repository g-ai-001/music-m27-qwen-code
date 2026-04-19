package app.music_m27_qwen_code.viewmodel

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import app.music_m27_qwen_code.data.model.Song
import app.music_m27_qwen_code.data.repository.MusicRepository
import app.music_m27_qwen_code.service.MusicService
import app.music_m27_qwen_code.utils.LyricParser
import app.music_m27_qwen_code.utils.Logger
import app.music_m27_qwen_code.utils.LyricLine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val playlist: List<Song> = emptyList(),
    val currentIndex: Int = 0,
    val lyrics: List<LyricLine> = emptyList(),
    val currentLyricIndex: Int = -1,
    val isFavorite: Boolean = false,
    val playerMode: PlayerMode = PlayerMode.COVER
)

enum class PlayerMode { COVER, LYRICS }

class PlayerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var repository: MusicRepository? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            updatePlayingState()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updatePlayingState()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val index = mediaController?.currentMediaItemIndex ?: 0
            val playlist = _uiState.value.playlist
            if (index in playlist.indices) {
                val song = playlist[index]
                _uiState.update {
                    it.copy(
                        currentSong = song,
                        currentIndex = index,
                        duration = mediaController?.duration ?: 0L
                    )
                }
                loadLyrics(song)
                checkFavorite(song.id)
            }
        }
    }

    fun initialize(context: Context) {
        repository = MusicRepository(context)
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            mediaController?.addListener(playerListener)
            Logger.i("MediaController connected")
        }, MoreExecutors.directExecutor())
    }

    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        viewModelScope.launch {
            _uiState.update { it.copy(playlist = songs) }
            mediaController?.let { controller ->
                controller.clearMediaItems()
                val mediaItems = songs.map { repository?.songToMediaItem(it)!! }
                controller.setMediaItems(mediaItems, startIndex, 0L)
                controller.prepare()
            }
        }
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun togglePlayPause() {
        if (_uiState.value.isPlaying) pause() else play()
    }

    fun next() {
        mediaController?.seekToNext()
    }

    fun previous() {
        mediaController?.seekToPrevious()
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
        _uiState.update { it.copy(currentPosition = position) }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val song = _uiState.value.currentSong ?: return@launch
            repository?.let { repo ->
                if (_uiState.value.isFavorite) {
                    repo.removeFavorite(song.id)
                } else {
                    repo.addFavorite(song.id)
                }
                _uiState.update { it.copy(isFavorite = !it.isFavorite) }
            }
        }
    }

    fun togglePlayerMode() {
        _uiState.update {
            it.copy(
                playerMode = if (it.playerMode == PlayerMode.COVER) PlayerMode.LYRICS else PlayerMode.COVER
            )
        }
    }

    fun startPositionUpdates() {
        viewModelScope.launch {
            while (isActive) {
                mediaController?.let { controller ->
                    _uiState.update {
                        it.copy(
                            currentPosition = controller.currentPosition,
                            duration = controller.duration.takeIf { d -> d > 0 } ?: it.duration
                        )
                    }
                    updateLyricIndex(_uiState.value.currentPosition)
                }
                delay(100)
            }
        }
    }

    private fun updatePlayingState() {
        _uiState.update { it.copy(isPlaying = mediaController?.isPlaying == true) }
    }

    private fun loadLyrics(song: Song) {
        viewModelScope.launch {
            val lrcPath = LyricParser.findLrcFile(song.path)
            val lyrics = if (lrcPath != null) {
                LyricParser.parseLrcFile(lrcPath)
            } else {
                emptyList()
            }
            _uiState.update { it.copy(lyrics = lyrics, currentLyricIndex = -1) }
        }
    }

    private fun updateLyricIndex(position: Long) {
        val lyrics = _uiState.value.lyrics
        if (lyrics.isEmpty()) return
        val newIndex = LyricParser.getCurrentLineIndex(lyrics, position)
        if (newIndex != _uiState.value.currentLyricIndex) {
            _uiState.update { it.copy(currentLyricIndex = newIndex) }
        }
    }

    private fun checkFavorite(songId: Long) {
        viewModelScope.launch {
            repository?.isFavorite(songId)?.collect { isFav ->
                _uiState.update { it.copy(isFavorite = isFav) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaController?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
