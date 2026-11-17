package com.example.kasku.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {
    fun formatRupiah(amount: Long): String {
        return try {
            val localeID = Locale.forLanguageTag("id-ID")
            val numberFormat = NumberFormat.getCurrencyInstance(localeID)
            numberFormat.maximumFractionDigits = 0
            val formatted = numberFormat.format(amount)
            formatted.replace("Rp", "Rp ").replace(",00", "").trim()
        } catch (_: Exception) {
            "Rp $amount"
        }
    }

    fun formatDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
            sdf.format(Date(timestamp))
        } catch (_: Exception) {
            "-"
        }
    }
}