package com.nyxtra.vpn

import android.app.Application
import android.util.Log
import com.nyxtra.vpn.core.LibboxSetup
import com.nyxtra.vpn.data.repository.MockProfileRepository
import java.io.File

class NyxtraApp : Application() {

    override fun onCreate() {
        super.onCreate()
        installCrashHandler()
        MockProfileRepository.init(this)
        LibboxSetup.ensureInitialized(this)
        CrashLogReporter.reportPreviousCrash(this)
    }

    private fun installCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e("NyxtraApp", "Uncaught exception in ${thread.name}: ${throwable.message}", throwable)
                val crashFile = File(filesDir, "crash_last.txt")
                val stack = Log.getStackTraceString(throwable)
                crashFile.writeText("Thread: ${thread.name}\n$stack")
            } catch (_: Exception) {
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
