package com.pcassemble.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.pcassemble.app.PcAssembleApp
import com.pcassemble.app.ui.nav.Routes
import com.pcassemble.app.ui.viewmodel.AuthViewModel
import com.pcassemble.app.ui.viewmodel.appViewModel

@Composable
fun AuthScreen(navController: NavHostController) {
    val context = LocalContext.current
    val repo = (context.applicationContext as PcAssembleApp).repository
    val vm: AuthViewModel = appViewModel { AuthViewModel(repo) }

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }

    val error by vm.error.collectAsState()
    LaunchedEffect(error) {
        error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))
        Text("装机助手", style = MaterialTheme.typography.headlineLarge)
        Text("电脑装机服务平台", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(32.dp))

        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("登录") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("注册") })
        }
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = username, onValueChange = { username = it },
            label = { Text("用户名") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("密码") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
        )
        if (tab == 1) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = nickname, onValueChange = { nickname = it },
                label = { Text("昵称（选填）") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(24.dp))

        val loading by vm.loading.collectAsState()
        if (loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "请输入用户名和密码", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val go = { navController.navigate(Routes.MAIN) { popUpTo(Routes.AUTH) { inclusive = true } } }
                    if (tab == 0) vm.login(username, password, go) else vm.register(username, password, nickname, go)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (tab == 0) "登录" else "注册并登录")
            }
        }
    }
}
