package com.example.kasku

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.kasku.data.AppDatabase
import com.example.kasku.data.TransaksiEntity
import com.example.kasku.ui.theme.ExpenseRed
import com.example.kasku.ui.theme.IncomeGreen
import com.example.kasku.ui.theme.KasKuTheme
import com.example.kasku.ui.theme.SkySecondary
import com.example.kasku.util.AttachmentUtils
import com.example.kasku.util.FormatUtils
import com.example.kasku.util.ThemeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailTransaksiActivity : ComponentActivity() {

    companion object {
        const val EXTRA_ID = "EXTRA_ID"
        const val EXTRA_JUDUL = "EXTRA_JUDUL"
        const val EXTRA_NOMINAL = "EXTRA_NOMINAL"
        const val EXTRA_JENIS = "EXTRA_JENIS"
        const val EXTRA_KATEGORI = "EXTRA_KATEGORI"
        const val EXTRA_TANGGAL = "EXTRA_TANGGAL"
        const val EXTRA_CATATAN = "EXTRA_CATATAN"
        const val EXTRA_LAMPIRAN_PATH = "EXTRA_LAMPIRAN_PATH"
        const val EXTRA_LAMPIRAN_NAMA = "EXTRA_LAMPIRAN_NAMA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeManager.init(applicationContext)

        val id = intent.getIntExtra(EXTRA_ID, -1)
        val judul = intent.getStringExtra(EXTRA_JUDUL) ?: "-"
        val nominal = intent.getLongExtra(EXTRA_NOMINAL, 0L)
        val jenis = intent.getStringExtra(EXTRA_JENIS) ?: "Pengeluaran"
        val kategori = intent.getStringExtra(EXTRA_KATEGORI) ?: "-"
        val tanggal = intent.getStringExtra(EXTRA_TANGGAL) ?: "-"
        val catatan = intent.getStringExtra(EXTRA_CATATAN) ?: ""
        val lampiranPath = intent.getStringExtra(EXTRA_LAMPIRAN_PATH) ?: ""
        val lampiranNama = intent.getStringExtra(EXTRA_LAMPIRAN_NAMA) ?: ""

        setContent {
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()

            KasKuTheme(darkTheme = isDarkMode) {
                DetailTransaksiScreen(
                    judul = judul,
                    nominal = nominal,
                    jenis = jenis,
                    kategori = kategori,
                    tanggal = tanggal,
                    catatan = catatan,
                    lampiranPath = lampiranPath,
                    lampiranNama = lampiranNama,
                    onBackClick = { finish() },
                    onEditClick = {
                        val editIntent = Intent(this, TransaksiFormActivity::class.java).apply {
                            putExtra(TransaksiFormActivity.EXTRA_MODE, "EDIT")
                            putExtra(TransaksiFormActivity.EXTRA_ID, id)
                            putExtra(TransaksiFormActivity.EXTRA_JUDUL, judul)
                            putExtra(TransaksiFormActivity.EXTRA_NOMINAL, nominal)
                            putExtra(TransaksiFormActivity.EXTRA_JENIS, jenis)
                            putExtra(TransaksiFormActivity.EXTRA_KATEGORI, kategori)
                            putExtra(TransaksiFormActivity.EXTRA_CATATAN, catatan)
                            putExtra(TransaksiFormActivity.EXTRA_LAMPIRAN_PATH, lampiranPath)
                            putExtra(TransaksiFormActivity.EXTRA_LAMPIRAN_NAMA, lampiranNama)
                        }
                        startActivity(editIntent)
                        finish()
                    },
                    onDeleteConfirm = {
                        hapusTransaksi(id, judul, nominal, jenis, kategori, catatan, tanggal, lampiranPath, lampiranNama)
                    }
                )
            }
        }
    }

    private fun hapusTransaksi(
        id: Int,
        judul: String,
        nominal: Long,
        jenis: String,
        kategori: String,
        catatan: String,
        tanggal: String,
        lampiranPath: String,
        lampiranNama: String
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Delete physical file attachment
                if (lampiranPath.isNotBlank()) {
                    AttachmentUtils.deletePhysicalFile(lampiranPath)
                }

                val db = AppDatabase.getDatabase(applicationContext)
                val entity = TransaksiEntity(
                    id = id,
                    judul = judul,
                    nominal = nominal,
                    jenis = jenis,
                    kategori = kategori,
                    catatan = catatan,
                    tanggal = tanggal,
                    lampiranPath = lampiranPath,
                    lampiranNama = lampiranNama
                )
                db.transaksiDao().delete(entity)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@DetailTransaksiActivity,
                        "Transaksi \"$judul\" berhasil dihapus",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@DetailTransaksiActivity,
                        "Gagal menghapus transaksi: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTransaksiScreen(
    judul: String,
    nominal: Long,
    jenis: String,
    kategori: String,
    tanggal: String,
    catatan: String,
    lampiranPath: String,
    lampiranNama: String,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteConfirm: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isPemasukan = jenis == "Pemasukan"

    // Load bitmap preview defensively
    val bitmapPreview = remember(lampiranPath) {
        if (lampiranPath.isNotBlank()) {
            AttachmentUtils.loadSampledBitmap(lampiranPath, 600, 600)
        } else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Transaksi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Receipt Card Layout
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App Logo & Receipt Header
                    Image(
                        painter = painterResource(id = R.drawable.ic_kasku_logo),
                        contentDescription = "Logo KasKu",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Bukti Transaksi Digital",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nominal Amount Banner
                    Text(
                        text = "${if (isPemasukan) "+" else "-"}${FormatUtils.formatRupiah(nominal)}",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isPemasukan) IncomeGreen else ExpenseRed
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Jenis & Category Pills
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isPemasukan) IncomeGreen.copy(alpha = 0.12f)
                                    else ExpenseRed.copy(alpha = 0.12f)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = jenis,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPemasukan) IncomeGreen else ExpenseRed
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = kategori,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        color = DividerDefaults.color.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Transaction Details List
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        DetailRow(label = "Judul Transaksi", value = judul)
                        DetailRow(label = "Kategori", value = kategori)
                        DetailRow(label = "Tipe Transaksi", value = jenis)
                        DetailRow(label = "Tanggal & Waktu", value = tanggal)

                        if (catatan.isNotBlank()) {
                            DetailRow(label = "Catatan", value = catatan)
                        }
                    }

                    // ATTACHMENT PREVIEW SECTION
                    if (lampiranPath.isNotBlank()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            color = DividerDefaults.color.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Bukti Lampiran:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (bitmapPreview != null) {
                                // Image attachment preview
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Image(
                                        bitmap = bitmapPreview.asImageBitmap(),
                                        contentDescription = "Preview Lampiran",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            } else {
                                // Non-image file pill card (e.g. PDF)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(14.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AttachFile,
                                            contentDescription = null,
                                            tint = SkySecondary
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = lampiranNama.ifBlank { "Dokumen Bukti" },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons Row (Edit & Delete) using standard Button components
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Edit Button
                Button(
                    onClick = onEditClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SkySecondary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Transaksi",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit Transaksi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                // Delete Button
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Transaksi",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hapus Transaksi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Confirmation AlertDialog for Deleting
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Hapus Transaksi?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Apakah Anda yakin ingin menghapus transaksi \"$judul\"? Data dan file bukti lampiran tidak dapat dikembalikan.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteConfirm()
                    }
                ) {
                    Text(
                        text = "Hapus",
                        color = ExpenseRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.5f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailTransaksiScreenPreview() {
    KasKuTheme {
        DetailTransaksiScreen(
            judul = "Belanja Bulanan",
            nominal = 2500000L,
            jenis = "Pengeluaran",
            kategori = "Belanja",
            tanggal = "02 Nov 2024, 14:30 WIB",
            catatan = "Bahan makanan & kebutuhan rumah",
            lampiranPath = "",
            lampiranNama = "",
            onBackClick = {},
            onEditClick = {},
            onDeleteConfirm = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailRowPreview() {
    KasKuTheme {
        DetailRow(
            label = "Judul Transaksi",
            value = "Belanja Bulanan"
        )
    }
}