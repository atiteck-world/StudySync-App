package com.atsuite.studysyncapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
            //args.putString("documentId", course.documentId)
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
        val btnEditCourse = view.findViewById<Button>(R.id.btnEditCourse)

        // Retrieve course details from arguments
        arguments?.let {
            course = Course(
                courseName = it.getString("courseName", ""),
                creditPoints = it.getInt("creditPoints", 0),
                startDate = it.getString("startDate", ""),
                endDate = it.getString("endDate", ""),
                //documentId = it.getString("documentId", "")
            )
            etCourseName.setText(course.courseName)
            etCreditPoints.setText(course.creditPoints.toString())
            etStartDate.setText(course.startDate)
            etEndDate.setText(course.endDate)
        }

        // Handle the Edit Course button click
        btnEditCourse.setOnClickListener {
            val updatedCourseName = etCourseName.text.toString().trim()
            val updatedCreditPoints = etCreditPoints.text.toString().toIntOrNull() ?: 0
            val updatedStartDate = etStartDate.text.toString().trim()
            val updatedEndDate = etEndDate.text.toString().trim()

            if (updatedCourseName.isNotEmpty() && updatedStartDate.isNotEmpty() && updatedEndDate.isNotEmpty()) {
                updateCourse(updatedCourseName, updatedCreditPoints, updatedStartDate, updatedEndDate)
            } else {
                Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateCourse(courseName: String, creditPoints: Int, startDate: String, endDate: String) {
        val userId = auth.currentUser?.uid ?: return

        val updatedCourse = mapOf(
            "courseName" to courseName,
            "creditPoints" to creditPoints,
            "startDate" to startDate,
            "endDate" to endDate
        )

        // Update course in Firestore
        db.collection("users").document(userId).collection("courses")
            .document(course.courseName)
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
