package app.music_m27_qwen_code.utils

import java.io.File
import java.util.regex.Pattern

data class LyricLine(
    val time: Long,
    val text: String
)

object LyricParser {
    private val lyricPattern = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2})\\](.*)")

    fun parseLrcFile(lrcPath: String): List<LyricLine> {
        val file = File(lrcPath)
        if (!file.exists()) return emptyList()

        return try {
            file.readLines()
                .mapNotNull { parseLine(it) }
                .sortedBy { it.time }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun findLrcFile(audioPath: String): String? {
        val audioFile = File(audioPath)
        if (!audioFile.exists()) return null

        val lrcFile = File(audioFile.parent, audioFile.nameWithoutExtension + ".lrc")
        if (lrcFile.exists()) return lrcFile.absolutePath

        val altLrcFile = File(audioFile.parent, audioFile.nameWithoutExtension + ".LRC")
        if (altLrcFile.exists()) return altLrcFile.absolutePath

        return null
    }

    private fun parseLine(line: String): LyricLine? {
        val matcher = lyricPattern.matcher(line)
        if (!matcher.matches()) return null

        val minutes = matcher.group(1).toLongOrNull() ?: return null
        val seconds = matcher.group(2).toLongOrNull() ?: return null
        val centiseconds = matcher.group(3).toLongOrNull() ?: return null
        val text = matcher.group(4)?.trim() ?: return null

        if (text.isEmpty()) return null

        val time = minutes * 60 * 1000 + seconds * 1000 + centiseconds * 10
        return LyricLine(time, text)
    }

    fun getCurrentLineIndex(lyrics: List<LyricLine>, currentPosition: Long): Int {
        if (lyrics.isEmpty()) return -1

        for (i in lyrics.indices.reversed()) {
            if (currentPosition >= lyrics[i].time) {
                return i
            }
        }
        return -1
    }
}
