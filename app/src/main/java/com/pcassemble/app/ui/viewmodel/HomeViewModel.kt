package com.pcassemble.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pcassemble.app.data.ConfigOut
import com.pcassemble.app.data.Network
import com.pcassemble.app.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: Repository) : ViewModel() {

    private val _configs = MutableStateFlow<List<ConfigOut>>(emptyList())
    val configs = _configs.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _selectedLevel = MutableStateFlow<String?>(null)
    val selectedLevel = _selectedLevel.asStateFlow()

    init {
        load()
    }

    fun selectLevel(level: String?) {
        _selectedLevel.value = level
        load()
    }

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _configs.value = repo.configs(scope = "official", level = _selectedLevel.value)
            } catch (e: Exception) {
                _error.value = Network.errorMessage(e)
            } finally {
                _loading.value = false
            }
        }
    }
}
