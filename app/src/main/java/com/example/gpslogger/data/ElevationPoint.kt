package com.example.gpslogger.data

/**
 * Data class representing elevation data at a specific distance
 * Used for elevation profile charts
 */
data class ElevationPoint(
    val distance: Float,  // Distance in meters from start
    val elevation: Float,  // Elevation in meters
    val elevationChange: Float  // Change from previous point in meters
)

/**
 * Statistics data class for trip elevation
 */
data class ElevationStatistics(
    val totalDistance: Float,
    val totalElevationGain: Float,
    val totalElevationLoss: Float,
    val maxElevation: Float,
    val minElevation: Float,
    val averageElevation: Float,
    val elevationProfile: List<ElevationPoint>
)