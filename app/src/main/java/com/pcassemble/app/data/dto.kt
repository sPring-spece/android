package com.pcassemble.app.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/** 与后端 schema 对应的 DTO（字段名对齐后端 snake_case） */

// ---------- 认证 ----------
@Serializable
data class RegisterRequest(val username: String, val password: String, val nickname: String? = null)

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class UserOut(
    val id: Int,
    val username: String,
    val nickname: String? = null,
    val avatar: String? = null,
    val phone: String? = null,
)

@Serializable
data class Token(val access_token: String, val token_type: String = "bearer", val user: UserOut)

// ---------- 配件 ----------
@Serializable
data class PartOut(
    val id: Int,
    val type: String,
    val name: String,
    val brand: String? = null,
    val price: Double,
    val image: String? = null,
    val power_consumption: Int = 0,
    val specs: JsonObject? = null,
    val sales: Int = 0,
) {
    fun specText(key: String): String? = specs?.get(key)?.jsonPrimitive?.contentOrNull
}

@Serializable
data class PartListOut(val total: Int, val page: Int, val page_size: Int, val items: List<PartOut>)

// ---------- 装机方案 ----------
@Serializable
data class ConfigPartOut(
    val part_type: String,
    val part_name: String,
    val part_brand: String? = null,
    val part_price: Double,
    val part_power: Int = 0,
    val part_specs: JsonObject? = null,
)

@Serializable
data class ConfigOut(
    val id: Int,
    val name: String,
    val level: String = "mainstream",
    val level_text: String? = null,
    val description: String? = null,
    val price: Double,
    val sales: Int = 0,
    val is_official: Int = 0,
    val parts: List<ConfigPartOut> = emptyList(),
)

@Serializable
data class ConfigListOut(val total: Int, val page: Int, val page_size: Int, val items: List<ConfigOut>)

// ---------- 选配校验 / 分享 / 推荐 ----------
@Serializable
data class ValidatePartIn(
    val part_id: Int? = null,
    val type: String,
    val name: String = "",
    val price: Double = 0.0,
    val power_consumption: Int = 0,
    val specs: JsonObject? = null,
)

@Serializable
data class ValidateRequest(val parts: List<ValidatePartIn>)

@Serializable
data class IssueOut(
    val code: String,
    val level: String,
    val message: String,
    val parts: List<String> = emptyList(),
)

@Serializable
data class ValidateResponse(val total_price: Double, val total_power: Int, val issues: List<IssueOut>)

@Serializable
data class ConfigShareRequest(
    val name: String,
    val level: String = "mainstream",
    val description: String? = null,
    val parts: List<ValidatePartIn>,
)

@Serializable
data class RecommendResponse(
    val parts: List<ValidatePartIn> = emptyList(),
    val total_price: Double = 0.0,
    val total_power: Int = 0,
    val issues: List<IssueOut> = emptyList(),
)

// ---------- 订单 ----------
@Serializable
data class OrderPartIn(
    val part_id: Int? = null,
    val type: String,
    val name: String,
    val price: Double = 0.0,
    val power_consumption: Int = 0,
    val specs: JsonObject? = null,
)

@Serializable
data class ReceiverIn(val name: String, val phone: String, val address: String)

@Serializable
data class OrderCreate(val parts: List<OrderPartIn>, val receiver: ReceiverIn, val remark: String? = null)

@Serializable
data class OrderItemOut(
    val part_id: Int? = null,
    val part_type: String,
    val part_name: String,
    val part_brand: String? = null,
    val part_price: Double,
    val part_power: Int = 0,
)

@Serializable
data class OrderOut(
    val id: Int,
    val order_no: String,
    val total_price: Double,
    val receiver_name: String,
    val receiver_phone: String,
    val receiver_address: String,
    val remark: String? = null,
    val status: String,
    val create_time: String,
    val pay_time: String? = null,
    val ship_time: String? = null,
    val complete_time: String? = null,
    val cancel_time: String? = null,
    val items: List<OrderItemOut> = emptyList(),
)

@Serializable
data class OrderListOut(val total: Int, val page: Int, val page_size: Int, val items: List<OrderOut>)

// ---------- 收藏 ----------
@Serializable
data class FavoriteOut(val id: Int, val create_time: String, val config: ConfigOut)

@Serializable
data class FavoriteListOut(val total: Int, val page: Int, val page_size: Int, val items: List<FavoriteOut>)

// ---------- 社区 ----------
@Serializable
data class PostCreate(val title: String, val content: String)

@Serializable
data class CommentCreate(val content: String)

@Serializable
data class CommentOut(
    val id: Int,
    val user_id: Int,
    val author_nickname: String? = null,
    val content: String,
    val create_time: String,
)

@Serializable
data class PostOut(
    val id: Int,
    val user_id: Int,
    val author_nickname: String? = null,
    val title: String,
    val content: String,
    val view_count: Int = 0,
    val create_time: String,
    val update_time: String,
    val comments: List<CommentOut> = emptyList(),
)

@Serializable
data class PostListOut(val total: Int, val page: Int, val page_size: Int, val items: List<PostOut>)

// ---------- 咨询 ----------
@Serializable
data class ConsultationCreate(
    val content: String,
    val contact: String? = null,
    val appointment_date: String? = null,
)

@Serializable
data class ConsultationOut(
    val id: Int,
    val user_id: Int,
    val content: String,
    val contact: String? = null,
    val appointment_date: String? = null,
    val status: String,
    val reply: String? = null,
    val reply_time: String? = null,
    val create_time: String,
)

@Serializable
data class ConsultationListOut(val total: Int, val page: Int, val page_size: Int, val items: List<ConsultationOut>)

// ---------- 错误 ----------
@Serializable
data class ApiError(val detail: String? = null)
