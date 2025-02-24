package com.example.gpslogger

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast // Ajout de l'importation manquante
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class SummaryActivity : AppCompatActivity() {

    private lateinit var summaryText: TextView
    private lateinit var speedChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        summaryText = findViewById(R.id.summary_text)
        speedChart = findViewById(R.id.speed_chart)

        // Récupérer les données de MainActivity
        val distance = intent.getFloatExtra("distance", 0f)
        val duration = intent.getLongExtra("duration", 0L)
        val maxSpeed = intent.getFloatExtra("maxSpeed", 0f)
        val avgSpeed = intent.getFloatExtra("avgSpeed", 0f)
        val points = MainActivity.pointDataList

        // Calculer le dénivelé total
        var totalElevationGain = 0.0
        for (i in 1 until points.size) {
            val elevationChange = points[i].altitude - points[i - 1].altitude
            if (elevationChange > 0) totalElevationGain += elevationChange
        }

        // Afficher les stats
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        val distanceKm = distance / 1000
        summaryText.text = """
            Distance: %.2f km
            Duration: %02d:%02d
            Max Speed: %.1f km/h
            Avg Speed: %.1f km/h
            Elevation Gain: %.1f m
        """.trimIndent().format(distanceKm, minutes, seconds, maxSpeed, avgSpeed, totalElevationGain)

        // Configurer le graphique de vitesse
        setupSpeedChart(points)

        // Récupérer la météo pour le dernier point
        if (points.isNotEmpty()) {
            val lastPoint = points.last()
            fetchWeather(lastPoint.latitude, lastPoint.longitude)
        }
    }

    private fun setupSpeedChart(points: List<PointData>) {
        val entries = points.mapIndexed { index, point ->
            Entry(index.toFloat(), point.speed) // Vitesse en km/h
        }
        val dataSet = LineDataSet(entries, "Speed (km/h)")
        dataSet.color = resources.getColor(android.R.color.holo_purple) // Utilisation d'une couleur intégrée
        dataSet.setDrawCircles(false)
        dataSet.lineWidth = 2f

        val lineData = LineData(dataSet)
        speedChart.data = lineData
        speedChart.description.isEnabled = false
        speedChart.invalidate() // Rafraîchir le graphique
    }

    // Interface pour OpenWeatherMap API
    interface WeatherApi {
        @GET("weather")
        fun getWeather(
            @Query("lat") lat: Double,
            @Query("lon") lon: Double,
            @Query("appid") apiKey: String,
            @Query("units") units: String = "metric"
        ): Call<WeatherResponse>
    }

    data class WeatherResponse(
        val main: Main,
        val weather: List<Weather>
    )

    data class Main(val temp: Float)
    data class Weather(val description: String)

    private fun fetchWeather(lat: Double, lon: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherApi = retrofit.create(WeatherApi::class.java)
        val apiKey = "b978580d7b360d823c6e131da0533e38" // Remplace par ta clé API
        val call = weatherApi.getWeather(lat, lon, apiKey)

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { weather ->
                        val currentText = summaryText.text.toString()
                        summaryText.text = "$currentText\nWeather: ${weather.weather[0].description}, Temp: ${weather.main.temp}°C"
                    }
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Toast.makeText(this@SummaryActivity, "Failed to fetch weather: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}