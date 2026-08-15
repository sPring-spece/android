package com.pcassemble.app.util

/** 配件类型英文 → 中文展示 */
val PART_TYPE_LABELS = mapOf(
    "cpu" to "CPU",
    "motherboard" to "主板",
    "gpu" to "显卡",
    "memory" to "内存",
    "storage" to "硬盘",
    "psu" to "电源",
    "case" to "机箱",
    "cooler" to "散热器",
)

fun partTypeLabel(type: String): String = PART_TYPE_LABELS[type] ?: type

/** 订单状态 → 中文 */
fun orderStatusLabel(status: String): String = when (status) {
    "pending" -> "待支付"
    "paid" -> "装机中"
    "shipped" -> "已发货"
    "completed" -> "已完成"
    "cancelled" -> "已取消"
    else -> status
}

/** 价位段 → 中文 */
fun levelLabel(level: String): String = when (level) {
    "entry" -> "入门"
    "mainstream" -> "主流"
    "high" -> "高端"
    "flagship" -> "发烧"
    else -> level
}

/** 金额格式化：1234.5 -> 1,234.50 元 */
fun money(price: Double): String = String.format("¥%,.2f", price)
