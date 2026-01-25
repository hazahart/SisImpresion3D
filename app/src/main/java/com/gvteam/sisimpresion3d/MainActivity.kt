package com.gvteam.sisimpresion3d

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gvteam.sisimpresion3d.ui.AppNavigation
import com.gvteam.sisimpresion3d.ui.theme.SisImpresion3dTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            SisImpresion3dTheme {
                AppNavigation()
            }
        }
    }
}