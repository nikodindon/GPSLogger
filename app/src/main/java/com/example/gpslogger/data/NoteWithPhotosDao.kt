package com.example.gpslogger.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for notes with photos
 */
@Dao
interface NoteWithPhotosDao {
    @Insert
    suspend fun insert(note: NoteWithPhotos)
    
    @Update
    suspend fun update(note: NoteWithPhotos)
    
    @Delete
    suspend fun delete(note: NoteWithPhotos)
    
    @Query("SELECT * FROM notes_with_photos WHERE tripId = :tripId ORDER BY timestamp DESC")
    fun getNotesForTrip(tripId: Int): Flow<List<NoteWithPhotos>>
    
    @Query("SELECT * FROM notes_with_photos WHERE id = :noteId")
    suspend fun getNoteById(noteId: Int): NoteWithPhotos?
    
    @Query("SELECT * FROM notes_with_photos ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteWithPhotos>>
    
    @Query("SELECT COUNT(*) FROM notes_with_photos WHERE tripId = :tripId")
    suspend fun getNoteCountForTrip(tripId: Int): Int
}

/**
 * Repository class for note operations
 */
class NoteRepository(private val noteWithPhotosDao: NoteWithPhotosDao) {
    
    val allNotes: Flow<List<NoteWithPhotos>> = noteWithPhotosDao.getAllNotes()
    
    suspend fun insert(note: NoteWithPhotos) {
        noteWithPhotosDao.insert(note)
    }
    
    suspend fun update(note: NoteWithPhotos) {
        noteWithPhotosDao.update(note)
    }
    
    suspend fun delete(note: NoteWithPhotos) {
        noteWithPhotosDao.delete(note)
    }
    
    fun getNotesForTrip(tripId: Int): Flow<List<NoteWithPhotos>> {
        return noteWithPhotosDao.getNotesForTrip(tripId)
    }
    
    suspend fun getNoteById(noteId: Int): NoteWithPhotos? {
        return noteWithPhotosDao.getNoteById(noteId)
    }
    
    suspend fun getNoteCountForTrip(tripId: Int): Int {
        return noteWithPhotosDao.getNoteCountForTrip(tripId)
    }
}