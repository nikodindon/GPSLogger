package com.example.gpslogger.data

import android.location.Location

/**
 * Utility class for calculating elevation statistics
 */
object ElevationCalculator {
    
    /**
     * Calculate elevation statistics from a list of GPS points
     */
    fun calculateElevationStatistics(gpsPoints: List<GpsPoint>): ElevationStatistics {
        if (gpsPoints.isEmpty()) {
            return ElevationStatistics(
                totalDistance = 0f,
                totalElevationGain = 0f,
                totalElevationLoss = 0f,
                maxElevation = 0f,
                minElevation = 0f,
                averageElevation = 0f,
                elevationProfile = emptyList()
            )
        }
        
        var totalDistance = 0f
        var totalGain = 0f
        var totalLoss = 0f
        var maxElevation = Float.MIN_VALUE
        var minElevation = Float.MAX_VALUE
        var totalElevation = 0f
        val elevationProfile = mutableListOf<ElevationPoint>()
        
        var previousLocation: Location? = null
        var previousElevation: Float? = null
        var cumulativeDistance = 0f
        
        gpsPoints.forEachIndexed { index, point ->
            val location = point.toLocation()
            
            // Calculate elevation statistics
            point.altitude?.let { altitude ->
                maxElevation = maxOf(maxElevation, altitude)
                minElevation = minOf(minElevation, altitude)
                totalElevation += altitude
                
                // Calculate elevation changes
                previousElevation?.let { prevElev ->
                    val elevationChange = altitude - prevElev
                    if (elevationChange > 0) {
                        totalGain += elevationChange
                    } else {
                        totalLoss += -elevationChange
                    }
                }
                previousElevation = altitude
            }
            
            // Calculate distance
            if (index > 0 && previousLocation != null) {
                val distance = location.distanceTo(previousLocation)
                totalDistance += distance
                cumulativeDistance += distance
                
                // Add elevation point for profile
                previousElevation?.let { elev ->
                    elevationProfile.add(
                        ElevationPoint(
                            distance = cumulativeDistance,
                            elevation = elev,
                            elevationChange = point.altitude - elev
                        )
                    )
                }
            }
            
            previousLocation = location
        }
        
        val averageElevation = if (gpsPoints.isNotEmpty()) {
            totalElevation / gpsPoints.size
        } else {
            0f
        }
        
        return ElevationStatistics(
            totalDistance = totalDistance,
            totalElevationGain = totalGain,
            totalElevationLoss = totalLoss,
            maxElevation = if (gpsPoints.isNotEmpty()) maxElevation else 0f,
            minElevation = if (gpsPoints.isNotEmpty()) minElevation else 0f,
            averageElevation = averageElevation,
            elevationProfile = elevationProfile
        )
    }
    
    /**
     * Convert GpsPoint to Location for distance calculations
     */
    private fun GpsPoint.toLocation(): Location {
        return Location("GPS").apply {
            latitude = this@toLocation.latitude
            longitude = this@toLocation.longitude
            altitude = this@toLocation.altitude ?: 0f
            accuracy = this@toLocation.accuracy
        }
    }
}