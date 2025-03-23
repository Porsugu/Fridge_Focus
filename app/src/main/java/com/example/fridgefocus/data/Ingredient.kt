package com.example.fridgefocus.models

data class Ingredient(
    val id: Int,
    val name: String,
    val quantity: String,
    val unit: String,
    val recipe_id: Int
)