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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.pcassemble.app.PcAssembleApp
import com.pcassemble.app.ui.viewmodel.OrdersViewModel
import com.pcassemble.app.ui.viewmodel.appViewModel
import com.pcassemble.app.util.money
import com.pcassemble.app.util.orderStatusLabel
import com.pcassemble.app.util.partTypeLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(navController: NavHostController, orderId: Int) {
    val context = LocalContext.current
    val repo = (context.applicationContext as PcAssembleApp).repository
    val vm: OrdersViewModel = appViewModel { OrdersViewModel(repo) }
    val order by vm.order.collectAsState()

    LaunchedEffect(Unit) { vm.loadOrder(orderId) }
    val error by vm.error.collectAsState()
    LaunchedEffect(error) { error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("订单详情") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        val o = order
        if (o == null) {
            CircularProgressIndicator(Modifier.padding(padding).padding(24.dp))
        } else {
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
                        Text(orderStatusLabel(o.status), style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary)
                        Text("订单号：${o.order_no}", style = MaterialTheme.typography.bodySmall)
                        Text("下单时间：${o.create_time}", style = MaterialTheme.typography.bodySmall)
                        o.pay_time?.let { Text("支付时间：$it", style = MaterialTheme.typography.bodySmall) }
                        o.ship_time?.let { Text("发货时间：$it", style = MaterialTheme.typography.bodySmall) }
                    }
                }
                Card {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("收货信息", style = MaterialTheme.typography.titleSmall)
                        Text("${o.receiver_name} ${o.receiver_phone}")
                        Text(o.receiver_address)
                        o.remark?.let { Text("备注：$it", style = MaterialTheme.typography.bodySmall) }
                    }
                }
                Card {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("配件清单", style = MaterialTheme.typography.titleSmall)
                        o.items.forEach { item ->
                            Row(Modifier.fillMaxWidth()) {
                                Text(partTypeLabel(item.part_type), color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 8.dp))
                                Text(item.part_name, modifier = Modifier.weight(1f))
                                Text(money(item.part_price))
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("合计：${money(o.total_price)}", style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error)
                    }
                }

                when (o.status) {
                    "pending" -> {
                        Button(onClick = { vm.pay(o.id) {
                            Toast.makeText(context, "支付成功", Toast.LENGTH_SHORT).show()
                        } }, modifier = Modifier.fillMaxWidth()) {
                            Text("模拟支付")
                        }
                        OutlinedButton(onClick = { vm.cancel(o.id) {
                            Toast.makeText(context, "订单已取消", Toast.LENGTH_SHORT).show()
                        } }, modifier = Modifier.fillMaxWidth()) {
                            Text("取消订单")
                        }
                    }
                    "shipped" -> {
                        Button(onClick = { vm.confirm(o.id) {
                            Toast.makeText(context, "确认收货成功", Toast.LENGTH_SHORT).show()
                        } }, modifier = Modifier.fillMaxWidth()) {
                            Text("确认收货")
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}
