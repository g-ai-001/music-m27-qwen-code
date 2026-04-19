package app.music_m27_qwen_code.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

object Logger {
    private const val TAG = "MusicApp"
    private const val MAX_LOG_LINES = 1000
    private val logQueue = ConcurrentLinkedQueue<String>()
    private var logFile: File? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private val fileNameFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun init(context: Context) {
        val logDir = context.getExternalFilesDir(null) ?: return
        val logFileName = "music_app_${fileNameFormat.format(Date())}.log"
        logFile = File(logDir, logFileName)
    }

    fun d(message: String, tag: String = TAG) {
        log("DEBUG", tag, message)
        Log.d(tag, message)
    }

    fun i(message: String, tag: String = TAG) {
        log("INFO", tag, message)
        Log.i(tag, message)
    }

    fun w(message: String, tag: String = TAG) {
        log("WARN", tag, message)
        Log.w(tag, message)
    }

    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        val fullMessage = if (throwable != null) {
            "$message\n${Log.getStackTraceString(throwable)}"
        } else {
            message
        }
        log("ERROR", tag, fullMessage)
        Log.e(tag, fullMessage)
    }

    private fun log(level: String, tag: String, message: String) {
        val timestamp = dateFormat.format(Date())
        val logLine = "[$timestamp] [$level] [$tag] $message"
        logQueue.offer(logLine)

        while (logQueue.size > MAX_LOG_LINES) {
            logQueue.poll()
        }

        try {
            logFile?.let { file ->
                FileWriter(file, true).use { writer ->
                    writer.appendLine(logLine)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write log to file", e)
        }
    }

    fun getLogContent(): String {
        return logQueue.joinToString("\n")
    }
}
