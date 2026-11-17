package com.example.kasku

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.kasku.util.CsvExporter
import com.example.kasku.util.FormatUtils
import com.example.kasku.util.ThemeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeManager.init(applicationContext)

        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.transaksiDao()

        setContent {
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()
            val batasAnggaran by ThemeManager.batasAnggaran.collectAsState()

            KasKuTheme(darkTheme = isDarkMode) {
                val transaksiList by dao.getAllTransaksi().collectAsState(initial = emptyList())

                MainScreen(
                    transaksiList = transaksiList,
                    isDarkMode = isDarkMode,
                    batasAnggaran = batasAnggaran,
                    onToggleTheme = {
                        ThemeManager.setDarkMode(this, !isDarkMode)
                    },
                    onUpdateBatasAnggaran = { newLimit ->
                        ThemeManager.setBatasAnggaran(this, newLimit)
                        Toast.makeText(this, "Batas anggaran berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    },
                    onExportCsv = {
                        CsvExporter.exportAndShareCsv(this, transaksiList)
                    },
                    onAddClick = { defaultJenis ->
                        val intent = Intent(this, TransaksiFormActivity::class.java).apply {
                            putExtra(TransaksiFormActivity.EXTRA_MODE, "CREATE")
                            putExtra(TransaksiFormActivity.EXTRA_JENIS, defaultJenis)
                        }
                        startActivity(intent)
                    },
                    onItemClick = { item ->
                        val intent = Intent(this, DetailTransaksiActivity::class.java).apply {
                            putExtra(DetailTransaksiActivity.EXTRA_ID, item.id)
                            putExtra(DetailTransaksiActivity.EXTRA_JUDUL, item.judul)
                            putExtra(DetailTransaksiActivity.EXTRA_NOMINAL, item.nominal)
                            putExtra(DetailTransaksiActivity.EXTRA_JENIS, item.jenis)
                            putExtra(DetailTransaksiActivity.EXTRA_KATEGORI, item.kategori)
                            putExtra(DetailTransaksiActivity.EXTRA_TANGGAL, item.tanggal)
                            putExtra(DetailTransaksiActivity.EXTRA_CATATAN, item.catatan)
                            putExtra(DetailTransaksiActivity.EXTRA_LAMPIRAN_PATH, item.lampiranPath)
                            putExtra(DetailTransaksiActivity.EXTRA_LAMPIRAN_NAMA, item.lampiranNama)
                        }
                        startActivity(intent)
                    },
                    onEditClick = { item ->
                        val intent = Intent(this, TransaksiFormActivity::class.java).apply {
                            putExtra(TransaksiFormActivity.EXTRA_MODE, "EDIT")
                            putExtra(TransaksiFormActivity.EXTRA_ID, item.id)
                            putExtra(TransaksiFormActivity.EXTRA_JUDUL, item.judul)
                            putExtra(TransaksiFormActivity.EXTRA_NOMINAL, item.nominal)
                            putExtra(TransaksiFormActivity.EXTRA_JENIS, item.jenis)
                            putExtra(TransaksiFormActivity.EXTRA_KATEGORI, item.kategori)
                            putExtra(TransaksiFormActivity.EXTRA_CATATAN, item.catatan)
                            putExtra(TransaksiFormActivity.EXTRA_LAMPIRAN_PATH, item.lampiranPath)
                            putExtra(TransaksiFormActivity.EXTRA_LAMPIRAN_NAMA, item.lampiranNama)
                        }
                        startActivity(intent)
                    },
                    onDeleteClick = { item ->
                        hapusTransaksi(item)
                    }
                )
            }
        }
    }

    private fun hapusTransaksi(item: TransaksiEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Delete physical attachment file if exists
                if (item.lampiranPath.isNotBlank()) {
                    AttachmentUtils.deletePhysicalFile(item.lampiranPath)
                }

                val db = AppDatabase.getDatabase(applicationContext)
                db.transaksiDao().delete(item)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Transaksi berhasil dihapus", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Gagal menghapus: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    transaksiList: List<TransaksiEntity>,
    isDarkMode: Boolean,
    batasAnggaran: Long,
    onToggleTheme: () -> Unit,
    onUpdateBatasAnggaran: (Long) -> Unit,
    onExportCsv: () -> Unit,
    onAddClick: (defaultJenis: String) -> Unit,
    onItemClick: (TransaksiEntity) -> Unit,
    onEditClick: (TransaksiEntity) -> Unit,
    onDeleteClick: (TransaksiEntity) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf("Semua") }

    var itemToDelete by remember { mutableStateOf<TransaksiEntity?>(null) }
    var showBatasDialog by rememberSaveable { mutableStateOf(false) }

    // Dynamic calculations
    val totalPemasukan = transaksiList.filter { it.jenis == "Pemasukan" }.sumOf { it.nominal }
    val totalPengeluaran = transaksiList.filter { it.jenis == "Pengeluaran" }.sumOf { it.nominal }
    val totalSaldo = totalPemasukan - totalPengeluaran

    // Expense vs Income progress ratio
    val incomeProgress = if (totalPemasukan > 0L) {
        (totalPengeluaran.toFloat() / totalPemasukan.toFloat()).coerceIn(0f, 1f)
    } else 0f

    // Smart Budget calculations
    val budgetRatio = if (batasAnggaran > 0L) {
        (totalPengeluaran.toFloat() / batasAnggaran.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val budgetPercent = (budgetRatio * 100).toInt()

    val (budgetStatusText, budgetColor) = when {
        budgetPercent >= 100 -> "Peringatan! Pengeluaran melebihi batas anggaran" to ExpenseRed
        budgetPercent >= 80 -> "Waspada! Pengeluaran mendekati batas anggaran" to Color(0xFFF59E0B) // Amber
        else -> "Anggaran Aman" to IncomeGreen
    }

    val filteredList = transaksiList.filter { item ->
        val matchesSearch = item.judul.contains(searchQuery, ignoreCase = true) ||
                item.catatan.contains(searchQuery, ignoreCase = true) ||
                item.kategori.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Pemasukan" -> item.jenis == "Pemasukan"
            "Pengeluaran" -> item.jenis == "Pengeluaran"
            else -> true
        }
        matchesSearch && matchesFilter
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
                                .size(38.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "KasKu",
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Fintech Ledger",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Export CSV Action Button
                    IconButton(onClick = onExportCsv) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export CSV",
                            tint = SkySecondary
                        )
                    }

                    // Theme Toggle Button (Sun / Moon)
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Mode",
                            tint = if (isDarkMode) Color(0xFFFBBF24) else MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddClick("Pengeluaran") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Transaksi",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // BENTO GRID DASHBOARD SECTION
            // 1. Wide Main Balance Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Saldo Saat Ini",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            )
                            // Cashflow Status Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (totalSaldo >= 0) "Surplus" else "Defisit",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = FormatUtils.formatRupiah(totalSaldo),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Expense / Income Progress Bar
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Rasio Pengeluaran",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = "${(incomeProgress * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { incomeProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.onPrimary,
                                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }

            // 2. Bento Split 50:50 Row Cards (Pemasukan & Pengeluaran)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pemasukan Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(IncomeGreen.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = IncomeGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pemasukan",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = FormatUtils.formatRupiah(totalPemasukan),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen
                            )
                        }
                    }

                    // Pengeluaran Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(ExpenseRed.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = null,
                                        tint = ExpenseRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pengeluaran",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = FormatUtils.formatRupiah(totalPengeluaran),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                        }
                    }
                }
            }

            // 3. Smart Budget Alert Card Bento
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Smart Budget Alert",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Batas: ${FormatUtils.formatRupiah(batasAnggaran)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Atur Batas Button
                            OutlinedButton(
                                onClick = { showBatasDialog = true },
                                modifier = Modifier.height(34.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "Atur Batas",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Budget Usage Progress Bar
                        LinearProgressIndicator(
                            progress = { budgetRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = budgetColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Status Badge & Warning Message
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(budgetColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = budgetStatusText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = budgetColor
                            )
                        }
                    }
                }
            }

            // 4. Quick Action Buttons Row (+ Pemasukan & - Pengeluaran)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onAddClick("Pemasukan") },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ Pemasukan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = { onAddClick("Pengeluaran") },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "- Pengeluaran",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // SEARCH BAR & FILTER SECTION
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari transaksi, catatan, atau kategori...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear"
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filters = listOf("Semua", "Pemasukan", "Pengeluaran")
                        filters.forEach { filter ->
                            val isSelected = selectedFilter == filter
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) {
                                            when (filter) {
                                                "Pemasukan" -> IncomeGreen
                                                "Pengeluaran" -> ExpenseRed
                                                else -> SkySecondary
                                            }
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                                    .clickable { selectedFilter = filter }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = filter,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Riwayat Transaksi (Timeline)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // TIMELINE LIST OR EMPTY STATE
            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_empty_transaksi),
                                contentDescription = "Kosong",
                                modifier = Modifier.size(110.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (transaksiList.isEmpty()) "Belum ada transaksi" else "Tidak ada transaksi yang cocok",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { item ->
                    TimelineTransaksiCard(
                        item = item,
                        onItemClick = { onItemClick(item) },
                        onEdit = { onEditClick(item) },
                        onDelete = { itemToDelete = item }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Dialog Update Batas Anggaran
    if (showBatasDialog) {
        var newLimitText by rememberSaveable { mutableStateOf(batasAnggaran.toString()) }
        var errorText by rememberSaveable { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showBatasDialog = false },
            title = {
                Text(
                    text = "Atur Batas Anggaran Bulanan",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Masukkan batas maksimal pengeluaran bulanan Anda:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newLimitText,
                        onValueChange = {
                            newLimitText = it.filter { c -> c.isDigit() }
                            errorText = null
                        },
                        label = { Text("Batas Anggaran (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = errorText != null,
                        supportingText = {
                            errorText?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = newLimitText.toLongOrNull()
                        if (parsed == null || parsed <= 0L) {
                            errorText = "Batas anggaran harus > 0"
                        } else {
                            onUpdateBatasAnggaran(parsed)
                            showBatasDialog = false
                        }
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatasDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Konfirmasi Hapus Transaksi
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Text(text = "Hapus Transaksi?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Apakah Anda yakin ingin menghapus \"${itemToDelete?.judul}\"? Bukti lampiran file juga akan dihapus.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDelete?.let { onDeleteClick(it) }
                        itemToDelete = null
                    }
                ) {
                    Text("Hapus", color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun TimelineTransaksiCard(
    item: TransaksiEntity,
    onItemClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isPemasukan = item.jenis == "Pemasukan"
    val accentColor = if (isPemasukan) IncomeGreen else ExpenseRed

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Colored Vertical Stripe Accent (Timeline Style)
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category & Info Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Category Badge Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.kategori,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Attachment File Indicator Badge
                        if (item.lampiranPath.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SkySecondary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AttachFile,
                                        contentDescription = "Bukti Terlampir",
                                        tint = SkySecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Bukti",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SkySecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.judul,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (item.catatan.isNotBlank()) {
                        Text(
                            text = item.catatan,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = item.tanggal,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Nominal Amount & Action Buttons Column
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isPemasukan) "+" else "-"}${FormatUtils.formatRupiah(item.nominal)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = accentColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = SkySecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus",
                                tint = ExpenseRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}