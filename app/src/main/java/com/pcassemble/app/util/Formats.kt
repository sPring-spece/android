package com.pcassemble.app.util

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

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

/** 从 specs(JsonObject) 安全取值：支持字符串与数组（DDR4/DDR5 合并展示） */
fun specValue(specs: JsonObject?, key: String): String? {
    val el = specs?.get(key) ?: return null
    return when (el) {
        is JsonPrimitive -> el.contentOrNull
        is JsonArray -> el.joinToString("/") { (it as? JsonPrimitive)?.contentOrNull ?: "" }
        else -> null
    }
}

/** 配件关键规格摘要（DIY 页面展示用） */
fun partSpecSummary(type: String, specs: JsonObject?): String {
    if (specs == null) return ""
    val keys = when (type) {
        "cpu" -> listOf("socket", "cores")
        "motherboard" -> listOf("socket", "formFactor", "memoryType")
        "memory" -> listOf("memoryType", "capacity")
        "gpu" -> listOf("capacity", "interface")
        "storage" -> listOf("capacity")
        "psu" -> listOf("power")
        "case" -> listOf("formFactor")
        "cooler" -> listOf("socket")
        else -> emptyList()
    }
    val items = keys.mapNotNull { specValue(specs, it) }.filter { it.isNotBlank() }
    return items.joinToString(" · ").take(48)
}
