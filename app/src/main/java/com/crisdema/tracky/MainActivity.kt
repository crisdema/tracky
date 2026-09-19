package com.crisdema.tracky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.crisdema.tracky.ui.navigation.Routes
import com.crisdema.tracky.ui.navigation.TrackyNavHost
import com.crisdema.tracky.ui.splash.SplashState
import com.crisdema.tracky.ui.splash.SplashViewModel
import com.crisdema.tracky.ui.theme.TrackyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            splashViewModel.uiState.value is SplashState.Loading
        }

        setContent {
            val state by splashViewModel.uiState.collectAsState()

            TrackyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (val current = state) {
                        SplashState.Loading -> Unit
                        SplashState.LoggedOut -> TrackyNavHost(startDestination = Routes.AUTH)
                        is SplashState.LoggedIn -> TrackyNavHost(startDestination = Routes.home(current.spaceId))
                    }
                }
            }
        }
    }
}