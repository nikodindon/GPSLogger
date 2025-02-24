package com.example.gpslogger

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryAdapter(
    private val context: Context,
    private val files: MutableList<File>,
    private val allFiles: List<File>
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileNameText: TextView = itemView.findViewById(R.id.file_name_text)
        val fileDetailsText: TextView = itemView.findViewById(R.id.file_details_text)
        val previewText: TextView = itemView.findViewById(R.id.preview_text)
        val previewImage: ImageView = itemView.findViewById(R.id.preview_image)
        val locationButton: ImageButton = itemView.findViewById(R.id.location_button)
        val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = files[position]
        holder.fileNameText.text = file.name

        // Détails : date, taille, type
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val size = file.length() / 1024 // Taille en KB
        holder.fileDetailsText.text = "${dateFormat.format(file.lastModified())} | ${size}KB | ${file.extension.uppercase()}"

        // Prévisualisation
        when (file.extension.lowercase()) {
            "csv", "kml", "txt" -> {
                holder.previewText.visibility = View.VISIBLE
                holder.previewImage.visibility = View.GONE
                val preview = file.readText().take(100)
                holder.previewText.text = preview
            }
            "png" -> {
                holder.previewText.visibility = View.GONE
                holder.previewImage.visibility = View.VISIBLE
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                holder.previewImage.setImageBitmap(bitmap)
            }
            else -> {
                holder.previewText.visibility = View.GONE
                holder.previewImage.visibility = View.GONE
            }
        }

        // Afficher le bouton "Location" uniquement pour les fichiers TXT
        if (file.extension.equals("txt", ignoreCase = true)) {
            holder.locationButton.visibility = View.VISIBLE
            holder.locationButton.setOnClickListener {
                val intent = Intent(context, MapNoteActivity::class.java).apply {
                    putExtra("file_path", file.absolutePath)
                }
                context.startActivity(intent)
            }
        } else {
            holder.locationButton.visibility = View.GONE
        }

        holder.fileNameText.setOnClickListener {
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                when (file.extension) {
                    "csv" -> setDataAndType(uri, "text/csv")
                    "kml" -> setDataAndType(uri, "application/vnd.google-earth.kml+xml")
                    "txt" -> setDataAndType(uri, "text/plain")
                    "mp3" -> setDataAndType(uri, "audio/mpeg")
                    "png" -> setDataAndType(uri, "image/png")
                    else -> setDataAndType(uri, "*/*")
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                context.startActivity(Intent.createChooser(intent, "Open ${file.name} with"))
            } catch (e: Exception) {
                Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
            }
        }

        holder.deleteButton.setOnClickListener {
            // Boîte de dialogue de confirmation
            AlertDialog.Builder(context)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete ${file.name}?")
                .setPositiveButton("Yes") { _, _ ->
                    if (file.delete()) {
                        files.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, files.size)
                        Toast.makeText(context, "${file.name} deleted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to delete ${file.name}", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    override fun getItemCount(): Int = files.size

    fun filterByType(type: String) {
        files.clear()
        files.addAll(
            when (type) {
                "All" -> allFiles
                else -> allFiles.filter { it.extension.equals(type, ignoreCase = true) }
            }
        )
        notifyDataSetChanged()
    }
}