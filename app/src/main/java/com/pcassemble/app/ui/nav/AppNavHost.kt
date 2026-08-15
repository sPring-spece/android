package com.pcassemble.app.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pcassemble.app.ui.screens.AuthScreen
import com.pcassemble.app.ui.screens.BuilderScreen
import com.pcassemble.app.ui.screens.CommunityScreen
import com.pcassemble.app.ui.screens.ConfigDetailScreen
import com.pcassemble.app.ui.screens.ConsultScreen
import com.pcassemble.app.ui.screens.FavoritesScreen
import com.pcassemble.app.ui.screens.MainScreen
import com.pcassemble.app.ui.screens.MineScreen
import com.pcassemble.app.ui.screens.NewPostScreen
import com.pcassemble.app.ui.screens.OrderConfirmScreen
import com.pcassemble.app.ui.screens.OrderDetailScreen
import com.pcassemble.app.ui.screens.OrderListScreen
import com.pcassemble.app.ui.screens.PostDetailScreen
import com.pcassemble.app.ui.screens.RecommendScreen
import com.pcassemble.app.ui.screens.SettingsScreen

object Routes {
    const val AUTH = "auth"
    const val MAIN = "main"
    const val BUILDER = "builder"
    const val ORDER_CONFIRM = "order_confirm"
    const val ORDER_LIST = "order_list"
    const val FAVORITES = "favorites"
    const val CONSULT = "consult"
    const val NEW_POST = "new_post"
    const val RECOMMEND = "recommend"
    const val SETTINGS = "settings"

    // 带参数路由的注册模式（含占位符）
    const val CONFIG = "config/{configId}"
    const val ORDER = "order/{orderId}"
    const val POST = "post/{postId}"

    // 实际导航路径（带真实 id）
    fun config(id: Int) = "config/$id"
    fun order(id: Int) = "order/$id"
    fun post(id: Int) = "post/$id"
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.MAIN) {
        composable(Routes.AUTH) { AuthScreen(navController) }
        composable(Routes.MAIN) { MainScreen(navController) }
        composable(Routes.BUILDER) { BuilderScreen(navController) }
        composable(Routes.ORDER_CONFIRM) { OrderConfirmScreen(navController) }
        composable(Routes.ORDER_LIST) { OrderListScreen(navController) }
        composable(Routes.FAVORITES) { FavoritesScreen(navController) }
        composable(Routes.CONSULT) { ConsultScreen(navController) }
        composable(Routes.NEW_POST) { NewPostScreen(navController) }
        composable(Routes.RECOMMEND) { RecommendScreen(navController) }
        composable(Routes.SETTINGS) { SettingsScreen(navController) }
        composable(
            Routes.CONFIG,
            arguments = listOf(navArgument("configId") { type = NavType.IntType }),
        ) { entry ->
            ConfigDetailScreen(navController, entry.arguments?.getInt("configId") ?: 0)
        }
        composable(
            Routes.ORDER,
            arguments = listOf(navArgument("orderId") { type = NavType.IntType }),
        ) { entry ->
            OrderDetailScreen(navController, entry.arguments?.getInt("orderId") ?: 0)
        }
        composable(
            Routes.POST,
            arguments = listOf(navArgument("postId") { type = NavType.IntType }),
        ) { entry ->
            PostDetailScreen(navController, entry.arguments?.getInt("postId") ?: 0)
        }
    }
}
