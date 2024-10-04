package com.atsuite.studysyncapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddScheduleActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_schedule)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        val etCourseName = findViewById<EditText>(R.id.etCourseName)
        val etCourseTime = findViewById<EditText>(R.id.etCourseTime)
        val btnAddSchedule = findViewById<Button>(R.id.btnAddSchedule)

        // Handle Add Schedule button click
        btnAddSchedule.setOnClickListener {
            val courseName = etCourseName.text.toString()
            val courseTime = etCourseTime.text.toString()

            if (courseName.isNotEmpty() && courseTime.isNotEmpty()) {
                addSchedule(courseName, courseTime)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addSchedule(courseName: String, courseTime: String) {
        // Get the current user ID
        val userId = auth.currentUser?.uid ?: return

        // Create a new schedule entry in Firestore
        val schedule = hashMapOf(
            "courseName" to courseName,
            "time" to courseTime
        )

        db.collection("users").document(userId).collection("schedules")
            .add(schedule)
            .addOnSuccessListener {
                Toast.makeText(this, "Schedule added successfully", Toast.LENGTH_SHORT).show()
                finish() // Close activity after adding the schedule
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add schedule", Toast.LENGTH_SHORT).show()
            }
    }
}
