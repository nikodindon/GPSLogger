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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationHandler = Handler(Looper.getMainLooper())
    private val timerHandler = Handler(Looper.getMainLooper())
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var isRecording = false
    private lateinit var startStopButton: Button
    private lateinit var shareButton: ImageButton
    private lateinit var recenterButton: Button
    private lateinit var map: MapView
    private lateinit var statsText: TextView
    private val points = mutableListOf<GeoPoint>()
    private var startTime: Long = 0
    private var totalDistance: Float = 0f

    private val locationRunnable = object : Runnable {
        override fun run() {
            if (isRecording) {
                getLocation()
                locationHandler.postDelayed(this, 10000)
            }
        }
    }

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isRecording) {
                updateStats()
                timerHandler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE))
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startStopButton = findViewById(R.id.start_stop_button)
        shareButton = findViewById(R.id.share_button)
        recenterButton = findViewById(R.id.recenter_button)
        map = findViewById(R.id.map)
        statsText = findViewById(R.id.stats_text)

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)
        map.controller.setZoom(15.0)
        map.controller.setCenter(GeoPoint(0.0, 0.0))

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

        shareButton.setOnClickListener { shareCsvFile() }

        recenterButton.setOnClickListener {
            if (points.isNotEmpty()) {
                val lastPoint = points.last()
                map.controller.setCenter(lastPoint)
                map.controller.setZoom(15.0)
                map.invalidate()
            } else {
                Toast.makeText(this, "Aucune position enregistrée", Toast.LENGTH_SHORT).show()
            }
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
        startTime = System.currentTimeMillis()
        points.clear()
        totalDistance = 0f
        getLocation()
        locationHandler.postDelayed(locationRunnable, 10000)
        timerHandler.post(timerRunnable)

        // Écrire "Démarrage de la marche" dans le CSV
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        saveToCsv("Démarrage de la marche $timestamp", "", "")

        Toast.makeText(this, "Enregistrement démarré", Toast.LENGTH_SHORT).show()
    }

    private fun stopRecording() {
        isRecording = false
        startStopButton.text = "Start"
        locationHandler.removeCallbacks(locationRunnable)
        timerHandler.removeCallbacks(timerRunnable)

        // Écrire "Arrêt de la marche" dans le CSV
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        saveToCsv("Arrêt de la marche $timestamp", "", "")
        saveToCsv("", "", "") // Ligne vide pour séparation claire

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
                    saveToCsv(timestamp, latitude.toString(), longitude.toString())

                    val point = GeoPoint(latitude, longitude)
                    if (points.isNotEmpty()) {
                        val lastPoint = points.last()
                        totalDistance += calculateDistance(lastPoint, point)
                    }
                    points.add(point)
                    val marker = Marker(map)
                    marker.position = point
                    marker.title = timestamp
                    map.overlays.add(marker)

                    if (points.size > 1) {
                        val polyline = Polyline()
                        polyline.setPoints(points)
                        polyline.color = 0xFF0000FF.toInt()
                        map.overlays.add(polyline)
                    }
                    map.controller.setCenter(point)
                    map.invalidate()

                    Toast.makeText(this, "Position : $latitude, $longitude", Toast.LENGTH_SHORT).show()
                } ?: Toast.makeText(this, "Position indisponible", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erreur : ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun calculateDistance(point1: GeoPoint, point2: GeoPoint): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            point1.latitude, point1.longitude,
            point2.latitude, point2.longitude,
            results
        )
        return results[0]
    }

    private fun updateStats() {
        val elapsedTime = System.currentTimeMillis() - startTime
        val minutes = (elapsedTime / 1000) / 60
        val seconds = (elapsedTime / 1000) % 60
        val distanceKm = totalDistance / 1000
        statsText.text = "Distance : %.2f km\nDurée : %02d:%02d".format(distanceKm, minutes, seconds)
    }

    private fun getCsvFileName(): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        return "marche du $currentDate.csv"
    }

    private fun saveToCsv(timestamp: String, latitude: String, longitude: String) {
        try {
            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), getCsvFileName())
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
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), getCsvFileName())
        if (file.exists()) {
            val uri: Uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
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
        locationHandler.removeCallbacks(locationRunnable)
        timerHandler.removeCallbacks(timerRunnable)
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}