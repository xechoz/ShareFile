package com.xechoz.sharefile.ui.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.xechoz.sharefile.feedback.FeedbackDraft
import com.xechoz.sharefile.feedback.FeedbackType
import com.xechoz.sharefile.feedback.FeedbackUrl
import com.xechoz.sharefile.platform.AppBuildInfo
import com.xechoz.sharefile.platform.LocalAppContainer
import com.xechoz.sharefile.resources.Res
import com.xechoz.sharefile.resources.action_back
import com.xechoz.sharefile.resources.action_feedback
import com.xechoz.sharefile.resources.app_name
import com.xechoz.sharefile.resources.feedback_email_label
import com.xechoz.sharefile.resources.feedback_email_placeholder
import com.xechoz.sharefile.resources.feedback_env_label
import com.xechoz.sharefile.resources.feedback_hint
import com.xechoz.sharefile.resources.feedback_message_label
import com.xechoz.sharefile.resources.feedback_message_placeholder
import com.xechoz.sharefile.resources.feedback_opened
import com.xechoz.sharefile.resources.feedback_submit
import com.xechoz.sharefile.resources.feedback_type_bug
import com.xechoz.sharefile.resources.feedback_type_label
import com.xechoz.sharefile.resources.feedback_type_other
import com.xechoz.sharefile.resources.feedback_type_suggestion
import com.xechoz.sharefile.ui.components.ContentContainer
import com.xechoz.sharefile.ui.icons.AppIcons
import com.xechoz.sharefile.ui.theme.PillShape
import com.xechoz.sharefile.ui.theme.ShareFileTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private const val MESSAGE_MAX = 1500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val appName = stringResource(Res.string.app_name)
    val openedMessage = stringResource(Res.string.feedback_opened)

    var message by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(FeedbackType.Bug) }
    var email by remember { mutableStateOf("") }

    FeedbackContent(
        message = message,
        type = type,
        email = email,
        appName = appName,
        appVersion = AppBuildInfo.VERSION,
        platform = container.platform.platformLabel,
        snackbarHostState = snackbarHostState,
        onMessageChange = { if (it.length <= MESSAGE_MAX) message = it },
        onTypeChange = { type = it },
        onEmailChange = { email = it },
        onSubmit = {
            container.platform.openUrl(
                FeedbackUrl.issuesNew(
                    FeedbackDraft(
                        message = message,
                        type = type,
                        email = email.takeIf { it.isNotBlank() },
                        appName = appName,
                        appVersion = AppBuildInfo.VERSION,
                        platform = container.platform.platformLabel,
                    ),
                ),
            )
            scope.launch { snackbarHostState.showSnackbar(openedMessage) }
        },
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedbackContent(
    message: String,
    type: FeedbackType,
    email: String,
    appName: String,
    appVersion: String,
    platform: String,
    snackbarHostState: SnackbarHostState,
    onMessageChange: (String) -> Unit,
    onTypeChange: (FeedbackType) -> Unit,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.action_feedback)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(AppIcons.ArrowLeft, contentDescription = stringResource(Res.string.action_back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        ContentContainer(
            modifier = Modifier.padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                OutlinedTextField(
                    value = message,
                    onValueChange = onMessageChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.feedback_message_label)) },
                    placeholder = { Text(stringResource(Res.string.feedback_message_placeholder)) },
                    minLines = 5,
                    maxLines = 10,
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text = stringResource(Res.string.feedback_type_label),
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FeedbackType.entries.forEach { option ->
                        FilterChip(
                            selected = option == type,
                            onClick = { onTypeChange(option) },
                            label = { Text(stringResource(option.label())) },
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.feedback_email_label)) },
                    placeholder = { Text(stringResource(Res.string.feedback_email_placeholder)) },
                    singleLine = true,
                )
                Spacer(Modifier.height(20.dp))
                EnvironmentCard(
                    appName = appName,
                    appVersion = appVersion,
                    platform = platform,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(Res.string.feedback_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onSubmit,
                    enabled = message.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = PillShape,
                ) {
                    Icon(AppIcons.OpenInNew, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = stringResource(Res.string.feedback_submit),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun EnvironmentCard(
    appName: String,
    appVersion: String,
    platform: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(Res.string.feedback_env_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$appName $appVersion",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = platform,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun FeedbackType.label() = when (this) {
    FeedbackType.Bug -> Res.string.feedback_type_bug
    FeedbackType.Suggestion -> Res.string.feedback_type_suggestion
    FeedbackType.Other -> Res.string.feedback_type_other
}

@Preview(showBackground = true)
@Composable
private fun FeedbackScreenPreview() {
    ShareFileTheme {
        FeedbackContent(
            message = "The QR code is hard to scan in dark mode.",
            type = FeedbackType.Suggestion,
            email = "user@example.com",
            appName = "Quick File Share",
            appVersion = "1.4.0",
            platform = "Android 14 (API 34), Pixel 7",
            snackbarHostState = remember { SnackbarHostState() },
            onMessageChange = {},
            onTypeChange = {},
            onEmailChange = {},
            onSubmit = {},
            onBack = {},
        )
    }
}
