package io.github.saeeddev94.pixelnr.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.saeeddev94.pixelnr.ImsRegistration
import io.github.saeeddev94.pixelnr.R

@Composable
fun ImsRegistrationItem(
    registration: ImsRegistration?,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier,
        leadingContent = {
            Icon(Icons.Default.HowToReg, contentDescription = null)
        },
        headlineContent = {
            Text(stringResource(R.string.imsRegistration))
        },
        supportingContent = {
            when {
                refreshing -> Text(stringResource(R.string.phoneRestarting))
                registration == null -> Text(stringResource(R.string.nrModeLoading))
                else -> Text(
                    text = registration.summary(),
                    color = when (registration.state) {
                        ImsRegistration.State.REGISTERED -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        },
        trailingContent = {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                when (refreshing || registration == null) {
                    true -> CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                    false -> IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.phoneRestart),
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun ImsRegistration.summary(): String {
    val status = stringResource(
        when (state) {
            ImsRegistration.State.REGISTERED -> R.string.imsRegistered
            ImsRegistration.State.REGISTERING -> R.string.imsRegistering
            ImsRegistration.State.NOT_REGISTERED -> R.string.imsNotRegistered
            ImsRegistration.State.UNKNOWN -> R.string.imsUnknown
        }
    )
    val features = buildList {
        if (voice) add(stringResource(R.string.imsFeatureVoice))
        if (video) add(stringResource(R.string.imsFeatureVideo))
        if (sms) add(stringResource(R.string.imsFeatureSms))
    }
    return when (features.isEmpty()) {
        true -> status
        false -> "$status · ${features.joinToString(", ")}"
    }
}
