package com.nyxtra.vpn

import android.app.Application
import android.util.Log
import io.nekohasekai.libbox.Libbox
import io.nekohasekai.libbox.SetupOptions
import java.io.File

class NyxtraApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initLibbox()
    }

    private fun initLibbox() {
        try {
            go.Seq.setContext(this)

            val baseDir = filesDir
            baseDir.mkdirs()
            val tempDir = cacheDir
            tempDir.mkdirs()

            // Remove any stale command socket from previous abnormal termination
            val sockFile = File(baseDir, "command.sock")
            if (sockFile.exists()) {
                sockFile.delete()
            }

            val options = SetupOptions().apply {
                basePath = baseDir.absolutePath
                workingPath = baseDir.absolutePath
                tempPath = tempDir.absolutePath
                fixAndroidStack = true
                logMaxLines = 3000
                debug = true
            }
            Libbox.setup(options)
            Log.i("NyxtraApp", "Libbox setup initialized successfully at ${baseDir.absolutePath}")
        } catch (t: Throwable) {
            Log.e("NyxtraApp", "Libbox setup failed: ${t.message}", t)
        }
    }
}
