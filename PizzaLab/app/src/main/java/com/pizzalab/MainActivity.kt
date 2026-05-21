package com.pizzalab

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.pizzalab.ui.navigation.PizzaLabBottomBar
import com.pizzalab.ui.navigation.PizzaLabNavHost
import com.pizzalab.ui.splash.SplashScreen
import com.pizzalab.ui.theme.PizzaLabTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean ->
        // Permission result handled — no special action needed.
        // Notification features will check permission at point of use.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Dismiss the system splash screen immediately — we have our own
        installSplashScreen()
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()

        setContent {
            PizzaLabTheme {
                PizzaLabApp()
            }
        }
    }

    /**
     * On Android 13+ (API 33), POST_NOTIFICATIONS requires a runtime permission.
     * We request it at launch so that process timers can show notifications later.
     */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}

@Composable
fun PizzaLabApp() {
    var showSplash by rememberSaveable { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(onSplashComplete = { showSplash = false })
    } else {
        val navController = rememberNavController()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = { PizzaLabBottomBar(navController) }
        ) { innerPadding ->
            PizzaLabNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
