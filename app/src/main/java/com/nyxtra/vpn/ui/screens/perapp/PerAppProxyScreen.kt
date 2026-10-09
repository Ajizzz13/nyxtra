package com.nyxtra.vpn.ui.screens.perapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyxtra.vpn.ui.components.AppItemRow
import com.nyxtra.vpn.ui.theme.NyxtraDark
import com.nyxtra.vpn.ui.theme.NyxtraDivider
import com.nyxtra.vpn.ui.theme.NyxtraSurface
import com.nyxtra.vpn.ui.theme.NyxtraTeal
import com.nyxtra.vpn.ui.theme.TextGray
import com.nyxtra.vpn.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerAppProxyScreen(
    viewModel: PerAppProxyViewModel,
    onNavigateBack: () -> Unit
) {
    val enabled by viewModel.enabled.collectAsState()
    val isWhitelist by viewModel.isWhitelist.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val apps by viewModel.filteredApps.collectAsState()
    val allApps by viewModel.allApps.collectAsState()

    val selectedCount = allApps.count { it.isSelected }

    Scaffold(
        containerColor = NyxtraDark,
        topBar = {
            TopAppBar(
                title = { Text("Per-App Proxy", color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
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
            // Enable Toggle Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("App Traffic Filtering", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text(
                        text = if (enabled) "Only selected apps are routed" else "Routing all system traffic",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = { viewModel.toggleEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = NyxtraTeal
                    )
                )
            }

            Divider(color = NyxtraDivider)

            if (enabled) {
                // Mode Chips & Quick Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = isWhitelist,
                        onClick = { viewModel.setMode(true) },
                        label = { Text("Whitelist") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NyxtraTeal,
                            selectedLabelColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = !isWhitelist,
                        onClick = { viewModel.setMode(false) },
                        label = { Text("Blacklist") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NyxtraTeal,
                            selectedLabelColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = { viewModel.selectAllGames() }) {
                        Icon(Icons.Default.SportsEsports, contentDescription = "Select Games", tint = NyxtraTeal)
                    }

                    IconButton(onClick = { viewModel.clearAll() }) {
                        Icon(Icons.Default.ClearAll, contentDescription = "Clear All", tint = TextGray)
                    }
                }

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearch(it) },
                    placeholder = { Text("Search apps...", color = TextGray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextGray) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )

                // List header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Installed Apps: ${apps.size}", color = TextGray, fontSize = 12.sp)
                    Text("$selectedCount selected", color = NyxtraTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Divider(color = NyxtraDivider)

                // App list
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(apps, key = { it.packageName }) { app ->
                        AppItemRow(
                            app = app,
                            onToggle = { viewModel.toggleApp(app.packageName) }
                        )
                    }
                }
            }
        }
    }
}
