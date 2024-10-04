package com.atsuite.studysyncapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddAssignmentActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_assignment)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        val etAssignmentName = findViewById<EditText>(R.id.etAssignmentName)
        val etDueDate = findViewById<EditText>(R.id.etDueDate)
        val btnAddAssignment = findViewById<Button>(R.id.btnAddAssignment)

        // Handle Add Assignment button click
        btnAddAssignment.setOnClickListener {
            val assignmentName = etAssignmentName.text.toString()
            val dueDate = etDueDate.text.toString()

            if (assignmentName.isNotEmpty() && dueDate.isNotEmpty()) {
                addAssignment(assignmentName, dueDate)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addAssignment(assignmentName: String, dueDate: String) {
        // Get the current user ID
        val userId = auth.currentUser?.uid ?: return

        // Create a new assignment entry in Firestore
        val assignment = hashMapOf(
            "assignmentName" to assignmentName,
            "dueDate" to dueDate
        )

        db.collection("users").document(userId).collection("assignments")
            .add(assignment)
            .addOnSuccessListener {
                Toast.makeText(this, "Assignment added successfully", Toast.LENGTH_SHORT).show()
                finish() // Close activity after adding the assignment
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add assignment", Toast.LENGTH_SHORT).show()
            }
    }
}
