package com.nyxtra.vpn.data.repository

import com.nyxtra.vpn.data.model.AppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object MockAppRepository {

    private val sampleApps = listOf(
        AppInfo("com.dts.freefireth", "Free Fire", isSystemApp = false, isGame = true, isSelected = true),
        AppInfo("com.dts.freefiremax", "Free Fire MAX", isSystemApp = false, isGame = true, isSelected = true),
        AppInfo("com.mobile.legends", "Mobile Legends: Bang Bang", isSystemApp = false, isGame = true, isSelected = true),
        AppInfo("com.tencent.ig", "PUBG Mobile", isSystemApp = false, isGame = true, isSelected = false),
        AppInfo("com.miHoYo.GenshinImpact", "Genshin Impact", isSystemApp = false, isGame = true, isSelected = false),
        AppInfo("com.riotgames.league.wildrift", "League of Legends: Wild Rift", isSystemApp = false, isGame = true, isSelected = false),
        AppInfo("com.google.android.youtube", "YouTube", isSystemApp = true, isGame = false, isSelected = false),
        AppInfo("com.android.chrome", "Google Chrome", isSystemApp = true, isGame = false, isSelected = false),
        AppInfo("com.whatsapp", "WhatsApp Messenger", isSystemApp = false, isGame = false, isSelected = false),
        AppInfo("org.telegram.messenger", "Telegram", isSystemApp = false, isGame = false, isSelected = false),
        AppInfo("com.discord", "Discord", isSystemApp = false, isGame = false, isSelected = false)
    )

    private val _apps = MutableStateFlow<List<AppInfo>>(sampleApps)
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    fun toggleApp(packageName: String) {
        _apps.update { list ->
            list.map {
                if (it.packageName == packageName) it.copy(isSelected = !it.isSelected) else it
            }
        }
    }

    fun selectAllGames() {
        _apps.update { list ->
            list.map {
                if (it.isGame) it.copy(isSelected = true) else it
            }
        }
    }

    fun clearAll() {
        _apps.update { list ->
            list.map { it.copy(isSelected = false) }
        }
    }

    fun getSelectedPackages(): Set<String> {
        return _apps.value.filter { it.isSelected }.map { it.packageName }.toSet()
    }
}
