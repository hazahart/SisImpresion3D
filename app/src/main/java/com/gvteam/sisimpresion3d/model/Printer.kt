package com.gvteam.sisimpresion3d.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PrinterStatus {
    @SerialName("libre") LIBRE,
    @SerialName("ocupada") OCUPADA,
    @SerialName("mantenimiento") MANTENIMIENTO
}

@Serializable
data class Printer(
    val id: Long,
    val name: String,
    val model: String,
    val location: String? = null,
    val status: PrinterStatus = PrinterStatus.LIBRE,
    @SerialName("current_order_id") val currentOrderId: String? = null
)