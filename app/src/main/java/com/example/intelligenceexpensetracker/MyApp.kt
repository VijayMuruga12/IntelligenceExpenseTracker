package com.example.intelligenceexpensetracker
import android.app.Application
import androidx.multidex.MultiDex
import com.google.firebase.FirebaseApp

class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this) // Initialize Firebase once for the entire app
        MultiDex.install(this)
    }
}
