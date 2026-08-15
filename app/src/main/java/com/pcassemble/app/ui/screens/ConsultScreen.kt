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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.pcassemble.app.PcAssembleApp
import com.pcassemble.app.ui.viewmodel.MineViewModel
import com.pcassemble.app.ui.viewmodel.appViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultScreen(navController: NavHostController) {
    val context = LocalContext.current
    val repo = (context.applicationContext as PcAssembleApp).repository
    val vm: MineViewModel = appViewModel { MineViewModel(repo) }
    val consults by vm.consults.collectAsState()
    var content by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadConsults() }
    val error by vm.error.collectAsState()
    LaunchedEffect(error) { error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("在线咨询") },
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
                        Text("提交装机咨询，工程师会尽快回复", style = MaterialTheme.typography.titleSmall)
                        OutlinedTextField(value = content, onValueChange = { content = it },
                            label = { Text("咨询内容") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = contact, onValueChange = { contact = it },
                            label = { Text("联系方式（选填）") }, singleLine = true,
                            modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = date, onValueChange = { date = it },
                            label = { Text("预约日期（选填，如 2026-09-01）") }, singleLine = true,
                            modifier = Modifier.fillMaxWidth())
                        Button(
                            onClick = {
                                if (content.isBlank()) {
                                    Toast.makeText(context, "请输入咨询内容", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                vm.submitConsult(content, contact, date) {
                                    Toast.makeText(context, "提交成功", Toast.LENGTH_SHORT).show()
                                    content = ""; contact = ""; date = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("提交咨询") }
                    }
                }
            }
            item { Text("我的咨询记录", style = MaterialTheme.typography.titleMedium) }
            if (consults.isEmpty()) {
                item { Text("暂无咨询记录", style = MaterialTheme.typography.bodySmall) }
            }
            items(consults, key = { it.id }) { c ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(c.content, style = MaterialTheme.typography.bodyLarge)
                        Text(c.create_time, style = MaterialTheme.typography.bodySmall)
                        if (c.status == "handled") {
                            Row {
                                Text("工程师回复：", color = MaterialTheme.colorScheme.primary)
                                Text(c.reply ?: "")
                            }
                        } else {
                            Text("待处理", color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }
        }
    }
}
