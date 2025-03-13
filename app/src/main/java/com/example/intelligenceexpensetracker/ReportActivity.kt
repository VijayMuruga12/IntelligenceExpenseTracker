package com.example.intelligenceexpensetracker

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.style.LineBreak.Companion.Paragraph
import com.google.firebase.database.*
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.auth.FirebaseAuth

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.*
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
//import com.itextpdf.layout.property.TextAlignment
//import com.itextpdf.layout.property.UnitValue
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ReportActivity : AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    private lateinit var database: DatabaseReference
    private lateinit var barChart: BarChart
    private lateinit var auth: FirebaseAuth
//    private  lateinit var exportButton:Button

    //    private lateinit var registerButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report)
        barChart = findViewById(R.id.incomeVsExpensesChart)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://intelligenceexpensetracker-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference()
        retrieveDataForBarChart()
        val exportButton: Button = findViewById(R.id.exportButton)
        exportButton.setOnClickListener {
//            generateEmptyPdf()
            fetchDataAndGeneratePdf()
        }

    }
//    private fun generateEmptyPdf() {
//        // Create a PdfDocument instance
//        val pdfDocument = android.graphics.pdf.PdfDocument()
//
//        // Create a page description with a specific width and height
//        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
//
//        // Start a page
//        val page = pdfDocument.startPage(pageInfo)
//
//        // Get the Canvas object to draw on the PDF
//        val canvas = page.canvas
//
//        // Optionally, you can draw something on the canvas (like a title)
//        canvas.drawText("Report Summary", 100f, 100f, android.graphics.Paint().apply {
//            textSize = 16f
//            color = Color.BLACK
//        })
//
//        // Finish the page
//        pdfDocument.finishPage(page)
//
//        // Save the document to a file
//        val filePath = "${externalCacheDir?.absolutePath}/EmptyReport.pdf"
//        try {
//            val file = java.io.File(filePath)
//            pdfDocument.writeTo(java.io.FileOutputStream(file))
//            Toast.makeText(this, "PDF generated successfully: $filePath", Toast.LENGTH_SHORT).show()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Toast.makeText(this, "Failed to generate PDF.", Toast.LENGTH_SHORT).show()
//        } finally {
//            // Close the document
//            pdfDocument.close()
//        }
//    }

    private fun retrieveDataForBarChart() {
//        Toast.makeText(this@ReportActivity, "Retrieve data.", Toast.LENGTH_SHORT).show()

        val user = auth.currentUser
        val userId = user?.uid
        val incomeData = mutableListOf<BarEntry>()
        val expenseData = mutableListOf<BarEntry>()
        val balanceData = mutableListOf<BarEntry>()

        if (userId != null) {
//            Toast.makeText(this@ReportActivity, "if data.", Toast.LENGTH_SHORT).show()

            database.child("Users/$userId/transaction/2024").addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    for (monthSnapshot in snapshot.children) {
                        val monthKey = monthSnapshot.key
                        if (monthKey == null) {
                            // Handle the case where the month key is missing or log it for debugging
                            Log.w("ReportsActivity", "Month key is null, skipping entry")
                            continue
                        }

                        val month = monthKey.toIntOrNull()
                        if (month == null) {
                            // Log the error if parsing to integer fails
                            Log.w("ReportsActivity", "Invalid month key format: $monthKey")
                            continue
                        }

                        // Now you can safely use the 'month' variable as an integer
                        var income = monthSnapshot.child("income").getValue(Int::class.java) ?: 0
                        var expense = monthSnapshot.child("expense").getValue(Int::class.java) ?: 0
                        val delAmt = monthSnapshot.child("delamt").getValue(Int::class.java) ?: 0
                        val addAmt = monthSnapshot.child("addamt").getValue(Int::class.java) ?: 0
                        income += addAmt
                        expense -= delAmt
                        var balance=income-expense
                        if(balance<0){
                            balance=0
                        }
//                        Toast.makeText(this@ReportActivity, "$month+$income+$expense", Toast.LENGTH_SHORT).show()

                        // Calculate balance if needed and add data to chart
                        incomeData.add(BarEntry(month.toFloat(), income.toFloat()))
                        expenseData.add(BarEntry(month.toFloat(), expense.toFloat()))
                        balanceData.add(BarEntry(month.toFloat(), balance.toFloat()))
                    }
                    Log.w("ReportsActivity", "income month key format: $incomeData")
                    Log.w("ReportsActivity", "expense month key format: $expenseData")

                    Log.w("ReportsActivity", "balance month key format: $balanceData")

                    displayBarChart(incomeData, expenseData, balanceData)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ReportActivity, "Failed to retrieve data.", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


    private fun displayBarChart(incomeData: List<BarEntry>, expenseData: List<BarEntry>, balanceData: List<BarEntry>) {
        // Create DataSets with distinct colors
        val incomeDataSet = BarDataSet(incomeData, "Income").apply { color = Color.GREEN }
        val expenseDataSet = BarDataSet(expenseData, "Expense").apply { color = Color.RED }
        val balanceDataSet = BarDataSet(balanceData, "Balance").apply { color = Color.BLUE }

        // Combine all datasets into BarData
        val data = BarData(incomeDataSet, expenseDataSet, balanceDataSet)
        data.barWidth = 0.12f // Set bar width

        // Set data to bar chart
        barChart.data = data

        // Configure the X-axis for monthly data
        barChart.xAxis.apply {
            granularity = 0.5f
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            setLabelCount(12, true) // Show all months (1-12)
            axisMinimum = 0f // Ensure x-axis starts at 0
            axisMaximum = 12f // Ensure x-axis ends at 12
        }

        // Configure the Y-axis if needed
        barChart.axisLeft.axisMinimum = 0f // Start Y-axis at 0
        barChart.axisRight.isEnabled = false // Optional: Disable right Y-axis

        // Group the bars for each month
        val groupSpace = 0.4f
        val barSpace = 0.1f
        barChart.groupBars(0f, groupSpace, barSpace)

        // Refresh the chart
        barChart.invalidate()
    }
    private fun fetchDataAndGeneratePdf() {
//        val userId = "your_user_id" // Replace with dynamic user ID if needed
        val user = auth.currentUser
        val userId = user?.uid
        if (userId != null) {
            database.child("Users/$userId").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.child("userName").value.toString()
                    val email = snapshot.child("email").value.toString()
                    val monthlyIncomeList = mutableListOf<MonthlyIncome>()

                    snapshot.child("transaction").children.forEach { yearSnapshot ->
                        yearSnapshot.children.forEach { monthSnapshot ->
                            val month = monthSnapshot.key?.toIntOrNull() ?: 0
                            val income = monthSnapshot.child("income").value.toString().toDoubleOrNull() ?: 0.0
                            val delamt= monthSnapshot.child("delamt").value.toString().toDoubleOrNull() ?: 0.0
                            val addamt=  monthSnapshot.child("delamt").value.toString().toDoubleOrNull() ?: 0.0

                            val expense = monthSnapshot.child("expense").value.toString().toDoubleOrNull() ?: 0.0
                            var balance=(income+addamt)-(expense+delamt);
                            var fbal=0;
                            if(balance.toInt() <0){
                                fbal=0;
                            }else{
                                fbal=balance.toInt();
                            }
                            monthlyIncomeList.add(MonthlyIncome(month, income+addamt,expense+delamt,fbal.toDouble()))
                        }
                    }

                    val todayDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
                    generatePdfReport(username, email, todayDate, monthlyIncomeList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ReportActivity, "Failed to fetch data: ${error.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

//    private fun generatePdfReport(
//        username: String,
//        email: String,
//        todayDate: String,
//        monthlyIncomeList: List<MonthlyIncome>
//    ) {
//        // Define the file path in the Documents directory
//        val documentsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//        val pdfFile = File(documentsPath, "IncomeReport_${username}.pdf")
//
//        try {
//            // Ensure the Documents directory exists
//            if (!documentsPath.exists()) {
//                documentsPath.mkdirs()
//            }
//
//            val pdfWriter = PdfWriter(pdfFile)
//            val pdfDocument = PdfDocument(pdfWriter)
//            val document = Document(pdfDocument)
//
//            // Add Title
//            val title = Paragraph("Monthly Income Report")
//                .setTextAlignment(TextAlignment.CENTER)
//                .setFontSize(20f)
//            document.add(title)
//
//            // Add User Details
//            val userDetails = """
//            Username: $username
//            Email: $email
//            Date: $todayDate
//        """.trimIndent()
//            val userDetailsParagraph = Paragraph(userDetails)
//                .setMarginTop(10f)
//                .setFontSize(14f)
//            document.add(userDetailsParagraph)
//
//            // Add Income Table
//            val table = Table(UnitValue.createPercentArray(floatArrayOf(1f, 2f))).useAllAvailableWidth()
//
//            // Table Header
//            table.addCell(Cell().add(Paragraph("Month")).setBold())
//            table.addCell(Cell().add(Paragraph("Income")).setBold())
//
//            // Populate Table Data
//            monthlyIncomeList.forEach { income ->
//                table.addCell(Cell().add(Paragraph(getMonthName(income.month))))
//                table.addCell(Cell().add(Paragraph(income.income.toString())))
//            }
//
//            document.add(table)
//
//            // Close Document
//            document.close()
//
//            // Notify the user about the generated PDF
//            Toast.makeText(this, "PDF saved to Documents: ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Toast.makeText(this, "Failed to generate PDF: ${e.message}", Toast.LENGTH_LONG).show()
//        }
//    }
//    private fun generatePdfReport(
//        username: String,
//        email: String,
//        todayDate: String,
//        monthlyIncomeList: List<MonthlyIncome>
//    ) {
//        val pdfDocument = android.graphics.pdf.PdfDocument()
//
//        // Page info for A4 size (595x842 points)
//        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
//        val page = pdfDocument.startPage(pageInfo)
//        val canvas = page.canvas
//
//        val paint = android.graphics.Paint()
//        paint.textSize = 16f
//        paint.color = Color.BLACK
//
//        // Draw Title
//        canvas.drawText("Monthly Income Report", 220f, 50f, paint)
//
//        // Draw User Details
//        canvas.drawText("Username: $username", 50f, 100f, paint)
//        canvas.drawText("Email: $email", 50f, 130f, paint)
//        canvas.drawText("Date: $todayDate", 50f, 160f, paint)
//
//        // Table Header
//        paint.textSize = 14f
//        canvas.drawText("Month", 50f, 200f, paint)
//        canvas.drawText("Income", 200f, 200f, paint)
//    canvas.drawText("Expense", 200f, 200f, paint)
//    canvas.drawText("Balance", 200f, 200f, paint)
//
//
//        // Populate Table Data
//        var yPosition = 230f
//        monthlyIncomeList.forEach { income ->
//            canvas.drawText(getMonthName(income.month), 50f, yPosition, paint)
//            canvas.drawText(income.income.toString(), 200f, yPosition, paint)
//            canvas.drawText(income.expense.toString(), 350f, yPosition, paint)
//            canvas.drawText(income.balance.toString(), 500f, yPosition, paint)
//
//
//            yPosition += 30f
//        }
//
//        pdfDocument.finishPage(page)
//
//        // Save PDF to Documents folder
//        val fileName = "IncomeReport_${username}_${System.currentTimeMillis()}.pdf"
//        val resolver = contentResolver
//        val contentValues = android.content.ContentValues().apply {
//            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
//            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
//            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOCUMENTS)
//        }
//
//        val uri = resolver.insert(android.provider.MediaStore.Files.getContentUri("external"), contentValues)
//        if (uri != null) {
//            try {
//                resolver.openOutputStream(uri)?.use { outputStream ->
//                    pdfDocument.writeTo(outputStream)
//                    Toast.makeText(this, "PDF saved in Documents: $fileName", Toast.LENGTH_SHORT).show()
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                Toast.makeText(this, "Failed to save PDF.", Toast.LENGTH_SHORT).show()
//            } finally {
//                pdfDocument.close()
//            }
//        } else {
//            Toast.makeText(this, "Failed to create file URI.", Toast.LENGTH_SHORT).show()
//            pdfDocument.close()
//        }
//    }
//

    private fun generatePdfReport(
        username: String,
        email: String,
        todayDate: String,
        monthlyIncomeList: List<MonthlyIncome>
    ) {
        val pdfDocument = android.graphics.pdf.PdfDocument()

        // Page info for A4 size (595x842 points)
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = android.graphics.Paint().apply {
            textSize = 16f
            color = Color.BLACK
        }

        // Draw Title
        canvas.drawText("Monthly Income Report", 220f, 50f, paint)

        // Draw User Details
        canvas.drawText("Username: $username", 50f, 100f, paint)
        canvas.drawText("Email: $email", 50f, 130f, paint)
        canvas.drawText("Date: $todayDate", 50f, 160f, paint)

        // Table Header
        paint.textSize = 14f
        paint.style = android.graphics.Paint.Style.FILL // Ensure text fills properly

        val startX = 50f // Start position of the table (X)
        val startY = 200f // Start position of the table (Y)
        val cellHeight = 40f // Height of each table cell
        val columnWidths = floatArrayOf(100f, 130f, 130f, 100f) // Column widths

        // Draw header cells
        val headers = listOf("Month", "Income", "Expense", "Balance")
        var xPosition = startX
        headers.forEachIndexed { index, header ->
            // Draw cell border
            paint.style = android.graphics.Paint.Style.STROKE
            canvas.drawRect(
                xPosition,
                startY,
                xPosition + columnWidths[index],
                startY + cellHeight,
                paint
            )

            // Draw header text
            paint.style = android.graphics.Paint.Style.FILL
            canvas.drawText(header, xPosition + 10f, startY + 20f, paint)

            xPosition += columnWidths[index]
        }

        // Populate Table Data
        var yPosition = startY + cellHeight
        monthlyIncomeList.forEach { income ->
            xPosition = startX
            val rowData = listOf(
                getMonthName(income.month),
                income.income.toString(),
                income.expense.toString(),
                income.balance.toString()
            )

            rowData.forEachIndexed { index, data ->
                // Draw cell border
                paint.style = android.graphics.Paint.Style.STROKE
                canvas.drawRect(
                    xPosition,
                    yPosition,
                    xPosition + columnWidths[index],
                    yPosition + cellHeight,
                    paint
                )

                // Draw cell data
                paint.style = android.graphics.Paint.Style.FILL
                canvas.drawText(data, xPosition + 10f, yPosition + 20f, paint)

                xPosition += columnWidths[index]
            }

            yPosition += cellHeight
        }

        pdfDocument.finishPage(page)

        // Save PDF to Documents folder
        val fileName = "IncomeReport_${username}_${System.currentTimeMillis()}.pdf"
        val resolver = contentResolver
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOCUMENTS)
        }

        val uri = resolver.insert(android.provider.MediaStore.Files.getContentUri("external"), contentValues)
        if (uri != null) {
            try {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                    Toast.makeText(this, "PDF saved in Documents: $fileName", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to save PDF.", Toast.LENGTH_SHORT).show()
            } finally {
                pdfDocument.close()
            }
        } else {
            Toast.makeText(this, "Failed to create file URI.", Toast.LENGTH_SHORT).show()
            pdfDocument.close()
        }
    }



    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> "Unknown"
        }
    }


}
data class MonthlyIncome(
    val month: Int = 0,
    val income: Double = 0.0,
    val expense:Double=0.0,
    val balance:Double=0.0
)
