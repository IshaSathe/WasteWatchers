package com.example.wastewatchers

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ImpactActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_impact)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val buttonToHome = findViewById<Button>(R.id.button_to_home)
        val tree1 = findViewById<Button>(R.id.tree_1)
        val tree2 = findViewById<Button>(R.id.tree_2)
        val tree3 = findViewById<Button>(R.id.tree_3)
        val tree4 = findViewById<Button>(R.id.tree_4)
        val tree5 = findViewById<Button>(R.id.tree_5)

        buttonToHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        tree1.setOnClickListener {
            showLinkPopup("https://www.epa.gov/recycle/recycling-basics-and-benefits")
        }

        tree2.setOnClickListener {
            showLinkPopup("https://www.futurelearn.com/info/courses/upcycling-for-change-from-green-ideas-to-startup-businesses/0/steps/67684")
        }

        tree3.setOnClickListener {
            showLinkPopup("https://www.usda.gov/foodlossandwaste/why")
        }

        tree4.setOnClickListener {
            showLinkPopup("https://www.epa.gov/smm/recycling-economic-information-rei-report#:~:text=Recycling%20also%20conserves%20resources%20and,to%20collect%20new%20raw%20materials.")
        }

        tree5.setOnClickListener {
            showLinkPopup("https://healtheplanet.com/100-ways-to-heal-the-planet/upcyclinghttps://healtheplanet.com/100-ways-to-heal-the-planet/upcycling")
        }
    }

    private fun showLinkPopup(url: String) {
        AlertDialog.Builder(this)
            .setTitle("Open Link")
            .setMessage("Do you want to open this link in your browser?")
            .setPositiveButton("Open") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}