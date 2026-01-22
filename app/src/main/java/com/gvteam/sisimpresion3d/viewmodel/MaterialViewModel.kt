package com.gvteam.sisimpresion3d.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gvteam.sisimpresion3d.data.repository.MaterialRepository
import com.gvteam.sisimpresion3d.model.Material
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MaterialViewModel(private val repository: MaterialRepository) : ViewModel() {

    private val _materials = MutableStateFlow<List<Material>>(emptyList())
    val materials: StateFlow<List<Material>> = _materials.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchMaterials()
        subscribeToChanges()
    }

    fun fetchMaterials() {
        viewModelScope.launch {
            if (_materials.value.isEmpty()) _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.getMaterials()
                _materials.value = result
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun subscribeToChanges() {
        viewModelScope.launch {
            try {
                repository.subscribeToRealtime().collect {
                    fetchMaterials()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Fallo Realtime: ${e.message}"
            }
        }
    }

    fun consumeMaterial(material: Material, consumedAmount: Int) {
        viewModelScope.launch {
            val newWeight = (material.remainingWeight - consumedAmount).coerceAtLeast(0)
            try {
                repository.updateWeight(material.id, newWeight)
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar: ${e.message}"
                e.printStackTrace()
            }
        }
    }
}