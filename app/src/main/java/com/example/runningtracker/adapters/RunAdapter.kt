package com.example.runningtracker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runningtracker.databinding.ItemRunBinding
import com.example.runningtracker.db.Run
import com.example.runningtracker.other.TrackingUtils
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunVH>() {

    inner class RunVH(val binding: ItemRunBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(model: Run) {
            binding.apply {
                Glide.with(root.context).load(model.img).into(ivRunImage)

                val calendar = Calendar.getInstance().apply {
                    timeInMillis = model.timestamp
                }
                val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
                tvDate.text = dateFormat.format(calendar.time)

                tvAvgSpeed.text = "${model.avgSpeedInKmh}km/h"

                tvDistance.text = "${model.distanceInMeters?.div(1000f)}km"

                tvTime.text = TrackingUtils.getFormattedStopWatchTime(model.timeInMillis)

                tvCalories.text = "${model.caloriesBurned}kcal"


            }
        }
    }

    val diffCallback = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Run>) = differ.submitList(list)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunVH {
        val inflater = LayoutInflater.from(parent.context)
        val view = ItemRunBinding.inflate(inflater, parent, false)
        return RunVH(view)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: RunVH, position: Int) {
        val model = differ.currentList[position]
        holder.bind(model)
    }
}