package com.nyxtra.vpn.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nyxtra.vpn.data.model.AppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

object MockAppRepository {

    private const val FILE_NAME = "nyxtra_perapp.json"

    private val gson = Gson()
    private var storageFile: File? = null

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    private val _enabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _isWhitelist = MutableStateFlow(true)
    val isWhitelist: StateFlow<Boolean> = _isWhitelist.asStateFlow()

    private var selectedPackages = mutableSetOf<String>()
    private var appsLoaded = false

    fun init(context: Context) {
        if (storageFile != null) return
        val file = File(context.filesDir, FILE_NAME)
        storageFile = file
        if (file.exists()) {
            try {
                val obj = gson.fromJson(file.readText(), JsonObject::class.java)
                _enabled.value = obj.get("enabled")?.asBoolean ?: false
                _isWhitelist.value = obj.get("whitelist")?.asBoolean ?: true
                obj.getAsJsonArray("selected")?.forEach {
                    selectedPackages.add(it.asString)
                }
            } catch (e: Exception) {
                Log.w("AppRepository", "Failed to load per-app state: ${e.message}")
            }
        }
        refreshSelection()
    }

    private fun persist() {
        val file = storageFile ?: return
        try {
            val obj = JsonObject().apply {
                addProperty("enabled", _enabled.value)
                addProperty("whitelist", _isWhitelist.value)
                val arr = com.google.gson.JsonArray()
                selectedPackages.forEach { arr.add(it) }
                add("selected", arr)
            }
            file.writeText(gson.toJson(obj))
        } catch (e: Exception) {
            Log.w("AppRepository", "Failed to save per-app state: ${e.message}")
        }
    }

    fun loadInstalledApps(context: Context) {
        try {
            val pm = context.packageManager
            val selfPkg = context.packageName
            val flags = PackageManager.GET_META_DATA
            val installed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(flags.toLong()))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledApplications(flags)
            }
            val list = installed
                .filter { it.packageName != selfPkg }
                .filter {
                    try {
                        val req = pm.getPackageInfo(it.packageName, PackageManager.GET_PERMISSIONS)
                        req.requestedPermissions?.contains("android.permission.INTERNET") == true
                    } catch (_: Exception) {
                        false
                    }
                }
                .map {
                    val label = try {
                        pm.getApplicationLabel(it).toString()
                    } catch (_: Exception) {
                        it.packageName
                    }
                    val isSystem = (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val isGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        it.category == ApplicationInfo.CATEGORY_GAME
                    } else {
                        false
                    }
                    AppInfo(
                        packageName = it.packageName,
                        appName = label,
                        isSystemApp = isSystem,
                        isGame = isGame,
                        isSelected = selectedPackages.contains(it.packageName)
                    )
                }
                .sortedWith(compareBy({ !it.isSelected }, { it.appName.lowercase() }))
            _apps.value = list
            appsLoaded = true
        } catch (t: Throwable) {
            Log.w("AppRepository", "Failed to list installed apps: ${t.message}")
        }
    }

    fun isAppsLoaded(): Boolean = appsLoaded

    private fun refreshSelection() {
        _apps.update { list ->
            list.map { it.copy(isSelected = selectedPackages.contains(it.packageName)) }
                .sortedWith(compareBy({ !it.isSelected }, { it.appName.lowercase() }))
        }
    }

    fun setEnabled(value: Boolean) {
        _enabled.value = value
        persist()
    }

    fun setWhitelistMode(whitelist: Boolean) {
        _isWhitelist.value = whitelist
        persist()
    }

    fun toggleApp(packageName: String) {
        if (selectedPackages.contains(packageName)) {
            selectedPackages.remove(packageName)
        } else {
            selectedPackages.add(packageName)
        }
        refreshSelection()
        persist()
    }

    fun selectAllGames() {
        _apps.value.filter { it.isGame }.forEach { selectedPackages.add(it.packageName) }
        refreshSelection()
        persist()
    }

    fun clearAll() {
        selectedPackages.clear()
        refreshSelection()
        persist()
    }

    fun isFilteringEnabled(): Boolean = _enabled.value

    fun isWhitelistMode(): Boolean = _isWhitelist.value

    fun getSelectedPackages(): Set<String> {
        return selectedPackages.toSet()
    }
}
