package com.example.grocerytracker

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.grocerytracker.R


class SaleProductAdapter(private var items: MutableList<Product>) : RecyclerView.Adapter<SaleProductAdapter.VH>() {
    private val qtyMap = mutableMapOf<Int, Int>()

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val etQty: EditText = view.findViewById(R.id.etQty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_sale_product, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        holder.tvName.text = p.name
        holder.tvPrice.text = "₹${"%.2f".format(p.price)}"
        holder.etQty.setText(qtyMap[p.id]?.toString() ?: "0")

        // Remove previous watcher to avoid multiple triggers
        holder.etQty.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString()?.toIntOrNull() ?: 0
                qtyMap[p.id] = q
            }
        })
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<Product>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<Pair<Product, Int>> {
        val res = mutableListOf<Pair<Product, Int>>()
        for (p in items) {
            val q = qtyMap[p.id] ?: 0
            if (q > 0) res.add(p to q)
        }
        return res
    }
}
