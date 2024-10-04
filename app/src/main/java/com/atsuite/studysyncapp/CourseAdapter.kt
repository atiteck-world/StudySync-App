package com.atsuite.studysyncapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Course(val courseName: String = "", val creditPoints: Int = 0, val startDate: String = "", val endDate: String = "")

class CourseAdapter(
    private var courses: List<Course>,
    private val onEditClick: (Course) -> Unit,
    private val onDeleteClick: (Course) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val courseName: TextView = view.findViewById(R.id.tvCourseName)
        val creditPoints: TextView = view.findViewById(R.id.tvCreditPoints)
        val semesterDates: TextView = view.findViewById(R.id.tvSemesterDates)
        val ivEditCourse: ImageView = view.findViewById(R.id.ivEditCourse)
        val ivDeleteCourse: ImageView = view.findViewById(R.id.ivDeleteCourse)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.courseName.text = course.courseName
        holder.creditPoints.text = "${course.creditPoints} Credit Points"
        holder.semesterDates.text = "${course.startDate} - ${course.endDate}"

        // Handle Edit and Delete Clicks
        holder.ivEditCourse.setOnClickListener {
            onEditClick(course)
        }
        holder.ivDeleteCourse.setOnClickListener {
            onDeleteClick(course)
        }
    }

    override fun getItemCount() = courses.size

    // Update the courses list dynamically
    fun updateCourses(newCourses: List<Course>) {
        courses = newCourses
        notifyDataSetChanged()
    }
}
