package com.example.wastewatchers

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val button_to_recycle = findViewById<Button>(R.id.button_to_recycle)
        val button_to_upcycle = findViewById<Button>(R.id.button_to_upcycle)
        val button_to_fresh = findViewById<Button>(R.id.button_to_fresh)
        val button_to_impact = findViewById<Button>(R.id.button_to_impact)


        button_to_recycle.setOnClickListener {
            val intent = Intent(this, RecycleActivity::class.java)
            startActivity(intent)
        }

        button_to_upcycle.setOnClickListener {
            val intent = Intent(this, UpcycleActivity::class.java)
            startActivity(intent)
        }

        button_to_fresh.setOnClickListener {
            val intent = Intent(this, FreshActivity::class.java)
            startActivity(intent)
        }

        button_to_impact.setOnClickListener {
            val intent = Intent(this, ImpactActivity::class.java)
            startActivity(intent)
        }
    }
}