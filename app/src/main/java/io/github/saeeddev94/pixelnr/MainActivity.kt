package io.github.saeeddev94.pixelnr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import io.github.saeeddev94.pixelnr.ui.home.HomeScreen
import io.github.saeeddev94.pixelnr.ui.home.HomeViewModel
import io.github.saeeddev94.pixelnr.ui.theme.Pixel5GTheme

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Pixel5GTheme {
                HomeScreen(
                    state = homeViewModel.state,
                    onChangeMode = { homeViewModel.changeNrMode(it) },
                )
            }
        }
    }
}
