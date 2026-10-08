package com.nyxtra.vpn.ui.screens.perapp

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyxtra.vpn.ui.components.AppItemRow
import com.nyxtra.vpn.ui.components.CommonTopBar
import com.nyxtra.vpn.ui.theme.NyxtraBackground
import com.nyxtra.vpn.ui.theme.NyxtraCard
import com.nyxtra.vpn.ui.theme.NyxtraCardBorder
import com.nyxtra.vpn.ui.theme.NyxtraCyberBlue
import com.nyxtra.vpn.ui.theme.NyxtraNeonGreen
import com.nyxtra.vpn.ui.theme.NyxtraSurface
import com.nyxtra.vpn.ui.theme.NyxtraSurfaceVariant
import com.nyxtra.vpn.ui.theme.TextMuted
import com.nyxtra.vpn.ui.theme.TextOnAccent
import com.nyxtra.vpn.ui.theme.TextPrimary
import com.nyxtra.vpn.ui.theme.TextSecondary

@Composable
fun PerAppProxyScreen(
    viewModel: PerAppProxyViewModel
) {
    val enabled by viewModel.enabled.collectAsState()
    val isWhitelist by viewModel.isWhitelist.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val apps by viewModel.filteredApps.collectAsState()
    val allApps by viewModel.allApps.collectAsState()

    val selectedCount = allApps.count { it.isSelected }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NyxtraBackground)
    ) {
        CommonTopBar(
            title = "APP FILTER",
            subtitle = "PER-APP PROXY"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Master Switch Card
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NyxtraCard)
                        .border(1.dp, NyxtraCardBorder, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Per-App Proxy",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (enabled) "App filtering rule is enforced" else "All device traffic routed through tunnel",
                            color = if (enabled) NyxtraNeonGreen else TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = enabled,
                        onCheckedChange = { viewModel.toggleEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NyxtraNeonGreen,
                            checkedTrackColor = NyxtraNeonGreen.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            if (enabled) {
                // Whitelist / Blacklist Mode Selector
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NyxtraCard)
                            .border(1.dp, NyxtraCardBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "FILTERING MODE",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ModePill(
                                label = "Whitelist (Proxy Selected)",
                                isSelected = isWhitelist,
                                onClick = { viewModel.setMode(true) },
                                modifier = Modifier.weight(1f)
                            )
                            ModePill(
                                label = "Blacklist (Bypass Selected)",
                                isSelected = !isWhitelist,
                                onClick = { viewModel.setMode(false) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val explanation = if (isWhitelist) {
                            "Only checked apps (e.g. Games) will enter the VPN tunnel. Other apps stay on regular connection."
                        } else {
                            "Checked apps will bypass the VPN tunnel and connect directly to the ISP."
                        }

                        Text(
                            text = explanation,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Quick Filter Buttons (Select All Games / Clear)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.selectAllGames() },
                            colors = ButtonDefaults.buttonColors(containerColor = NyxtraSurfaceVariant),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = NyxtraNeonGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Select Games", color = TextPrimary, fontSize = 11.sp)
                        }

                        Button(
                            onClick = { viewModel.clearAll() },
                            colors = ButtonDefaults.buttonColors(containerColor = NyxtraSurfaceVariant),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ClearAll,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Clear All", color = TextPrimary, fontSize = 11.sp)
                        }
                    }
                }

                // Search app input
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearch(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search installed apps...", color = TextMuted, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = NyxtraSurface,
                            unfocusedContainerColor = NyxtraSurface,
                            focusedBorderColor = NyxtraNeonGreen,
                            unfocusedBorderColor = NyxtraCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "INSTALLED APPS (${apps.size})",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$selectedCount selected",
                            color = NyxtraNeonGreen,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                items(apps, key = { it.packageName }) { app ->
                    AppItemRow(
                        app = app,
                        onToggle = { viewModel.toggleApp(app.packageName) }
                    )
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Per-App Proxy is turned off",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Switch the toggle on to isolate ping for gaming apps only.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun ModePill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) NyxtraNeonGreen.copy(alpha = 0.15f) else NyxtraSurfaceVariant)
            .border(
                width = 1.dp,
                color = if (isSelected) NyxtraNeonGreen else NyxtraCardBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) NyxtraNeonGreen else TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
