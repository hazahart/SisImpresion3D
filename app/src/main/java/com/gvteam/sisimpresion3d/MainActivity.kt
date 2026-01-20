package com.gvteam.sisimpresion3d

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gvteam.sisimpresion3d.ui.screens.DashboardScreen
import com.gvteam.sisimpresion3d.viewmodel.PrinterViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Instanciamos el ViewModel
        val viewModel = PrinterViewModel()

        setContent {
            // Llamamos a nuestra pantalla principal
            DashboardScreen(viewModel = viewModel)
        }
    }
}