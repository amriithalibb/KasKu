package com.example.kasku

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.kasku.data.AppDatabase
import com.example.kasku.data.TransaksiEntity
import com.example.kasku.ui.theme.EmeraldPrimary
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

class TransaksiFormActivity : ComponentActivity() {

    companion object {
        const val EXTRA_MODE = "EXTRA_MODE" // "CREATE" or "EDIT"
        const val EXTRA_ID = "EXTRA_ID"
        const val EXTRA_JUDUL = "EXTRA_JUDUL"
        const val EXTRA_NOMINAL = "EXTRA_NOMINAL"
        const val EXTRA_JENIS = "EXTRA_JENIS"
        const val EXTRA_KATEGORI = "EXTRA_KATEGORI"
        const val EXTRA_CATATAN = "EXTRA_CATATAN"
        const val EXTRA_LAMPIRAN_PATH = "EXTRA_LAMPIRAN_PATH"
        const val EXTRA_LAMPIRAN_NAMA = "EXTRA_LAMPIRAN_NAMA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeManager.init(applicationContext)

        val mode = intent.getStringExtra(EXTRA_MODE) ?: "CREATE"
        val existingId = intent.getIntExtra(EXTRA_ID, -1)
        val initialJudul = intent.getStringExtra(EXTRA_JUDUL) ?: ""
        val initialNominalLong = intent.getLongExtra(EXTRA_NOMINAL, 0L)
        val initialNominal = if (initialNominalLong > 0L) initialNominalLong.toString() else ""
        val initialJenis = intent.getStringExtra(EXTRA_JENIS) ?: "Pengeluaran"
        val initialKategori = intent.getStringExtra(EXTRA_KATEGORI) ?: "Makan & Minum"
        val initialCatatan = intent.getStringExtra(EXTRA_CATATAN) ?: ""
        val initialLampiranPath = intent.getStringExtra(EXTRA_LAMPIRAN_PATH) ?: ""
        val initialLampiranNama = intent.getStringExtra(EXTRA_LAMPIRAN_NAMA) ?: ""

        setContent {
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()

            KasKuTheme(darkTheme = isDarkMode) {
                TransaksiFormScreen(
                    mode = mode,
                    initialId = existingId,
                    initialJudul = initialJudul,
                    initialNominal = initialNominal,
                    initialJenis = initialJenis,
                    initialKategori = initialKategori,
                    initialCatatan = initialCatatan,
                    initialLampiranPath = initialLampiranPath,
                    initialLampiranNama = initialLampiranNama,
                    onBackClick = { finish() },
                    onSaveClick = { id, judul, nominal, jenis, kategori, catatan, lampiranPath, lampiranNama ->
                        simpanTransaksi(id, judul, nominal, jenis, kategori, catatan, lampiranPath, lampiranNama)
                    }
                )
            }
        }
    }

