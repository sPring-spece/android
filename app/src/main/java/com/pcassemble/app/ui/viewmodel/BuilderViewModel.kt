package com.pcassemble.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pcassemble.app.data.IssueOut
import com.pcassemble.app.data.Network
import com.pcassemble.app.data.OrderPartIn
import com.pcassemble.app.data.PartOut
import com.pcassemble.app.data.Repository
import com.pcassemble.app.data.ValidatePartIn
import com.pcassemble.app.data.ValidateResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** 选配器：加载配件库 → 按类型选择/替换 → 实时调用后端校验（总价/功耗/兼容性问题） */
class BuilderViewModel(private val repo: Repository) : ViewModel() {

    private val _catalog = MutableStateFlow<Map<String, List<PartOut>>>(emptyMap())
    val catalog = _catalog.asStateFlow()

    private val _selected = MutableStateFlow<Map<String, ValidatePartIn>>(emptyMap())
    val selected = _selected.asStateFlow()

    private val _validate = MutableStateFlow<ValidateResponse?>(null)
    val validate = _validate.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadCatalog() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val all = repo.parts()
                _catalog.value = all.groupBy { it.type }
                // 从共享会话恢复（方案详情进入时已预填）
                if (_selected.value.isEmpty() && BuilderSession.parts.isNotEmpty()) {
                    _selected.value = BuilderSession.parts.associate { it.type to it.toValidatePartIn() }
                    runValidate()
                }
            } catch (e: Exception) {
                _error.value = Network.errorMessage(e)
            } finally {
                _loading.value = false
            }
        }
    }

    /** 从方案快照初始化选配器 */
    fun initWithSnapshot(parts: List<ValidatePartIn>) {
        _selected.value = parts.associateBy { it.type }
        runValidate()
    }

    fun select(part: PartOut) {
        _selected.value = _selected.value + (part.type to part.toValidatePartIn())
        runValidate()
    }

    fun remove(type: String) {
        _selected.value = _selected.value - type
        runValidate()
    }

    private fun runValidate() {
        val parts = _selected.value.values.toList()
        if (parts.isEmpty()) {
            _validate.value = null
            BuilderSession.parts = emptyList()
            BuilderSession.totalPrice = 0.0
            BuilderSession.totalPower = 0
            BuilderSession.issues = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                val res = repo.validate(parts)
                _validate.value = res
                // 同步到共享会话，供下单确认页使用
                BuilderSession.parts = parts.map { it.toOrderPartIn() }
                BuilderSession.totalPrice = res.total_price
                BuilderSession.totalPower = res.total_power
                BuilderSession.issues = res.issues
            } catch (e: Exception) {
                _error.value = Network.errorMessage(e)
            }
        }
    }

    fun reset() {
        _selected.value = emptyMap()
        _validate.value = null
        BuilderSession.reset()
    }

    val hasFatal: Boolean get() = _validate.value?.issues?.any { it.level == "fatal" } == true
}

fun PartOut.toValidatePartIn() = ValidatePartIn(
    part_id = id,
    type = type,
    name = name,
    price = price,
    power_consumption = power_consumption,
    specs = specs,
)

fun ValidatePartIn.toOrderPartIn() = OrderPartIn(
    part_id = part_id,
    type = type,
    name = name,
    price = price,
    power_consumption = power_consumption,
    specs = specs,
)

fun OrderPartIn.toValidatePartIn() = ValidatePartIn(
    part_id = part_id,
    type = type,
    name = name,
    price = price,
    power_consumption = power_consumption,
    specs = specs,
)

fun ValidatePartIn.toIssueParts(): List<String> = listOf(name)
