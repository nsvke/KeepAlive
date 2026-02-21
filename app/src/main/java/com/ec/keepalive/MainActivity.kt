package com.ec.keepalive

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ec.keepalive.sui.theme.KeepAliveTheme
import com.ec.keepalive.ui.components.contacts_list.ContactListViewModel
import com.ec.keepalive.ui.components.splash.AnimatedSplashScreen
import com.ec.keepalive.ui.screens.home.HomeScreen
import com.ec.keepalive.ui.screens.permission.PermissionScreen
import com.ec.keepalive.utils.NotificationHelper
import com.ec.keepalive.utils.getRequiredPermission
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel: ContactListViewModel by viewModels()
        viewModel.checkAndSync(applicationContext)

        splashScreen.setKeepOnScreenCondition {
            !viewModel.isSplashReady.value || viewModel.contactListState.value == null
        }

        setContent {
            KeepAliveTheme() {

                val contactList by viewModel.contactListState.collectAsState()
                val isDataLoaded = contactList != null

                var appDestination by remember { mutableStateOf(0) }

                var isSplashVisible by remember { mutableStateOf(true) }

                val canDismissSplash = isDataLoaded && appDestination != 0

                val context = LocalContext.current

                LaunchedEffect(Unit) {
                    val permissions = getRequiredPermission()
                    val allGranted = permissions.all { perm ->
                        ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
                    }

                    appDestination = if (allGranted) 2 else 1
                }

                Box(modifier = Modifier.fillMaxSize()){
                    when(appDestination) {
                        0 -> {
                            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
                        }
                        1 -> {
                            PermissionScreen(
                                onPermissionGranted = {
                                    appDestination = 2
                                },
                                onPermissionDenied = { finish() }
                            )
                        }
                        2 -> {
                            HomeScreen(contactListViewModel = viewModel)
                        }
                    }
                    if(isSplashVisible) {
                        AnimatedSplashScreen(
                            animationTrigger = canDismissSplash,
                            onAnimationFinished = {
                                isSplashVisible = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KeepAliveTheme {
        Greeting("Android")
    }
}