    private fun simpanTransaksi(
        id: Int,
        judul: String,
        nominal: Long,
        jenis: String,
        kategori: String,
        catatan: String,
        lampiranPath: String,
        lampiranNama: String
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val timestamp = System.currentTimeMillis()
                val tanggalFormatted = FormatUtils.formatDate(timestamp)

                val entity = TransaksiEntity(
                    id = if (id > 0) id else 0,
                    judul = judul,
                    nominal = nominal,
                    jenis = jenis,
                    kategori = kategori,
                    catatan = catatan,
                    tanggal = tanggalFormatted,
                    timestamp = timestamp,
                    lampiranPath = lampiranPath,
                    lampiranNama = lampiranNama
                )

                if (id > 0) {
                    db.transaksiDao().update(entity)
                } else {
                    db.transaksiDao().insert(entity)
                }

                withContext(Dispatchers.Main) {
                    val message = if (id > 0) "Transaksi berhasil diperbarui" else "Transaksi berhasil disimpan"
                    Toast.makeText(this@TransaksiFormActivity, message, Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@TransaksiFormActivity,
                        "Gagal menyimpan transaksi: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransaksiFormScreen(
    mode: String,
    initialId: Int,
    initialJudul: String,
    initialNominal: String,
    initialJenis: String,
    initialKategori: String,
    initialCatatan: String,
    initialLampiranPath: String,
    initialLampiranNama: String,
    onBackClick: () -> Unit,
    onSaveClick: (id: Int, judul: String, nominal: Long, jenis: String, kategori: String, catatan: String, lampiranPath: String, lampiranNama: String) -> Unit
) {
    val context = LocalContext.current

    // Retain inputs and file attachment state using rememberSaveable
    var judul by rememberSaveable { mutableStateOf(initialJudul) }
    var nominalText by rememberSaveable { mutableStateOf(initialNominal) }
    var jenis by rememberSaveable { mutableStateOf(initialJenis) }
    var kategori by rememberSaveable { mutableStateOf(initialKategori) }
    var catatan by rememberSaveable { mutableStateOf(initialCatatan) }
    var lampiranPath by rememberSaveable { mutableStateOf(initialLampiranPath) }
    var lampiranNama by rememberSaveable { mutableStateOf(initialLampiranNama) }

    var judulError by rememberSaveable { mutableStateOf<String?>(null) }
    var nominalError by rememberSaveable { mutableStateOf<String?>(null) }
    var kategoriError by rememberSaveable { mutableStateOf<String?>(null) }

    // Document / Image File Picker Launcher
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val result = AttachmentUtils.copyUriToInternalStorage(context, uri)
            if (result != null) {
                // Delete previous file if replacing
                if (lampiranPath.isNotBlank() && lampiranPath != result.first) {
                    AttachmentUtils.deletePhysicalFile(lampiranPath)
                }
                lampiranPath = result.first
                lampiranNama = result.second
                Toast.makeText(context, "Bukti lampiran berhasil diunggah", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gagal mengunggah bukti lampiran", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val categories = listOf(
        "Makan & Minum",
        "Transportasi",
        "Belanja",
        "Tagihan",
        "Gaji & Bonus",
        "Lainnya"
    )

    val quickNominals = listOf(
        10000L to "+10.000",
        25000L to "+25.000",
        50000L to "+50.000",
        100000L to "+100.000"
    )

    // Load bitmap preview defensively
    val bitmapPreview = remember(lampiranPath) {
        if (lampiranPath.isNotBlank()) {
            AttachmentUtils.loadSampledBitmap(lampiranPath, 400, 400)
        } else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_kasku_logo),
                            contentDescription = "Logo KasKu",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (mode == "EDIT") "Edit Transaksi" else "Tambah Transaksi Baru",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
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
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Jenis Transaksi Card (Pemasukan / Pengeluaran)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Jenis Transaksi",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val isPengeluaran = jenis == "Pengeluaran"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isPengeluaran) ExpenseRed.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isPengeluaran) 2.dp else 1.dp,
                                    color = if (isPengeluaran) ExpenseRed else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { jenis = "Pengeluaran" }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isPengeluaran) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = ExpenseRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = "Pengeluaran",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPengeluaran) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val isPemasukan = jenis == "Pemasukan"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isPemasukan) IncomeGreen.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isPemasukan) 2.dp else 1.dp,
                                    color = if (isPemasukan) IncomeGreen else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { jenis = "Pemasukan" }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isPemasukan) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = IncomeGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = "Pemasukan",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPemasukan) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Form Inputs Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Judul Transaksi Input
                    OutlinedTextField(
                        value = judul,
                        onValueChange = {
                            judul = it
                            if (judulError != null) judulError = null
                        },
                        label = { Text("Judul Transaksi") },
                        placeholder = { Text("Contoh: Nasi Goreng, Gaji Bulanan") },
                        isError = judulError != null,
                        supportingText = {
                            judulError?.let {
                                Text(text = it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // Nominal Input Section
                    Column {
                        OutlinedTextField(
                            value = nominalText,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() }
                                nominalText = filtered
                                if (nominalError != null) nominalError = null
                            },
                            label = { Text("Nominal (Rp)") },
                            placeholder = { Text("Contoh: 50000") },
                            isError = nominalError != null,
                            supportingText = {
                                nominalError?.let {
                                    Text(text = it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick Nominal Increment Buttons
                        Text(
                            text = "Tambah Cepat Nominal:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            quickNominals.forEach { (amount, label) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            val current = nominalText.toLongOrNull() ?: 0L
                                            nominalText = (current + amount).toString()
                                            if (nominalError != null) nominalError = null
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Kategori Selection
                    Column {
                        Text(
                            text = "Kategori Transaksi",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categories.forEach { cat ->
                                val isSelected = kategori == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            if (isSelected) EmeraldPrimary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable {
                                            kategori = cat
                                            if (kategoriError != null) kategoriError = null
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        if (kategoriError != null) {
                            Text(
                                text = kategoriError!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    }

                    // Catatan Tambahan Input
                    OutlinedTextField(
                        value = catatan,
                        onValueChange = { catatan = it },
                        label = { Text("Catatan Tambahan (Opsional)") },
                        placeholder = { Text("Catatan detail transaksi...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // UPLOAD FILE BUKTI TRANSAKSI SECTION
                    Column {
                        Text(
                            text = "Bukti Transaksi / Lampiran (Opsional)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (lampiranPath.isNotBlank()) {
                            // File Attachment Card Preview
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    if (bitmapPreview != null) {
                                        Image(
                                            bitmap = bitmapPreview.asImageBitmap(),
                                            contentDescription = "Preview Bukti",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(160.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AttachFile,
                                                contentDescription = null,
                                                tint = SkySecondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (lampiranNama.isNotBlank()) lampiranNama else "File Bukti",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Row {
                                            // Change file button
                                            IconButton(
                                                onClick = {
                                                    documentPickerLauncher.launch(arrayOf("image/*", "application/pdf"))
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.UploadFile,
                                                    contentDescription = "Ganti",
                                                    tint = SkySecondary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            // Delete file button
                                            IconButton(
                                                onClick = {
                                                    AttachmentUtils.deletePhysicalFile(lampiranPath)
                                                    lampiranPath = ""
                                                    lampiranNama = ""
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Hapus File",
                                                    tint = ExpenseRed,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Upload Button
                            OutlinedButton(
                                onClick = {
                                    documentPickerLauncher.launch(arrayOf("image/*", "application/pdf"))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.UploadFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Upload Bukti (Foto / PDF)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons Row (Batal & Simpan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Batal",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        val trimmedJudul = judul.trim()
                        val parsedNominal = nominalText.trim().toLongOrNull()

                        var isValid = true
                        if (trimmedJudul.isBlank()) {
                            judulError = "Judul transaksi tidak boleh kosong"
                            isValid = false
                        }

                        if (parsedNominal == null || parsedNominal <= 0L) {
                            nominalError = "Nominal harus berupa angka dan lebih dari 0"
                            isValid = false
                        }

                        if (kategori.isBlank()) {
                            kategoriError = "Pilih salah satu kategori"
                            isValid = false
                        }

                        if (isValid && parsedNominal != null) {
                            onSaveClick(
                                initialId,
                                trimmedJudul,
                                parsedNominal,
                                jenis,
                                kategori,
                                catatan.trim(),
                                lampiranPath,
                                lampiranNama
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(
                        text = if (mode == "EDIT") "Update Transaksi" else "Simpan Transaksi",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}