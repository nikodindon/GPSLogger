package com.example.gpslogger.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gpslogger.R
import com.example.gpslogger.databinding.FragmentMapBinding
import com.example.gpslogger.map.MapManager
import org.osmdroid.views.MapView

class MapFragment : Fragment() {
    
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    
    private val mapManager: MapManager by lazy {
        MapManager.getInstance(requireContext())
    }
    
    private lateinit var mapView: MapView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        mapView = binding.mapView
        initializeMap()
    }
    
    private fun initializeMap() {
        val providerType = mapManager.getCurrentProvider()
        
        when (providerType) {
            MapProviderType.OSM -> {
                mapManager.initializeMap(mapView)
            }
            MapProviderType.GOOGLE -> {
                // Google Maps initialization would go here
                // val googleMap = mapManager.initializeMap(googleMapOptions)
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}