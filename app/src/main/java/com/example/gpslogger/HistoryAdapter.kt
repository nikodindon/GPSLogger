package com.example.gpslogger

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast // Ajoute cette importation
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class HistoryAdapter(
    private val context: Context,
    private val files: List<File>
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileNameText: TextView = itemView.findViewById(R.id.file_name_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = files[position]
        holder.fileNameText.text = file.name

        holder.fileNameText.setOnClickListener {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, if (file.extension == "csv") "text/csv" else "application/vnd.google-earth.kml+xml")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                context.startActivity(Intent.createChooser(intent, "Ouvrir ${file.name} avec"))
            } catch (e: Exception) {
                Toast.makeText(context, "Aucune application trouvée pour ouvrir ce fichier", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = files.size
}