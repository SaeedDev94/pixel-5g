package io.github.saeeddev94.pixelnr.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.saeeddev94.pixelnr.ImsOption
import io.github.saeeddev94.pixelnr.R
import io.github.saeeddev94.pixelnr.ui.theme.Pixel5GTheme

@Composable
fun ImsSection(
    state: ImsState,
    moduleActive: Boolean,
    onChange: (option: ImsOption, value: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SettingsHeader(text = stringResource(R.string.settingsCalling))

        if (moduleActive) {
            ListItem(
                leadingContent = {
                    Icon(Icons.Default.Warning, contentDescription = null)
                },
                headlineContent = {
                    Text(stringResource(R.string.imsModuleWarning))
                },
                colors = ListItemDefaults.colors(
                    leadingIconColor = MaterialTheme.colorScheme.error,
                    headlineColor = MaterialTheme.colorScheme.error,
                ),
            )
        }

        when (state) {
            ImsState.Loading -> ListItem(
                headlineContent = { Text(stringResource(R.string.nrModeLoading)) },
                trailingContent = {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                },
            )
            is ImsState.Failed -> ListItem(
                headlineContent = {
                    Text(
                        text = state.reason ?: stringResource(R.string.nrModeError),
                        color = MaterialTheme.colorScheme.error,
                    )
                },
            )
            is ImsState.Loaded -> {
                ImsOption.entries.forEach { option ->
                    val value = state.values[option] ?: option.androidDefault
                    val checked = value != option.inverted
                    ListItem(
                        modifier = Modifier.toggleable(
                            value = checked,
                            role = Role.Switch,
                            onValueChange = { onChange(option, it != option.inverted) },
                        ),
                        leadingContent = {
                            Icon(option.icon, contentDescription = null)
                        },
                        headlineContent = {
                            Text(stringResource(option.title))
                        },
                        trailingContent = {
                            Switch(
                                checked = checked,
                                onCheckedChange = null,
                            )
                        },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ImsSectionPreview() {
    Pixel5GTheme {
        ImsSection(
            state = ImsState.Loaded(
                values = ImsOption.entries.associateWith { it.recommended },
            ),
            moduleActive = true,
            onChange = { _, _ -> },
        )
    }
}
