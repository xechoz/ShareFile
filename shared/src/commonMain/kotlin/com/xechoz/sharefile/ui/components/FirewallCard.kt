package com.xechoz.sharefile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.xechoz.sharefile.platform.FirewallStatus
import com.xechoz.sharefile.platform.FirewallUnknown
import com.xechoz.sharefile.resources.Res
import com.xechoz.sharefile.resources.action_allow_firewall
import com.xechoz.sharefile.resources.action_allowing
import com.xechoz.sharefile.resources.cd_copy_command
import com.xechoz.sharefile.resources.firewall_blocked_detail
import com.xechoz.sharefile.resources.firewall_title
import com.xechoz.sharefile.resources.firewall_unknown_detail
import com.xechoz.sharefile.ui.icons.AppIcons
import com.xechoz.sharefile.ui.theme.PillShape
import com.xechoz.sharefile.ui.theme.ShareFileTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun firewallUnknownMessage(reason: FirewallUnknown): String = when (reason) {
    FirewallUnknown.UfwRulesUnreadable -> stringResource(Res.string.firewall_unknown_detail)
}

@Composable
fun FirewallCard(
    status: FirewallStatus,
    isAllowing: Boolean,
    command: String?,
    onAllow: () -> Unit,
    onCopied: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val detail = when (status) {
        FirewallStatus.Allowed -> return
        FirewallStatus.Blocked -> stringResource(Res.string.firewall_blocked_detail)
        is FirewallStatus.Unknown -> firewallUnknownMessage(status.reason)
    }
    val clipboard = LocalClipboardManager.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    AppIcons.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(Res.string.firewall_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            if (command != null) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = command,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp),
                        )
                        IconButton(onClick = {
                            clipboard.setText(AnnotatedString(command))
                            onCopied()
                        }) {
                            Icon(AppIcons.ContentCopy, contentDescription = stringResource(Res.string.cd_copy_command))
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Button(onClick = onAllow, shape = PillShape, enabled = !isAllowing) {
                Text(
                    if (isAllowing) {
                        stringResource(Res.string.action_allowing)
                    } else {
                        stringResource(Res.string.action_allow_firewall)
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FirewallCardPreview() {
    ShareFileTheme {
        FirewallCard(
            status = FirewallStatus.Blocked,
            isAllowing = false,
            command = "sudo ufw allow from 192.168.8.0/24 to any port 8080 proto tcp",
            onAllow = {},
            onCopied = {},
        )
    }
}
