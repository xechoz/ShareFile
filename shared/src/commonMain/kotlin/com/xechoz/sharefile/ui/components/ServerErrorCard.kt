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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xechoz.sharefile.resources.Res
import com.xechoz.sharefile.resources.action_retry
import com.xechoz.sharefile.resources.server_error_no_local_address
import com.xechoz.sharefile.resources.server_error_start_failed
import com.xechoz.sharefile.resources.server_error_title
import com.xechoz.sharefile.server.ServerError
import com.xechoz.sharefile.ui.icons.AppIcons
import com.xechoz.sharefile.ui.theme.PillShape
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun serverErrorMessage(reason: ServerError): String = when (reason) {
    ServerError.NoLocalAddress -> stringResource(Res.string.server_error_no_local_address)
    ServerError.StartFailed -> stringResource(Res.string.server_error_start_failed)
}

@Composable
fun ServerErrorCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    AppIcons.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(Res.string.server_error_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(Modifier.height(14.dp))
            Button(onClick = onRetry, shape = PillShape) {
                Text(stringResource(Res.string.action_retry))
            }
        }
    }
}
