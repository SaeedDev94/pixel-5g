package io.github.saeeddev94.pixelnr.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.saeeddev94.pixelnr.R

@Composable
fun NoSimPage(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.noSimCard),
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}
