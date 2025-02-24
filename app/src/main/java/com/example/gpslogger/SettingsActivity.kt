package com.example.gpslogger

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var csvCheckBox: CheckBox
    private lateinit var kmlCheckBox: CheckBox
    private lateinit var saveButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialiser SharedPreferences
        sharedPreferences = getSharedPreferences("GPSLoggerPrefs", MODE_PRIVATE)

        csvCheckBox = findViewById(R.id.checkbox_csv)
        kmlCheckBox = findViewById(R.id.checkbox_kml)
        saveButton = findViewById(R.id.save_button)

        // Charger les valeurs actuelles depuis l'intent (ou SharedPreferences si tu préfères)
        csvCheckBox.isChecked = intent.getBooleanExtra("use_csv", true)
        kmlCheckBox.isChecked = intent.getBooleanExtra("use_kml", false)

        saveButton.setOnClickListener {
            // Sauvegarder les valeurs dans SharedPreferences
            val editor = sharedPreferences.edit()
            editor.putBoolean("use_csv", csvCheckBox.isChecked)
            editor.putBoolean("use_kml", kmlCheckBox.isChecked)
            editor.apply() // Sauvegarde asynchrone

            // Renvoyer les valeurs à WelcomeActivity
            val resultIntent = Intent().apply {
                putExtra("use_csv", csvCheckBox.isChecked)
                putExtra("use_kml", kmlCheckBox.isChecked)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}