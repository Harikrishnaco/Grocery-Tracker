package com.example.grocerytracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class GroceryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GroceryRepository(application)

    // LiveData for UI
    private val _products = MutableLiveData<List<Product>>(emptyList())
    val products: LiveData<List<Product>> get() = _products

    private val _sales = MutableLiveData<List<Sale>>(emptyList())
    val sales: LiveData<List<Sale>> get() = _sales

    private val _lowStock = MutableLiveData<List<Product>>(emptyList())
    val lowStock: LiveData<List<Product>> get() = _lowStock

    // -----------------------
    // Loading helpers
    // -----------------------
    fun loadProducts() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) { repository.getAllProducts() }
            _products.postValue(list)
        }
    }

    fun loadSales() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) { repository.getAllSales() }
            _sales.postValue(list)
        }
    }

    fun loadLowStock() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) { repository.getLowStockProducts() }
            _lowStock.postValue(list)
        }
    }

    // -----------------------
    // Product write operations
    // -----------------------
    fun addOrUpdateProduct(product: Product, onComplete: (() -> Unit)? = null, onError: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { repository.insertProduct(product) }
                loadProducts()
                onComplete?.invoke()
            } catch (e: Exception) {
                onError?.invoke(e.message ?: "Failed to save product")
            }
        }
    }

    fun deleteProduct(product: Product, onComplete: (() -> Unit)? = null, onError: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { repository.deleteProduct(product) }
                loadProducts()
                onComplete?.invoke()
            } catch (e: Exception) {
                onError?.invoke(e.message ?: "Failed to delete product")
            }
        }
    }

    // -----------------------
    // Create sale transaction (validation + atomic insert)
    // -----------------------
    /**
     * items: List of Pair<Product, quantity>
     * onComplete: (saleId: Long) -> Unit  -- called on success
     * onError: (message: String) -> Unit  -- called on failure
     */
    fun createSaleAndItemsTransactional(
        items: List<Pair<Product, Int>>,
        onComplete: (Long) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 1) validate stock
                val ok = withContext(Dispatchers.IO) { repository.canMakeSale(items) }
                if (!ok) {
                    onError("Insufficient stock for one or more items")
                    return@launch
                }

                // 2) build Sale object (with timestamp)
                val total = items.sumOf { it.first.price * it.second }
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val sale = Sale(date = sdf.format(Date()), totalAmount = total)

                // 3) perform transactional insertion and stock update
                val saleId = withContext(Dispatchers.IO) {
                    repository.createSaleWithItemsAndReduceStock(sale, items)
                }

                // 4) refresh UI data
                loadProducts()
                loadSales()
                loadLowStock()

                // 5) callback success
                onComplete(saleId)
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Failed to create sale")
            }
        }
    }
}
