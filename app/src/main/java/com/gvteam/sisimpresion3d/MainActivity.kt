package com.gvteam.sisimpresion3d

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gvteam.sisimpresion3d.ui.AppNavigation
import com.gvteam.sisimpresion3d.ui.theme.SisImpresion3dTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SisImpresion3dTheme {
                AppNavigation()
            }
        }
    }
}