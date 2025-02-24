package com.example.gpslogger

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File

class MapNoteActivity : AppCompatActivity() {

    private lateinit var map: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE))
        setContentView(R.layout.activity_map_note)

        map = findViewById(R.id.map)
        val backButton: ImageButton = findViewById(R.id.back_button)

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)

        val filePath = intent.getStringExtra("file_path") ?: return
        val file = File(filePath)

        val (latitude, longitude) = when (file.extension) {
            "txt" -> extractLocationFromTextFile(file)
            "mp3", "png" -> extractLocationFromFileName(file)
            else -> Pair(0.0, 0.0)
        }

        if (latitude != 0.0 && longitude != 0.0) {
            val point = GeoPoint(latitude, longitude)
            map.controller.setZoom(15.0)
            map.controller.setCenter(point)

            val marker = Marker(map)
            marker.position = point
            marker.title = file.name
            map.overlays.add(marker)
            map.invalidate()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun extractLocationFromTextFile(file: File): Pair<Double, Double> {
        file.readLines().forEach { line ->
            if (line.startsWith("Latitude:")) {
                val latitude = line.substringAfter("Latitude:").trim().toDoubleOrNull() ?: 0.0
                val longitudeLine = file.readLines().find { it.startsWith("Longitude:") }
                val longitude = longitudeLine?.substringAfter("Longitude:")?.trim()?.toDoubleOrNull() ?: 0.0
                return Pair(latitude, longitude)
            }
        }
        return Pair(0.0, 0.0)
    }

    private fun extractLocationFromFileName(file: File): Pair<Double, Double> {
        val timestamp = file.name.removePrefix("Note_").removePrefix("GPSLogger_Snapshot_").removeSuffix(".${file.extension}")
        val point = MainActivity.pointDataList.find { it.timestamp.contains(timestamp) }
        return if (point != null) Pair(point.latitude, point.longitude) else Pair(0.0, 0.0)
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