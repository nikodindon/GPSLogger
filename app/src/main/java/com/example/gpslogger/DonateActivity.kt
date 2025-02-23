package com.example.gpslogger

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DonateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)

        val paypalText: TextView = findViewById(R.id.paypal_text)
        val erc20Text: TextView = findViewById(R.id.erc20_text)
        val splText: TextView = findViewById(R.id.spl_text)
        val backButton: ImageButton = findViewById(R.id.back_button) // Nouveau bouton

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        paypalText.setOnClickListener {
            val email = "larrydavid6923@gmail.com"
            clipboard.setText(email)
            Toast.makeText(this, "Paypal address copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        erc20Text.setOnClickListener {
            val address = "0xd79d8c135463DF9e4DDDD98A87EEdcaad337cFa7"
            clipboard.setText(address)
            Toast.makeText(this, "ERC20 address copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        splText.setOnClickListener {
            val address = "2kxcPaCyCQdKCzbs2JBNCvbJBjPYKjXKVqe6nAy9oSn5"
            clipboard.setText(address)
            Toast.makeText(this, "SPL address copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        backButton.setOnClickListener {
            finish() // Retour à l’écran précédent (WelcomeActivity)
        }
    }

    private fun ClipboardManager.setText(text: String) {
        setPrimaryClip(android.content.ClipData.newPlainText("Donation Address", text))
    }
}