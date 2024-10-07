package com.example.intelligenceexpensetracker
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import android.widget.Button
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.intelligenceexpensetracker.ui.theme.IntelligenceExpenseTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets((WindowInsetsCompat.Type.systemBars()))
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val loginbutton: Button = findViewById(R.id.loginbutton)
        loginbutton.setOnClickListener{
//            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }

        val registerbutton: Button = findViewById(R.id.registerbutton)
        registerbutton.setOnClickListener{
//            val intent = Intent(this,RegisterActivity::class.java)
            startActivity(intent)
        }

    }
}