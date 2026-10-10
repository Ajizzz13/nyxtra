package com.nyxtra.vpn.core

import android.content.Context
import android.util.Log
import io.nekohasekai.libbox.Libbox
import io.nekohasekai.libbox.SetupOptions
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

object LibboxSetup {

    private val initialized = AtomicBoolean(false)

    fun ensureInitialized(context: Context): Boolean {
        if (initialized.get()) return true
        synchronized(this) {
            if (initialized.get()) return true
            try {
                go.Seq.setContext(context.applicationContext)
            } catch (t: Throwable) {
                Log.w("LibboxSetup", "setContext notice: ${t.message}")
            }
            try {
                val baseDir = context.filesDir
                baseDir.mkdirs()
                val tempDir = context.cacheDir
                tempDir.mkdirs()
                File(baseDir, "command.sock").delete()
                val options = SetupOptions().apply {
                    basePath = baseDir.absolutePath
                    workingPath = baseDir.absolutePath
                    tempPath = tempDir.absolutePath
                    fixAndroidStack = true
                    logMaxLines = 3000
                    debug = true
                }
                Libbox.setup(options)
                Log.i("LibboxSetup", "Libbox setup done at ${baseDir.absolutePath}")
                initialized.set(true)
                return true
            } catch (t: Throwable) {
                Log.e("LibboxSetup", "Libbox setup failed: ${t.message}", t)
                return false
            }
        }
    }
}
