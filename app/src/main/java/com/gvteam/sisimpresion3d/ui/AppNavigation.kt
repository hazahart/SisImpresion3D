package com.gvteam.sisimpresion3d.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gvteam.sisimpresion3d.ui.screens.DashboardScreen
import com.gvteam.sisimpresion3d.viewmodel.PrinterViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            val viewModel: PrinterViewModel = viewModel()
            DashboardScreen(viewModel = viewModel)
        }
    }
}