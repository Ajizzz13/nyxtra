package com.nyxtra.vpn.ui.screens.logs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyxtra.vpn.data.model.LogLevel
import com.nyxtra.vpn.ui.components.LogItemRow
import com.nyxtra.vpn.ui.theme.BorderStrong
import com.nyxtra.vpn.ui.theme.BorderSubtle
import com.nyxtra.vpn.ui.theme.CanvasBg
import com.nyxtra.vpn.ui.theme.CardBg
import com.nyxtra.vpn.ui.theme.CardSelectedBg
import com.nyxtra.vpn.ui.theme.SurfaceBg
import com.nyxtra.vpn.ui.theme.TextMuted
import com.nyxtra.vpn.ui.theme.TextPrimary
import com.nyxtra.vpn.ui.theme.TextSecondary

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .background(SurfaceBg)
                .border(1.dp, BorderSubtle)
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Live Logs",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    val text = viewModel.getExportableText()
                    clipboardManager.setText(AnnotatedString(text))
                    Toast.makeText(context, "Logs copied!", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondary, modifier = Modifier.size(19.dp))
                }

                IconButton(onClick = { viewModel.clearLogs() }) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(19.dp))
                }
            }
        }

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LogFilterChip("ALL", selectedLevel == null, { viewModel.filterByLevel(null) }, Modifier.weight(1f))
            LogFilterChip("INFO", selectedLevel == LogLevel.INFO, { viewModel.filterByLevel(LogLevel.INFO) }, Modifier.weight(1f))
            LogFilterChip("WARN", selectedLevel == LogLevel.WARN, { viewModel.filterByLevel(LogLevel.WARN) }, Modifier.weight(1f))
            LogFilterChip("ERROR", selectedLevel == LogLevel.ERROR, { viewModel.filterByLevel(LogLevel.ERROR) }, Modifier.weight(1f))
        }

        // Terminal container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CardBg)
                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            if (logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "LOG BUFFER EMPTY", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.8.sp)
                }
            } else {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(logs) { entry ->
                        LogItemRow(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) CardSelectedBg else CardBg
    val borderCol = if (isSelected) BorderStrong else BorderSubtle
    val textCol = if (isSelected) TextPrimary else TextMuted

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textCol,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
