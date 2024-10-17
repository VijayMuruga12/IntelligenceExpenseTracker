package com.example.intelligenceexpensetracker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var UserNameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var Confirm_passwordInput: EditText
    private lateinit var registerButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    @SuppressLint("WrongViewCast", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        val loginButton: TextView = findViewById(R.id.login_link)
        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Initialize Firebase Auth and Realtime Database with the provided URL
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://intelligenceexpensetracker-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        // Testing database write
        database.child("test-node").setValue("test-value").addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Firebase", "Data successfully written")
            } else {
                Log.e("Firebase", "Error: ${task.exception?.message}")
            }
        }

        UserNameInput = findViewById(R.id.reg_username_input)
        emailInput = findViewById(R.id.reg_email_input)
        passwordInput = findViewById(R.id.reg_password_input)
        Confirm_passwordInput = findViewById(R.id.reg_confirmpassword_input)
        registerButton = findViewById(R.id.registerButton)

        registerButton.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val UserName = UserNameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val c_password = Confirm_passwordInput.text.toString().trim()

        if (UserName.isEmpty()) {
            UserNameInput.error = "Full name is required"
            return
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Enter a valid email"
            return
        }
        if (password.isEmpty() || password.length < 6) {
            passwordInput.error = "Password must be at least 6 characters"
            return
        }
        if (password != c_password) {
            Confirm_passwordInput.error = "Confirm Password doesn't match"
            return
        }

        // Proceed with registration
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val userObj = User(userId, UserName, email, password)
                    userId?.let {
                        database.child("Users").child(it).setValue(userObj)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, LoginActivity::class.java))
                                } else {
                                    Toast.makeText(this, "Failed to store user data", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
