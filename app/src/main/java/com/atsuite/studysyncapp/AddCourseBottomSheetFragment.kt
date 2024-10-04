package com.atsuite.studysyncapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
        val btnAddCourse = view.findViewById<Button>(R.id.btnAddCourse)

        // Handle Add Course button click
        btnAddCourse.setOnClickListener {
            val courseName = etCourseName.text.toString().trim()
            val creditPoints = etCreditPoints.text.toString().toIntOrNull()
            val startDate = etStartDate.text.toString().trim()
            val endDate = etEndDate.text.toString().trim()

            if (courseName.isNotEmpty() && creditPoints != null && startDate.isNotEmpty() && endDate.isNotEmpty()) {
                addCourse(courseName, creditPoints, startDate, endDate)
            } else {
                Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addCourse(courseName: String, creditPoints: Int, startDate: String, endDate: String) {
        val userId = auth.currentUser?.uid ?: return

        val course = hashMapOf(
            "courseName" to courseName,
            "creditPoints" to creditPoints,
            "startDate" to startDate,
            "endDate" to endDate
        )

        db.collection("users").document(userId).collection("courses")
            .add(course)
            .addOnSuccessListener {
                Toast.makeText(context, "Course added successfully", Toast.LENGTH_SHORT).show()
                dismiss() // Close the BottomSheet after adding the course
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to add course", Toast.LENGTH_SHORT).show()
            }
    }
}
