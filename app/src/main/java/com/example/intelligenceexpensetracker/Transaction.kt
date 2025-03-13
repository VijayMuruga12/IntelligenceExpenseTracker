package com.example.intelligenceexpensetracker
data class Transaction(
    var id: Int,
    val address: String,
    val body: String,
    val date: String,
    val amount: Double,
    val category: String
)

