package com.example.gpslogger.ui.notes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
nimport android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gpslogger.R

/**
 * Adapter for displaying photos in a note
 */
class NotePhotoAdapter(private val photos: List<String>) : 
    RecyclerView.Adapter<NotePhotoAdapter.PhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo_thumbnail, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun getItemCount(): Int = photos.size

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.photoImageView)
        
        fun bind(photoUri: String) {
            // Load photo using Glide or similar library
            Glide.with(itemView.context)
                .load(photoUri)
                .into(imageView)
        }
    }
}