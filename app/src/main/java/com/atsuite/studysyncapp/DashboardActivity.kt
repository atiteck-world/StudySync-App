package com.atsuite.studysyncapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.BiasAbsoluteAlignment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setSubtitle("Master your time")
        setSupportActionBar(toolbar)

        // Initialize bottom navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_dashboard -> {
                    // Handle dashboard navigation
                    true
                }
                R.id.menu_assignments -> {
                    // Navigate to assignments activity
                    /*val intent = Intent(this, AssignmentsActivity::class.java)
                    startActivity(intent)*/
                    true
                }
                R.id.menu_settings -> {
                    // Navigate to settings
                    /*val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)*/
                    true
                }
                else -> false
            }
        }

        // Initialize views
        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val tvDate = findViewById<TextView>(R.id.tvDate)

        // Set greeting based on time of day
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (currentHour) {
            in 0..11 -> "Good Morning!"
            in 12..17 -> "Good Afternoon!"
            else -> "Good Evening!"
        }
        tvGreeting.text = "$greeting, ${auth.currentUser?.displayName ?: "User"}"

        // Set current date
        val currentDate = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date())
        tvDate.text = currentDate

        // Load schedule, assignments, and progress from Firestore
        loadSchedule()
        loadAssignments()
        loadProgress()
    }

    private fun loadSchedule() {
        val userId = auth.currentUser?.uid ?: return
        val layoutSchedule = findViewById<LinearLayout>(R.id.layoutSchedule)

        db.collection("users").document(userId).collection("schedules")
            .get()
            .addOnSuccessListener { result ->
                layoutSchedule.removeAllViews() // Clear any previous views
                for (document in result) {
                    val courseName = document.getString("courseName")
                    val time = document.getString("time")
                    val scheduleId = document.id

                    val scheduleView = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(0, 8, 0, 8)
                    }

                    val scheduleTextView = TextView(this).apply {
                        text = "$time - $courseName"
                        textSize = 16f
                        setPadding(0, 0, 16, 0)
                    }

                    val editButton = Button(this).apply {
                        text = "Edit"
                        setOnClickListener {
                            // Navigate to EditScheduleActivity
                            val intent = Intent(this@DashboardActivity, EditScheduleActivity::class.java)
                            intent.putExtra("SCHEDULE_ID", scheduleId)
                            startActivity(intent)
                        }
                    }

                    // Create delete button
                    val deleteButton = Button(this).apply {
                        text = "Delete"
                        setOnClickListener {
                            deleteSchedule(scheduleId)
                        }
                    }

                    scheduleView.addView(scheduleTextView)
                    scheduleView.addView(editButton)
                    scheduleView.addView(deleteButton)

                    layoutSchedule.addView(scheduleView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load schedule", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadAssignments() {
        val userId = auth.currentUser?.uid ?: return
        val layoutAssignments = findViewById<LinearLayout>(R.id.layoutAssignments)

        db.collection("users").document(userId).collection("assignments")
            .get()
            .addOnSuccessListener { result ->
                layoutAssignments.removeAllViews() // Clear previous views
                for (document in result) {
                    val assignmentName = document.getString("assignmentName")
                    val dueDate = document.getString("dueDate")
                    val assignmentId = document.id

                    // Create a view for each assignment
                    val assignmentView = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(0, 8, 0, 8)
                    }

                    val assignmentTextView = TextView(this).apply {
                        text = "$assignmentName due $dueDate"
                        textSize = 16f
                        setPadding(0, 0, 16, 0)
                    }

                    // Create delete button
                    val deleteButton = Button(this).apply {
                        text = "Delete"
                        setOnClickListener {
                            deleteAssignment(assignmentId)
                        }
                    }

                    assignmentView.addView(assignmentTextView)
                    assignmentView.addView(deleteButton)

                    layoutAssignments.addView(assignmentView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load assignments", Toast.LENGTH_SHORT).show()
            }
    }


    private fun loadProgress() {
        val userId = auth.currentUser?.uid ?: return
        val layoutProgress = findViewById<LinearLayout>(R.id.layoutProgress)

        db.collection("users").document(userId).collection("progress")
            .get()
            .addOnSuccessListener { result ->
                layoutProgress.removeAllViews() // Clear previous views
                for (document in result) {
                    val courseName = document.getString("courseName")
                    val hoursStudied = document.getLong("hoursStudied") ?: 0
                    val sessionsCompleted = document.getLong("sessionsCompleted") ?: 0
                    layoutProgress.addView(createTextView("$courseName - Hours studied: $hoursStudied, Sessions: $sessionsCompleted"))
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load progress", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteAssignment(assignmentId: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("assignments").document(assignmentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Assignment deleted successfully", Toast.LENGTH_SHORT).show()
                loadAssignments() // Refresh the list after deletion
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete assignment", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteSchedule(scheduleId: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("schedules").document(scheduleId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Schedule deleted successfully", Toast.LENGTH_SHORT).show()
                loadAssignments() // Refresh the list after deletion
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete Schedule", Toast.LENGTH_SHORT).show()
            }
    }


    private fun createTextView(text: String): TextView {
        val textView = TextView(this)
        textView.text = text
        textView.textSize = 16f
        textView.setPadding(0, 8, 0, 8)
        return textView
    }
}
