package com.gvteam.sisimpresion3d.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gvteam.sisimpresion3d.data.SupabaseClient
import com.gvteam.sisimpresion3d.data.repository.MaterialRepository
import com.gvteam.sisimpresion3d.ui.screens.LoginScreen
import com.gvteam.sisimpresion3d.ui.screens.MainScreen
import com.gvteam.sisimpresion3d.viewmodel.MaterialViewModel
import com.gvteam.sisimpresion3d.viewmodel.PrinterViewModel
import io.github.jan.supabase.auth.auth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var isCheckingSession by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf("login") }

    LaunchedEffect(Unit) {
        val sessionRestored = try {
            SupabaseClient.client.auth.loadFromStorage()
        } catch (e: Exception) {
            false
        }
        startDestination = if (sessionRestored) "main" else "login"
        isCheckingSession = false
    }

    if (isCheckingSession) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        NavHost(navController = navController, startDestination = startDestination) {
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

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
}