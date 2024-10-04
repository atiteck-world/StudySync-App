package com.atsuite.studysyncapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditAssignmentActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var assignmentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_assignment)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Get the assignmentId passed from the previous activity
        assignmentId = intent.getStringExtra("ASSIGNMENT_ID") ?: return

        // Initialize views
        val etAssignmentName = findViewById<EditText>(R.id.etAssignmentName)
        val etDueDate = findViewById<EditText>(R.id.etDueDate)
        val btnEditAssignment = findViewById<Button>(R.id.btnEditAssignment)

        // Load existing assignment details
        loadAssignmentDetails(assignmentId, etAssignmentName, etDueDate)

        // Handle Update Assignment button click
        btnEditAssignment.setOnClickListener {
            val assignmentName = etAssignmentName.text.toString()
            val dueDate = etDueDate.text.toString()

            if (assignmentName.isNotEmpty() && dueDate.isNotEmpty()) {
                updateAssignment(assignmentName, dueDate)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadAssignmentDetails(assignmentId: String, etAssignmentName: EditText, etDueDate: EditText) {
        val userId = auth.currentUser?.uid ?: return

        // Fetch assignment details from Firestore
        db.collection("users").document(userId).collection("assignments").document(assignmentId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    etAssignmentName.setText(document.getString("assignmentName"))
                    etDueDate.setText(document.getString("dueDate"))
                } else {
                    Toast.makeText(this, "Assignment not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load assignment details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateAssignment(assignmentName: String, dueDate: String) {
        val userId = auth.currentUser?.uid ?: return

        // Update assignment in Firestore
        val updatedAssignment = hashMapOf(
            "assignmentName" to assignmentName,
            "dueDate" to dueDate
        )

        db.collection("users").document(userId).collection("assignments").document(assignmentId)
            .update(updatedAssignment as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Assignment updated successfully", Toast.LENGTH_SHORT).show()
                finish() // Close activity after updating
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update assignment", Toast.LENGTH_SHORT).show()
            }
    }
}
