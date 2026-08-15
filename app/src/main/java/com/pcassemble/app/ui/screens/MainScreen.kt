package com.pcassemble.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.pcassemble.app.PcAssembleApp
import com.pcassemble.app.ui.nav.Routes

@Composable
fun MainScreen(navController: NavHostController) {
    val context = LocalContext.current
    val repo = (context.applicationContext as PcAssembleApp).repository
    var tab by rememberSaveable { mutableIntStateOf(0) }

    // 等登录态恢复完成再决定是否跳登录页（避免已登录用户冷启动被强制重登）
    val sessionReady by repo.sessionReady.collectAsState()
    val token by repo.token.collectAsState()
    LaunchedEffect(sessionReady, token) {
        if (sessionReady && token == null) {
            navController.navigate(Routes.AUTH) { popUpTo(Routes.MAIN) { inclusive = true } }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text("首页") },
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Filled.Forum, contentDescription = null) },
                    label = { Text("社区") },
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    label = { Text("我的") },
                )
            }
        },
    ) { padding ->
        when (tab) {
            0 -> HomeScreen(navController, Modifier.padding(padding))
            1 -> CommunityScreen(navController, Modifier.padding(padding))
            else -> MineScreen(navController, Modifier.padding(padding))
        }
    }
}
