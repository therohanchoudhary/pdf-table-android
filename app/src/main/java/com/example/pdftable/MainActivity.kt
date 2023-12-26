package com.example.pdftable

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var generatePDFBtn: Button

    private var pageHeight = 1120
    private var pageWidth = 792

    private lateinit var bmp: Bitmap
    private lateinit var scaledbmp: Bitmap

    private var PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        generatePDFBtn = findViewById(R.id.idBtnGeneratePdf)

        bmp = ContextCompat.getDrawable(this, R.drawable.ic_launcher_background)?.toBitmap()!!
        scaledbmp = Bitmap.createScaledBitmap(bmp, 140, 140, false)

        if (checkPermissions()) {
            Toast.makeText(this, "Permissions Granted..", Toast.LENGTH_SHORT).show()
        } else {
            requestPermission()
        }

        generatePDFBtn.setOnClickListener {
            generatePDF()
        }
    }

    private fun getRecordMap(): HashMap<String, List<User>> {
        val users = listOf(
            User("001", "John Doe", "28", "New York"),
            User("002", "Jane Smith", "35", "Los Angeles"),
            User("004", "Emily Chen", "30", "Sydney"),
            User("003", "Alex Lee", "22", "London"),
            User("006", "Sarah Turner", "27", "London"),
            User("007", "James Brown", "31", "London"),
            User("005", "David Kim", "25", "Seoul"),
            User("008", "Maria Garcia", "33", "Madrid"),
            User("012", "Sofia Rodriguez", "28", "Madrid"),
            User("009", "Roberto Silva", "29", "Rio de Janeiro"),
            User("013", "Gabriela Oliveira", "31", "Rio de Janeiro"),
            User("010", "Mei Chen", "26", "Beijing"),
            User("014", "Li Wei", "29", "Beijing"),
            User("011", "Yusuke Tanaka", "32", "Tokyo"),
            User("015", "Aiko Yamamoto", "34", "Tokyo")
        )
        val map = hashMapOf<String, List<User>>()
        for (user in users) {
            val locationKey = user.location
            if (map.containsKey(locationKey)) {
                val userList = map[locationKey]!!.toMutableList()
                userList.add(user)
                map[locationKey] = userList
            } else {
                map[locationKey] = listOf(user)
            }
        }
        return map
    }



    private fun generatePDF() {
        val pdfDocument = PdfDocument()
        val paint = Paint()

        val recordMap = getRecordMap()

        val myPageInfo: PdfDocument.PageInfo? =
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()

        val myPage: PdfDocument.Page = pdfDocument.startPage(myPageInfo)
        val canvas: Canvas = myPage.canvas

        drawTableHeaders(canvas, paint)
        drawTableContent(canvas, paint, recordMap)

        pdfDocument.finishPage(myPage)

        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "GFG.pdf")

        try {
            FileOutputStream(file).use { fileOutputStream ->
                pdfDocument.writeTo(fileOutputStream)
                Toast.makeText(
                    applicationContext,
                    "PDF file generated.. ${file.path}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                applicationContext,
                "Fail to generate PDF file..",
                Toast.LENGTH_SHORT
            ).show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun drawTableHeaders(canvas: Canvas, paint: Paint) {
        paint.apply {
            textAlign = Paint.Align.LEFT
            typeface = Typeface.DEFAULT_BOLD
            textSize = HEADER_TEXT_SIZE
            color = ContextCompat.getColor(this@MainActivity, R.color.purple_200)
            style = Paint.Style.STROKE
            strokeWidth = BORDER_STROKE_WIDTH
        }

        val headers = arrayOf("Location", "Name", "Age", "User Id")

        var startX = 56F

        for ((index, header) in headers.withIndex()) {
            canvas.drawText(header, startX, TABLE_HEADER_HEIGHT, paint)
            startX += columnWidths[index]
        }
    }

    private fun drawTableContent(canvas: Canvas, paint: Paint, recordMap: Map<String, List<User>>) {
        paint.apply {
            typeface = Typeface.DEFAULT
            textSize = CONTENT_TEXT_SIZE
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = BORDER_STROKE_WIDTH
        }

        var startY = TABLE_HEADER_HEIGHT

        for ((location, users) in recordMap) {
            val span = users.size

            drawCell(canvas, paint, 56F, startY, columnWidths[0], TABLE_HEADER_HEIGHT * span, location)

            var userStartX: Float
            var userStartY = startY

            for (user in users) {
                userStartX = 56F + columnWidths[0]
                drawCell(canvas, paint, userStartX, userStartY, columnWidths[1], TABLE_HEADER_HEIGHT, user.name)
                userStartX += columnWidths[1]

                drawCell(canvas, paint, userStartX, userStartY, columnWidths[2], TABLE_HEADER_HEIGHT, user.age)
                userStartX += columnWidths[2]

                drawCell(canvas, paint, userStartX, userStartY, columnWidths[3], TABLE_HEADER_HEIGHT, user.userId)
                userStartX += columnWidths[3]

                userStartY += TABLE_HEADER_HEIGHT
            }
            startY += TABLE_HEADER_HEIGHT * span
        }
    }

    private fun drawCell(canvas: Canvas, paint: Paint, startX: Float, startY: Float, width: Float, height: Float, text: String) {
        canvas.drawRect(startX, startY, startX + width, startY + height, paint)
        canvas.drawText(text, startX + 5, startY + 25, paint)
    }



    private fun checkPermissions(): Boolean {
        val writeStoragePermission = ContextCompat.checkSelfPermission(
            applicationContext,
            WRITE_EXTERNAL_STORAGE
        )

        val readStoragePermission = ContextCompat.checkSelfPermission(
            applicationContext,
            READ_EXTERNAL_STORAGE
        )

        return writeStoragePermission == PackageManager.PERMISSION_GRANTED
                && readStoragePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE), PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1]
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this, "Permission Denied..", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    companion object {
        private const val TABLE_HEADER_HEIGHT = 40F
        private const val BORDER_STROKE_WIDTH = 2F
        private const val HEADER_TEXT_SIZE = 15F
        private const val CONTENT_TEXT_SIZE = 12F
        private val columnWidths = floatArrayOf(150F, 200F, 50F, 100F)
    }
}
