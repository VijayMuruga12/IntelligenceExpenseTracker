package com.example.intelligenceexpensetracker
import android.os.Bundle
import androidx.activity.ComponentActivity
import android.widget.Button
import android.content.Intent
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main) //activity_main.xml
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets((WindowInsetsCompat.Type.systemBars()))
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val loginmainpage: Button = findViewById(R.id.loginbutton)
        loginmainpage.setOnClickListener {
            Log.d("MainActivity", "Login button clicked")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        val newregister: Button = findViewById(R.id.registerbutton) // loginButton ID from your XML
        newregister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }


    }
}