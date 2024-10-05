package com.atsuite.studysyncapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddScheduleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_schedule)

        val etCourseName = findViewById<EditText>(R.id.etCourseName)
        val etScheduleTime = findViewById<EditText>(R.id.etScheduleTime)
        val etScheduleDate = findViewById<EditText>(R.id.etScheduleDate)
        val btnAddEditSchedule = findViewById<Button>(R.id.btnAddEditSchedule)

        btnAddEditSchedule.setOnClickListener {
            val courseName = etCourseName.text.toString().trim()
            val scheduleTime = etScheduleTime.text.toString().trim()
            val scheduleDate = etScheduleDate.text.toString().trim()

            if (courseName.isNotEmpty() && scheduleTime.isNotEmpty() && scheduleDate.isNotEmpty()) {
                // Handle schedule creation (store in Firebase or database)
                Toast.makeText(this, "Schedule added successfully", Toast.LENGTH_SHORT).show()
                finish()  // Close the Add Schedule screen
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
