package com.nyxtra.vpn.ui.screens.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyxtra.vpn.data.model.ProtocolType
import com.nyxtra.vpn.data.model.TransportType
import com.nyxtra.vpn.data.model.VpnProfile
import com.nyxtra.vpn.data.parser.UriParser
import com.nyxtra.vpn.data.repository.MockProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfilesViewModel : ViewModel() {

    val allProfiles: StateFlow<List<VpnProfile>> = MockProfileRepository.profiles

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _importDialogVisible = MutableStateFlow(false)
    val importDialogVisible: StateFlow<Boolean> = _importDialogVisible.asStateFlow()

    private val _editingProfile = MutableStateFlow<VpnProfile?>(null)
    val editingProfile: StateFlow<VpnProfile?> = _editingProfile.asStateFlow()

    private val _isPingingAll = MutableStateFlow(false)
    val isPingingAll: StateFlow<Boolean> = _isPingingAll.asStateFlow()

    val filteredProfiles: StateFlow<List<VpnProfile>> = combine(allProfiles, _searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            list.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.serverAddress.contains(query, ignoreCase = true) ||
                it.bugHost.contains(query, ignoreCase = true) ||
                it.protocol.displayName.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectProfile(id: String) {
        MockProfileRepository.selectProfile(id)
    }

    fun deleteProfile(id: String) {
        MockProfileRepository.deleteProfile(id)
    }

    fun pingProfile(id: String) {
        viewModelScope.launch {
            MockProfileRepository.pingProfile(id)
        }
    }

    fun pingAll() {
        viewModelScope.launch {
            _isPingingAll.value = true
            MockProfileRepository.pingAll()
            _isPingingAll.value = false
        }
    }

    fun showImportDialog() {
        _importDialogVisible.value = true
    }

    fun hideImportDialog() {
        _importDialogVisible.value = false
    }

    fun importUri(rawUri: String): Boolean {
        val parsed = UriParser.parse(rawUri)
        return if (parsed != null) {
            MockProfileRepository.addProfile(parsed)
            _importDialogVisible.value = false
            true
        } else {
            false
        }
    }

    fun exportUri(profile: VpnProfile): String {
        return UriParser.exportToUri(profile)
    }

    fun startCreateProfile() {
        _editingProfile.value = VpnProfile(
            name = "New Gaming Tunnel",
            protocol = ProtocolType.VLESS,
            serverAddress = "",
            serverPort = 443,
            uuidOrPassword = "",
            bugHost = "",
            sni = "",
            path = "/",
            transport = TransportType.WS,
            isTls = true
        )
    }

    fun startEditProfile(profile: VpnProfile) {
        _editingProfile.value = profile
    }

    fun saveEditingProfile(profile: VpnProfile) {
        val exists = allProfiles.value.any { it.id == profile.id }
        if (exists) {
            MockProfileRepository.updateProfile(profile)
        } else {
            MockProfileRepository.addProfile(profile)
        }
        _editingProfile.value = null
    }

    fun cancelEditing() {
        _editingProfile.value = null
    }
}
