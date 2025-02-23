package com.example.gpslogger

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.CheckBox

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val startAppButton: Button = findViewById(R.id.start_app_button)
        val historyButton: Button = findViewById(R.id.history_button)
        val donateButton: Button = findViewById(R.id.donate_button)
        val csvCheckBox: CheckBox = findViewById(R.id.csv_checkbox)
        val kmlCheckBox: CheckBox = findViewById(R.id.kml_checkbox)

        startAppButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("use_csv", csvCheckBox.isChecked)
                putExtra("use_kml", kmlCheckBox.isChecked)
            }
            startActivity(intent)
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
    }
}