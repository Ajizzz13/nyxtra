package com.nyxtra.vpn.core

import android.content.Context
import android.util.Log
import com.nyxtra.vpn.data.model.LogLevel
import com.nyxtra.vpn.data.repository.MockLogsRepository
import java.io.File

object CrashLogReporter {

    fun reportPreviousCrash(context: Context) {
        try {
            val crashFile = File(context.filesDir, "crash_last.txt")
            if (crashFile.exists()) {
                val text = try {
                    crashFile.readText()
                } catch (_: Exception) {
                    ""
                }
                if (text.isNotBlank()) {
                    MockLogsRepository.addLog(LogLevel.ERROR, "CRASH", "Previous run died, see details below")
                    text.lines().take(25).forEach { line ->
                        val clean = line.replace("(", "[").replace(")", "]").take(220)
                        if (clean.isNotBlank()) {
                            MockLogsRepository.addLog(LogLevel.ERROR, "CRASH", clean)
                        }
                    }
                }
                try {
                    crashFile.delete()
                } catch (_: Exception) {
                }
            }
        } catch (t: Throwable) {
            Log.w("CrashLogReporter", "crash file notice: ${t.message}")
        }

        try {
            val dir = context.filesDir
            val reports = dir.listFiles { f -> f.name.startsWith("CrashReport") } ?: emptyArray()
            val latest = reports.maxByOrNull { it.lastModified() }
            if (latest != null && latest.exists() && latest.length() > 0) {
                MockLogsRepository.addLog(LogLevel.ERROR, "CORE", "Native crash report found: ${latest.name}")
                try {
                    latest.readLines().takeLast(15).forEach { line ->
                        val clean = line.replace("(", "[").replace(")", "]").take(220)
                        if (clean.isNotBlank()) {
                            MockLogsRepository.addLog(LogLevel.ERROR, "CORE", clean)
                        }
                    }
                } catch (_: Exception) {
                }
            }
        } catch (t: Throwable) {
            Log.w("CrashLogReporter", "native report notice: ${t.message}")
        }

        try {
            val tail = DebugFileLog.readTail(context, 30)
            if (tail.isNotEmpty()) {
                MockLogsRepository.addLog(LogLevel.INFO, "DEBUG", "Event trail from previous run below")
                tail.forEach { line ->
                    val clean = line.replace("(", "[").replace(")", "]").take(220)
                    if (clean.isNotBlank()) {
                        MockLogsRepository.addLog(LogLevel.INFO, "TRACE", clean)
                    }
                }
            }
        } catch (t: Throwable) {
            Log.w("CrashLogReporter", "trail notice: ${t.message}")
        }
    }
}
