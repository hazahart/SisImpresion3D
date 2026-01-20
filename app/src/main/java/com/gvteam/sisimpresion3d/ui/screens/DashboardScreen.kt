package com.gvteam.sisimpresion3d.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gvteam.sisimpresion3d.ui.components.PrinterCard
import com.gvteam.sisimpresion3d.viewmodel.PrinterViewModel

@OptIn(ExperimentalMaterial3Api::class) // Necesario para la TopAppBar moderna
@Composable
fun DashboardScreen(viewModel: PrinterViewModel) {
    val printers by viewModel.printers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // 1. Box Raíz con el gradiente (Para que cubra toda la pantalla, incluyendo detrás de la barra)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                )
            )
    ) {
        // 2. Scaffold: Estructura base para AppBar y contenido
        Scaffold(
            // Hacemos el Scaffold transparente para que se vea tu gradiente del Box
            containerColor = Color.Transparent,

            // 3. Configuración de la Barra Superior Centrada
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Impresión 3D - Sistemas computacionales",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent, // Barra transparente
                        titleContentColor = Color.White // Texto blanco
                    )
                )
            }
        ) { innerPadding ->
            // 4. Contenido principal (respetando el padding de la barra)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // ¡Importante! Evita que el contenido quede bajo la barra
                    .padding(horizontal = 20.dp) // Margen lateral
            ) {

                // Subtítulo
                Text(
                    text = "Panel de Control - Industria 4.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF38BDF8))
                    }
                } else if (errorMessage != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "$errorMessage", color = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.fetchPrinters() }) {
                            Text("Reintentar")
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(printers) { printer ->
                            PrinterCard(printer = printer)
                        }
                    }
                }
            }
        }
    }
}