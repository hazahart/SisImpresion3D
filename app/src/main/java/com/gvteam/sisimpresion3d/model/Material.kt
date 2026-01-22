package com.gvteam.sisimpresion3d.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Material(
    val id: String,

    val type: String,
    val brand: String,
    val color: String,

    @SerialName("color_hex")
    val colorHex: String = "#FFFFFF",

    @SerialName("initial_weight_g")
    val initialWeight: Int = 1000,

    @SerialName("remaining_weight_g")
    val remainingWeight: Int = 1000,

    @SerialName("cost_per_unit")
    val cost: Double = 0.0,

    @SerialName("is_active")
    val isActive: Boolean = true
) {
    val percentageLeft: Float
        get() = if (initialWeight > 0) remainingWeight.toFloat() / initialWeight.toFloat() else 0f
}