package com.example.grocerytracker

import android.content.Context
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GroceryRepository(context: Context) {

    private val db: AppDatabase = AppDatabase.getDatabase(context.applicationContext)
    private val productDao: ProductDao = db.productDao()
    private val saleDao: SaleDao = db.saleDao()
    private val saleItemDao: SaleItemDao = db.saleItemDao()

    // -----------------------
    // Product operations
    // -----------------------
    suspend fun insertProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.insert(product)
    }

    suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.update(product)
    }

    suspend fun deleteProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.delete(product)
    }

    suspend fun getAllProducts(): List<Product> = withContext(Dispatchers.IO) {
        productDao.getAllProducts()
    }

    suspend fun getLowStockProducts(): List<Product> = withContext(Dispatchers.IO) {
        productDao.getLowStock()
    }

    // -----------------------
    // Sale / SaleItem operations
    // -----------------------
    suspend fun insertSale(sale: Sale): Long = withContext(Dispatchers.IO) {
        saleDao.insertSale(sale)
    }

    suspend fun getAllSales(): List<Sale> = withContext(Dispatchers.IO) {
        saleDao.getAllSales()
    }

    suspend fun insertSaleItem(item: SaleItem): Long = withContext(Dispatchers.IO) {
        saleItemDao.insertSaleItem(item)
    }

    // -----------------------
    // Validation helper
    // -----------------------
    suspend fun canMakeSale(items: List<Pair<Product, Int>>): Boolean = withContext(Dispatchers.IO) {
        items.all { (prod, qty) -> prod.stock >= qty }
    }

    // -----------------------
    // Transactional operation: insert sale, items, reduce stock (suspending)
    // -----------------------
    // Returns saleId (Long)
    suspend fun createSaleWithItemsAndReduceStock(
        sale: Sale,
        items: List<Pair<Product, Int>> // Pair(Product, qty)
    ): Long = withContext(Dispatchers.IO) {
        var saleId: Long = -1L

        // Use Room's suspending withTransaction so we can call suspend DAO methods inside
        db.withTransaction {
            // insert sale -> returns Long
            saleId = saleDao.insertSale(sale)

            // insert sale items and update product stock
            for ((prod, qty) in items) {
                val saleItem = SaleItem(
                    saleId = saleId.toInt(), // your entity uses Int for saleId
                    productId = prod.id,
                    quantity = qty,
                    price = prod.price
                )
                saleItemDao.insertSaleItem(saleItem)

                val newStock = (prod.stock - qty).coerceAtLeast(0)
                val updatedProduct = prod.copy(stock = newStock)
                productDao.update(updatedProduct)
            }
        }

        saleId
    }
}
