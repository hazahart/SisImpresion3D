package com.gvteam.sisimpresion3d.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gvteam.sisimpresion3d.model.Printer
import com.gvteam.sisimpresion3d.model.PrinterStatus

@Composable
fun PrinterCard(printer: Printer) {
    val (colorFondo, colorTexto, textoEstado) = when (printer.status) {
        PrinterStatus.LIBRE -> Triple(Color(0xFF4CAF50), Color.Green, "Disponible")
        PrinterStatus.OCUPADA -> Triple(Color(0xFFF44336), Color.Red, "Ocupada")
        PrinterStatus.MANTENIMIENTO -> Triple(Color(0xFFFF9800), Color.Yellow, "Mantenimiento")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = printer.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )

                // status
                Surface(
                    color = colorFondo.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, colorTexto.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = textoEstado,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = colorTexto,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${printer.model} • ${printer.location ?: "Sin ubicación"}",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}