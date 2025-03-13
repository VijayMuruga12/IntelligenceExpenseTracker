package com.example.intelligenceexpensetracker
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class TransactionActivity : AppCompatActivity() {
    private val SMS_PERMISSION_CODE = 101
    val smsStringBuilder = StringBuilder()
    private val db = FirebaseFirestore.getInstance()
    private val transactions = mutableListOf<Transaction>()
    val transactionList = mutableListOf<AmountList>()
    private lateinit var smsAdapter: SmsAdapter
    private lateinit var smsMessages: SmsAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    var totalDebited = 0.0;
    var totalCredited = 0.0;
    var finaltotalCredited = 0.0;
    var finaltotalDebited = 0.0;
    var amtAdd=0.0
    var amtdel=0.0
    private lateinit var totalExpenses: TextView
    private lateinit var totalIncome: TextView
    private lateinit var balance: TextView
    private lateinit var spendingChart: PieChart
    private lateinit var monthYearPicker: TextView
    private lateinit var addbutton: Button
    private lateinit var delbutton: Button
    private lateinit var viewbutton: Button
    private var selectedMonth: Int =
        Calendar.getInstance().get(Calendar.MONTH) // Set to current month
    private var selectedYear: Int =
        Calendar.getInstance().get(Calendar.YEAR)   // Set to current year

    @SuppressLint("WrongViewCast")
    private lateinit var registerButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.transaction_layout)
        totalExpenses = findViewById(R.id.totalExpenses)
        totalIncome = findViewById(R.id.totalIncome)
        balance = findViewById(R.id.balance)
        spendingChart = findViewById(R.id.spendingChart)
        monthYearPicker = findViewById(R.id.monthYearPicker)
        addbutton = findViewById(R.id.addTransaction);
        delbutton = findViewById(R.id.deleteTransaction);
        viewbutton = findViewById(R.id.viewReport);

        addbutton.setOnClickListener {
            showTransactionDialog("Add")
        }
        delbutton.setOnClickListener { showTransactionDialog("Delete") }
        viewbutton.setOnClickListener {
            val intent = Intent(this, ReportActivity::class.java)
            startActivity(intent)
        }

        auth = FirebaseAuth.getInstance()
        database =
            FirebaseDatabase.getInstance("https://intelligenceexpensetracker-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference()
        // Display the current month and year on the UI
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        monthYearPicker.text = monthFormat.format(Calendar.getInstance().time)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_SMS),
                SMS_PERMISSION_CODE
            )
            CalcuateMonthfirst()
            Toast.makeText(this, "Permissions now granted", Toast.LENGTH_SHORT).show()
        } else {
            CalcuateMonthfirst()
            readSmsFromUser()
            Toast.makeText(this, "Permissions already granted", Toast.LENGTH_SHORT)
                .show()// Replace with the desired phone number
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            CalcuateMonthfirst()
            readSmsFromUser() // Replace with the desired phone number

            Toast.makeText(this, "Permissions", Toast.LENGTH_SHORT).show()
        }
    }

    private fun CalcuateMonthfirst() {

        val smsUri = Uri.parse("content://sms/inbox")
        val cursor = contentResolver.query(smsUri, null, null, null, "date DESC")
        if (cursor != null) {
            val bodyIndex = cursor.getColumnIndexOrThrow("body")
            val dateIndex = cursor.getColumnIndexOrThrow("date")
            val addressindex = cursor.getColumnIndexOrThrow("address")

//            smsStringBuilder.append("From: $bodyIndex\n")
            if (bodyIndex == -1) {
                Toast.makeText(this, "Columns not found.", Toast.LENGTH_SHORT).show()
                return
            }

            for (i in 0..11) {
                if (cursor.moveToFirst()) {

                    do {

                        val messageBody = cursor.getString(bodyIndex)
                        val datemsg = cursor.getLong(dateIndex)
                        val address = cursor.getString(addressindex)
                        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                        val formattedDate = dateFormat.format(Date(datemsg))
                        val messageDate = Date(datemsg)
                        val calendar = Calendar.getInstance()
                        calendar.time = messageDate
                        val isCurrentYear =
                            (calendar.get(Calendar.YEAR) == selectedYear && calendar.get(Calendar.MONTH) == i)
                        if (categorizeMessage(messageBody, address) && isCurrentYear) {
                            val category = determineCategory(messageBody)
                            val amount = parseAmountFromSms(messageBody)
                            if (amount != 0.0) {
                                if (category == "Salary" || category == "Credited") {
                                    totalCredited += amount
                                } else {
                                    totalDebited += amount
                                }
                            }

                        }
                    } while (cursor.moveToNext())
                } else {
//                // No messages found for this user
                    Toast.makeText(this, "No messages found for this user.", Toast.LENGTH_SHORT)
                        .show()
                }

                saveIncomeExpenseToFirebaseone(totalCredited, totalDebited, i + 1, "2024")
            }
        }
    }

    private fun saveIncomeExpenseToFirebaseone(
        income: Double,
        expenses: Double,
        month: Int,
        year: String
    ) {

        val user = auth.currentUser
        val userId = user?.uid
        var incomeExpenseData=IncomeExpense()
        val incomeExpenseRef = database.child("Users").child("$userId/transaction/2024/$month")
        if (incomeExpenseRef != null) {

            incomeExpenseRef.get()
                .addOnSuccessListener { dataSnapshot ->

                    if (!dataSnapshot.exists()) {
                        // First time: Initialize addorDel to 0.0
                        incomeExpenseData =  IncomeExpense(month = month, income = income, expense = expenses, addamt = 0.0, delamt = 0.0)
                        incomeExpenseRef.setValue(incomeExpenseData)
                    } else {
                        amtAdd=dataSnapshot.child("addamt").getValue(Double::class.java) ?: 0.0
                        amtdel=dataSnapshot.child("delamt").getValue(Double::class.java) ?: 0.0
                        incomeExpenseData = IncomeExpense(month = month, income = income, expense = expenses,addamt = amtAdd, delamt = amtdel)
                        incomeExpenseRef.setValue(incomeExpenseData)

                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "No messages found for this error.", Toast.LENGTH_SHORT)
                        .show()

                }
        }
        totalDebited = 0.0
        totalCredited = 0.0
    }

    private fun readSmsFromUser() {
        val smsUri = Uri.parse("content://sms/inbox")
        val cursor = contentResolver.query(smsUri, null, null, null, "date DESC")
//        smsStringBuilder.append("From: $cursor\n")
        if (cursor != null) {
            val bodyIndex = cursor.getColumnIndexOrThrow("body")
            val dateIndex = cursor.getColumnIndexOrThrow("date")
            val addressindex = cursor.getColumnIndexOrThrow("address")

//            smsStringBuilder.append("From: $bodyIndex\n")
            if (bodyIndex == -1) {
                Toast.makeText(this, "Columns not found.", Toast.LENGTH_SHORT).show()
                return
            }
            val datemont = monthYearPicker.text.toString()
            val (month, year) = datemont.split(" ")
            val yearInt = year.toInt()
            fun getCalendarMonth(month: String): Int? {
                return when (month) {
                    "January" -> Calendar.JANUARY
                    "February" -> Calendar.FEBRUARY
                    "March" -> Calendar.MARCH
                    "April" -> Calendar.APRIL
                    "May" -> Calendar.MAY
                    "June" -> Calendar.JUNE
                    "July" -> Calendar.JULY
                    "August" -> Calendar.AUGUST
                    "September" -> Calendar.SEPTEMBER
                    "October" -> Calendar.OCTOBER
                    "November" -> Calendar.NOVEMBER
                    "December" -> Calendar.DECEMBER
                    else -> null // Return null if the month name is invalid
                }
            }

            val calenderconstant = getCalendarMonth(month)
            monthYearPicker.setOnClickListener {
                showMonthYearPicker()
            }
            // Check if there are any rows in the cursor
            if (cursor.moveToFirst()) {
                do {
                    val messageBody = cursor.getString(bodyIndex)
                    val datemsg = cursor.getLong(dateIndex)
                    val address = cursor.getString(addressindex)
                    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val formattedDate = dateFormat.format(Date(datemsg))
                    val messageDate = Date(datemsg)
                    val calendar = Calendar.getInstance()
                    calendar.time = messageDate

                    val isOctober2024 =
                        (calendar.get(Calendar.YEAR) == yearInt && calendar.get(Calendar.MONTH) == calenderconstant)
                    if (categorizeMessage(messageBody, address) && isOctober2024) {
                        val category = determineCategory(messageBody)
                        val amount = parseAmountFromSms(messageBody)
                        if (amount != 0.0) {
                            smsStringBuilder.append("Address: $address\n")
                            smsStringBuilder.append("Category: $category\n")
                            smsStringBuilder.append("Amount: $amount\n")
                            smsStringBuilder.append("Date: $formattedDate$$")
                            val transaction = Transaction(
                                id = 0, // Assign a unique ID
                                address = address,
                                body = messageBody,
                                date = datemsg.toString(),
                                amount = amount,
                                category = category
                            )
                            transactions.add(transaction)
                        }
                    }
                } while (cursor.moveToNext()) // Move to the next row
            } else {
//                // No messages found for this user
                Toast.makeText(this, "No messages found for this user.", Toast.LENGTH_SHORT).show()
            }
            cursor.close()
            setUpRecyclerView()
            updateTotals()
            setUpSpendingChart(calenderconstant.toString())
        } else {
            Toast.makeText(this, "Unable to access SMS content.", Toast.LENGTH_SHORT).show()
        }
    }
