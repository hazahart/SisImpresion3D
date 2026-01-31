package com.gvteam.sisimpresion3d.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gvteam.sisimpresion3d.data.SupabaseClient
import com.gvteam.sisimpresion3d.model.Budget
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

data class BudgetState(
    val grams: String = "",
    val hours: String = "",
    val minutes: String = "",
    val clientName: String = "",
    val projectName: String = "",
    val isUrgent: Boolean = false,
    val deliveryDate: String = "",
    val notes: String = "",
    val materialCostPerKg: String = "350",
    val electricityCostKwh: String = "3.5",
    val printerWatts: String = "150",
    val machineWearPerHour: String = "10",
    val profitMarginPercent: String = "30",
    val isStudent: Boolean = false,
    val discountPercent: Int = 20,
    val resultTotal: Double = 0.0,
    val resultSubtotal: Double = 0.0,
    val resultMaterialCost: Double = 0.0,
    val resultEnergyCost: Double = 0.0,
    val resultWearCost: Double = 0.0,
    val resultProfit: Double = 0.0,
    val resultDiscount: Double = 0.0,
    val isSaving: Boolean = false,
    val saveMessage: String? = null
)

class BudgetViewModel : ViewModel() {

    private val _state = MutableStateFlow(BudgetState())
    val state = _state.asStateFlow()

    fun onClientNameChange(v: String) {
        _state.value = _state.value.copy(clientName = v)
    }

    fun onProjectNameChange(v: String) {
        _state.value = _state.value.copy(projectName = v)
    }

    fun onUrgentChange(v: Boolean) {
        _state.value = _state.value.copy(isUrgent = v)
    }

    fun onDateChange(v: String) {
        _state.value = _state.value.copy(deliveryDate = v)
    }

    fun onNotesChange(v: String) {
        _state.value = _state.value.copy(notes = v)
    }

    fun onGramsChange(v: String) {
        _state.value = _state.value.copy(grams = v); calculate()
    }

    fun onHoursChange(v: String) {
        _state.value = _state.value.copy(hours = v); calculate()
    }

    fun onMinutesChange(v: String) {
        _state.value = _state.value.copy(minutes = v); calculate()
    }

    fun onMaterialCostChange(v: String) {
        _state.value = _state.value.copy(materialCostPerKg = v); calculate()
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
        val matCost = (matPrice / 1000.0) * grams
        val energyCost = (watts / 1000.0) * totalHours * kwhPrice
        val wearCost = wear * totalHours
        val base = matCost + energyCost + wearCost
        val profit = base * (margin / 100.0)
        val subtotal = base + profit
        val discount = if (s.isStudent) subtotal * (s.discountPercent / 100.0) else 0.0
        val total = subtotal - discount

        _state.value = s.copy(
            resultMaterialCost = matCost,
            resultEnergyCost = energyCost,
            resultWearCost = wearCost,
            resultProfit = profit,
            resultSubtotal = subtotal,
            resultDiscount = discount,
            resultTotal = total
        )
    }

    fun saveBudget() {
        val s = _state.value
        if (s.clientName.isBlank() || s.projectName.isBlank() || s.resultTotal <= 0) {
            _state.value = s.copy(saveMessage = "Faltan datos (Cliente, Proyecto o Cálculo)")
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

                    val budget = Budget(
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

                    SupabaseClient.client.from("budgets").insert(budget)

                    _state.value = s.copy(isSaving = false, saveMessage = "¡Presupuesto Guardado!")
                    resetForm()
                } else {
                    _state.value = s.copy(isSaving = false, saveMessage = "Error: Sesión no válida")
                }
            } catch (e: Exception) {
                _state.value =
                    s.copy(isSaving = false, saveMessage = "Error al guardar: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun resetForm() {
        _state.value = _state.value.copy(
            clientName = "",
            projectName = "",
            grams = "",
            hours = "",
            minutes = "",
            isUrgent = false,
            deliveryDate = "",
            notes = "",
            resultTotal = 0.0,
            resultSubtotal = 0.0
        )
    }

    fun clearMessage() {
        _state.value = _state.value.copy(saveMessage = null)
    }

    fun formatCurrency(amount: Double): String =
        NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(amount)
}