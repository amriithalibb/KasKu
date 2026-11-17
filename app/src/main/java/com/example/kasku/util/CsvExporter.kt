package com.example.kasku.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.kasku.data.TransaksiEntity
import java.io.File
import java.io.FileWriter

object CsvExporter {
    fun exportAndShareCsv(context: Context, transaksiList: List<TransaksiEntity>) {
        if (transaksiList.isEmpty()) {
            Toast.makeText(context, "Belum ada data transaksi untuk di-export", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val fileName = "Laporan_KasKu_${System.currentTimeMillis()}.csv"
            val file = File(exportDir, fileName)
            val writer = FileWriter(file)

            // Write CSV Header
            writer.append("No,Tanggal,Jenis,Kategori,Judul,Nominal,Catatan,File_Lampiran\n")

            // Write CSV Rows
            transaksiList.forEachIndexed { index, item ->
                val cleanJudul = item.judul.replace(",", " ")
                val cleanCatatan = item.catatan.replace(",", " ").replace("\n", " ")
                val cleanKategori = item.kategori.replace(",", " ")
                val cleanLampiran = if (item.lampiranNama.isNotBlank()) item.lampiranNama else "-"

                writer.append("${index + 1},${item.tanggal},${item.jenis},$cleanKategori,$cleanJudul,${item.nominal},$cleanCatatan,$cleanLampiran\n")
            }

            writer.flush()
            writer.close()

            // Generate Content URI using FileProvider
            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Laporan Keuangan KasKu")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Laporan CSV KasKu"))
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal meng-export CSV: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}