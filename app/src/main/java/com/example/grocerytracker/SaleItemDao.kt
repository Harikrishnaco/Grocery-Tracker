package com.example.grocerytracker

import androidx.room.*

@Dao
interface SaleItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItem(item: SaleItem): Long  // ✅ Returns ID

    @Query("SELECT * FROM sale_items")
    suspend fun getAllSaleItems(): List<SaleItem>
}
