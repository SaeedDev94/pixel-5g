package io.github.saeeddev94.pixelnr.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.saeeddev94.pixelnr.R

@Composable
fun SetupSlidePage(
    slide: SetupSlide,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier
                .padding(bottom = 32.dp)
                .size(128.dp),
            imageVector = slide.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(slide.title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            modifier = Modifier
                .padding(vertical = 16.dp),
            text = stringResource(slide.description),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
        )
        when (slide.granted) {
            true -> Icon(
                modifier = Modifier
                    .padding(top = 32.dp)
                    .size(48.dp),
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            false -> Button(
                modifier = Modifier
                    .padding(top = 32.dp)
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                onClick = slide.onRequest,
            ) {
                Text(
                    text = stringResource(R.string.setupGrantAccess),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}
