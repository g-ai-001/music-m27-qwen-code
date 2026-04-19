package app.music_m27_qwen_code.data.database

import androidx.room.*
import app.music_m27_qwen_code.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: Long): Song?

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%'")
    fun searchSongs(query: String): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>)

    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()

    @Query("SELECT * FROM songs ORDER BY dateAdded DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int = 20): Flow<List<Song>>
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Insert
    suspend fun insert(playlist: Playlist): Long

    @Delete
    suspend fun delete(playlist: Playlist)

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: Long): Playlist?
}

@Dao
interface PlaylistSongDao {
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_song_map psm ON s.id = psm.songId
        WHERE psm.playlistId = :playlistId
        ORDER BY psm.orderIndex ASC
    """)
    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>>

    @Insert
    suspend fun insert(map: PlaylistSongMap)

    @Query("DELETE FROM playlist_song_map WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeFromPlaylist(playlistId: Long, songId: Long)
}

@Dao
interface FavoriteDao {
    @Query("SELECT s.* FROM songs s INNER JOIN favorites f ON s.id = f.songId ORDER BY f.addedAt DESC")
    fun getAllFavorites(): Flow<List<Song>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    fun isFavorite(songId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun removeFavorite(songId: Long)
}
