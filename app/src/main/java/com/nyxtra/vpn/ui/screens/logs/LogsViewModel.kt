package com.nyxtra.vpn.ui.screens.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyxtra.vpn.data.model.LogEntry
import com.nyxtra.vpn.data.model.LogLevel
import com.nyxtra.vpn.data.repository.MockLogsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class LogsViewModel : ViewModel() {

    val allLogs: StateFlow<List<LogEntry>> = MockLogsRepository.logs

    private val _selectedLevel = MutableStateFlow<LogLevel?>(null)
    val selectedLevel: StateFlow<LogLevel?> = _selectedLevel.asStateFlow()

    val filteredLogs: StateFlow<List<LogEntry>> = combine(allLogs, _selectedLevel) { logs, level ->
        if (level == null) logs else logs.filter { it.level == level }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun filterByLevel(level: LogLevel?) {
        _selectedLevel.value = level
    }

    fun clearLogs() {
        MockLogsRepository.clear()
    }

    fun getExportableText(): String {
        return allLogs.value.joinToString("\n") {
            "${it.formattedTime()} [${it.level.name}] [${it.tag}] ${it.message}"
        }
    }
}
