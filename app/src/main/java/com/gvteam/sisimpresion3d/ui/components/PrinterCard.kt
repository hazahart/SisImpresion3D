package com.gvteam.sisimpresion3d.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gvteam.sisimpresion3d.model.Printer

@Composable
fun PrinterCard(printer: Printer) {
    val statusString = printer.status.toString().uppercase()

    val (colorFondo, colorTexto, textoEstado) = when (statusString) {
        "LIBRE", "DISPONIBLE" -> Triple(Color(0xFF4CAF50), Color(0xFF4CAF50), "Disponible")
        "OCUPADA" -> Triple(Color(0xFFF44336), Color(0xFFF44336), "Ocupada")
        "MANTENIMIENTO" -> Triple(Color(0xFFFF9800), Color(0xFFFF9800), "Mantenimiento")
        else -> Triple(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondary, statusString)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
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
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    color = colorFondo.copy(alpha = 0.15f),
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}