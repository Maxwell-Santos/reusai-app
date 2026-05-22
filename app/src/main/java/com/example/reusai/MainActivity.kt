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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.reusai.data.network.RetrofitClient
import com.example.reusai.data.network.TokenManager
import com.example.reusai.data.repository.ItemRepository
import com.example.reusai.ui.screens.CreateItemScreen
import com.example.reusai.ui.screens.HomeScreen
import com.example.reusai.ui.screens.ItemDetailsScreen
import com.example.reusai.ui.screens.LoginScreen
import com.example.reusai.ui.screens.ProfileScreen
import com.example.reusai.ui.screens.RegisterScreen
import com.example.reusai.ui.screens.TradeOfferScreen
import com.example.reusai.ui.theme.ReusaiTheme
import com.example.reusai.ui.viewmodels.DetailsViewModel
import com.example.reusai.ui.viewmodels.HomeViewModel
import com.example.reusai.ui.viewmodels.ProfileViewModel
import com.example.reusai.ui.viewmodels.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(this)
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

    val repository = ItemRepository(RetrofitClient.instance)
    val tokenManager = RetrofitClient.getTokenManager() ?: TokenManager(LocalContext.current)
    val factory = ViewModelFactory(repository, tokenManager)

    val authRoutes = listOf(AppDestinations.LOGIN.route, AppDestinations.REGISTER.route)
    // Show bottom bar only on main screens, hide on Details, Login, Register
    val showBottomBar = currentRoute != null &&
                       currentRoute !in authRoutes &&
                       !currentRoute.startsWith(AppDestinations.DETAILS.route)

    val bottomNavItems = listOf(
        AppDestinations.HOME,
        AppDestinations.PROPOSALS,
        AppDestinations.PUBLISH,
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
                            imageVector = item.icon,
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
                val homeViewModel: HomeViewModel = viewModel(factory = factory)
                HomeScreen(
                    viewModel = homeViewModel,
                    onItemClick = { itemId ->
                        navController.navigate("${AppDestinations.DETAILS.route}/$itemId")
                    }
                )
            }

            composable(
                route = "${AppDestinations.DETAILS.route}/{itemId}",
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                val detailsViewModel: DetailsViewModel = viewModel(factory = factory)
                ItemDetailsScreen(
                    itemId = itemId,
                    viewModel = detailsViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onOfferTrade = {
                        navController.navigate("${AppDestinations.TRADE_OFFER.route}/$itemId")
                    }
                )
            }

            composable(
                route = "${AppDestinations.TRADE_OFFER.route}/{itemId}",
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                val detailsViewModel: DetailsViewModel = viewModel(factory = factory)
                TradeOfferScreen(
                    itemId = itemId,
                    viewModel = detailsViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onTradeSuccess = {
                        // For now just go back, maybe show a snackbar later
                        navController.popBackStack()
                    }
                )
            }

            composable(AppDestinations.PROFILE.route) {
                val profileViewModel: ProfileViewModel = viewModel(factory = factory)
                ProfileScreen(
                    viewModel = profileViewModel,
                    onAddNewItem = { navController.navigate(AppDestinations.PUBLISH.route) },
                    onLogoutSuccess = {
                        navController.navigate(AppDestinations.LOGIN.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onSettingsClick = {},
                    onSeeAllReviews = {},
                    onEditItem = { itemId ->
                        navController.navigate("${AppDestinations.PUBLISH.route}?isEditMode=true&itemId=$itemId")
                    }
                )
            }

            composable(
                route = "${AppDestinations.PUBLISH.route}?isEditMode={isEditMode}&itemId={itemId}",
                arguments = listOf(
                    navArgument("isEditMode") {
                        type = NavType.BoolType
                        defaultValue = false
                    },
                    navArgument("itemId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val isEditMode = backStackEntry.arguments?.getBoolean("isEditMode") ?: false
                val itemId = backStackEntry.arguments?.getString("itemId")

                CreateItemScreen(
                    isEditMode = isEditMode,
                    itemId = itemId,
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
    val icon: ImageVector,
) {
    HOME("home", "Início", Icons.Default.Home),
    DETAILS("details", "Detalhes", Icons.Default.Info),
    PROPOSALS("proposals", "Propostas", Icons.Default.SwapHoriz),
    PUBLISH("publish", "Publicar", Icons.Default.AddCircle),
    CHAT("chat", "Chat", Icons.AutoMirrored.Filled.Chat),
    PROFILE("profile", "Perfil", Icons.Default.Person),
    REGISTER("register", "Cadastro", Icons.Default.PersonAdd),
    LOGIN("login", "Login", Icons.AutoMirrored.Filled.Login),
    TRADE_OFFER("trade_offer", "Oferecer Troca", Icons.Default.SwapHoriz)
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(
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
