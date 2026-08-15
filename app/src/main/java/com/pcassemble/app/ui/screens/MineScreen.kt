package com.pcassemble.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.pcassemble.app.PcAssembleApp
import com.pcassemble.app.ui.nav.Routes
import com.pcassemble.app.ui.viewmodel.MineViewModel
import com.pcassemble.app.ui.viewmodel.appViewModel

@Composable
fun MineScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repo = (context.applicationContext as PcAssembleApp).repository
    val vm: MineViewModel = appViewModel { MineViewModel(repo) }
    val user by repo.currentUser.collectAsState()

    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text(user?.nickname ?: user?.username ?: "未登录",
                        style = MaterialTheme.typography.headlineSmall)
                    Text("@${user?.username ?: "-"}", style = MaterialTheme.typography.bodySmall)
                }
            }

            MenuItem(Icons.Filled.AutoAwesome, "预算智能配机", "输入预算自动生成配置单") {
                navController.navigate(Routes.RECOMMEND)
            }
            MenuItem(Icons.Filled.ReceiptLong, "我的订单", "查看订单状态与物流") {
                navController.navigate(Routes.ORDER_LIST)
            }
            MenuItem(Icons.Filled.Favorite, "我的收藏", "收藏的配置单") {
                navController.navigate(Routes.FAVORITES)
            }
            MenuItem(Icons.Filled.Headset, "在线咨询", "装机问题找工程师") {
                navController.navigate(Routes.CONSULT)
            }
            MenuItem(Icons.Filled.Settings, "服务器设置", "后端地址变了在这里改，无需重装") {
                navController.navigate(Routes.SETTINGS)
            }
            MenuItem(Icons.Filled.Logout, "退出登录", "清除本地登录状态") {
                vm.logout {
                    Toast.makeText(context, "已退出登录", Toast.LENGTH_SHORT).show()
                    navController.navigate(Routes.AUTH) { popUpTo(Routes.MAIN) { inclusive = true } }
                }
            }
        }
    }
}

@Composable
private fun MenuItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(desc, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}
