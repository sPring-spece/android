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
import androidx.compose.material3.HorizontalDivider
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
import com.pcassemble.app.ui.viewmodel.CommunityViewModel
import com.pcassemble.app.ui.viewmodel.appViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(navController: NavHostController, postId: Int) {
    val context = LocalContext.current
    val repo = (context.applicationContext as PcAssembleApp).repository
    val vm: CommunityViewModel = appViewModel { CommunityViewModel(repo) }
    val post by vm.post.collectAsState()
    var comment by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadPost(postId) }
    val message by vm.message.collectAsState()
    LaunchedEffect(message) { message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }
    val error by vm.error.collectAsState()
    LaunchedEffect(error) { error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("帖子详情") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        val p = post
        if (p == null) {
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
                Column {
                    Text(p.title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(4.dp))
                    Text("${p.author_nickname ?: "匿名"} · ${p.create_time} · 浏览 ${p.view_count}",
                        style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Text(p.content, style = MaterialTheme.typography.bodyLarge)
                }
                HorizontalDivider()
                Text("评论（${p.comments.size}）", style = MaterialTheme.typography.titleMedium)
                p.comments.forEach { c ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(c.content, style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(4.dp))
                            Text("${c.author_nickname ?: "匿名"} · ${c.create_time}",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                if (p.comments.isEmpty()) {
                    Text("暂无评论，来抢沙发", style = MaterialTheme.typography.bodySmall)
                }
                HorizontalDivider()
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = comment, onValueChange = { comment = it },
                        placeholder = { Text("写下你的评论") },
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                    )
                    Spacer(Modifier.height(0.dp))
                    Button(
                        onClick = {
                            if (comment.isBlank()) return@Button
                            vm.comment(postId, comment)
                            comment = ""
                        },
                        modifier = Modifier.padding(start = 8.dp),
                    ) { Text("发送") }
                }
            }
        }
    }
}
