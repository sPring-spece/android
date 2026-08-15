package com.pcassemble.app.ui.screens

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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.pcassemble.app.data.OrderOut
import com.pcassemble.app.ui.nav.Routes
import com.pcassemble.app.ui.viewmodel.OrdersViewModel
import com.pcassemble.app.ui.viewmodel.appViewModel
import com.pcassemble.app.util.money
import com.pcassemble.app.util.orderStatusLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(navController: NavHostController) {
    val context = LocalContext.current
    val repo = (context.applicationContext as PcAssembleApp).repository
    val vm: OrdersViewModel = appViewModel { OrdersViewModel(repo) }
    val orders by vm.orders.collectAsState()

    LaunchedEffect(Unit) { vm.loadOrders() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的订单") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        if (orders.isEmpty()) {
            Text("暂无订单", Modifier.padding(padding).padding(24.dp))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(orders, key = { it.id }) { order ->
                    OrderCard(order) { navController.navigate(Routes.order(order.id)) }
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: OrderOut, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Row {
                Text("订单号 ${order.order_no}", style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f))
                Text(orderStatusLabel(order.status),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(4.dp))
            Text("${order.items.size} 件配件 · ${order.create_time}", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))
            Text(money(order.total_price), style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error)
        }
    }
}
