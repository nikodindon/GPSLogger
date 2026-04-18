package com.example.gpslogger.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
nimport androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.gpslogger.R
import com.example.gpslogger.databinding.FragmentElevationBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class ElevationFragment : Fragment() {
    
    private var _binding: FragmentElevationBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ElevationViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentElevationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupChart()
        setupObservers()
        loadStatistics()
    }
    
    private fun setupChart() {
        val chart = binding.elevationChart
        
        // Disable description
        chart.description = Description().apply {
            text = ""
        }
        
        // Enable touch gestures
        chart.isTouchEnabled = true
        
        // Enable scaling and dragging
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        
        // Enable pinch zoom
        chart.setPinchZoom(true)
        
        // Set background color
        chart.setBackgroundColor(resources.getColor(R.color.chart_background))
    }
    
    private fun setupObservers() {
        viewModel.elevationStatistics.observe(viewLifecycleOwner, Observer { statistics ->
            statistics?.let {
                updateChart(it)
                updateStatisticsUI(it)
            }
        })
        
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading == true) View.VISIBLE else View.GONE
        })
        
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            errorMessage?.let {
                // Show error message to user
            }
        })
    }
    
    private fun updateChart(statistics: ElevationStatistics) {
        val chart = binding.elevationChart
        
        // Create elevation line data
        val elevationEntries = statistics.elevationProfile.mapIndexed { index, point ->
            Entry(index.toFloat(), point.elevation)
        }
        
        val elevationDataSet = LineDataSet(elevationEntries, "Elevation").apply {
            color = getColor(requireContext(), R.color.elevation_line)
            valueTextColor = getColor(requireContext(), R.color.elevation_text)
            lineWidth = 2f
            setDrawCircles(true)
            circleColor = getColor(requireContext(), R.color.elevation_point)
            circleRadius = 4f
            setDrawValues(true)
        }
        
        // Create elevation change data (optional secondary chart)
        val changeEntries = statistics.elevationProfile.mapIndexed { index, point ->
            Entry(index.toFloat(), point.elevationChange)
        }
        
        val changeDataSet = LineDataSet(changeEntries, "Elevation Change").apply {
            color = getColor(requireContext(), R.color.change_line)
            valueTextColor = getColor(requireContext(), R.color.change_text)
            lineWidth = 1.5f
            setDrawCircles(false)
            setDrawValues(false)
        }
        
        val data = LineData(elevationDataSet, changeDataSet)
        chart.data = data
        chart.invalidate()
    }
    
    private fun updateStatisticsUI(statistics: ElevationStatistics) {
        binding.apply {
            tvTotalDistance.text = "${statistics.totalDistance} m"
            tvTotalGain.text = "+${statistics.totalElevationGain} m"
            tvTotalLoss.text = "${statistics.totalElevationLoss} m"
            tvMaxElevation.text = "${statistics.maxElevation} m"
            tvMinElevation.text = "${statistics.minElevation} m"
            tvAvgElevation.text = "${statistics.averageElevation} m"
        }
    }
    
    private fun loadStatistics() {
        // Load for current trip or overall
        viewModel.loadOverallElevationStatistics()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}