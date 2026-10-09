package com.nyxtra.vpn.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyxtra.vpn.data.model.VpnProfile
import com.nyxtra.vpn.ui.theme.NyxtraCard
import com.nyxtra.vpn.ui.theme.NyxtraCardSelected
import com.nyxtra.vpn.ui.theme.NyxtraRed
import com.nyxtra.vpn.ui.theme.NyxtraTeal
import com.nyxtra.vpn.ui.theme.TextGray
import com.nyxtra.vpn.ui.theme.TextWhite

@Composable
fun TunnelProfileCard(
    profile: VpnProfile,
    onSelect: () -> Unit,
    onPing: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = profile.isSelected

    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) NyxtraCardSelected else NyxtraCard
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = NyxtraTeal,
                    unselectedColor = TextGray
                )
            )

            Spacer(modifier = Modifier.width(4.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                val host = if (profile.bugHost.isNotBlank()) profile.bugHost else profile.serverAddress
                Text(
                    text = "${profile.protocol.displayName} • $host:${profile.serverPort}",
                    color = TextGray,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Ping display (clickable)
            val (pingColor, pingText) = when {
                profile.pingMs == null -> TextGray to "PING"
                profile.pingMs < 0 -> NyxtraRed to "Timeout"
                else -> NyxtraTeal to "${profile.pingMs} ms"
            }
            Text(
                text = pingText,
                color = pingColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable(onClick = onPing)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )

            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = TextGray,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onShare,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = TextGray,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = TextGray,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
