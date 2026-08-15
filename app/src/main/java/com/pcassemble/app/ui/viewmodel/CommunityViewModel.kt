package com.pcassemble.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pcassemble.app.data.Network
import com.pcassemble.app.data.PostOut
import com.pcassemble.app.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommunityViewModel(private val repo: Repository) : ViewModel() {

    private val _posts = MutableStateFlow<List<PostOut>>(emptyList())
    val posts = _posts.asStateFlow()

    private val _post = MutableStateFlow<PostOut?>(null)
    val post = _post.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun loadPosts(keyword: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _posts.value = repo.posts(keyword)
            } catch (e: Exception) {
                _error.value = Network.errorMessage(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadPost(id: Int) {
        viewModelScope.launch {
            try {
                _post.value = repo.postDetail(id)
            } catch (e: Exception) {
                _error.value = Network.errorMessage(e)
            }
        }
    }

    fun createPost(title: String, content: String, onSuccess: (PostOut) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                onSuccess(repo.createPost(title, content))
            } catch (e: Exception) {
                _error.value = Network.errorMessage(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun comment(postId: Int, content: String) {
        viewModelScope.launch {
            try {
                repo.createComment(postId, content)
                _message.value = "评论成功"
                loadPost(postId)
            } catch (e: Exception) {
                _error.value = Network.errorMessage(e)
            }
        }
    }
}
