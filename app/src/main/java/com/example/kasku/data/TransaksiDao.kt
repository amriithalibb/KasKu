package com.example.kasku.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransaksiDao {
    @Query("SELECT * FROM transaksi ORDER BY timestamp DESC")
    fun getAllTransaksi(): Flow<List<TransaksiEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaksi: TransaksiEntity)

    @Update
    suspend fun update(transaksi: TransaksiEntity)

    @Delete
    suspend fun delete(transaksi: TransaksiEntity)
}