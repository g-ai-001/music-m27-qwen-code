package app.music_m27_qwen_code.utils

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import app.music_m27_qwen_code.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MediaScanner {
    suspend fun scanLocalMusic(context: Context): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED
        )

        val selection = "${MediaStore.Audio.Media.DURATION} > ?"
        val selectionArgs = arrayOf("30000")

        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val albumId = it.getLong(albumIdColumn)
                val albumArtUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    albumId
                ).toString()

                songs.add(
                    Song(
                        id = id,
                        title = it.getString(titleColumn) ?: "Unknown",
                        artist = it.getString(artistColumn) ?: "Unknown Artist",
                        album = it.getString(albumColumn) ?: "Unknown Album",
                        duration = it.getLong(durationColumn),
                        path = it.getString(dataColumn) ?: "",
                        albumArtUri = albumArtUri,
                        dateAdded = it.getLong(dateAddedColumn) * 1000
                    )
                )
            }
        }

        songs
    }
}
