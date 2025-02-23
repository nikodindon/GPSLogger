package com.example.gpslogger

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale // Ajoute cette importation
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var shareAllButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recyclerView = findViewById(R.id.history_recycler_view)
        shareAllButton = findViewById(R.id.share_all_button)

        val documentsDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val csvFiles = documentsDir?.listFiles { file -> file.extension == "csv" }?.toList() ?: emptyList()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = HistoryAdapter(this, csvFiles)

        shareAllButton.setOnClickListener {
            if (csvFiles.isNotEmpty()) {
                shareAllFilesAsZip(csvFiles)
            } else {
                Toast.makeText(this, "Aucun fichier à zipper", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareAllFilesAsZip(files: List<File>) {
        val zipFileName = "GPSLogger_History_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.zip"
        val zipFile = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), zipFileName)

        ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
            for (file in files) {
                val zipEntry = ZipEntry(file.name)
                zipOut.putNextEntry(zipEntry)
                file.inputStream().use { input ->
                    input.copyTo(zipOut)
                }
                zipOut.closeEntry()
            }
        }

        val uri: Uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", zipFile)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Historique GPS Logger")
            putExtra(Intent.EXTRA_TEXT, "Voici tous mes fichiers CSV zippés.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share all in zip file?"))
    }
}