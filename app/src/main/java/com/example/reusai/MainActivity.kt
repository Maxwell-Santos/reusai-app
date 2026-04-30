package com.example.reusai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.reusai.ui.screens.CreateItemScreen
import com.example.reusai.ui.screens.HomeScreen
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

@Composable
fun ReusaiApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val authRoutes = listOf(AppDestinations.LOGIN.route, AppDestinations.REGISTER.route)
    val showBottomBar = currentRoute !in authRoutes && currentRoute != null

    val bottomNavItems = listOf(
        AppDestinations.HOME,
        AppDestinations.PROPOSALS,
        AppDestinations.CHAT,
        AppDestinations.PROFILE
    )

    NavigationSuiteScaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        layoutType = if (showBottomBar) {
            NavigationSuiteType.NavigationBar
        } else {
            NavigationSuiteType.None
        },
        navigationSuiteItems = {
            bottomNavItems.forEach { item ->
                item(
                    icon = {
                        Icon(
                            painter = painterResource(item.icon),
                            contentDescription = item.label,
                            modifier = Modifier.size(26.dp)
                        )
                    },
                    label = {
                        Text(
                            item.label,
                            fontSize = 10.sp
                        )
                    },
                    selected = currentRoute == item.route,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(AppDestinations.HOME.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = AppDestinations.LOGIN.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(AppDestinations.LOGIN.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(AppDestinations.HOME.route) {
                            popUpTo(AppDestinations.LOGIN.route) { inclusive = true }
                        }
                    },
                    onSignUpClick = {
                        navController.navigate(AppDestinations.REGISTER.route)
                    }
                )
            }

            composable(AppDestinations.REGISTER.route) {
                RegisterScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLoginClick = { navController.popBackStack() }
                )
            }

            composable(AppDestinations.HOME.route) {
                HomeScreen()
            }

            composable(AppDestinations.PROFILE.route) {
                ProfileScreen(
                    onAddNewItem = { navController.navigate(AppDestinations.PUBLISH.route) },
                    onSettingsClick = {},
                    onSeeAllReviews = {},
                    onEditItem = {}
                )
            }

            composable(AppDestinations.PUBLISH.route) {
                CreateItemScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onPublish = { navController.popBackStack() }
                )
            }

            composable(AppDestinations.PROPOSALS.route) {
                Greeting(name = "Propostas")
            }

            composable(AppDestinations.CHAT.route) {
                Greeting(name = "Chat")
            }
        }
    }
}

enum class AppDestinations(
    val route: String,
    val label: String,
    val icon: Int,
) {
    HOME("home", "Início", R.drawable.ic_home),
    PROPOSALS("proposals", "Propostas", R.drawable.ic_favorite),
    CHAT("chat", "Chat", R.drawable.ic_account_box),
    PROFILE("profile", "Perfil", R.drawable.ic_account_box),
    PUBLISH("publish", "Publicar", R.drawable.ic_favorite),
    REGISTER("register", "Cadastro", R.drawable.ic_account_box),
    LOGIN("login", "Login", R.drawable.ic_account_box)
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