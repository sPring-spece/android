package com.pcassemble.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pcassemble.app.data.Network
import com.pcassemble.app.data.Repository
import com.pcassemble.app.data.UserOut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repo: Repository) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _user = MutableStateFlow<UserOut?>(null)
    val user = _user.asStateFlow()

    fun login(username: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _user.value = repo.login(username, password)
                onSuccess()
            } catch (e: Exception) {
                _error.value = Network.errorMessage(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun register(username: String, password: String, nickname: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _user.value = repo.register(username, password, nickname.ifBlank { null })
                onSuccess()
            } catch (e: Exception) {
                _error.value = Network.errorMessage(e)
            } finally {
                _loading.value = false
            }
        }
    }
}
