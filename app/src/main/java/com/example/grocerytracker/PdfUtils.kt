package com.example.grocerytracker

import android.content.Context
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

object PdfUtils {
    fun savePdf(context: Context, fileName: String, document: PdfDocument): File? {
        val file = File(context.getExternalFilesDir(null), fileName)
        return try {
            val fos = FileOutputStream(file)
            document.writeTo(fos)
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
