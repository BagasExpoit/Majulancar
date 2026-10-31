package id.biz.majulancar.kasir.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.biz.majulancar.kasir.ui.screens.AboutScreen
import id.biz.majulancar.kasir.ui.screens.CartScreen
import id.biz.majulancar.kasir.ui.screens.MenuScreen
import id.biz.majulancar.kasir.ui.screens.OmzetScreen

private enum class Dest(val route: String, val label: String, val emoji: String) {
    Menu("menu", "Menu", "🍜"),
    Cart("keranjang", "Keranjang", "🛒"),
    Omzet("omzet", "Omzet", "📊"),
    About("tentang", "Tentang", "ℹ️"),
}

private fun NavController.goTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppRoot(vm: AppViewModel, darkTheme: Boolean) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val snackbar = remember { SnackbarHostState() }
    val cart by vm.cart.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()
    val cartCount = cart.sumOf { it.quantity }

    LaunchedEffect(Unit) {
        vm.messages.collect { message ->
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(message)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { TopBar(darkTheme = darkTheme, onToggleTheme = { vm.setDarkTheme(!darkTheme) }) },
        bottomBar = {
            NavigationBar {
                Dest.values().forEach { dest ->
                    NavigationBarItem(
                        selected = currentRoute == dest.route,
                        onClick = { if (currentRoute != dest.route) navController.goTab(dest.route) },
                        icon = {
                            if (dest == Dest.Cart && cartCount > 0) {
                                BadgedBox(badge = { Badge { Text(if (cartCount > 99) "99+" else cartCount.toString()) } }) {
                                    Text(dest.emoji, fontSize = 22.sp)
                                }
                            } else {
                                Text(dest.emoji, fontSize = 22.sp)
                            }
                        },
                        label = { Text(dest.label) },
                    )
                }
            }
        },
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            NavHost(
                navController = navController,
                startDestination = Dest.Menu.route,
                modifier = Modifier.padding(padding),
            ) {
                composable(Dest.Menu.route) {
                    MenuScreen(vm, onOpenCart = { navController.goTab(Dest.Cart.route) })
                }
                composable(Dest.Cart.route) {
                    CartScreen(vm, onGoToMenu = { navController.goTab(Dest.Menu.route) })
                }
                composable(Dest.Omzet.route) { OmzetScreen(vm) }
                composable(Dest.About.route) { AboutScreen() }
            }
        }
    }
}

@Composable
private fun TopBar(darkTheme: Boolean, onToggleTheme: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "🍜 Maju Lancar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            FilledTonalIconButton(
                onClick = onToggleTheme,
                modifier = Modifier.semantics {
                    contentDescription = if (darkTheme) "Ganti ke tema terang" else "Ganti ke tema gelap"
                },
            ) {
                Text(if (darkTheme) "☀️" else "🌙", fontSize = 20.sp)
            }
        }
    }
}
