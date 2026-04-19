package app.music_m27_qwen_code.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val albumArtUri: String?,
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val coverUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_song_map", primaryKeys = ["playlistId", "songId"])
data class PlaylistSongMap(
    val playlistId: Long,
    val songId: Long,
    val orderIndex: Int
)

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey val songId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

data class PlaylistWithSongs(
    val playlist: Playlist,
    val songs: List<Song>
)
