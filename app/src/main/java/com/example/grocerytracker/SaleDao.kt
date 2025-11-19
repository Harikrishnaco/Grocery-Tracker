package com.example.grocerytracker

import androidx.room.*

@Dao
interface SaleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long  // ✅ Returns ID

    @Query("SELECT * FROM sales")
    suspend fun getAllSales(): List<Sale>
}
