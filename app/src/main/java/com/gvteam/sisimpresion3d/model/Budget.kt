package com.gvteam.sisimpresion3d.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Budget(
    val id: Long = 0,
    @SerialName("user_id") val userId: String,
    @SerialName("client_name") val clientName: String,
    @SerialName("project_name") val projectName: String,
    @SerialName("total_cost") val totalCost: Double,
    val grams: Double,
    @SerialName("print_time_hours") val printTimeHours: Double,
    @SerialName("is_urgent") val isUrgent: Boolean,
    @SerialName("delivery_date") val deliveryDate: String?,
    val notes: String?,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class BudgetInsert(
    @SerialName("user_id") val userId: String,
    @SerialName("client_name") val clientName: String,
    @SerialName("project_name") val projectName: String,
    @SerialName("total_cost") val totalCost: Double,
    val grams: Double,
    @SerialName("print_time_hours") val printTimeHours: Double,
    @SerialName("is_urgent") val isUrgent: Boolean,
    @SerialName("delivery_date") val deliveryDate: String?,
    val notes: String?
)