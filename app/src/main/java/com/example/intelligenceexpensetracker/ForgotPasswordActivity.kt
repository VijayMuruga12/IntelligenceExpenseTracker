package com.example.intelligenceexpensetracker

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity: AppCompatActivity() {
    private lateinit var emailInput: EditText
    // Initialize FirebaseAuth instance
    val auth = FirebaseAuth.getInstance()

    // Assuming the user is already logged in
    val user = auth.currentUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.forgot_password)
        emailInput=findViewById(R.id.F_P_emailInput)
        val reset_button: Button = findViewById(R.id.resetButton) // loginButton ID from your XML
        reset_button.setOnClickListener {
            val email = emailInput.text.toString()

            if (email.isNotEmpty()) {
                updatePassword(email)
            } else {
                Toast.makeText(this, "Please enter your registered email", Toast.LENGTH_SHORT).show()
            }

        }

    }
    private fun updatePassword(email: String) {
        val email = emailInput.text.toString().trim()
        if (TextUtils.isEmpty(email.toString())) {
            emailInput.error = "Email is required"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Enter a valid email"
            return
        }
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent to $email", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Failed to send reset email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }

    }

}
