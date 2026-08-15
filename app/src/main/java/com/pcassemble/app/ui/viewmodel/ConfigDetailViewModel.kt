package com.pcassemble.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pcassemble.app.data.ConfigOut
import com.pcassemble.app.data.Network
import com.pcassemble.app.data.OrderPartIn
import com.pcassemble.app.data.Repository
import com.pcassemble.app.data.ValidatePartIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConfigDetailViewModel(private val repo: Repository) : ViewModel() {

    private val _config = MutableStateFlow<ConfigOut?>(null)
    val config = _config.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun load(id: Int) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _config.value = repo.configDetail(id)
            } catch (e: Exception) {
                _error.value = Network.errorMessage(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun favorite() = action { repo.favorite(it.id); "已收藏" }

    fun unfavorite() = action { repo.unfavorite(it.id); "已取消收藏" }

    fun clone(onSuccess: (ConfigOut) -> Unit) {
        val cfg = _config.value ?: return
        viewModelScope.launch {
            _loading.value = true
            try {
                val cloned = repo.cloneConfig(cfg.id)
                onSuccess(cloned)
            } catch (e: Exception) {
                _error.value = Network.errorMessage(e)
            } finally {
                _loading.value = false
            }
        }
    }

    /** 方案快照 → 选配器初始配件 */
    fun toOrderParts(): List<OrderPartIn> =
        _config.value?.parts?.map {
            OrderPartIn(
                part_id = null,
                type = it.part_type,
                name = it.part_name,
                price = it.part_price,
                power_consumption = it.part_power,
                specs = it.part_specs,
            )
        } ?: emptyList()

    fun toValidateParts(): List<ValidatePartIn> =
        _config.value?.parts?.map {
            ValidatePartIn(
                part_id = null,
                type = it.part_type,
                name = it.part_name,
                price = it.part_price,
                power_consumption = it.part_power,
                specs = it.part_specs,
            )
        } ?: emptyList()

    private inline fun action(crossinline block: suspend (ConfigOut) -> String) {
        val cfg = _config.value ?: return
        viewModelScope.launch {
            try {
                _message.value = block(cfg)
            } catch (e: Exception) {
                _error.value = Network.errorMessage(e)
            }
        }
    }
}
