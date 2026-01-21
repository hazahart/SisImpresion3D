package com.gvteam.sisimpresion3d

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.gvteam.sisimpresion3d.ui.AppNavigation
import com.gvteam.sisimpresion3d.ui.screens.DashboardScreen
import com.gvteam.sisimpresion3d.viewmodel.PrinterViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                AppNavigation()
            }
        }
    }
}