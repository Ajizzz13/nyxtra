package com.nyxtra.vpn.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyxtra.vpn.data.model.TunStackMode
import com.nyxtra.vpn.ui.theme.NyxtraDark
import com.nyxtra.vpn.ui.theme.NyxtraDivider
import com.nyxtra.vpn.ui.theme.NyxtraSurface
import com.nyxtra.vpn.ui.theme.NyxtraTeal
import com.nyxtra.vpn.ui.theme.TextGray
import com.nyxtra.vpn.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val config by viewModel.config.collectAsState()

    Scaffold(
        containerColor = NyxtraDark,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = TextWhite) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Direct FD Handover
            SettingsSwitchItem(
                title = "Direct FD Handover",
                description = "Pass TUN file descriptor directly to Sing-box Core",
                checked = config.directFdHandover,
                enabled = false,
                onCheckedChange = {}
            )

            Divider(color = NyxtraDivider)

            // MTU
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("MTU", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text("${config.mtu}", color = NyxtraTeal, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Text("Virtual interface MTU (1280 - 1340)", color = TextGray, fontSize = 12.sp)

                Slider(
                    value = config.mtu.toFloat(),
                    onValueChange = { viewModel.updateMtu(it.toInt()) },
                    valueRange = 1280f..1340f,
                    steps = 6,
                    colors = SliderDefaults.colors(
                        thumbColor = NyxtraTeal,
                        activeTrackColor = NyxtraTeal,
                        inactiveTrackColor = NyxtraDivider
                    )
                )
            }

            Divider(color = NyxtraDivider)

            // TUN Stack Mode
            Column {
                Text("TUN Stack Mode", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text("Network packet processing mode", color = TextGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                TunStackMode.values().forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateTunStack(mode) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = config.tunStack == mode,
                            onClick = { viewModel.updateTunStack(mode) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = NyxtraTeal,
                                unselectedColor = TextGray
                            )
                        )
                        Text(
                            text = mode.displayName,
                            color = TextWhite,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Divider(color = NyxtraDivider)

            // Zero Routing & Zero Sniffing
            SettingsSwitchItem(
                title = "Zero Routing & Zero Sniffing",
                description = "Disable domain sniffing & regex rules for low latency gaming",
                checked = config.zeroRoutingSniffing,
                enabled = true,
                onCheckedChange = { viewModel.toggleZeroRouting(it) }
            )

            Divider(color = NyxtraDivider)

            // TCP NoDelay
            SettingsSwitchItem(
                title = "TCP NoDelay & Keepalive",
                description = "Aggressive socket transmission to prevent modem sleep jitter",
                checked = config.tcpNoDelay,
                enabled = true,
                onCheckedChange = { viewModel.toggleTcpNoDelay(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Reset Button
            Button(
                onClick = {
                    viewModel.resetToDefaults()
                    Toast.makeText(context, "Settings reset to defaults", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NyxtraSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset to Defaults", color = TextWhite)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(description, color = TextGray, fontSize = 12.sp)
        }

        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NyxtraTeal
            )
        )
    }
}
