package com.example.intelligenceexpensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SmsAdapter(private val smsList: MutableList<String>) : RecyclerView.Adapter<SmsAdapter.SmsViewHolder>() {

    // ViewHolder class that holds the TextView for each message
    class SmsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val smsTextView: TextView = itemView.findViewById(R.id.smsTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsViewHolder {
        // Inflate the layout for each SMS item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sms_item, parent, false)
        return SmsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SmsViewHolder, position: Int) {
        // Bind the message to the TextView
        val smsMessage = smsList[position]
        holder.smsTextView.text = smsMessage
    }

    override fun getItemCount(): Int {
        // Return the number of messages
        return smsList.size
    }
    fun clearSmsList(){
        smsList.clear()
    }
}