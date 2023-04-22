package com.example.runningtracker.other

import android.content.Context
import android.view.LayoutInflater
import com.example.runningtracker.databinding.MarkerViewBinding
import com.example.runningtracker.db.Run
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView(
    val runs: List<Run>,
    c: Context,
    layoutID: Int
) : MarkerView(c, layoutID) {


    private val binding: MarkerViewBinding = MarkerViewBinding.inflate(LayoutInflater.from(context))

    init {
        addView(binding.root)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if (e == null) {
            return
        }
        val curRunID = e.x.toInt()
        val run = runs[curRunID]

        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        binding.tvDate.text = dateFormat.format(calendar.time)

        binding.tvAvgSpeed.text = "${run.avgSpeedInKmh}km/h"

        binding.tvDistance.text = "${run.distanceInMeters?.div(1000f)}km"

        binding.tvDuration.text = TrackingUtils.getFormattedStopWatchTime(run.timeInMillis)

        binding.tvCaloriesBurned.text = "${run.caloriesBurned}kcal"

    }


}









