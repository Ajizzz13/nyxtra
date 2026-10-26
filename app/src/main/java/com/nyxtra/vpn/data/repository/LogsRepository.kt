package com.nyxtra.vpn.data.repository

import com.nyxtra.vpn.data.model.LogEntry
import com.nyxtra.vpn.data.model.LogLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object LogsRepository {

    // The previous "initial logs" hardcoded a fake telemetry snapshot (MTU 1280,
    // TCP_NODELAY on, core ready) that never changed with the actual settings and
    // appeared before anything had happened. Start empty; real state is logged by
    // the service once it is known.
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
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
