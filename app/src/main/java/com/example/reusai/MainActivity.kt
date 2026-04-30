package com.example.reusai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reusai.ui.screens.CreateItemScreen
import com.example.reusai.ui.screens.LoginScreen
import com.example.reusai.ui.screens.ProfileScreen
import com.example.reusai.ui.screens.RegisterScreen
import com.example.reusai.ui.theme.ReusaiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReusaiTheme {
                ReusaiApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun ReusaiApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    val bottomNavItems = listOf(
        AppDestinations.HOME,
        AppDestinations.PROPOSALS,
        AppDestinations.CHAT,
        AppDestinations.PROFILE
    )

    NavigationSuiteScaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        navigationSuiteItems = {
            bottomNavItems.forEach {
                item(
                    icon = {
                        Icon(
                            painter = painterResource(it.icon),
                            contentDescription = it.label,
                            modifier = Modifier.size(26.dp)
                        )
                    },
                    label = {
                        Text(
                            it.label,
                            fontSize = 10.sp
                        )
                    },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        when (currentDestination) {

            AppDestinations.PROFILE -> {
                ProfileScreen(
                    onAddNewItem = { currentDestination = AppDestinations.PUBLISH },
                    onSettingsClick = {},
                    onSeeAllReviews = {},
                    onEditItem = {}
                )
            }

            AppDestinations.PUBLISH -> {
                CreateItemScreen(
                    onNavigateBack = { currentDestination = AppDestinations.PROFILE },
                    onPublish = { currentDestination = AppDestinations.PROFILE }
                )
            }

            AppDestinations.REGISTER -> {
                RegisterScreen(
                    onNavigateBack = { currentDestination = AppDestinations.LOGIN },
                    onLoginClick = { currentDestination = AppDestinations.LOGIN }
                )
            }

            AppDestinations.LOGIN -> {
                LoginScreen(
                    onLoginSuccess = { currentDestination = AppDestinations.PROFILE },
                    onSignUpClick = { currentDestination = AppDestinations.REGISTER },
                    onForgotPasswordClick = {}
                )
            }

            else -> {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Greeting(
                        name = currentDestination.label,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Início", R.drawable.ic_home),
    PROPOSALS("Propostas", R.drawable.ic_favorite),
    CHAT("Chat", R.drawable.ic_account_box),
    PROFILE("Perfil", R.drawable.ic_account_box),
    PUBLISH("Publicar", R.drawable.ic_favorite),
    REGISTER("Cadastro", R.drawable.ic_account_box),
    LOGIN("Login", R.drawable.ic_account_box)
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
    ReusaiTheme {
        Greeting("Android")
    }
}