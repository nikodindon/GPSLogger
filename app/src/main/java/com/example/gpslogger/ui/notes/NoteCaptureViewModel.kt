package com.example.gpslogger.ui.notes

import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.gpslogger.data.NoteWithPhotos
import com.example.gpslogger.data.NoteWithPhotosRepository
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class NoteCaptureViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: NoteWithPhotosRepository
    private val _currentPhotoPath = MutableLiveData<String?>()
    private val _noteWithPhotos = MutableLiveData<NoteWithPhotos?>()
    
    val currentPhotoPath: LiveData<String?> = _currentPhotoPath
    val noteWithPhotos: LiveData<NoteWithPhotos?> = _noteWithPhotos
    
    init {
        // Initialize repository
        repository = NoteWithPhotosRepository(/* pass your DAO here */)
    }
    
    /**
     * Dispatch intent to capture a photo
     */
    fun dispatchTakePictureIntent(context: android.content.Context) {
        val takePictureIntent = android.provider.MediaStore.ACTION_IMAGE_CAPTURE
        
        // Create the File where the photo should go
        val photoFile: File? = createImageFile()
        photoFile?.also {
            val photoURI: Uri = getPhotoUri(it)
            _currentPhotoPath.value = photoURI.toString()
            
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            context.startActivity(takePictureIntent)
        }
    }
    
    /**
     * Create a file for the photo
     */
    private fun createImageFile(): File? {
        val storageDir = getApplication<Application>().getExternalFilesDir(null)
        return File.createTempFile(
            "GPSLogger_${System.currentTimeMillis()}_",
            ".jpg",
            storageDir
        ).apply {
            // Save the file path for use in ACTION_VIEW intents
        }
    }
    
    /**
     * Get URI for the photo file using ContentResolver
     */
    private fun getPhotoUri(file: File): Uri {
        val contentResolver: ContentResolver = getApplication().contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "GPSLogger/Notes")
        }
        return contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )!!
    }
    
    /**
     * Add a photo to the current note
     */
    fun addPhotoToNote(photoUri: String) {
        val currentNote = _noteWithPhotos.value
        if (currentNote != null) {
            val updatedNote = currentNote.withAddedPhoto(photoUri)
            _noteWithPhotos.value = updatedNote
        }
    }
    
    /**
     * Remove a photo from the note
     */
    fun removePhotoFromNote(photoUri: String) {
        val currentNote = _noteWithPhotos.value
        if (currentNote != null) {
            val updatedNote = currentNote.withRemovedPhoto(photoUri)
            _noteWithPhotos.value = updatedNote
        }
    }
    
    /**
     * Save the note with photos to database
     */
    fun saveNote() {
        _noteWithPhotos.value?.let { note ->
            viewModelScope.launch {
                repository.insert(note)
            }
        }
    }
    
    /**
     * Update existing note
     */
    fun updateNote() {
        _noteWithPhotos.value?.let { note ->
            viewModelScope.launch {
                repository.update(note)
            }
        }
    }
}

// Extension functions for NoteWithPhotos
private fun NoteWithPhotos.withAddedPhoto(photoUri: String): NoteWithPhotos {
    return this.copy(photoUris = this.photoUris + photoUri)
}

private fun NoteWithPhotos.withRemovedPhoto(photoUri: String): NoteWithPhotos {
    return this.copy(photoUris = this.photoUris.filter { it != photoUri })
}