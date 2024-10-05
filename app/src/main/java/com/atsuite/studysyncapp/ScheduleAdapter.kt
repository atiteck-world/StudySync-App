package com.atsuite.studysyncapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Schedule(val courseName: String, val day: String, val studyHours: Float, val progress: String)

class ScheduleAdapter(private val schedules: List<Schedule>) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    class ScheduleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val courseName: TextView = view.findViewById(R.id.tvCourseName)
        val day: TextView = view.findViewById(R.id.tvDay)
        val studyHours: TextView = view.findViewById(R.id.tvStudyHours)
        val progress: TextView = view.findViewById(R.id.tvProgress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = schedules[position]
        holder.courseName.text = schedule.courseName
        holder.day.text = "Day: ${schedule.day}"
        holder.studyHours.text = "Study Hours: ${schedule.studyHours}"
        holder.progress.text = "Progress: ${schedule.progress}"  // Optional field for progress
    }

    override fun getItemCount() = schedules.size
}
