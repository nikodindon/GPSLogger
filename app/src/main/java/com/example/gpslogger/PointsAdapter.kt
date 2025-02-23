package com.example.gpslogger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class PointData(val timestamp: String, val latitude: Double, val longitude: Double, val speed: Float, val altitude: Double)

class PointsAdapter(private val points: MutableList<PointData>) : RecyclerView.Adapter<PointsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pointText: TextView = itemView.findViewById(R.id.point_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_point, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val point = points[position]
        holder.pointText.text = "${point.timestamp} | Lat: ${point.latitude} | Lon: ${point.longitude} | Speed: %.1f km/h | Alt: %.1f m".format(point.speed, point.altitude)
    }

    override fun getItemCount(): Int = points.size
}