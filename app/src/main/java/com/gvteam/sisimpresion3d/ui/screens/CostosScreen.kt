package com.gvteam.sisimpresion3d.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gvteam.sisimpresion3d.viewmodel.BudgetViewModel

@Composable
fun CostosScreen(
    paddingValues: PaddingValues,
    viewModel: BudgetViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.saveMessage) {
        state.saveMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Nuevo Presupuesto",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Detalles del Cliente",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = state.projectName,
                    onValueChange = { viewModel.onProjectNameChange(it) },
                    label = { Text("Nombre del Proyecto / Pieza") },
                    leadingIcon = { Icon(Icons.Default.Inventory2, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    )
                )

                OutlinedTextField(
                    value = state.clientName,
                    onValueChange = { viewModel.onClientNameChange(it) },
                    label = { Text("Nombre del Cliente") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Cálculo de Costos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InputNumber(
                        state.grams,
                        { viewModel.onGramsChange(it) },
                        "Peso (g)",
                        Icons.Default.Scale,
                        Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InputNumber(
                        state.hours,
                        { viewModel.onHoursChange(it) },
                        "Horas",
                        Icons.Default.Timer,
                        Modifier.weight(1f)
                    )
                    InputNumber(
                        state.minutes,
                        { viewModel.onMinutesChange(it) },
                        "Min",
                        Icons.Default.Timer,
                        Modifier.weight(1f)
                    )
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Entrega y Detalles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("¿Es Urgente?", modifier = Modifier.weight(1f))
                    Switch(
                        checked = state.isUrgent,
                        onCheckedChange = { viewModel.onUrgentChange(it) })
                }

                OutlinedTextField(
                    value = state.deliveryDate,
                    onValueChange = { viewModel.onDateChange(it) },
                    label = { Text(if (state.isUrgent) "Fecha de Entrega (Aprox)" else "Fecha de Entrega (Opcional)") },
                    leadingIcon = { Icon(Icons.Default.DateRange, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { viewModel.onNotesChange(it) },
                    label = { Text("Características (Color, Relleno, etc.)") },
                    leadingIcon = { Icon(Icons.Default.EditNote, null) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Descuento Estudiante (${state.discountPercent}%)",
                        modifier = Modifier.weight(1f)
                    )
                    Checkbox(
                        checked = state.isStudent,
                        onCheckedChange = { viewModel.onStudentToggle(it) })
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Total Estimado", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = viewModel.formatCurrency(state.resultTotal),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.saveBudget() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSaving && state.resultTotal > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Save, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Presupuesto")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun InputNumber(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        leadingIcon = { Icon(icon, null) }, modifier = modifier, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}