package com.example.intelligenceexpensetracker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    private lateinit var startbutton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home)
        startbutton = findViewById(R.id.startButton) // loginButton ID from your XML
//        startbutton.setOnClickListener {
//
//            val intent = Intent(this, TransactionActivity::class.java)
//            startActivity(intent)
//        }
    }
}
