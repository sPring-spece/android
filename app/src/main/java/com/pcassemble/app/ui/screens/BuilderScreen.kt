package com.pcassemble.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.pcassemble.app.PcAssembleApp
import com.pcassemble.app.data.IssueOut
import com.pcassemble.app.data.PartOut
import com.pcassemble.app.data.ValidatePartIn
import com.pcassemble.app.ui.nav.Routes
import com.pcassemble.app.ui.viewmodel.BuilderViewModel
import com.pcassemble.app.ui.viewmodel.appViewModel
import com.pcassemble.app.util.money
import com.pcassemble.app.util.partSpecSummary
import com.pcassemble.app.util.partTypeLabel

private val PART_ORDER = listOf("cpu", "motherboard", "gpu", "memory", "storage", "psu", "case", "cooler")

private val TYPE_COLORS = mapOf(
    "cpu" to Color(0xFF1565C0),
    "motherboard" to Color(0xFF00838F),
    "gpu" to Color(0xFF6A1B9A),
    "memory" to Color(0xFF2E7D32),
    "storage" to Color(0xFFEF6C00),
    "psu" to Color(0xFF5D4037),
    "case" to Color(0xFF546E7A),
    "cooler" to Color(0xFF37474F),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuilderScreen(navController: NavHostController) {
    val context = LocalContext.current
    val repo = (context.applicationContext as PcAssembleApp).repository
    val vm: BuilderViewModel = appViewModel { BuilderViewModel(repo) }
    val selected by vm.selected.collectAsState()
    val catalog by vm.catalog.collectAsState()
    val validate by vm.validate.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    LaunchedEffect(Unit) { vm.loadCatalog() }
    LaunchedEffect(error) { error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }

    var pickerType by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val fatalCount = validate?.issues?.count { it.level == "fatal" } ?: 0
    val warnCount = validate?.issues?.count { it.level == "warning" } ?: 0
    val issueColor = when {
        fatalCount > 0 -> Color(0xFFC62828)
        warnCount > 0 -> Color(0xFFEF6C00)
        selected.isNotEmpty() -> Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.outline
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DIY 装机") },
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
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.navigationBarsPadding(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            money(validate?.total_price ?: 0.0),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Text(
                            "已选 ${selected.size}/${PART_ORDER.size} 件 · 功耗 ${validate?.total_power ?: 0}W" +
                                if (fatalCount > 0) " · ${fatalCount} 个致命问题" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = issueColor,
                        )
                    }
                    Button(
                        onClick = { navController.navigate(Routes.ORDER_CONFIRM) },
                        enabled = selected.isNotEmpty() && !vm.hasFatal,
                    ) {
                        Text(if (vm.hasFatal) "存在不兼容" else "去下单")
                    }
                }
            }
        },
    ) { padding ->
        if (loading && catalog.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // 顶部状态卡
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = issueColor.copy(alpha = 0.08f),
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (fatalCount == 0 && warnCount == 0 && selected.isNotEmpty())
                                    Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                contentDescription = null,
                                tint = issueColor,
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    when {
                                        fatalCount > 0 -> "存在 $fatalCount 个不兼容项，无法下单"
                                        warnCount > 0 -> "有 $warnCount 个警告，建议调整"
                                        selected.isNotEmpty() -> "全部兼容，可以放心下单"
                                        else -> "选择配件开始组装"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = issueColor,
                                )
                                if (fatalCount > 0 || warnCount > 0) {
                                    Text(
                                        "点击下方配件槽位可更换",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline,
                                    )
                                }
                            }
                        }
                    }
                }

                // 已选配件槽位
                items(PART_ORDER) { type ->
                    val part = selected[type]
                    PartSlot(
                        type = type,
                        part = part,
                        onClick = { pickerType = type },
                        onRemove = { vm.remove(type) },
                    )
                }

                // 兼容性问题详情
                val v = validate
                if (v != null && v.issues.isNotEmpty()) {
                    item {
                        Text("兼容性检查（${v.issues.size} 项）", style = MaterialTheme.typography.titleSmall)
                    }
                    // 不设 key：不同规则可能产生相同 code+message，key 重复会导致 LazyColumn 崩溃
                    items(v.issues) { issue ->
                        IssueItem(issue)
                    }
                }
            }
        }
    }

    // 配件选择底部抽屉
    pickerType?.let { type ->
        val candidates = catalog[type] ?: emptyList()
        ModalBottomSheet(
            onDismissRequest = { pickerType = null },
            sheetState = sheetState,
        ) {
            Column(Modifier.padding(bottom = 16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "选择${partTypeLabel(type)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "${candidates.size} 款可选",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
                LazyColumn(Modifier.fillMaxWidth()) {
                    items(candidates, key = { it.id }) { part ->
                        PartPickerItem(
                            part = part,
                            imageUrl = repo.imageUrl(part.image),
                            specText = partSpecSummary(type, part.specs),
                            selected = selected[type]?.part_id == part.id,
                            onClick = {
                                vm.select(part)
                                pickerType = null
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PartSlot(type: String, part: ValidatePartIn?, onClick: () -> Unit, onRemove: () -> Unit) {
    val context = LocalContext.current
    val repo = (context.applicationContext as PcAssembleApp).repository
    val color = TYPE_COLORS[type] ?: MaterialTheme.colorScheme.primary

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (part == null) color.copy(alpha = 0.05f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 类型徽标
            Text(
                partTypeLabel(type),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
            Spacer(Modifier.width(12.dp))

            if (part == null) {
                Text(
                    "点击添加${partTypeLabel(type)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.Filled.Add, contentDescription = "添加", tint = color)
            } else {
                if (part.image != null) {
                    AsyncImage(
                        model = repo.imageUrl(part.image),
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        part.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val spec = partSpecSummary(type, part.specs)
                    if (spec.isNotBlank()) {
                        Text(
                            spec,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Text(
                    money(part.price),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Close, contentDescription = "移除", tint = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
private fun PartPickerItem(
    part: PartOut,
    imageUrl: String?,
    specText: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = part.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    part.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (specText.isNotBlank()) {
                    Text(
                        specText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                part.brand?.let {
                    Text(
                        "$it · 功耗 ${part.power_consumption}W",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
            Text(
                money(part.price),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.width(8.dp))
            if (selected) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "已选",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
            modifier = Modifier.padding(horizontal = 20.dp),
        )
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
