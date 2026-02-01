package com.gvteam.sisimpresion3d.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gvteam.sisimpresion3d.data.SupabaseClient
import com.gvteam.sisimpresion3d.model.Budget
import com.gvteam.sisimpresion3d.model.BudgetInsert // <--- Importamos la nueva clase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

data class BudgetState(
    // Inputs
    val grams: String = "",
    val hours: String = "",
    val minutes: String = "",
    val clientName: String = "",
    val projectName: String = "",
    val isUrgent: Boolean = false,
    val deliveryDate: String = "",
    val notes: String = "",

    // Configuración Base
    val materialCostPerKg: String = "350",
    val electricityCostKwh: String = "3.5",
    val printerWatts: String = "150",
    val machineWearPerHour: String = "10",

    // Configuración Variable
    val profitMarginPercent: String = "30",
    val urgencySurchargePercent: Int = 20,
    val isStudent: Boolean = false,
    val discountPercent: Int = 20,

    // Resultados
    val resultTotal: Double = 0.0,
    val resultSubtotal: Double = 0.0,
    val resultMaterialCost: Double = 0.0,
    val resultEnergyCost: Double = 0.0,
    val resultWearCost: Double = 0.0,
    val resultProfit: Double = 0.0,
    val resultUrgencyFee: Double = 0.0,
    val resultDiscount: Double = 0.0,

    // Historial y UI
    val budgets: List<Budget> = emptyList(),
    val isLoadingHistory: Boolean = false,
    val isSaving: Boolean = false,
    val saveMessage: String? = null
)

class BudgetViewModel : ViewModel() {

    private val _state = MutableStateFlow(BudgetState())
    val state = _state.asStateFlow()

    init {
        fetchBudgets()
    }

    // --- Inputs Simples ---
    fun onClientNameChange(v: String) {
        _state.value = _state.value.copy(clientName = v)
    }

    fun onProjectNameChange(v: String) {
        _state.value = _state.value.copy(projectName = v)
    }

    fun onDateChange(v: String) {
        _state.value = _state.value.copy(deliveryDate = v)
    }

    fun onNotesChange(v: String) {
        _state.value = _state.value.copy(notes = v)
    }

    // --- Inputs que Recalculan ---
    fun onGramsChange(v: String) {
        _state.value = _state.value.copy(grams = v); calculate()
    }

    fun onHoursChange(v: String) {
        _state.value = _state.value.copy(hours = v); calculate()
    }

    fun onMinutesChange(v: String) {
        _state.value = _state.value.copy(minutes = v); calculate()
    }

    fun onMarginChange(v: String) {
        _state.value = _state.value.copy(profitMarginPercent = v); calculate()
    }

    fun onUrgentChange(v: Boolean) {
        _state.value = _state.value.copy(isUrgent = v); calculate()
    }

    fun onStudentToggle(v: Boolean) {
        _state.value = _state.value.copy(isStudent = v); calculate()
    }

    private fun calculate() {
        val s = _state.value

        val grams = s.grams.toDoubleOrNull() ?: 0.0
        val h = s.hours.toDoubleOrNull() ?: 0.0
        val m = s.minutes.toDoubleOrNull() ?: 0.0
        val matPrice = s.materialCostPerKg.toDoubleOrNull() ?: 0.0
        val kwhPrice = s.electricityCostKwh.toDoubleOrNull() ?: 0.0
        val watts = s.printerWatts.toDoubleOrNull() ?: 0.0
        val wear = s.machineWearPerHour.toDoubleOrNull() ?: 0.0
        val margin = s.profitMarginPercent.toDoubleOrNull() ?: 0.0

        val totalHours = h + (m / 60.0)

        // 1. Costos Base
        val matCost = (matPrice / 1000.0) * grams
        val energyCost = (watts / 1000.0) * totalHours * kwhPrice
        val wearCost = wear * totalHours
        val baseCost = matCost + energyCost + wearCost

        // 2. Ganancia
        val profit = baseCost * (margin / 100.0)

        // 3. Subtotal
        var subtotal = baseCost + profit

        // 4. Recargo Urgencia
        val urgencyFee = if (s.isUrgent) subtotal * (s.urgencySurchargePercent / 100.0) else 0.0
        var totalAccumulated = subtotal + urgencyFee

        // 5. Descuento Estudiante
        val discount = if (s.isStudent) totalAccumulated * (s.discountPercent / 100.0) else 0.0

        val finalTotal = totalAccumulated - discount

        _state.value = s.copy(
            resultMaterialCost = matCost,
            resultEnergyCost = energyCost,
            resultWearCost = wearCost,
            resultProfit = profit,
            resultSubtotal = subtotal,
            resultUrgencyFee = urgencyFee,
            resultDiscount = discount,
            resultTotal = finalTotal
        )
    }

    fun saveBudget() {
        val s = _state.value
        if (s.clientName.isBlank() || s.projectName.isBlank() || s.resultTotal <= 0) {
            _state.value = s.copy(saveMessage = "Faltan datos o cálculo inválido")
            return
        }

        viewModelScope.launch {
            _state.value = s.copy(isSaving = true, saveMessage = null)
            try {
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                if (currentUser != null) {
                    val totalHours =
                        (s.hours.toDoubleOrNull() ?: 0.0) + ((s.minutes.toDoubleOrNull()
                            ?: 0.0) / 60.0)

                    // USAMOS LA CLASE 'BudgetInsert' QUE NO TIENE ID NI CREATED_AT
                    val budgetToInsert = BudgetInsert(
                        userId = currentUser.id,
                        clientName = s.clientName,
                        projectName = s.projectName,
                        totalCost = s.resultTotal,
                        grams = s.grams.toDoubleOrNull() ?: 0.0,
                        printTimeHours = totalHours,
                        isUrgent = s.isUrgent,
                        deliveryDate = if (s.deliveryDate.isBlank()) null else s.deliveryDate,
                        notes = if (s.notes.isBlank()) null else s.notes
                    )

                    SupabaseClient.client.from("budgets").insert(budgetToInsert)

                    _state.value = s.copy(isSaving = false, saveMessage = "¡Presupuesto Guardado!")
                    resetForm()
                    fetchBudgets()
                }
            } catch (e: Exception) {
                _state.value = s.copy(isSaving = false, saveMessage = "Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun fetchBudgets() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingHistory = true)
            try {
                val list = SupabaseClient.client
                    .from("budgets")
                    .select { order("created_at", Order.DESCENDING) }
                    .decodeList<Budget>()
                _state.value = _state.value.copy(budgets = list, isLoadingHistory = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoadingHistory = false)
            }
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.from("budgets").delete { filter { eq("id", id) } }
                fetchBudgets()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun resetForm() {
        _state.value = _state.value.copy(
            clientName = "", projectName = "", grams = "", hours = "", minutes = "",
            isUrgent = false, deliveryDate = "", notes = "", resultTotal = 0.0,
            resultSubtotal = 0.0, resultUrgencyFee = 0.0, resultDiscount = 0.0
        )
    }

    fun clearMessage() {
        _state.value = _state.value.copy(saveMessage = null)
    }

    fun formatCurrency(amount: Double): String =
        NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(amount)
}