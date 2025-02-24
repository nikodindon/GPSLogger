package com.example.gpslogger

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    private var useCsv = true
    private var useKml = false
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        sharedPreferences = getSharedPreferences("GPSLoggerPrefs", MODE_PRIVATE)
        useCsv = sharedPreferences.getBoolean("use_csv", true)
        useKml = sharedPreferences.getBoolean("use_kml", false)

        val startAppButton: Button = findViewById(R.id.start_app_button)
        val historyButton: Button = findViewById(R.id.history_button)
        val donateButton: Button = findViewById(R.id.donate_button)
        val settingsButton: Button = findViewById(R.id.settings_button)

        startAppButton.setOnClickListener {
            if (MainActivity.isRecordingGlobally) {
                // Si un enregistrement est en cours, retourner à MainActivity sans nouvelle instance
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            } else {
                // Sinon, lancer une nouvelle instance
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("use_csv", useCsv)
                    putExtra("use_kml", useKml)
                }
                startActivity(intent)
            }
            finish()
        }

        historyButton.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        donateButton.setOnClickListener {
            val intent = Intent(this, DonateActivity::class.java)
            startActivity(intent)
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java).apply {
                putExtra("use_csv", useCsv)
                putExtra("use_kml", useKml)
            }
            startActivityForResult(intent, SETTINGS_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {
            useCsv = data?.getBooleanExtra("use_csv", true) ?: true
            useKml = data?.getBooleanExtra("use_kml", false) ?: false
        }
    }

    companion object {
        private const val SETTINGS_REQUEST_CODE = 2
    }
}