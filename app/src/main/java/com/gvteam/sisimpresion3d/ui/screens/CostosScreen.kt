package com.gvteam.sisimpresion3d.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gvteam.sisimpresion3d.model.Budget
import com.gvteam.sisimpresion3d.viewmodel.BudgetViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun CostosScreen(
    paddingValues: PaddingValues,
    viewModel: BudgetViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Calculadora", "Historial")

    LaunchedEffect(state.saveMessage) {
        state.saveMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                    icon = {
                        Icon(
                            if (index == 0) Icons.Default.Calculate else Icons.Default.History,
                            null
                        )
                    }
                )
            }
        }
        when (selectedTab) {
            0 -> CalculatorView(state, viewModel)
            1 -> HistoryView(state, viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorView(
    state: com.gvteam.sisimpresion3d.viewmodel.BudgetState,
    viewModel: BudgetViewModel
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val today =
                    LocalDate.now().atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
                return utcTimeMillis >= today
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val date =
                            Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        viewModel.onDateChange(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDatePicker = false
                }) { Text("Cancelar") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // --- TARJETA CLIENTE ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Datos del Cliente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                OutlinedTextField(
                    value = state.projectName,
                    onValueChange = { viewModel.onProjectNameChange(it) },
                    label = { Text("Proyecto / Pieza") },
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
                    label = { Text("Nombre Cliente") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )
            }
        }

        // --- TARJETA PARAMETROS ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Parámetros",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InputNumber(
                        state.grams,
                        { viewModel.onGramsChange(it) },
                        "Peso (g)",
                        Modifier.weight(1f)
                    )
                    InputNumber(
                        state.hours,
                        { viewModel.onHoursChange(it) },
                        "Horas",
                        Modifier.weight(1f)
                    )
                    InputNumber(
                        state.minutes,
                        { viewModel.onMinutesChange(it) },
                        "Min",
                        Modifier.weight(1f)
                    )
                }
                // --- NUEVO: Input de Margen ---
                InputNumber(
                    state.profitMarginPercent,
                    { viewModel.onMarginChange(it) },
                    "Margen de Ganancia (%)",
                    Modifier.fillMaxWidth()
                )
            }
        }

        // --- TARJETA ENTREGA ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalShipping,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Entrega",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Switch Urgencia
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("¿Es Urgente?", style = MaterialTheme.typography.bodyLarge)
                        if (state.isUrgent) Text(
                            "+${state.urgencySurchargePercent}% al subtotal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Switch(
                        checked = state.isUrgent,
                        onCheckedChange = { viewModel.onUrgentChange(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.error,
                            checkedTrackColor = MaterialTheme.colorScheme.errorContainer
                        )
                    )
                }

                // DatePicker Bloqueado
                OutlinedTextField(
                    value = state.deliveryDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (state.isUrgent) "Fecha (Urgente)" else "Fecha (Opcional)") },
                    leadingIcon = { Icon(Icons.Default.DateRange, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    interactionSource = remember { MutableInteractionSource() }.also { src ->
                        LaunchedEffect(src) {
                            src.interactions.collect {
                                if (it is PressInteraction.Release) showDatePicker = true
                            }
                        }
                    }
                )

                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { viewModel.onNotesChange(it) },
                    label = { Text("Notas / Color") },
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

        // --- TARJETA TOTAL ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Desglose visual
                if (state.isUrgent) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Cargo por Urgencia",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "+ ${viewModel.formatCurrency(state.resultUrgencyFee)}",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (state.isStudent) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Descuento Estudiante",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "- ${viewModel.formatCurrency(state.resultDiscount)}",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Estimado", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = viewModel.formatCurrency(state.resultTotal),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.saveBudget() }, modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSaving && state.resultTotal > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (state.isSaving) CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    else {
                        Icon(
                            Icons.Default.Save,
                            null
                        ); Spacer(modifier = Modifier.width(8.dp)); Text("Guardar Presupuesto")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun HistoryView(
    state: com.gvteam.sisimpresion3d.viewmodel.BudgetState,
    viewModel: BudgetViewModel
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoadingHistory) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (state.budgets.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.History,
                    null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "No hay presupuestos guardados",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.budgets) { budget ->
                    BudgetCard(
                        budget,
                        onDelete = { viewModel.deleteBudget(budget.id) },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetCard(budget: Budget, onDelete: () -> Unit, viewModel: BudgetViewModel) {
    val formattedDate = remember(budget.createdAt) {
        try {
            if (!budget.createdAt.isNullOrBlank()) ZonedDateTime.parse(budget.createdAt)
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) else null
        } catch (e: Exception) {
            null
        }
    }
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (budget.isUrgent) {
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                            Text(
                                "URGENTE",
                                color = Color.White
                            )
                        }; Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        budget.projectName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Cliente: ${budget.clientName}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${budget.grams}g • ${String.format("%.1f", budget.printTimeHours)} hrs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (budget.deliveryDate != null) Text(
                    "Entrega: ${budget.deliveryDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                if (formattedDate != null) Text(
                    "Creado: $formattedDate",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    viewModel.formatCurrency(budget.totalCost),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        null,
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun InputNumber(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        modifier = modifier, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}