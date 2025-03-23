package com.example.fridgefocus.ui.home
// In com.example.fridgefocus.ui.home.HomeViewModel
package com.example.fridgefocus.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fridgefocus.models.Inventory
import com.example.fridgefocus.repository.FridgeFocusRepository
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    private val repository = FridgeFocusRepository()
    
    private val _inventoryItems = MutableLiveData<List<Inventory>>()
    val inventoryItems: LiveData<List<Inventory>> = _inventoryItems
    
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error
    
    private val _successMessage = MutableLiveData<String?>(null)
    val successMessage: LiveData<String?> = _successMessage
    
    // Load all inventory items
    fun loadInventory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _inventoryItems.value = repository.getInventoryItems()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load inventory: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Add new inventory item
    fun addInventoryItem(name: String, quantity: String, unit: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.addInventoryItem(name, quantity, unit)
                _successMessage.value = "Item added successfully!"
                // Reload the inventory to get the updated list
                loadInventory()
            } catch (e: Exception) {
                _error.value = "Failed to add item: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Update existing inventory item
    fun updateInventoryItem(name: String, newName: String? = null, 
                           newQuantity: String? = null, newUnit: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateInventoryItem(name, newName, newQuantity, newUnit)
                _successMessage.value = "Item updated successfully!"
                loadInventory()
            } catch (e: Exception) {
                _error.value = "Failed to update item: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Delete inventory item
    fun deleteInventoryItem(name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteInventoryItem(name)
                _successMessage.value = "Item deleted successfully!"
                loadInventory()
            } catch (e: Exception) {
                _error.value = "Failed to delete item: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}