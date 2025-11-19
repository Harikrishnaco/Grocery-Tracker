package com.example.grocerytracker

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grocerytracker.databinding.ActivityReportBinding

class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportBinding
    private val vm: GroceryViewModel by viewModels()
    private lateinit var adapter: ReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ReportAdapter(mutableListOf())
        binding.recyclerReports.layoutManager = LinearLayoutManager(this)
        binding.recyclerReports.adapter = adapter

        vm.sales.observe(this) { sales ->
            adapter.update(sales)
        }

        vm.loadSales()
    }
}