//    private fun setUpSpendingChart(month: String) {
//        totalDebited = 0.0
//        totalCredited = 0.0
//        amtdel = 0.0
//        amtAdd = 0.0
//
//        val user = auth.currentUser
//        val userId = user?.uid
//        val entries = mutableListOf<PieEntry>()
//        val monInt = (month.toInt() + 1).toString()
//
//        if (userId == null) {
//            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // Reference for the current and previous month data
//        val currentMonthRef = FirebaseDatabase.getInstance("https://intelligenceexpensetracker-default-rtdb.asia-southeast1.firebasedatabase.app/")
//            .getReference("Users").child("$userId/transaction/2024/$monInt")
//
//        val prevMonth = if (month.toInt() == 0) "12" else month // Handle year transition
////        val prevYear = 2024 // Adjust year if needed
//        val prevMonthRef = FirebaseDatabase.getInstance("https://intelligenceexpensetracker-default-rtdb.asia-southeast1.firebasedatabase.app/")
//            .getReference("Users").child("$userId/transaction/2024/$prevMonth")
//
//        prevMonthRef.get()
//            .addOnSuccessListener { prevSnapshot ->
//                val previncome= prevSnapshot.child("income").getValue(Double::class.java) ?: 0.0
//                val prevexpense = prevSnapshot.child("expense").getValue(Double::class.java) ?: 0.0
//                val prevadd = prevSnapshot.child("addamt").getValue(Double::class.java) ?: 0.0
//
//                val prevdel = prevSnapshot.child("delamt").getValue(Double::class.java) ?: 0.0
//
//                Toast.makeText(this,"${previncome+prevexpense+prevdel+prevadd}", Toast.LENGTH_SHORT).show()
//
//
//                // Fetch current month data
//                currentMonthRef.get()
//                    .addOnSuccessListener { dataSnapshot ->
//                        if (dataSnapshot.exists()) {
//                            totalCredited = dataSnapshot.child("income").getValue(Double::class.java) ?: 0.0
//                            amtAdd = dataSnapshot.child("addamt").getValue(Double::class.java) ?: 0.0
//                            totalDebited = dataSnapshot.child("expense").getValue(Double::class.java) ?: 0.0
//                            amtdel = dataSnapshot.child("delamt").getValue(Double::class.java) ?: 0.0
//
//                            // Include previous month's balance in current income
//                            totalCredited = totalCredited + amtAdd + (previncome+prevadd)-(prevexpense+prevdel)
//                            totalDebited = totalDebited - amtdel
//
//                            val balanceAmount = totalCredited - totalDebited
//                            entries.add(PieEntry(totalCredited.toFloat(), "Income"))
//                            entries.add(PieEntry(totalDebited.toFloat(), "Expense"))
//                            if (balanceAmount > 0) {
//                                entries.add(PieEntry(balanceAmount.toFloat(), "Balance"))
//                            }
//
//                            val dataSet = PieDataSet(entries, "Monthly Financial Summary").apply {
//                                colors = listOf(
//                                    Color.GREEN,
//                                    Color.RED,
//                                    Color.BLUE
//                                ) // Colors for income, expense, balance
//                                sliceSpace = 2f
//                                valueTextSize = 12f
//                            }
//
//                            // Update the chart with the data
//                            spendingChart.apply {
//                                data = PieData(dataSet)
//                                description.isEnabled = false
//                                legend.isEnabled = true
//                                animateY(1000)
//                                invalidate()
//                            }
//
//                            // Update UI
//                            totalIncome.text = getString(R.string.income_format, totalCredited)
//                            totalExpenses.text = getString(R.string.expense_format, totalDebited)
//                            balance.text = getString(R.string.balance_format, balanceAmount)
//
//                            // Save the current month's balance
//                            currentMonthRef.child("balance").setValue(balanceAmount)
//                        } else {
//                            Toast.makeText(this, "No data for the current month", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                    .addOnFailureListener { exception ->
//                        Toast.makeText(this, "Failed to fetch current month data: ${exception.message}", Toast.LENGTH_SHORT).show()
//                    }
//            }
//            .addOnFailureListener { exception ->
//                Toast.makeText(this, "Failed to fetch previous month data: ${exception.message}", Toast.LENGTH_SHORT).show()
//            }
//    }


    private fun setUpSpendingChart(month: String) {
        totalDebited=0.0
        totalCredited=0.0
        amtdel=0.0
        amtAdd=0.0
        val user = auth.currentUser
        val userId = user?.uid
        val entries = mutableListOf<PieEntry>()
        val monInt = (month.toInt() + 1).toString()
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Initialize Firebase Database reference pointing to the specific user's income and expense for the given month
        val dbRef = userId?.let {
            FirebaseDatabase.getInstance("https://intelligenceexpensetracker-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Users").child("$it/transaction/2024/$monInt")

        }
        if (dbRef != null) {
            dbRef.get()
                .addOnSuccessListener { dataSnapshot ->
                    if (dataSnapshot.exists()) {


                        // Fetch income and expense values from the database
                        totalCredited =
                            dataSnapshot.child("income").getValue(Double::class.java) ?: 0.0
                        amtAdd=dataSnapshot.child("addamt").getValue(Double::class.java) ?: 0.0
                        totalDebited =
                            dataSnapshot.child("expense").getValue(Double::class.java) ?: 0.0
                        amtdel=dataSnapshot.child("delamt").getValue(Double::class.java) ?: 0.0
                        totalCredited=totalCredited+amtAdd
                        totalDebited=totalDebited-amtdel
                        entries.add(PieEntry(totalCredited.toFloat(), "Income"))
                        entries.add(PieEntry(totalDebited.toFloat(), "Expense"))
                        val balanceAmount = totalCredited - totalDebited
                        if (balanceAmount > 0) {
                            entries.add(PieEntry(balanceAmount.toFloat(), "Balance"))
                        }
                        val dataSet = PieDataSet(entries, "Monthly Financial Summary").apply {
                            colors = listOf(
                                Color.GREEN,
                                Color.RED,
                                Color.BLUE
                            ) // Colors for income, expense, balance
                            sliceSpace = 2f
                            valueTextSize = 12f
                        }

                        // Update the chart with the data
                        spendingChart.apply {
                            data = PieData(dataSet)
                            description.isEnabled = false
                            legend.isEnabled = true
                            animateY(1000)
                            invalidate() // Refresh chart with new data
                        }
                        // Create the PieDataSet and configure it
                        totalIncome.text = getString(R.string.income_format, totalCredited)
                        totalExpenses.text = getString(R.string.expense_format, totalDebited)
                        balance.text =
                            getString(R.string.balance_format, totalCredited - totalDebited)

                    } else {
                        Toast.makeText(this, "Failed fetch data:}", Toast.LENGTH_SHORT).show()

                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Failed to fetch data: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }


    private fun setUpRecyclerView() {
        val smsMessages = smsStringBuilder.toString().trim().split("$$")
        // Find the RecyclerView
        val smsRecyclerView: RecyclerView = findViewById(R.id.smsRecyclerView)
        // Set up the RecyclerView with the adapter
        smsRecyclerView.layoutManager = LinearLayoutManager(this)
//        smsRecyclerView.adapter = SmsAdapter(smsMessages)
        smsAdapter = SmsAdapter(smsMessages.toMutableList())  // initialize adapter here
        smsRecyclerView.adapter = smsAdapter
    }

    private fun categorizeMessage(messageBody: String, address: String): Boolean {
        val lowerCaseMessage = messageBody.lowercase()
        val addressmessage = address.uppercase()
        return (lowerCaseMessage.contains("debited") || lowerCaseMessage.contains("credited")) && (addressmessage.contains(
            "SBIUPI"
        ) || addressmessage.contains("IOBCHN"))
    }

    private fun parseAmountFromSms(body: String): Double {
        // Correct regex pattern to extract amounts with "Rs."
        val pattern = Pattern.compile("[Rs.](\\d{1,9}(?:,\\d{3})*(?:\\.\\d{2})?)")
        val matcher = pattern.matcher(body)

        return if (matcher.find()) {
            matcher.group(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0
        } else {
            0.0
        }
    }

    private fun determineCategory(body: String): String {
        val lowerCaseBody = body.lowercase()
        val categoryKeywords = mapOf(
            "Food" to listOf("restaurant", "food", "cafe"),
            "Transport" to listOf("uber", "taxi", "bus"),
            "Salary" to listOf("salary"),

            "Credited" to listOf("credited"),
            "Debited" to listOf("debited"),
//            "Others" to listOf("transaction", "atm", "payment"),

        )

        for ((category, keywords) in categoryKeywords) {
            for (keyword in keywords) {
                if (lowerCaseBody.contains(keyword)) {
                    return category
                }
            }
        }
        return "General"
    }

    private fun updateTotals() {
//        Toast.makeText(this, "123", Toast.LENGTH_SHORT).show()
        val smsList = smsStringBuilder.split("$$").map { it.trim() }
        for (message in smsList) {
            val lines = message.trim().split("\n") // Split each transaction by lines
            var address = ""
            var category = ""
            var amount = 0.0
            var date = ""

            for (line in lines) {
                when {
                    line.startsWith("Address:") -> address = line.removePrefix("Address:").trim()
                    line.startsWith("Category:") -> category = line.removePrefix("Category:").trim()
                    line.startsWith("Amount:") -> amount =
                        line.removePrefix("Amount:").trim().toDoubleOrNull() ?: 0.0

                    line.startsWith("Date:") -> date = line.removePrefix("Date:").trim()
                }
            }
            if (address.isNotEmpty() && category.isNotEmpty() && date.isNotEmpty()) {
                transactionList.add(AmountList(address, date, amount, category))
            }

        }
        for (transaction in transactionList) {
            println("Category: ${transaction.category}, Amount: ${transaction.amount}")
            if (transaction.category == "Salary" || transaction.category == "Credited") {
                totalCredited += transaction.amount
            } else {
                totalDebited += transaction.amount
            }
        }
    }


    private fun showMonthYearPicker() {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            this,
            { _, year, month, _ ->
                // Update TextView with selected month and year
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                updateMonthYear(calendar)
                if (::smsAdapter.isInitialized) {
                    clearRecyclerView()
                }
                readSmsFromUser()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Hide day picker in DatePickerDialog
        dialog.datePicker.findViewById<View>(
            resources.getIdentifier("android:id/day", null, null)
        )?.visibility = View.GONE

        dialog.show()
    }

    private fun updateMonthYear(calendar: Calendar) {
        val format = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        monthYearPicker.text = format.format(calendar.time)
    }

    private fun clearRecyclerView() {
        totalDebited = 0.0;
        totalCredited = 0.0;
        transactions.clear()
        transactionList.clear()
        smsStringBuilder.setLength(0)
        smsAdapter.clearSmsList()// Clear adapter's data list directly
        smsAdapter.notifyDataSetChanged() // Notify adapter to update RecyclerView
    }
    fun getCalendarMonth(month: String): Int? {
        return when (month) {
            "January 2024" -> 1
            "February 2024" -> 2
            "March 2024" -> 3
            "April 2024" ->4
            "May 2024" -> 5
            "June 2024" -> 6
            "July 2024" -> 7
            "August 2024" -> 8
            "September 2024" -> 9
            "October 2024" -> 10
            "November 2024" -> 11
            "December 2024" -> 12
            else -> null // Return null if the month name is invalid
        }
    }

    private fun showTransactionDialog(action: String) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_transaction, null)
        builder.setView(dialogView)

        val inputMonth = dialogView.findViewById<EditText>(R.id.inputMonth)
        val inputIncome = dialogView.findViewById<EditText>(R.id.inputIncome)
        val inputExpense = dialogView.findViewById<EditText>(R.id.inputExpense)
        inputMonth.isFocusable = false
        inputMonth.isClickable = false
        val datemont = monthYearPicker.text.toString()

        val calenderconstant = getCalendarMonth(datemont)
        Toast.makeText(this, "$datemont+$calenderconstant", Toast.LENGTH_SHORT).show()

        inputMonth.setText(datemont)
        builder.setTitle("$action Transaction")
            .setPositiveButton(action) { _, _ ->
                val monthinp = inputMonth.text.toString().toIntOrNull() ?: 0
                val income = inputIncome.text.toString().toDoubleOrNull() ?: 0.0
                val expense = inputExpense.text.toString().toDoubleOrNull() ?: 0.0

                if (action == "Add") {
                    // Assume `newTransactionAmount` is the amount you entered in the dialog
                    UpdateIncomeExpenseToFirebaseone(income, expense, calenderconstant.toString())

                }
                if (action == "Delete") {
                    DeleteIncomeExpenseToFirebaseone(income, expense, calenderconstant.toString())

                }
//                    finaltotalCredited=totalCredited
//                    finaltotalDebited=totalDebited
                // Save updated totals to Firebase
//                    val month = monthinp
//                    val year = Calendar.getInstance().get(Calendar.YEAR)
//
//                     updateAdd()

            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.create().show()
    }

    private fun updateAdd() {
        totalIncome.text = getString(R.string.income_format, finaltotalCredited)
        totalExpenses.text = getString(R.string.expense_format, finaltotalDebited)
        balance.text = getString(R.string.balance_format, finaltotalCredited - finaltotalDebited)
    }

    private fun UpdateIncomeExpenseToFirebaseone(income: Double, expense: Double, month: String) {
        val user = auth.currentUser
        val userId = user?.uid
//        val entries = mutableListOf<PieEntry>()
//        val monInt=(month.toInt()+1).toString()
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Initialize Firebase Database reference pointing to the specific user's income and expense for the given month
        val dbRef = userId?.let {
            FirebaseDatabase.getInstance("https://intelligenceexpensetracker-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Users").child("$it/transaction/2024/$month")

        }


        // Save or update data
        if (dbRef != null) {
            dbRef.get()
                .addOnSuccessListener { dataSnapshot ->
                    if (dataSnapshot.exists()) {


                        // Fetch income and expense values from the database
                         var alreadyCredited = dataSnapshot.child("addamt").getValue(Double::class.java) ?: 0.0
                         var alreadyDebited = dataSnapshot.child("delamt").getValue(Double::class.java) ?: 0.0
                        alreadyCredited+=income
                        alreadyDebited+=expense
                        val data = mapOf(
                            "addamt" to alreadyCredited,
                            "delamt" to alreadyDebited
                        )
                        dbRef.updateChildren(data).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                setUpSpendingChart((month.toInt()-1).toString())

                            } else {
                                println("Error updating income and expense: ${task.exception}")
                            }
                        }
                    }
                }
        }
    }
    private fun DeleteIncomeExpenseToFirebaseone(income: Double, expense: Double, month: String) {
        val user = auth.currentUser
        val userId = user?.uid
//        val entries = mutableListOf<PieEntry>()
//        val monInt=(month.toInt()+1).toString()
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Initialize Firebase Database reference pointing to the specific user's income and expense for the given month
        val dbRef = userId?.let {
            FirebaseDatabase.getInstance("https://intelligenceexpensetracker-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Users").child("$it/transaction/2024/$month")

        }


        // Save or update data
        if (dbRef != null) {
            dbRef.get()
                .addOnSuccessListener { dataSnapshot ->
                    if (dataSnapshot.exists()) {


                        // Fetch income and expense values from the database
                        var alreadyCredited = dataSnapshot.child("addamt").getValue(Double::class.java) ?: 0.0
                        var alreadyDebited = dataSnapshot.child("delamt").getValue(Double::class.java) ?: 0.0
                        alreadyCredited-=income
                        alreadyDebited-=expense
                        val data = mapOf(
                            "addamt" to alreadyCredited,
                            "delamt" to alreadyDebited
                        )
                        dbRef.updateChildren(data).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                setUpSpendingChart((month.toInt()-1).toString())


                            } else {
                                println("Error updating income and expense: ${task.exception}")
                            }
                        }
                    }
                }
        }
    }

}


