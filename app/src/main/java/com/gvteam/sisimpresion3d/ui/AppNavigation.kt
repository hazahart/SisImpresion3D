package com.gvteam.sisimpresion3d.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gvteam.sisimpresion3d.data.SupabaseClient
import com.gvteam.sisimpresion3d.data.repository.MaterialRepository
import com.gvteam.sisimpresion3d.ui.screens.MainScreen
import com.gvteam.sisimpresion3d.viewmodel.MaterialViewModel
import com.gvteam.sisimpresion3d.viewmodel.PrinterViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            val viewModelFactory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return when {
                        modelClass.isAssignableFrom(PrinterViewModel::class.java) -> {
                            PrinterViewModel() as T
                        }

                        modelClass.isAssignableFrom(MaterialViewModel::class.java) -> {
                            val repository = MaterialRepository(SupabaseClient.client)
                            MaterialViewModel(repository) as T
                        }

                        else -> throw IllegalArgumentException("ViewModel desconocido")
                    }
                }
            }

            val printerViewModel: PrinterViewModel = viewModel(factory = viewModelFactory)
            val materialViewModel: MaterialViewModel = viewModel(factory = viewModelFactory)

            MainScreen(
                printerViewModel = printerViewModel,
                materialViewModel = materialViewModel
            )
        }
    }
}