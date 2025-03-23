package com.example.fridgefocus

import Item
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class mealDetail : AppCompatActivity() {

    private var clicked = 0
    private lateinit var ingredientBtn:Button
    private lateinit var instrutionBtn:Button
    private lateinit var recipeText:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_meal_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.hide()
        val itemList = intent.getParcelableArrayListExtra<Item>("itemList") ?: arrayListOf()
        var recipe = intent.getStringExtra("recipe")
        recipe = recipe!!.split("_")
            .mapIndexed { index, step -> "${index + 1}. $step" }  // Add numbers
            .joinToString("\n")  // Join with new lines

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.addItemDecoration(ItemSpacingDecoration(16))
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ItemAdapter(itemList)
        recipeText = findViewById(R.id.recipe)
        ingredientBtn = findViewById(R.id.ingredientBtn)
        instrutionBtn = findViewById(R.id.instructionBtn)
        ingredientBtn.setOnClickListener {
            if(clicked == 1){
                ingredientBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#000000"))
                ingredientBtn.setTextColor(Color.parseColor("#FFFFFF"))
                instrutionBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F1F1F1"))
                instrutionBtn.setTextColor(Color.parseColor("#000000"))
                recyclerView.isInvisible = false
                recipeText.isInvisible = true
                clicked = 0
            }
        }

        instrutionBtn.setOnClickListener {
            if(clicked == 0){
                instrutionBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#000000"))
                instrutionBtn.setTextColor(Color.parseColor("#FFFFFF"))
                ingredientBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F1F1F1"))
                ingredientBtn.setTextColor(Color.parseColor("#000000"))
                recyclerView.isInvisible = true
                recipeText.isInvisible = false
                recipeText.setText(recipe)
                clicked = 1
            }
        }


    }

}