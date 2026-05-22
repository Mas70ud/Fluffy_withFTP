package app.fluffy.ui

import android.Manifest
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import app.fluffy.ftp.FtpServerManager
import kotlinx.coroutines.delay
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(initialPath: String = "/storage/emulated/0") {
    val context = LocalContext.current
    var currentPath by remember { mutableStateOf(initialPath) }
    var fileList by remember { mutableStateOf(listOf<File>()) }
    var permissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        if (granted) loadFiles(currentPath) { fileList = it }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            permissionGranted = true
            loadFiles(currentPath) { fileList = it }
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    LaunchedEffect(currentPath) {
        if (permissionGranted) loadFiles(currentPath) { fileList = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(File(currentPath).name.ifEmpty { "Internal Storage" }) },
                navigationIcon = {
                    if (currentPath != "/storage/emulated/0" && currentPath != "/storage/emulated") {
                        IconButton(onClick = {
                            File(currentPath).parentFile?.let { currentPath = it.absolutePath }
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        },
        bottomBar = {
            FtpStatusBar()
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(fileList) { file ->
                FileItem(file) {
                    if (file.isDirectory) currentPath = file.absolutePath
                }
            }
        }
    }
}

@Composable
fun FileItem(file: File, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
            contentDescription = null,
            tint = if (file.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(file.name, fontSize = MaterialTheme.typography.bodyLarge.fontSize)
    }
}

private fun loadFiles(path: String, onResult: (List<File>) -> Unit) {
    val dir = File(path)
    val files = dir.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name })) ?: emptyList()
    onResult(files)
}

@Composable
fun FtpStatusBar() {
    val context = LocalContext.current
    var ftpAddress by remember { mutableStateOf("در حال دریافت...") }

    LaunchedEffect(Unit) {
        while (true) {
            ftpAddress = FtpServerManager.getServerAddress(context, 2101)
            delay(2000)
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.InsertDriveFile, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("FTP فعال: $ftpAddress (پورت 2101)", style = MaterialTheme.typography.bodySmall)
        }
    }
}