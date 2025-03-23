package com.example.fridgefocus.api

import com.example.fridgefocus.models.Inventory
import com.example.fridgefocus.models.Recipe
import com.example.fridgefocus.models.Ingredient
import okhttp3.ResponseBody
import retrofit2.http.*

interface FridgeFocusApi {
    // Inventory endpoints
    @GET("inventory")
    suspend fun getInventory(): List<Inventory>
    
    @GET("inventory/{id}")
    suspend fun getInventoryById(@Path("id") id: Int): Inventory
    
    @GET("inventory/name/{name}")
    suspend fun getInventoryByName(@Path("name") name: String): Inventory
    
    @POST("addInventory")
    suspend fun addInventory(@Body inventory: Inventory): Inventory
    
    @PUT("updateInventory/{name}")
    suspend fun updateInventory(@Path("name") name: String, @Body inventory: Inventory): Inventory
    
    @DELETE("deleteInventory/{name}")
    suspend fun deleteInventory(@Path("name") name: String): ResponseBody
    
    // Recipe endpoints
    @GET("recipes")
    suspend fun getRecipes(): List<Recipe>
    
    @GET("recipes/{name}")
    suspend fun getRecipeByName(@Path("name") name: String): Recipe
    
    @POST("addRecipes")
    suspend fun addRecipe(@Body recipe: Recipe): Recipe
    
    @DELETE("recipes/{id}")
    suspend fun deleteRecipe(@Path("id") id: Int): ResponseBody
    
    // Ingredients endpoints
    @GET("ingredients")
    suspend fun getIngredients(): List<Ingredient>
    
    @GET("recipes/{recipeName}/ingredients")
    suspend fun getRecipeIngredients(@Path("recipeName") recipeName: String): List<Ingredient>
}