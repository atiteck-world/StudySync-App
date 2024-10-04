package com.atsuite.studysyncapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditScheduleActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var scheduleId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_schedule)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Get the scheduleId passed from the previous activity
        scheduleId = intent.getStringExtra("SCHEDULE_ID") ?: return

        // Initialize views
        val etCourseName = findViewById<EditText>(R.id.etCourseName)
        val etCourseTime = findViewById<EditText>(R.id.etCourseTime)
        val btnEditSchedule = findViewById<Button>(R.id.btnEditSchedule)

        // Load existing schedule details
        loadScheduleDetails(scheduleId, etCourseName, etCourseTime)

        // Handle Edit Schedule button click
        btnEditSchedule.setOnClickListener {
            val courseName = etCourseName.text.toString()
            val courseTime = etCourseTime.text.toString()

            if (courseName.isNotEmpty() && courseTime.isNotEmpty()) {
                updateSchedule(courseName, courseTime)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadScheduleDetails(scheduleId: String, etCourseName: EditText, etCourseTime: EditText) {
        val userId = auth.currentUser?.uid ?: return

        // Fetch schedule details
        db.collection("users").document(userId).collection("schedules").document(scheduleId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    etCourseName.setText(document.getString("courseName"))
                    etCourseTime.setText(document.getString("time"))
                } else {
                    Toast.makeText(this, "Schedule not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load schedule details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateSchedule(courseName: String, courseTime: String) {
        val userId = auth.currentUser?.uid ?: return

        // Update schedule in Firestore
        val updatedSchedule = hashMapOf(
            "courseName" to courseName,
            "time" to courseTime
        )

        db.collection("users").document(userId).collection("schedules").document(scheduleId)
            .update(updatedSchedule as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Schedule updated successfully", Toast.LENGTH_SHORT).show()
                finish() // Close activity after updating
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update schedule", Toast.LENGTH_SHORT).show()
            }
    }
}
