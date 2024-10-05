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

class EditCourseBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var course: Course

    companion object {
        fun newInstance(course: Course): EditCourseBottomSheetFragment {
            val fragment = EditCourseBottomSheetFragment()
            val args = Bundle()
            args.putString("courseName", course.courseName)
            args.putInt("creditPoints", course.creditPoints)
            args.putString("startDate", course.startDate)
            args.putString("endDate", course.endDate)
            args.putFloat("totalStudyHours", course.totalStudyHours)
            args.putFloat("completedStudyHours", course.completedStudyHours)
            args.putFloat("dailyStudyHours", course.dailyStudyHours)
            args.putInt("studyDaysPerWeek", course.studyDaysPerWeek)
            args.putString("documentId", course.documentId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_edit_course, container, false)
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
        val etTotalStudyHours = view.findViewById<TextView>(R.id.tvTotalStudyHours)
        val etCompletedStudyHours = view.findViewById<EditText>(R.id.etCompletedStudyHours)
        val etDailyStudyHours = view.findViewById<EditText>(R.id.etDailyStudyHours)
        val etStudyDaysPerWeek = view.findViewById<EditText>(R.id.etStudyDaysPerWeek)
        val btnEditCourse = view.findViewById<Button>(R.id.btnEditCourse)

        // Retrieve course details from arguments
        arguments?.let {
            course = Course(
                courseName = it.getString("courseName", ""),
                creditPoints = it.getInt("creditPoints", 0),
                startDate = it.getString("startDate", ""),
                endDate = it.getString("endDate", ""),
                totalStudyHours = it.getFloat("totalStudyHours", 0f),
                completedStudyHours = it.getFloat("completedStudyHours", 0f),
                dailyStudyHours = it.getFloat("dailyStudyHours", 0f),
                studyDaysPerWeek = it.getInt("studyDaysPerWeek", 0),
                documentId = it.getString("documentId", "")
            )
            etCourseName.setText(course.courseName)
            etCreditPoints.setText(course.creditPoints.toString())
            etStartDate.setText(course.startDate)
            etEndDate.setText(course.endDate)
            etTotalStudyHours.setText(course.totalStudyHours.toString())
            etCompletedStudyHours.setText(course.completedStudyHours.toString())
            etDailyStudyHours.setText(course.dailyStudyHours.toString())
            etStudyDaysPerWeek.setText(course.studyDaysPerWeek.toString())
        }

        // Handle the Edit Course button click
        btnEditCourse.setOnClickListener {
            val updatedCourseName = etCourseName.text.toString().trim()
            val updatedCreditPoints = etCreditPoints.text.toString().toIntOrNull() ?: 0
            val updatedStartDate = etStartDate.text.toString().trim()
            val updatedEndDate = etEndDate.text.toString().trim()
            val updatedTotalStudyHours = etTotalStudyHours.text.toString().toFloatOrNull() ?: 0f
            val updatedCompletedStudyHours = etCompletedStudyHours.text.toString().toFloatOrNull() ?: 0f
            val updatedDailyStudyHours = etDailyStudyHours.text.toString().toFloatOrNull() ?: 0f
            val updatedStudyDaysPerWeek = etStudyDaysPerWeek.text.toString().toIntOrNull() ?: 0

            if (updatedCourseName.isNotEmpty() && updatedStartDate.isNotEmpty() && updatedEndDate.isNotEmpty()) {
                updateCourse(updatedCourseName, updatedCreditPoints, updatedStartDate, updatedEndDate, updatedTotalStudyHours, updatedCompletedStudyHours, updatedDailyStudyHours, updatedStudyDaysPerWeek)
            } else {
                Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateCourse(
        courseName: String,
        creditPoints: Int,
        startDate: String,
        endDate: String,
        totalStudyHours: Float,
        completedStudyHours: Float,
        dailyStudyHours: Float,
        studyDaysPerWeek: Int
    ) {
        val userId = auth.currentUser?.uid ?: return

        val updatedCourse = mapOf(
            "courseName" to courseName,
            "creditPoints" to creditPoints,
            "startDate" to startDate,
            "endDate" to endDate,
            "totalStudyHours" to totalStudyHours,
            "completedStudyHours" to completedStudyHours,
            "dailyStudyHours" to dailyStudyHours,
            "studyDaysPerWeek" to studyDaysPerWeek
        )

        // Update course in Firestore
        db.collection("users").document(userId).collection("courses")
            .document(course.documentId)
            .update(updatedCourse)
            .addOnSuccessListener {
                Toast.makeText(context, "Course updated successfully", Toast.LENGTH_SHORT).show()
                dismiss()  // Close the bottom sheet after updating
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update course", Toast.LENGTH_SHORT).show()
            }
    }
}
