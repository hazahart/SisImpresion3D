package com.gvteam.sisimpresion3d.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomScreen(val route: String, val title: String, val icon: ImageVector) {

    object Home : BottomScreen(
        route = "home",
        title = "Impresoras",
        icon = Icons.Default.Home
    )

    object Insumos : BottomScreen(
        route = "insumos",
        title = "Insumos",
        icon = Icons.Default.Inventory
    )

    object Costos : BottomScreen(
        route = "costos",
        title = "Costos y presupuestos",
        icon = Icons.Default.AttachMoney
    )
}