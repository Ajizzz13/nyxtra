package com.nyxtra.vpn

import android.app.Application
import android.util.Log
import com.nyxtra.vpn.core.CrashLogReporter
import com.nyxtra.vpn.core.LibboxSetup
import com.nyxtra.vpn.data.model.LogLevel
import com.nyxtra.vpn.data.repository.EngineConfigRepository
import com.nyxtra.vpn.data.repository.AppRepository
import com.nyxtra.vpn.data.repository.LogsRepository
import com.nyxtra.vpn.data.repository.ProfileRepository
import java.io.File

class NyxtraApp : Application() {

    override fun onCreate() {
        super.onCreate()
        installCrashHandler()
        ProfileRepository.init(this)
        AppRepository.init(this)
        EngineConfigRepository.init(this)
        // Read previous crash artefacts BEFORE libbox setup truncates CrashReport file
        CrashLogReporter.reportPreviousCrash(this)
        val coreReady = LibboxSetup.ensureInitialized(this)
        logStartupState(coreReady)
    }

    /**
     * Report the actual runtime configuration instead of the hardcoded snapshot
     * that used to be shown regardless of the user's settings.
     */
    private fun logStartupState(coreReady: Boolean) {
        val engine = EngineConfigRepository.current()
        LogsRepository.addLog(
            LogLevel.INFO, "SYSTEM",
            "Nyxtra ${BuildConfig.VERSION_NAME} (${BuildConfig.APPLICATION_ID})"
        )
        LogsRepository.addLog(
            LogLevel.INFO, "ENGINE",
            "MTU=${engine.mtu} stack=${engine.tunStack.tag} " +
                "sniff=${if (engine.zeroRoutingSniffing) "dns-only" else "full"} " +
                "tcpNoDelay=${engine.tcpNoDelay} keepalive=${engine.keepaliveIntervalSeconds}s"
        )
        LogsRepository.addLog(
            LogLevel.INFO, "CORE",
            "Sing-box libbox initialised: $coreReady"
        )
        if (!coreReady) {
            LogsRepository.addLog(
                LogLevel.ERROR, "CORE",
                "Core runtime unavailable. Rebuild libbox.aar (see README) before connecting."
            )
        }
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
