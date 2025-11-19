package com.example.grocerytracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.grocerytracker.databinding.ActivityAddEditProductBinding

class AddEditProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditProductBinding
    private val vm: GroceryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val productId = intent.getIntExtra("productId", -1)

        if (productId != -1) {
            // Edit mode — find product details (if loaded)
            vm.products.observe(this) { products ->
                val product = products.find { it.id == productId }
                product?.let {
                    binding.etName.setText(it.name)
                    binding.etCategory.setText(it.category)
                    binding.etPrice.setText(it.price.toString())
                    binding.etStock.setText(it.stock.toString())
                }
            }
            vm.loadProducts()
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString()
            val category = binding.etCategory.text.toString()
            val price = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
            val stock = binding.etStock.text.toString().toIntOrNull() ?: 0

            if (name.isBlank() || category.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val product = if (productId == -1) {
                Product(name = name, category = category, price = price, stock = stock)
            } else {
                Product(id = productId, name = name, category = category, price = price, stock = stock)
            }

            vm.addOrUpdateProduct(product) // ✅ works now
            Toast.makeText(this, "Product saved successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
