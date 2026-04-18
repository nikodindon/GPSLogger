package com.example.gpslogger.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

/**
 * Entity representing a note with attached photos
 * Supports multiple photos per note
 */
@Entity(tableName = "notes_with_photos")
data class NoteWithPhotos(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val title: String,
    
    val description: String?,
    
    val audioUri: String?,
    
    // List of photo URIs as strings (stored as TEXT in database)
    @TypeConverters(PhotoUriConverter::class)
    val photoUris: List<String>,
    
    val latitude: Double,
    
    val longitude: Double,
    
    val timestamp: Long = System.currentTimeMillis(),
    
    val tripId: Int? = null
) {
    // Helper function to add a photo URI
    fun withAddedPhoto(photoUri: String): NoteWithPhotos {
        return copy(photoUris = photoUris + photoUri)
    }
    
    // Helper function to remove a photo URI
    fun withRemovedPhoto(photoUri: String): NoteWithPhotos {
        return copy(photoUris = photoUris - photoUri)
    }
}

/**
 * Type converter for List<String> to store photo URIs
 */
class PhotoUriConverter {
    @TypeConverter
    fun fromStringList(value: List<String>?): List<String> {
        return value ?: emptyList()
    }
    
    @TypeConverter
    fun toStringList(values: List<String>): List<String> {
        return values
    }
}