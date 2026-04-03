package com.simplexray.an.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.simplexray.an.ui.navigation.AppNavHost
import com.simplexray.an.ui.theme.AppTheme
import com.simplexray.an.ui.theme.AppThemeAnimationDefaults
import com.simplexray.an.viewmodel.MainViewModel
import com.simplexray.an.viewmodel.MainViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels { MainViewModelFactory(application) }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false

        setContent {
            val themeMode = mainViewModel.settingsState.collectAsStateWithLifecycle().value.switches.themeMode

            AppTheme(themeMode = themeMode) {
                val statusBarColor = MaterialTheme.colorScheme.surface
                val navigationBarColor = MaterialTheme.colorScheme.surfaceContainer

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SideEffect {
                        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                        insetsController.isAppearanceLightStatusBars = statusBarColor.luminance() > 0.5f
                        insetsController.isAppearanceLightNavigationBars = navigationBarColor.luminance() > 0.5f
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        AppNavHost(mainViewModel)

                        Spacer(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .windowInsetsTopHeight(WindowInsets.statusBars)
                                .background(statusBarColor)
                        )

                        Spacer(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .windowInsetsBottomHeight(WindowInsets.navigationBars)
                                .background(navigationBarColor)
                        )
                    }
                }
            }
        }

        // Dynamically apply "Hide from the Recents screen" setting
        val am = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
        am.appTasks?.firstOrNull()?.setExcludeFromRecents(mainViewModel.prefs.hideFromRecents)

        processShareIntent(intent)
        Log.d(TAG, "MainActivity onCreate called.")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent?.let {
            processShareIntent(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Activity Coroutine Scope cancelled.")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "MainActivity onConfigurationChanged called.")
    }

    private fun processShareIntent(intent: Intent) {
        val currentIntentHash = intent.hashCode()
        if (lastProcessedIntentHash == currentIntentHash) return
        lastProcessedIntentHash = currentIntentHash

        when (intent.action) {
            Intent.ACTION_SEND -> {
                intent.clipData?.getItemAt(0)?.uri?.let { uri ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val text =
                                contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                            text?.let { mainViewModel.handleSharedContent(it) }
                        } catch (e: Exception) {
                            Log.e("Share", "Error reading shared file", e)
                        }
                    }
                }
            }

            Intent.ACTION_VIEW -> {
                intent.data?.toString()?.let { uriString ->
                    if (uriString.startsWith("simplexray://")) {
                        lifecycleScope.launch {
                            mainViewModel.handleSharedContent(uriString)
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
        private var lastProcessedIntentHash: Int = 0
    }
}