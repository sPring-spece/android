package com.pcassemble.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import com.pcassemble.app.data.OrderPartIn
import com.pcassemble.app.data.IssueOut

/** 选配会话：Builder 页写入，下单确认页读取（跨页面共享，无需导航传参） */
object BuilderSession {
    var parts: List<OrderPartIn> = emptyList()
    var totalPrice: Double = 0.0
    var totalPower: Int = 0
    var issues: List<IssueOut> = emptyList()
    var configName: String = "我的配置单"

    fun reset() {
        parts = emptyList()
        totalPrice = 0.0
        totalPower = 0
        issues = emptyList()
        configName = "我的配置单"
    }
}

/** 通用 ViewModel 工厂：页面里 appViewModel { XxxViewModel(App.repository) } */
@Composable
inline fun <reified VM : ViewModel> appViewModel(crossinline create: () -> VM): VM =
    viewModel(factory = viewModelFactory { initializer { create() } })
