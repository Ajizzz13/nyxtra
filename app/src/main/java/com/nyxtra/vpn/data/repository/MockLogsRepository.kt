package com.nyxtra.vpn.data.repository

import com.nyxtra.vpn.data.model.LogEntry
import com.nyxtra.vpn.data.model.LogLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object MockLogsRepository {

    private val initialLogs = listOf(
        LogEntry(level = LogLevel.INFO, tag = "SYSTEM", message = "Nyxtra Gaming Client v1.0.0 initialized"),
        LogEntry(level = LogLevel.INFO, tag = "ENGINE", message = "Direct FD Handover configured: ON"),
        LogEntry(level = LogLevel.INFO, tag = "KERNEL", message = "TUN stack mode: system (Linux Kernel Direct)"),
        LogEntry(level = LogLevel.INFO, tag = "MTU", message = "Virtual interface MTU clamped to 1280 bytes"),
        LogEntry(level = LogLevel.INFO, tag = "TCP", message = "TCP_NODELAY = enabled, keepalive = 15s"),
        LogEntry(level = LogLevel.INFO, tag = "CORE", message = "Sing-box core v1.9.0-rc ready for tunnel establishment")
    )

    private val _logs = MutableStateFlow<List<LogEntry>>(initialLogs)
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    fun addLog(level: LogLevel, tag: String, message: String) {
        _logs.update { current ->
            (current + LogEntry(level = level, tag = tag, message = message)).takeLast(250)
        }
    }

    fun clear() {
        _logs.value = emptyList()
    }
}
