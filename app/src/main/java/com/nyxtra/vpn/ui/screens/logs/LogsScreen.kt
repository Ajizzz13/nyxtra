package com.nyxtra.vpn.ui.screens.logs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyxtra.vpn.data.model.LogLevel
import com.nyxtra.vpn.ui.components.LogItemRow
import com.nyxtra.vpn.ui.theme.NyxtraDark
import com.nyxtra.vpn.ui.theme.NyxtraDivider
import com.nyxtra.vpn.ui.theme.NyxtraSurface
import com.nyxtra.vpn.ui.theme.NyxtraTeal
import com.nyxtra.vpn.ui.theme.TextGray
import com.nyxtra.vpn.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    viewModel: LogsViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val logs by viewModel.filteredLogs.collectAsState()
    val selectedLevel by viewModel.selectedLevel.collectAsState()

    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Scaffold(
        containerColor = NyxtraDark,
        topBar = {
            TopAppBar(
                title = { Text("Live Logs", color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val text = viewModel.getExportableText()
                        clipboardManager.setText(AnnotatedString(text))
                        Toast.makeText(context, "Logs copied", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextWhite)
                    }

                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NyxtraSurface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedLevel == null,
                    onClick = { viewModel.filterByLevel(null) },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NyxtraTeal,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedLevel == LogLevel.INFO,
                    onClick = { viewModel.filterByLevel(LogLevel.INFO) },
                    label = { Text("Info") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NyxtraTeal,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedLevel == LogLevel.WARN,
                    onClick = { viewModel.filterByLevel(LogLevel.WARN) },
                    label = { Text("Warn") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NyxtraTeal,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedLevel == LogLevel.ERROR,
                    onClick = { viewModel.filterByLevel(LogLevel.ERROR) },
                    label = { Text("Error") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NyxtraTeal,
                        selectedLabelColor = Color.White
                    )
                )
            }

            Divider(color = NyxtraDivider)

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No logs", color = TextGray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    items(logs) { entry ->
                        LogItemRow(entry = entry)
                    }
                }
            }
        }
    }
}
