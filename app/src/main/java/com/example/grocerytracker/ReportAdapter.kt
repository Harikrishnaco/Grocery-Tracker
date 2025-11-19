package com.example.grocerytracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.grocerytracker.databinding.ItemReportBinding

class ReportAdapter(private var sales: MutableList<Sale>) :
    RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(val binding: ItemReportBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun getItemCount() = sales.size

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val sale = sales[position]
        holder.binding.tvSaleId.text = "Sale ID: ${sale.id}"
        holder.binding.tvDate.text = "Date: ${sale.date}"
        holder.binding.tvTotal.text = "Total: ₹${sale.totalAmount}"
    }

    fun update(newList: List<Sale>) {
        sales.clear()
        sales.addAll(newList)
        notifyDataSetChanged()
    }
}
