package com.gvteam.sisimpresion3d.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,

    @SerialName("full_name")
    val fullName: String? = null,

    val email: String? = null,

    val role: String? = "student",

    @SerialName("avatar_url")
    val avatarUrl: String? = null,

    val info: String? = "¡Hola! Estoy usando SisImpresión 3D.",

    @SerialName("is_external")
    val isExternal: Boolean = false,

    @SerialName("control_number")
    val controlNumber: String? = null,

    val career: String? = null,
    val semester: String? = null
)