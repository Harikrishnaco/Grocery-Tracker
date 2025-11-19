package com.example.grocerytracker

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grocerytracker.databinding.ActivityNewSaleBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class NewSaleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewSaleBinding
    private val vm: GroceryViewModel by viewModels()
    private lateinit var adapter: SaleProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewSaleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = SaleProductAdapter(mutableListOf())
        binding.recyclerSaleProducts.layoutManager = LinearLayoutManager(this)
        binding.recyclerSaleProducts.adapter = adapter

        vm.products.observe(this) { list ->
            adapter.update(list)
        }

        vm.loadProducts()

        binding.btnGenerateReceipt.setOnClickListener {
            val selected = adapter.getSelectedItems() // List<Pair<Product, Int>>
            if (selected.isEmpty()) {
                Toast.makeText(this, "Select items (quantity > 0)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnGenerateReceipt.isEnabled = false

            vm.createSaleAndItemsTransactional(
                selected,
                onComplete = { saleId ->
                    generatePdfReceipt(
                        saleId.toInt(),
                        selected,
                        selected.sumOf { it.first.price * it.second }
                    )
                    runOnUiThread {
                        Toast.makeText(this, "Sale recorded (ID = $saleId)", Toast.LENGTH_SHORT).show()
                        binding.btnGenerateReceipt.isEnabled = true
                    }
                },
                onError = { err ->
                    runOnUiThread {
                        Toast.makeText(this, "Error: $err", Toast.LENGTH_LONG).show()
                        binding.btnGenerateReceipt.isEnabled = true
                    }
                }
            )
        }

    }

    private fun generatePdfReceipt(saleId: Int, items: List<Pair<Product, Int>>, total: Double) {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        paint.textSize = 10f

        var y = 20
        canvas.drawText("Grocery Tracker Receipt", 10f, y.toFloat(), paint); y += 20
        canvas.drawText("Sale ID: $saleId", 10f, y.toFloat(), paint); y += 20
        canvas.drawText("Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}", 10f, y.toFloat(), paint); y += 20

        for ((prod, qty) in items) {
            canvas.drawText("${prod.name} x$qty  ₹${"%.2f".format(prod.price * qty)}", 10f, y.toFloat(), paint)
            y += 15
        }

        y += 10
        canvas.drawText("Total: ₹${"%.2f".format(total)}", 10f, y.toFloat(), paint)

        pdf.finishPage(page)

        val file = java.io.File(getExternalFilesDir(null), "receipt_$saleId.pdf")
        try {
            val fos = FileOutputStream(file)
            pdf.writeTo(fos)
            pdf.close()
            Toast.makeText(this, "Receipt saved: ${file.path}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving receipt: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
