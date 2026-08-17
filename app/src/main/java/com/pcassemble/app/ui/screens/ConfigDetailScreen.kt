package com.pcassemble.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.pcassemble.app.PcAssembleApp
import com.pcassemble.app.data.ConfigOut
import com.pcassemble.app.ui.nav.Routes
import com.pcassemble.app.ui.viewmodel.ConfigDetailViewModel
import com.pcassemble.app.ui.viewmodel.BuilderSession
import com.pcassemble.app.ui.viewmodel.appViewModel
import com.pcassemble.app.util.levelLabel
import com.pcassemble.app.util.money
import com.pcassemble.app.util.partTypeLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigDetailScreen(navController: NavHostController, configId: Int) {
    val context = LocalContext.current
    val repo = (context.applicationContext as PcAssembleApp).repository
    val vm: ConfigDetailViewModel = appViewModel { ConfigDetailViewModel(repo) }
    val config by vm.config.collectAsState()

    LaunchedEffect(Unit) { vm.load(configId) }
    val error by vm.error.collectAsState()
    LaunchedEffect(error) { error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }
    val message by vm.message.collectAsState()
    LaunchedEffect(message) { message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("方案详情") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        val cfg = config
        if (cfg == null) {
            CircularProgressIndicator(Modifier.padding(padding).padding(24.dp))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Column {
                        if (cfg.image != null) {
                            AsyncImage(
                                model = repo.imageUrl(cfg.image),
                                contentDescription = cfg.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(4f / 3f)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop,
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                        Text(cfg.name, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${levelLabel(cfg.level)} · ${cfg.parts.size} 件配件 · 销量 ${cfg.sales}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        cfg.description?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(it, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(money(cfg.price), style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error)
                    }
                }
                // 不设 key：不同配件可能产生相同的 part_type+part_name（如同一型号出现两次），key 重复会导致 LazyColumn 崩溃
                items(cfg.parts) { part ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (part.part_image != null) {
                            AsyncImage(
                                model = repo.imageUrl(part.part_image),
                                contentDescription = part.part_name,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                            )
                            Spacer(Modifier.width(10.dp))
                        }
                        Text(
                            partTypeLabel(part.part_type),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(64.dp),
                        )
                        Text(part.part_name, modifier = Modifier.weight(1f))
                        Text(money(part.part_price), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                // 方案快照 → 选配器初始状态
                                BuilderSession.reset()
                                vm.toValidateParts().let { parts ->
                                    BuilderSession.parts = parts.map { it.toOrderPartIn() }
                                    BuilderSession.configName = cfg.name
                                    BuilderSession.totalPrice = cfg.price
                                }
                                navController.navigate(Routes.BUILDER)
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Filled.Build, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("按此方案选配")
                        }
                        OutlinedButton(
                            onClick = { vm.favorite() },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Filled.FavoriteBorder, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("收藏")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { vm.clone { cloned ->
                            Toast.makeText(context, "已克隆到我的配置单", Toast.LENGTH_SHORT).show()
                            navController.navigate(Routes.config(cloned.id))
                        } },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.Favorite, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("克隆为我的配置单")
                    }
                }
            }
        }
    }
}

private fun com.pcassemble.app.data.ValidatePartIn.toOrderPartIn() =
    com.pcassemble.app.data.OrderPartIn(
        part_id = part_id, type = type, name = name,
        price = price, power_consumption = power_consumption, specs = specs,
    )
