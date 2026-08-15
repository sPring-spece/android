package com.pcassemble.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.pcassemble.app.data.PartOut
import com.pcassemble.app.data.ValidatePartIn
import com.pcassemble.app.ui.nav.Routes
import com.pcassemble.app.ui.viewmodel.BuilderViewModel
import com.pcassemble.app.ui.viewmodel.appViewModel
import com.pcassemble.app.util.money
import com.pcassemble.app.util.partTypeLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuilderScreen(navController: NavHostController) {
    val context = LocalContext.current
    val repo = (context.applicationContext as PcAssembleApp).repository
    val vm: BuilderViewModel = appViewModel { BuilderViewModel(repo) }
    val selected by vm.selected.collectAsState()
    val catalog by vm.catalog.collectAsState()
    val validate by vm.validate.collectAsState()

    LaunchedEffect(Unit) { vm.loadCatalog() }
    val error by vm.error.collectAsState()
    LaunchedEffect(error) { error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }

    var pickerType by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("在线选配") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(onClick = { vm.reset() }) { Text("清空") }
                },
            )
        },
        bottomBar = {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("总价：${money(validate?.total_price ?: 0.0)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error)
                            Text("整机功耗：${validate?.total_power ?: 0}W",
                                style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = { navController.navigate(Routes.ORDER_CONFIRM) },
                            enabled = selected.isNotEmpty() && !vm.hasFatal,
                        ) {
                            Text(if (vm.hasFatal) "存在不兼容" else "去下单")
                        }
                    }
                }
            }
        },
    ) { padding ->
        val loading by vm.loading.collectAsState()
        if (loading && catalog.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // 已选配件
                items(PART_ORDER) { type ->
                    val part = selected[type]
                    PartSlot(
                        type = type,
                        part = part,
                        onClick = { pickerType = type },
                        onRemove = { vm.remove(type) },
                    )
                }
                // 兼容性问题
                val v = validate
                if (v != null && v.issues.isNotEmpty()) {
                    item { Text("兼容性检查（${v.issues.size} 项）", style = MaterialTheme.typography.titleSmall) }
                    items(v.issues, key = { it.code + it.message }) { issue ->
                        IssueItem(issue)
                    }
                }
                if (v != null && v.issues.isEmpty() && selected.isNotEmpty()) {
                    item {
                        Text("✅ 全部兼容，可以放心下单", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }

    // 配件选择弹窗
    pickerType?.let { type ->
        val candidates = catalog[type] ?: emptyList()
        AlertDialog(
            onDismissRequest = { pickerType = null },
            title = { Text("选择${partTypeLabel(type)}") },
            text = {
                LazyColumn(Modifier.height(360.dp)) {
                    items(candidates) { part ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { vm.select(part); pickerType = null }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(part.name, style = MaterialTheme.typography.bodyLarge)
                                part.brand?.let {
                                    Text("$it · 功耗 ${part.power_consumption}W",
                                        style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Text(money(part.price), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { pickerType = null }) { Text("取消") } },
        )
    }
}

private val PART_ORDER = listOf("cpu", "motherboard", "gpu", "memory", "storage", "psu", "case", "cooler")

@Composable
private fun PartSlot(type: String, part: ValidatePartIn?, onClick: () -> Unit, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                partTypeLabel(type),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(56.dp),
            )
            if (part == null) {
                Text("未选择，点击添加", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.Add, contentDescription = "添加")
            } else {
                Column(Modifier.weight(1f)) {
                    Text(part.name, style = MaterialTheme.typography.bodyLarge)
                    Text(money(part.price), style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error)
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Close, contentDescription = "移除")
                }
            }
        }
    }
}

@Composable
private fun IssueItem(issue: IssueOut) {
    val isFatal = issue.level == "fatal"
    val color = if (isFatal) Color(0xFFC62828) else Color(0xFFEF6C00)
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = color)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    if (isFatal) "【致命】${issue.message}" else "【警告】${issue.message}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = color,
                )
            }
        }
    }
}
