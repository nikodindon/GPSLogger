package com.example.gpslogger.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
nimport androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.gpslogger.R
import com.example.gpslogger.databinding.FragmentStatisticsBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter

class StatisticsFragment : Fragment() {
    
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: StatisticsViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupSpeedChart()
        setupPaceChart()
        loadStatistics()
    }
    
    private fun setupSpeedChart() {
        val chart = binding.speedDistributionChart
        chart.description = Description().apply { text = "" }
        chart.isTouchEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)
    }
    
    private fun setupPaceChart() {
        val chart = binding.paceDistributionChart
        chart.description = Description().apply { text = "" }
        chart.isTouchEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)
    }
    
    private fun loadStatistics() {
        // This would load from repository
        // For now, show placeholder
        updateSampleData()
    }
    
    private fun updateSampleData() {
        // Sample speed distribution data
        val speedEntries = listOf(
            BarEntry(0f, 5f),  // 0-5 km/h
            BarEntry(1f, 15f), // 5-10 km/h
            BarEntry(2f, 50f), // 10-15 km/h
            BarEntry(3f, 25f), // 15-20 km/h
            BarEntry(4f, 5f)   // 20+ km/h
        )
        
        val speedDataSet = BarDataSet(speedEntries, "Speed Distribution").apply {
            colors = listOf(
                getColor(requireContext(), R.color.speed_0_5),
                getColor(requireContext(), R.color.speed_5_10),
                getColor(requireContext(), R.color.speed_10_15),
                getColor(requireContext(), R.color.speed_15_20),
                getColor(requireContext(), R.color.speed_20_plus)
            )
            valueTextSize = 10f
        }
        
        val speedData = BarData(speedDataSet)
        binding.speedDistributionChart.data = speedData
        binding.speedDistributionChart.invalidate()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}