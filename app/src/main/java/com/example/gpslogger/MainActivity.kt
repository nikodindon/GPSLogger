package com.example.gpslogger

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val handler = Handler(Looper.getMainLooper())
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val csvFileName = "gps_log.csv"
    private var isRecording = false
    private lateinit var startStopButton: Button
    private lateinit var shareButton: ImageButton

    private val locationRunnable = object : Runnable {
        override fun run() {
            if (isRecording) {
                getLocation()
                handler.postDelayed(this, 10000) // 10 secondes
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startStopButton = findViewById(R.id.start_stop_button)
        shareButton = findViewById(R.id.share_button)

        // Configurer le bouton Start/Stop
        startStopButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                if (checkPermissions()) {
                    startRecording()
                } else {
                    requestPermissions()
                }
            }
        }

        // Configurer le bouton de partage
        shareButton.setOnClickListener {
            shareCsvFile()
        }
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording()
        } else {
            Toast.makeText(this, "Permissions refusées", Toast.LENGTH_LONG).show()
        }
    }

    private fun startRecording() {
        isRecording = true
        startStopButton.text = "Stop"
        getLocation() // Première exécution immédiate
        handler.postDelayed(locationRunnable, 10000) // Démarre le cycle
        Toast.makeText(this, "Enregistrement démarré", Toast.LENGTH_SHORT).show()
    }

    private fun stopRecording() {
        isRecording = false
        startStopButton.text = "Start"
        handler.removeCallbacks(locationRunnable)
        Toast.makeText(this, "Enregistrement arrêté", Toast.LENGTH_SHORT).show()
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    saveToCsv(latitude, longitude, timestamp)
                    Toast.makeText(this, "Position : $latitude, $longitude", Toast.LENGTH_SHORT).show()
                } ?: Toast.makeText(this, "Position indisponible", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erreur : ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToCsv(latitude: Double, longitude: Double, timestamp: String) {
        try {
            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), csvFileName)
            val isNewFile = !file.exists()
            val writer = FileWriter(file, true)
            if (isNewFile) {
                writer.append("Timestamp,Latitude,Longitude\n")
            }
            writer.append("$timestamp,$latitude,$longitude\n")
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erreur écriture : ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun shareCsvFile() {
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), csvFileName)
        if (file.exists()) {
            val uri: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Fichier GPS Logger")
                putExtra(Intent.EXTRA_TEXT, "Voici le fichier CSV avec mes positions GPS.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Partager le fichier CSV"))
        } else {
            Toast.makeText(this, "Aucun fichier CSV à partager", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(locationRunnable)
    }
}