package com.pcassemble.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.pcassemble.app.PcAssembleApp
import com.pcassemble.app.data.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 服务器地址设置页：电脑 IP 变化时在这里改地址，无需重装 App */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val repo = (context.applicationContext as PcAssembleApp).repository
    val currentUrl by repo.serverUrl.collectAsState()
    val scope = rememberCoroutineScope()

    var input by remember { mutableStateOf("") }
    var testing by remember { mutableStateOf(false) }

    LaunchedEffect(currentUrl) {
        if (input.isBlank()) input = currentUrl ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("服务器设置") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "后端地址可在此修改。电脑 IP 变化或切换后端时，改完点保存即可，无需重新安装 App。",
                style = MaterialTheme.typography.bodyMedium,
            )
            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("当前地址", style = MaterialTheme.typography.titleSmall)
                    Text(
                        currentUrl ?: "未设置",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("服务器地址") },
                placeholder = { Text("http://192.168.1.101:8000/api/") },
                supportingText = { Text("可省略 http://，会自动补全；必须以 / 结尾") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedButton(
                onClick = {
                    if (input.isBlank()) {
                        Toast.makeText(context, "请输入地址", Toast.LENGTH_SHORT).show()
                        return@OutlinedButton
                    }
                    val url = normalizeInput(input)
                    testing = true
                    scope.launch {
                        val ok = withContext(Dispatchers.IO) { Network.checkHealth(url) }
                        testing = false
                        Toast.makeText(
                            context,
                            if (ok) "连接成功 ✅" else "连接失败：请检查后端是否 --host 0.0.0.0 启动、手机与电脑同网络",
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                },
                enabled = !testing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (testing) {
                    CircularProgressIndicator(Modifier.height(18.dp))
                } else {
                    Text("测试连接")
                }
            }
            Button(
                onClick = {
                    if (input.isBlank()) {
                        Toast.makeText(context, "请输入地址", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    repo.reconfigureServer(normalizeInput(input))
                    Toast.makeText(context, "已保存，立即生效", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("保存并应用")
            }
        }
    }
}

/** 用户输入规范化：补 http:// 前缀与结尾斜杠 */
private fun normalizeInput(raw: String): String {
    var url = raw.trim()
    if (!url.contains("://")) url = "http://$url"
    return Network.normalizeBaseUrl(url)
}
