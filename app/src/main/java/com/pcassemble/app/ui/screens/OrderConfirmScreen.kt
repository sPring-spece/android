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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.pcassemble.app.PcAssembleApp
import com.pcassemble.app.data.ReceiverIn
import com.pcassemble.app.ui.nav.Routes
import com.pcassemble.app.ui.viewmodel.BuilderSession
import com.pcassemble.app.ui.viewmodel.OrdersViewModel
import com.pcassemble.app.ui.viewmodel.appViewModel
import com.pcassemble.app.util.money
import com.pcassemble.app.util.partTypeLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderConfirmScreen(navController: NavHostController) {
    val context = LocalContext.current
    val repo = (context.applicationContext as PcAssembleApp).repository
    val vm: OrdersViewModel = appViewModel { OrdersViewModel(repo) }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }

    val error by vm.error.collectAsState()
    val submitting by vm.submitting.collectAsState()
    LaunchedEffect(error) { error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }

    val parts = BuilderSession.parts
    val total = BuilderSession.totalPrice

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("确认订单") },
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
            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("配件清单（${parts.size} 件）", style = MaterialTheme.typography.titleSmall)
                    parts.forEach { p ->
                        Row(Modifier.fillMaxWidth()) {
                            Text(partTypeLabel(p.type), color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp))
                            Text(p.name, modifier = Modifier.weight(1f))
                            Text(money(p.price))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("合计：${money(total)}", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error)
                }
            }

            OutlinedTextField(value = name, onValueChange = { name = it },
                label = { Text("收货人") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = phone, onValueChange = { phone = it },
                label = { Text("手机号") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = address, onValueChange = { address = it },
                label = { Text("收货地址") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = remark, onValueChange = { remark = it },
                label = { Text("备注（选填）") }, singleLine = true, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank() || address.isBlank()) {
                        Toast.makeText(context, "请填写完整收货信息", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (!Regex("^1\\d{10}$").matches(phone)) {
                        Toast.makeText(context, "手机号格式不正确", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    vm.createOrder(parts, ReceiverIn(name, phone, address), remark.ifBlank { null }) { order ->
                        Toast.makeText(context, "下单成功", Toast.LENGTH_SHORT).show()
                        BuilderSession.reset()
                        navController.navigate(Routes.order(order.id)) {
                            popUpTo(Routes.MAIN)
                        }
                    }
                },
                enabled = parts.isNotEmpty() && !submitting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (submitting) {
                    CircularProgressIndicator(Modifier.height(20.dp))
                } else {
                    Text("提交订单（${money(total)}）")
                }
            }
        }
    }
}
