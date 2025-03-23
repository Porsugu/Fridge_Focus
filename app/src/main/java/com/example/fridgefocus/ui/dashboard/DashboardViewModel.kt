package com.example.fridgefocus.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fridgefocus.models.Ingredient
import com.example.fridgefocus.models.Recipe
import com.example.fridgefocus.repository.FridgeFocusRepository
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text
    
    private val repository = FridgeFocusRepository()
    
    private val _recipe = MutableLiveData<Recipe>()
    val recipe: LiveData<Recipe> = _recipe
    
    private val _recipeIngredients = MutableLiveData<List<Ingredient>>()
    val recipeIngredients: LiveData<List<Ingredient>> = _recipeIngredients
    
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error
    
    // Add a success message LiveData for user feedback
    private val _successMessage = MutableLiveData<String?>(null)
    val successMessage: LiveData<String?> = _successMessage
    
    fun loadRecipe(recipeName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _recipe.value = repository.getRecipeByName(recipeName)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load recipe: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadRecipeIngredients(recipeName: String) {
        viewModelScope.launch {
            try {
                _recipeIngredients.value = repository.getRecipeIngredients(recipeName)
            } catch (e: Exception) {
                _error.value = "Failed to load recipe ingredients: ${e.message}"
            }
        }
    }
    
    // Load all recipes
    fun loadAllRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val recipes = repository.getAllRecipes()
                if (recipes.isNotEmpty()) {
                    _recipe.value = recipes[0]
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load recipes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Add a new recipe
    fun addRecipe(name: String, guide: String, url: String = "", ingredients: List<Ingredient> = emptyList()) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newRecipe = repository.addRecipe(name, guide, url, ingredients)
                _recipe.value = newRecipe
                _successMessage.value = "Recipe added successfully!"
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to add recipe: ${e.message}"
                _successMessage.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}