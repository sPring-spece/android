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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.pcassemble.app.PcAssembleApp
import com.pcassemble.app.data.IssueOut
import com.pcassemble.app.data.OrderPartIn
import com.pcassemble.app.ui.nav.Routes
import com.pcassemble.app.ui.viewmodel.BuilderSession
import com.pcassemble.app.ui.viewmodel.MineViewModel
import com.pcassemble.app.ui.viewmodel.appViewModel
import com.pcassemble.app.util.money
import com.pcassemble.app.util.partTypeLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendScreen(navController: NavHostController) {
    val context = LocalContext.current
    val repo = (context.applicationContext as PcAssembleApp).repository
    val vm: MineViewModel = appViewModel { MineViewModel(repo) }
    val recommend by vm.recommend.collectAsState()
    var budget by remember { mutableStateOf("5000") }

    val error by vm.error.collectAsState()
    val loading by vm.loading.collectAsState()
    LaunchedEffect(error) { error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("预算智能配机") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("输入预算，AI 自动搭配一套兼容配置", style = MaterialTheme.typography.titleSmall)
                        OutlinedTextField(value = budget, onValueChange = { budget = it },
                            label = { Text("预算（元）") }, singleLine = true,
                            modifier = Modifier.fillMaxWidth())
                        Button(onClick = { vm.recommend(budget) },
                            enabled = !loading,
                            modifier = Modifier.fillMaxWidth()) {
                            if (loading) CircularProgressIndicator(Modifier.height(20.dp))
                            else Text("生成配置单")
                        }
                    }
                }
            }

            recommend?.let { res ->
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("总价：${money(res.total_price)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f))
                        Text("功耗 ${res.total_power}W", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                items(res.parts, key = { it.type }) { p ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text(partTypeLabel(p.type), color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp))
                        Text(p.name, modifier = Modifier.weight(1f))
                        Text(money(p.price))
                    }
                }
                if (res.issues.isNotEmpty()) {
                    items(res.issues, key = { it.code + it.message }) { issue ->
                        RecommendIssue(issue)
                    }
                }
                item {
                    Button(
                        onClick = {
                            if (res.issues.any { it.level == "fatal" }) {
                                Toast.makeText(context, "存在致命兼容问题，无法下单", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            // 结果 → 选配器
                            BuilderSession.reset()
                            BuilderSession.parts = res.parts.map {
                                OrderPartIn(it.part_id, it.type, it.name, it.price, it.power_consumption, it.specs)
                            }
                            BuilderSession.totalPrice = res.total_price
                            BuilderSession.totalPower = res.total_power
                            BuilderSession.issues = res.issues
                            BuilderSession.configName = "智能配机（${budget} 元）"
                            navController.navigate(Routes.BUILDER)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("去选配器调整") }
                }
            }
        }
    }
}

@Composable
private fun RecommendIssue(issue: IssueOut) {
    val isFatal = issue.level == "fatal"
    val color = if (isFatal) Color(0xFFC62828) else Color(0xFFEF6C00)
    Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))) {
        Text(issue.message, Modifier.padding(12.dp), color = color)
    }
}
