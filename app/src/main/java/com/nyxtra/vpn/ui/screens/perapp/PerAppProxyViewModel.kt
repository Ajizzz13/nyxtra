package com.nyxtra.vpn.ui.screens.perapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyxtra.vpn.data.model.AppInfo
import com.nyxtra.vpn.data.repository.MockAppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class PerAppProxyViewModel : ViewModel() {

    private val _enabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _isWhitelist = MutableStateFlow(true)
    val isWhitelist: StateFlow<Boolean> = _isWhitelist.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allApps: StateFlow<List<AppInfo>> = MockAppRepository.apps

    val filteredApps: StateFlow<List<AppInfo>> = combine(allApps, _searchQuery) { apps, query ->
        if (query.isBlank()) {
            apps
        } else {
            apps.filter {
                it.appName.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleEnabled(value: Boolean) {
        _enabled.value = value
    }

    fun setMode(whitelist: Boolean) {
        _isWhitelist.value = whitelist
    }

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun toggleApp(packageName: String) {
        MockAppRepository.toggleApp(packageName)
    }

    fun selectAllGames() {
        MockAppRepository.selectAllGames()
    }

    fun clearAll() {
        MockAppRepository.clearAll()
    }
}
