package com.example.grocerytracker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProductListActivity : AppCompatActivity() {

    private val vm: GroceryViewModel by viewModels()
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        adapter = ProductAdapter(mutableListOf()) { product, action ->
            when (action) {
                "edit" -> {
                    val i = Intent(this, AddEditProductActivity::class.java)
                    i.putExtra("productId", product.id)
                    startActivity(i)
                }
                "delete" -> {
                    Toast.makeText(this, "Delete feature not implemented yet", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val recycler = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerProducts)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        val fab = findViewById<FloatingActionButton>(R.id.fabAdd)
        fab.setOnClickListener {
            startActivity(Intent(this, AddEditProductActivity::class.java))
        }

        vm.products.observe(this) { list -> adapter.update(list) }
        vm.loadProducts()
    }

    override fun onResume() {
        super.onResume()
        vm.loadProducts()
    }
}
