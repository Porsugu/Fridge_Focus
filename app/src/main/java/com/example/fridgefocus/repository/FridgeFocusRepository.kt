Copypackage com.example.fridgefocus.repository

import com.example.fridgefocus.api.RetrofitClient
import com.example.fridgefocus.models.Ingredient
import com.example.fridgefocus.models.Inventory
import com.example.fridgefocus.models.Recipe

class FridgeFocusRepository {
    private val api = RetrofitClient.fridgeFocusApi
    
    // Inventory methods
    suspend fun getInventoryItems(): List<Inventory> {
        return api.getInventory()
    }
    
    suspend fun getInventoryItemById(id: Int): Inventory {
        return api.getInventoryById(id)
    }
    
    suspend fun getInventoryItemByName(name: String): Inventory {
        return api.getInventoryByName(name)
    }
    
    suspend fun addInventoryItem(name: String, quantity: String, unit: String): Inventory {
        val newItem = Inventory(0, name, quantity, unit) // ID is auto-generated on server
        return api.addInventory(newItem)
    }
    
    suspend fun updateInventoryItem(name: String, newName: String? = null, 
                                  newQuantity: String? = null, newUnit: String? = null): Inventory {
        // First get the current item
        val currentItem = api.getInventoryByName(name)
        
        // Update only the provided fields
        val updatedItem = Inventory(
            id = currentItem.id,
            name = newName ?: currentItem.name,
            quantity = newQuantity ?: currentItem.quantity,
            unit = newUnit ?: currentItem.unit
        )
        
        return api.updateInventory(name, updatedItem)
    }
    
    suspend fun deleteInventoryItem(name: String) {
        api.deleteInventory(name)
    }
    
    // Recipe methods
    suspend fun getAllRecipes(): List<Recipe> {
        return api.getRecipes()
    }
    
    suspend fun getRecipeByName(name: String): Recipe {
        return api.getRecipeByName(name)
    }
    
    suspend fun addRecipe(name: String, guide: String, url: String = "", 
                        ingredients: List<Ingredient> = emptyList()): Recipe {
        val newRecipe = Recipe(0, name, guide, url, ingredients)
        return api.addRecipe(newRecipe)
    }
    
    suspend fun deleteRecipe(id: Int) {
        api.deleteRecipe(id)
    }
    
    // Ingredient methods
    suspend fun getAllIngredients(): List<Ingredient> {
        return api.getIngredients()
    }
    
    suspend fun getRecipeIngredients(recipeName: String): List<Ingredient> {
        return api.getRecipeIngredients(recipeName)
    }
}