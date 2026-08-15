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
import androidx.compose.material.icons.filled.Edit
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
import com.pcassemble.app.data.PostOut
import com.pcassemble.app.ui.nav.Routes
import com.pcassemble.app.ui.viewmodel.CommunityViewModel
import com.pcassemble.app.ui.viewmodel.appViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repo = (context.applicationContext as PcAssembleApp).repository
    val vm: CommunityViewModel = appViewModel { CommunityViewModel(repo) }
    val posts by vm.posts.collectAsState()
    var keyword by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadPosts() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("装机社区") },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.NEW_POST) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "发帖")
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = keyword,
                onValueChange = {
                    keyword = it
                    vm.loadPosts(it.ifBlank { null })
                },
                placeholder = { Text("搜索帖子标题") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            if (posts.isEmpty()) {
                Text("暂无帖子，点击右上角发帖", Modifier.padding(24.dp))
            } else {
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(posts, key = { it.id }) { post ->
                        PostCard(post) { navController.navigate(Routes.post(post.id)) }
                    }
                }
            }
        }
    }
}

@Composable
fun PostCard(post: PostOut, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Text(post.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(post.content, style = MaterialTheme.typography.bodyMedium,
                maxLines = 2)
            Spacer(Modifier.height(8.dp))
            Text(
                "${post.author_nickname ?: "匿名"} · ${post.create_time} · 浏览 ${post.view_count} · ${post.comments.size} 评论",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
