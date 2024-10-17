package com.example.intelligenceexpensetracker
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class LoginActivity:AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    private lateinit var auth: FirebaseAuth
    private lateinit var email: EditText
    private lateinit var password: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("LoginActivity", "LoginActivity started")
        enableEdgeToEdge()
        setContentView(R.layout.login) //login.xml
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        email = findViewById(R.id.email_input)
        password = findViewById(R.id.password_input)
        val loginButton: Button = findViewById(R.id.loginButton)
        loginButton.setOnClickListener {
            val email = email.text.toString()
            val password = password.text.toString()
            if (isValidEmail(email)) {
                signIn(email, password)
            } else {
                Toast.makeText(this, "Invalid email format.", Toast.LENGTH_SHORT).show()
            }
        }
        val Regbutton: TextView = findViewById(R.id.registeration_link) // loginButton ID from your XML
        Regbutton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
        private fun isValidEmail(email: String): Boolean {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
        private fun signIn(email: String, password: String) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success
                        Toast.makeText(this, "Authentication Successful.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)

                    } else {

                        // If sign in fails
                        Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }
}