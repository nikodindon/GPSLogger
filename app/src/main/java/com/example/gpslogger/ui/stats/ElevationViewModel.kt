package com.example.gpslogger.ui.stats

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.gpslogger.data.ElevationCalculator
import com.example.gpslogger.data.ElevationStatistics
import com.example.gpslogger.data.GpsPointRepository
import kotlinx.coroutines.launch

class ElevationViewModel(private val repository: GpsPointRepository) : AndroidViewModel(application) {
    
    private val _elevationStatistics = MutableLiveData<ElevationStatistics?>()
    val elevationStatistics: LiveData<ElevationStatistics?> = _elevationStatistics
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    /**
     * Load elevation statistics for a specific trip
     */
    fun loadElevationForTrip(tripId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val gpsPoints = repository.getGpsPointsForTrip(tripId)
                val statistics = ElevationCalculator.calculateElevationStatistics(gpsPoints)
                _elevationStatistics.value = statistics
            } catch (e: Exception) {
                _errorMessage.value = "Failed to calculate elevation: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load elevation statistics for all trips
     */
    fun loadOverallElevationStatistics() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val allPoints = repository.getAllGpsPoints()
                val statistics = ElevationCalculator.calculateElevationStatistics(allPoints)
                _elevationStatistics.value = statistics
            } catch (e: Exception) {
                _errorMessage.value = "Failed to calculate elevation: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Get formatted elevation data for chart display
     */
    fun getElevationChartData(): List<Pair<Float, Float>> {
        return _elevationStatistics.value?.elevationProfile?.map {
            it.distance to it.elevation
        } ?: emptyList()
    }
    
    /**
     * Get formatted elevation change data for chart display
     */
    fun getElevationChangeChartData(): List<Pair<Float, Float>> {
        return _elevationStatistics.value?.elevationProfile?.map {
            it.distance to it.elevationChange
        } ?: emptyList()
    }
}