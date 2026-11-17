package com.example.kasku.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaksi")
data class TransaksiEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val judul: String,
    val nominal: Long,
    val jenis: String, // "Pemasukan" or "Pengeluaran"
    val kategori: String,
    val catatan: String = "",
    val tanggal: String,
    val timestamp: Long = System.currentTimeMillis(),
    val lampiranPath: String = "",
    val lampiranNama: String = ""
)