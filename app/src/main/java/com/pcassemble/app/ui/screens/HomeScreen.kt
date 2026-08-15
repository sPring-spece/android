package com.pcassemble.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.pcassemble.app.ui.viewmodel.HomeViewModel
import com.pcassemble.app.ui.viewmodel.appViewModel
import com.pcassemble.app.util.levelLabel
import com.pcassemble.app.util.money

private val LEVELS = listOf(null, "entry", "mainstream", "high", "flagship")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repo = (context.applicationContext as PcAssembleApp).repository
    val vm: HomeViewModel = appViewModel { HomeViewModel(repo) }
    val configs by vm.configs.collectAsState()
    val selectedLevel by vm.selectedLevel.collectAsState()
    val loading by vm.loading.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("装机助手") }) },
        modifier = modifier,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 预算配机入口
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(Routes.RECOMMEND) },
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("预算智能配机", style = MaterialTheme.typography.titleMedium)
                            Text("输入预算，自动生成兼容配置单", style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }

            // DIY 装机入口
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(Routes.BUILDER) },
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Build, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("DIY 组装", style = MaterialTheme.typography.titleMedium)
                            Text("从零开始挑选配件，实时兼容性校验", style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }

            // 价位段筛选
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val labels = mapOf<String?, String>(null to "全部", "entry" to "入门", "mainstream" to "主流", "high" to "高端", "flagship" to "发烧")
                    LEVELS.forEach { level ->
                        FilterChip(
                            selected = selectedLevel == level,
                            onClick = { vm.selectLevel(level) },
                            label = { Text(labels[level] ?: level ?: "") },
                        )
                    }
                }
            }

            if (loading) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            items(configs, key = { it.id }) { config ->
                ConfigCard(config = config, onClick = { navController.navigate(Routes.config(config.id)) })
            }
        }
    }
}

@Composable
fun ConfigCard(config: ConfigOut, onClick: () -> Unit) {
    val context = LocalContext.current
    val repo = (context.applicationContext as PcAssembleApp).repository
    Card(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)) {
        Column {
            if (config.image != null) {
                AsyncImage(
                    model = repo.imageUrl(config.image),
                    contentDescription = config.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(config.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Text(levelLabel(config.level), style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(4.dp))
                config.description?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                Spacer(Modifier.height(8.dp))
                Row {
                    Text(money(config.price), style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.weight(1f))
                    Text("${config.parts.size} 件配件 · 销量 ${config.sales}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
