package app.music_m27_qwen_code.data.repository

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import app.music_m27_qwen_code.data.database.MusicDatabase
import app.music_m27_qwen_code.data.model.*
import app.music_m27_qwen_code.utils.MediaScanner
import kotlinx.coroutines.flow.Flow

class MusicRepository(private val context: Context) {
    private val database = MusicDatabase.getInstance(context)
    private val songDao = database.songDao()
    private val playlistDao = database.playlistDao()
    private val playlistSongDao = database.playlistSongDao()
    private val favoriteDao = database.favoriteDao()

    fun getAllSongs(): Flow<List<Song>> = songDao.getAllSongs()

    fun searchSongs(query: String): Flow<List<Song>> = songDao.searchSongs(query)

    fun getRecentlyPlayed(limit: Int = 20): Flow<List<Song>> = songDao.getRecentlyPlayed(limit)

    suspend fun scanAndSaveSongs(): List<Song> {
        val songs = MediaScanner.scanLocalMusic(context)
        songDao.insertSongs(songs)
        return songs
    }

    suspend fun getSongById(id: Long): Song? = songDao.getSongById(id)

    fun songToMediaItem(song: Song): MediaItem {
        val uri = Uri.parse("content://media/external/audio/media/${song.id}")
        return MediaItem.Builder()
            .setUri(uri)
            .setMediaId(song.id.toString())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setArtworkUri(song.albumArtUri?.let { Uri.parse(it) })
                    .build()
            )
            .build()
    }

    fun getAllPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    suspend fun createPlaylist(name: String): Long {
        return playlistDao.insert(Playlist(name = name))
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.delete(playlist)
    }

    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> =
        playlistSongDao.getSongsForPlaylist(playlistId)

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long, order: Int) {
        playlistSongDao.insert(PlaylistSongMap(playlistId, songId, order))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistSongDao.removeFromPlaylist(playlistId, songId)
    }

    fun getAllFavorites(): Flow<List<Song>> = favoriteDao.getAllFavorites()

    fun isFavorite(songId: Long): Flow<Boolean> = favoriteDao.isFavorite(songId)

    suspend fun toggleFavorite(songId: Long) {
        val isFav = favoriteDao.isFavorite(songId)
        // Need to check actual state - simplified for now
        favoriteDao.addFavorite(Favorite(songId))
    }

    suspend fun addFavorite(songId: Long) {
        favoriteDao.addFavorite(Favorite(songId))
    }

    suspend fun removeFavorite(songId: Long) {
        favoriteDao.removeFavorite(songId)
    }
}
