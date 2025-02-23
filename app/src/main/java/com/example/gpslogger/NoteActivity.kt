package com.example.gpslogger

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteActivity : AppCompatActivity() {

    private lateinit var recordAudioButton: Button
    private lateinit var saveNoteButton: Button
    private lateinit var noteText: EditText
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private val AUDIO_PERMISSION_REQUEST_CODE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        recordAudioButton = findViewById(R.id.record_audio_button)
        saveNoteButton = findViewById(R.id.save_note_button)
        noteText = findViewById(R.id.note_text)

        // Récupérer les coordonnées GPS passées depuis MainActivity
        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)

        recordAudioButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                if (checkAudioPermission()) {
                    startRecording()
                } else {
                    requestAudioPermission()
                }
            }
        }

        saveNoteButton.setOnClickListener {
            saveNote()
        }
    }

    private fun checkAudioPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            AUDIO_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AUDIO_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording()
        } else {
            Toast.makeText(this, "Permission d’enregistrement refusée", Toast.LENGTH_LONG).show()
        }
    }

    private fun startRecording() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        audioFile = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "Note_$timestamp.mp3")

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile!!.absolutePath)
            try {
                prepare()
                start()
                isRecording = true
                recordAudioButton.text = "Arrêter l’enregistrement"
                Toast.makeText(this@NoteActivity, "Enregistrement commencé", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@NoteActivity, "Erreur démarrage enregistrement : ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                release()
                isRecording = false
                recordAudioButton.text = "Enregistrer une note vocale"
                Toast.makeText(this@NoteActivity, "Note vocale enregistrée", Toast.LENGTH_SHORT).show()
                shareNote(audioFile!!)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@NoteActivity, "Erreur arrêt enregistrement : ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        mediaRecorder = null
    }

    private fun saveNote() {
        val text = noteText.text.toString()
        if (text.isNotEmpty()) {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Note_$timestamp.txt")
            FileWriter(file).use { writer ->
                writer.write("Timestamp: $timestamp\nLatitude: $latitude\nLongitude: $longitude\nNote: $text")
            }
            Toast.makeText(this, "Note textuelle enregistrée", Toast.LENGTH_SHORT).show()
            shareNote(file)
        } else if (audioFile == null) {
            Toast.makeText(this, "Aucune note à sauvegarder", Toast.LENGTH_SHORT).show()
        }
        finish() // Retour à MainActivity après sauvegarde
    }

    private fun shareNote(file: File) {
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = if (file.extension == "mp3") "audio/mpeg" else "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Note GPS Logger")
            putExtra(Intent.EXTRA_TEXT, "Voici une note liée à mon parcours.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Partager la note"))
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaRecorder = null
    }
}