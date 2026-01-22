package com.gvteam.sisimpresion3d.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gvteam.sisimpresion3d.data.repository.PrinterRepository
import com.gvteam.sisimpresion3d.model.Printer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PrinterViewModel : ViewModel() {
    private val repository = PrinterRepository()

    private val _printers = MutableStateFlow<List<Printer>>(emptyList())
    val printers = _printers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        fetchPrinters()
        subscribeToChanges()
    }

    fun fetchPrinters() {
        viewModelScope.launch {
            if (_printers.value.isEmpty()) _isLoading.value = true
            _errorMessage.value = null
            try {
                val res = repository.getPrinters()
                _printers.value = res.sortedBy { it.id }
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión: ${e.localizedMessage}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun subscribeToChanges() {
        viewModelScope.launch {
            try {
                repository.subscribeToRealtime().collect {
                    fetchPrinters()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}