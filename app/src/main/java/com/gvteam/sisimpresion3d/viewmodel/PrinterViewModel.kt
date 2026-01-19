package com.gvteam.sisimpresion3d.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gvteam.sisimpresion3d.data.PrinterRepository
import com.gvteam.sisimpresion3d.model.Printer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PrinterViewModel : ViewModel() {
    private val repository = PrinterRepository()

    // estados observables
    private val _printers = MutableStateFlow<List<Printer>>(emptyList())
    val printers = _printers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        fetchPrinters()
    }

    fun fetchPrinters() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val res = repository.getPrinters()
                // ordenar por ID
                _printers.value = res.sortedBy { it.id }
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión: ${e.localizedMessage}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}