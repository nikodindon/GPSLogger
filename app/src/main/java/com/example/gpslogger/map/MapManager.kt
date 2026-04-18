package com.example.gpslogger.map

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import org.osmdroid.views.MapView

/**
 * Manages map provider switching and configuration
 */
class MapManager private constructor(private val context: Context) {
    
    private var currentProvider: MapProviderType = MapProviderFactory.getDefaultProvider()
    private var mapProvider: MapProvider = MapProviderFactory.createProvider(currentProvider)
    
    fun setMapProvider(type: MapProviderType) {
        if (currentProvider != type) {
            currentProvider = type
            mapProvider = MapProviderFactory.createProvider(type)
            // Notify UI to recreate map
        }
    }
    
    fun getCurrentProvider(): MapProviderType = currentProvider
    
    fun getMapProvider(): MapProvider = mapProvider
    
    fun initializeMap(mapView: MapView): MapView {
        mapProvider.initializeMap(mapView)
        return mapView
    }
    
    fun initializeMap(googleMapOptions: GoogleMapOptions): GoogleMap {
        return mapProvider.initializeMap(googleMapOptions)
    }
    
    fun isOfflineCapable(): Boolean = mapProvider.isOfflineCapable()
    
    companion object {
        @Volatile
        private var instance: MapManager? = null
        
        fun getInstance(context: Context): MapManager {
            return instance ?: synchronized(this) {
                instance ?: MapManager(context.applicationContext).also { instance = it }
            }
        }
    }
}