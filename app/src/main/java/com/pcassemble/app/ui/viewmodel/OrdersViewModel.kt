package com.pcassemble.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pcassemble.app.data.Network
import com.pcassemble.app.data.OrderOut
import com.pcassemble.app.data.ReceiverIn
import com.pcassemble.app.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** 下单确认与订单管理 */
class OrdersViewModel(private val repo: Repository) : ViewModel() {

    private val _submitting = MutableStateFlow(false)
    val submitting = _submitting.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _orders = MutableStateFlow<List<OrderOut>>(emptyList())
    val orders = _orders.asStateFlow()

    private val _order = MutableStateFlow<OrderOut?>(null)
    val order = _order.asStateFlow()

    fun createOrder(parts: List<com.pcassemble.app.data.OrderPartIn>, receiver: ReceiverIn, remark: String?, onSuccess: (OrderOut) -> Unit) {
        viewModelScope.launch {
            _submitting.value = true
            _error.value = null
            try {
                val order = repo.createOrder(parts, receiver, remark)
                onSuccess(order)
            } catch (e: Exception) {
                _error.value = Network.errorMessage(e)
            } finally {
                _submitting.value = false
            }
        }
    }

    fun loadOrders() {
        viewModelScope.launch {
            try {
                _orders.value = repo.orders()
            } catch (e: Exception) {
                _error.value = Network.errorMessage(e)
            }
        }
    }

    fun loadOrder(id: Int) {
        viewModelScope.launch {
            try {
                _order.value = repo.orderDetail(id)
            } catch (e: Exception) {
                _error.value = Network.errorMessage(e)
            }
        }
    }

    fun pay(id: Int, onSuccess: () -> Unit) = action(id, onSuccess) { repo.payOrder(it) }

    fun cancel(id: Int, onSuccess: () -> Unit) = action(id, onSuccess) { repo.cancelOrder(it) }

    fun confirm(id: Int, onSuccess: () -> Unit) = action(id, onSuccess) { repo.confirmOrder(it) }

    private inline fun action(
        id: Int,
        crossinline onSuccess: () -> Unit,
        crossinline block: suspend (Int) -> OrderOut,
    ) {
        viewModelScope.launch {
            try {
                _order.value = block(id)
                onSuccess()
            } catch (e: Exception) {
                _error.value = Network.errorMessage(e)
            }
        }
    }
}
