package com.example.gpslogger.map

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.GoogleMapOptions
import org.osmdroid.views.MapView

/**
 * Enum for available map providers
 */enum class MapProviderType {
    OSM,      // OpenStreetMap with osmdroid
    GOOGLE    // Google Maps
}

/**
 * Interface for map provider implementations
 */
interface MapProvider {
    fun getMapType(): MapProviderType
    fun isOfflineCapable(): Boolean
    fun initializeMap(mapView: MapView)
    fun initializeMap(googleMapOptions: GoogleMapOptions): GoogleMap
    fun getMapLayoutId(): Int
}

/**
 * OpenStreetMap provider using osmdroid
 */
class OsmMapProvider : MapProvider {
    override fun getMapType(): MapProviderType = MapProviderType.OSM
    
    override fun isOfflineCapable(): Boolean = true
    
    override fun initializeMap(mapView: MapView) {
        // osmdroid initialization
        mapView.setTileSource(osmdroid.tileprovider.tilesource.OfflineTileSourceBase)
    }
    
    override fun initializeMap(googleMapOptions: GoogleMapOptions): GoogleMap {
        throw UnsupportedOperationException("OsmProvider doesn't support Google Maps")
    }
    
    override fun getMapLayoutId(): Int = R.layout.map_osm_fragment
}

/**
 * Google Maps provider
 */
class GoogleMapProvider : MapProvider {
    override fun getMapType(): MapProviderType = MapProviderType.GOOGLE
    
    override fun isOfflineCapable(): Boolean = false
    
    override fun initializeMap(mapView: MapView): GoogleMap {
        throw UnsupportedOperationException("GoogleProvider doesn't support MapView directly")
    }
    
    override fun initializeMap(googleMapOptions: GoogleMapOptions): GoogleMap {
        // Google Maps initialization
        return GoogleMap(googleMapOptions)
    }
    
    override fun getMapLayoutId(): Int = R.layout.map_google_fragment
}

/**
 * Map provider factory
 */
object MapProviderFactory {
    fun createProvider(type: MapProviderType): MapProvider {
        return when (type) {
            MapProviderType.OSM -> OsmMapProvider()
            MapProviderType.GOOGLE -> GoogleMapProvider()
        }
    }
    
    fun getDefaultProvider(): MapProviderType = MapProviderType.OSM
}