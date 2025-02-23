package com.example.gpslogger

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File
import java.io.FileOutputStream
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
    private lateinit var startStopButton: ImageButton
    private lateinit var shareButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var snapshotButton: ImageButton
    private lateinit var noteButton: ImageButton
    private lateinit var recenterButton: Button
    private lateinit var detailButton: Button
    private lateinit var map: MapView
    private lateinit var statsText: TextView
    private lateinit var pointsRecyclerView: RecyclerView
    private val points = mutableListOf<GeoPoint>()
    private val pointDataList = mutableListOf<PointData>()
    private lateinit var pointsAdapter: PointsAdapter
    private var startTime: Long = 0
    private var totalDistance: Float = 0f
    private var useCsv = true
    private var useKml = false
    private var currentSpeed: Float = 0f
    private var maxSpeed: Float = 0f
    private var totalSpeed: Float = 0f
    private var speedCount: Int = 0
    private var detailsVisible = false

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

        useCsv = intent.getBooleanExtra("use_csv", true)
        useKml = intent.getBooleanExtra("use_kml", false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startStopButton = findViewById(R.id.start_stop_button)
        shareButton = findViewById(R.id.share_button)
        backButton = findViewById(R.id.back_button)
        snapshotButton = findViewById(R.id.snapshot_button)
        noteButton = findViewById(R.id.note_button)
        recenterButton = findViewById(R.id.recenter_button)
        detailButton = findViewById(R.id.detail_button)
        map = findViewById(R.id.map)
        statsText = findViewById(R.id.stats_text)
        pointsRecyclerView = findViewById(R.id.points_recycler_view)

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)
        map.controller.setZoom(15.0)
        map.controller.setCenter(GeoPoint(0.0, 0.0))

        pointsAdapter = PointsAdapter(pointDataList)
        pointsRecyclerView.layoutManager = LinearLayoutManager(this)
        pointsRecyclerView.adapter = pointsAdapter

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

        shareButton.setOnClickListener { shareFiles() }

        backButton.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        snapshotButton.setOnClickListener { shareMapSnapshot() }

        noteButton.setOnClickListener {
            if (pointDataList.isNotEmpty()) {
                val lastPoint = pointDataList.last()
                val intent = Intent(this, NoteActivity::class.java).apply {
                    putExtra("latitude", lastPoint.latitude)
                    putExtra("longitude", lastPoint.longitude)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "No position recorded to add a note", Toast.LENGTH_SHORT).show()
            }
        }

        recenterButton.setOnClickListener {
            if (points.isNotEmpty()) {
                val lastPoint = points.last()
                map.controller.setCenter(lastPoint)
                map.controller.setZoom(15.0)
                map.invalidate()
            } else {
                Toast.makeText(this, "No position recorded", Toast.LENGTH_SHORT).show()
            }
        }

        detailButton.setOnClickListener {
            detailsVisible = !detailsVisible
            if (detailsVisible) {
                pointsRecyclerView.visibility = View.VISIBLE
                detailButton.text = "Hide Details"
            } else {
                pointsRecyclerView.visibility = View.GONE
                detailButton.text = "Detail"
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
            Toast.makeText(this, "Permissions denied", Toast.LENGTH_LONG).show()
        }
    }

    private fun startRecording() {
        isRecording = true
        startStopButton.setImageResource(R.drawable.ic_stop)
        startTime = System.currentTimeMillis()
        points.clear()
        pointDataList.clear()
        totalDistance = 0f
        currentSpeed = 0f
        maxSpeed = 0f
        totalSpeed = 0f
        speedCount = 0
        getLocation()
        locationHandler.postDelayed(locationRunnable, 10000)
        timerHandler.post(timerRunnable)

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        if (useCsv) saveToCsv("Start of the walk $timestamp", "", "")
        if (useKml) startKml(timestamp)
        Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
    }

    private fun stopRecording() {
        isRecording = false
        startStopButton.setImageResource(R.drawable.ic_play)
        locationHandler.removeCallbacks(locationRunnable)
        timerHandler.removeCallbacks(timerRunnable)

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        if (useCsv) {
            saveToCsv("End of the walk $timestamp", "", "")
            saveToCsv("", "", "")
        }
        if (useKml) stopKml(timestamp)
        Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show()
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
                    val altitude = it.altitude
                    val speed = it.speed
                    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    if (useCsv) saveToCsv(timestamp, latitude.toString(), longitude.toString())
                    if (useKml) saveToKml(latitude, longitude, timestamp)

                    val point = GeoPoint(latitude, longitude)
                    if (points.isNotEmpty()) {
                        val lastPoint = points.last()
                        totalDistance += calculateDistance(lastPoint, point)
                    }
                    points.add(point)
                    pointDataList.add(PointData(timestamp, latitude, longitude, speed * 3.6f, altitude))
                    if (detailsVisible) {
                        pointsAdapter.notifyItemInserted(pointDataList.size - 1)
                        pointsRecyclerView.scrollToPosition(pointDataList.size - 1)
                    }

                    currentSpeed = speed
                    if (speed > maxSpeed) maxSpeed = speed
                    totalSpeed += speed
                    speedCount++

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

                    Toast.makeText(this, "Position: $latitude, $longitude", Toast.LENGTH_SHORT).show()
                } ?: Toast.makeText(this, "Position unavailable", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
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
        val currentSpeedKmh = currentSpeed * 3.6f
        val maxSpeedKmh = maxSpeed * 3.6f
        val avgSpeedKmh = if (speedCount > 0) (totalSpeed / speedCount) * 3.6f else 0f
        val altitude = if (pointDataList.isNotEmpty()) pointDataList.last().altitude else 0.0

        statsText.text = "Distance: %.2f km\nDuration: %02d:%02d\nSpeed: %.1f km/h\nVmax: %.1f km/h\nVavg: %.1f km/h\nAltitude: %.1f m"
            .format(distanceKm, minutes, seconds, currentSpeedKmh, maxSpeedKmh, avgSpeedKmh, altitude)
    }

    private fun shareMapSnapshot() {
        try {
            if (map.width <= 0 || map.height <= 0) {
                Toast.makeText(this, "Map is not yet ready", Toast.LENGTH_SHORT).show()
                return
            }

            val bitmap = Bitmap.createBitmap(map.width, map.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            map.draw(canvas)

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "GPSLogger_Snapshot_$timestamp.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
            }

            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "GPS Logger Snapshot")
                putExtra(Intent.EXTRA_TEXT, "Here is a snapshot of my current journey.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Share map snapshot"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error sharing: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun getFileName(extension: String): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        return "walk_$currentDate.$extension"
    }

    private fun saveToCsv(timestamp: String, latitude: String, longitude: String) {
        try {
            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), getFileName("csv"))
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
            Toast.makeText(this, "Error writing CSV: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun startKml(timestamp: String) {
        try {
            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), getFileName("kml"))
            val isNewFile = !file.exists()
            val writer = FileWriter(file, true)
            if (isNewFile) {
                writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                writer.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n")
                writer.append("  <Document>\n")
                writer.append("    <name>GPS Logger Track</name>\n")
                writer.append("    <Style id=\"trackStyle\">\n")
                writer.append("      <LineStyle>\n")
                writer.append("        <color>ff0000ff</color>\n")
                writer.append("        <width>4</width>\n")
                writer.append("      </LineStyle>\n")
                writer.append("    </Style>\n")
                writer.append("    <Placemark>\n")
                writer.append("      <name>Track Start</name>\n")
                writer.append("      <description>Start of the walk $timestamp</description>\n")
                writer.append("      <styleUrl>#trackStyle</styleUrl>\n")
                writer.append("      <LineString>\n")
                writer.append("        <coordinates>\n")
            } else {
                writer.append("\n")
                writer.append("<!-- Start of the walk $timestamp -->\n")
            }
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error writing KML: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveToKml(latitude: Double, longitude: Double, timestamp: String) {
        try {
            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), getFileName("kml"))
            val writer = FileWriter(file, true)
            writer.append("          $longitude,$latitude,0\n")
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error writing KML: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopKml(timestamp: String) {
        try {
            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), getFileName("kml"))
            val writer = FileWriter(file, true)
            writer.append("        </coordinates>\n")
            writer.append("      </LineString>\n")
            writer.append("    </Placemark>\n")
            writer.append("<!-- End of the walk $timestamp -->\n")
            if (!isRecording) {
                writer.append("  </Document>\n")
                writer.append("</kml>\n")
            }
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error writing KML: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun shareFiles() {
        val intents = mutableListOf<Intent>()
        if (useCsv) {
            val csvFile = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), getFileName("csv"))
            if (csvFile.exists()) {
                val csvUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", csvFile)
                intents.add(Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, csvUri)
                    putExtra(Intent.EXTRA_SUBJECT, "GPS Logger CSV File")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                })
            }
        }
        if (useKml) {
            val kmlFile = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), getFileName("kml"))
            if (kmlFile.exists()) {
                val kmlUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", kmlFile)
                intents.add(Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.google-earth.kml+xml"
                    putExtra(Intent.EXTRA_STREAM, kmlUri)
                    putExtra(Intent.EXTRA_SUBJECT, "GPS Logger KML File")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                })
            }
        }
        if (intents.isNotEmpty()) {
            val chooserIntent = Intent.createChooser(intents.removeAt(0), "Share files")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toTypedArray())
            startActivity(chooserIntent)
        } else {
            Toast.makeText(this, "No files to share", Toast.LENGTH_LONG).show()
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