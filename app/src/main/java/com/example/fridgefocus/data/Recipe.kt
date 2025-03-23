package com.example.fridgefocus.models

data class Recipe(
    val id: Int,
    val name: String,
    val guide: String,
    val url: String,
    val ingredients: List<Ingredient>? = null
)