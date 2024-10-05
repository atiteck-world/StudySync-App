package com.atsuite.studysyncapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

class AddCourseBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.bottom_sheet_add_course, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etCourseName = view.findViewById<EditText>(R.id.etCourseName)
        val etCreditPoints = view.findViewById<EditText>(R.id.etCreditPoints)
        val etStartDate = view.findViewById<EditText>(R.id.etStartDate)
        val etEndDate = view.findViewById<EditText>(R.id.etEndDate)
        val etDailyStudyHours = view.findViewById<EditText>(R.id.etDailyStudyHours)
        val etStudyDaysPerWeek = view.findViewById<EditText>(R.id.etStudyDaysPerWeek)
        val tvTotalStudyHours = view.findViewById<TextView>(R.id.tvTotalStudyHours)
        val etCompletedStudyHours = view.findViewById<EditText>(R.id.etCompletedStudyHours)
        val btnAddCourse = view.findViewById<Button>(R.id.btnAddCourse)

        // Handle Add Course button click
        btnAddCourse.setOnClickListener {
            val courseName = etCourseName.text.toString().trim()
            val creditPoints = etCreditPoints.text.toString().toIntOrNull()
            val startDate = etStartDate.text.toString().trim()
            val endDate = etEndDate.text.toString().trim()
            val dailyStudyHours = etDailyStudyHours.text.toString().toFloatOrNull()
            val studyDaysPerWeek = etStudyDaysPerWeek.text.toString().toIntOrNull()
            val completedStudyHours = etCompletedStudyHours.text.toString().toFloatOrNull() ?: 0f

            if (courseName.isNotEmpty() && creditPoints != null && startDate.isNotEmpty() && endDate.isNotEmpty() && dailyStudyHours != null && studyDaysPerWeek != null) {
                val totalStudyHours = calculateTotalStudyHours(startDate, endDate, dailyStudyHours, studyDaysPerWeek)
                tvTotalStudyHours.text = totalStudyHours.toString()
                if (totalStudyHours != null) {
                    addCourse(courseName, creditPoints, startDate, endDate, totalStudyHours, completedStudyHours, dailyStudyHours, studyDaysPerWeek)
                } else {
                    Toast.makeText(context, "Invalid date format. Please use dd/MM/yyyy", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateTotalStudyHours(startDate: String, endDate: String, dailyStudyHours: Float, studyDaysPerWeek: Int): Float? {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            val start = dateFormat.parse(startDate)
            val end = dateFormat.parse(endDate)

            // Calculate the difference in days
            val diffInMillis = end.time - start.time
            val daysBetween = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()

            // Convert days to weeks (rounded up)
            val weeksBetween = ceil(daysBetween / 7.0).toInt()

            // Calculate total study hours
            dailyStudyHours * studyDaysPerWeek * weeksBetween
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun addCourse(courseName: String, creditPoints: Int, startDate: String, endDate: String, totalStudyHours: Float, completedStudyHours: Float = 0f, dailyStudyHours: Float, studyDaysPerWeek: Int) {
        val userId = auth.currentUser?.uid ?: return

        val course = hashMapOf(
            "courseName" to courseName,
            "creditPoints" to creditPoints,
            "startDate" to startDate,
            "endDate" to endDate,
            "totalStudyHours" to totalStudyHours,
            "completedStudyHours" to completedStudyHours,
            "dailyStudyHours" to dailyStudyHours,
            "studyDaysPerWeek" to studyDaysPerWeek
        )

        val courseRef = db.collection("users").document(userId).collection("courses").document()
        courseRef.set(course)
            .addOnSuccessListener {
                Toast.makeText(context, "Course added successfully", Toast.LENGTH_SHORT).show()
                dismiss() // Close the BottomSheet after adding the course
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to add course", Toast.LENGTH_SHORT).show()
            }
    }
}
