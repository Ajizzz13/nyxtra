package com.nyxtra.vpn.core

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DebugFileLog {

    private const val FILE_NAME = "nyxtra_debug.log"
    private const val MAX_LINES = 200

    @Synchronized
    fun append(context: Context, tag: String, message: String) {
        try {
            val clean = message.replace("(", "[").replace(")", "]")
            val ts = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
            val file = File(context.filesDir, FILE_NAME)
            file.appendText("$ts [$tag] $clean\n")
            trimIfNeeded(file)
        } catch (t: Throwable) {
            Log.w("DebugFileLog", "append notice: ${t.message}")
        }
    }

    @Synchronized
    fun readTail(context: Context, maxLines: Int = 60): List<String> {
        return try {
            val file = File(context.filesDir, FILE_NAME)
            if (!file.exists()) return emptyList()
            file.readLines().takeLast(maxLines)
        } catch (t: Throwable) {
            Log.w("DebugFileLog", "read notice: ${t.message}")
            emptyList()
        }
    }

    private fun trimIfNeeded(file: File) {
        try {
            if (file.length() > 256 * 1024) {
                val lines = file.readLines()
                file.writeText(lines.takeLast(MAX_LINES).joinToString("\n") + "\n")
            }
        } catch (_: Exception) {
        }
    }
}
