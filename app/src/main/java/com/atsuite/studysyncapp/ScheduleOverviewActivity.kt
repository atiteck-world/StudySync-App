package com.atsuite.studysyncapp

import android.os.Bundle
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ScheduleOverviewActivity : AppCompatActivity() {

    private lateinit var scheduleAdapter: ScheduleAdapter
    private val scheduleList = mutableListOf<Schedule>()
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_overview)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setSubtitle("Schedules")
        setSupportActionBar(toolbar)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Set up RecyclerView
        val rvSchedules = findViewById<RecyclerView>(R.id.rvSchedules)
        rvSchedules.layoutManager = LinearLayoutManager(this)
        scheduleAdapter = ScheduleAdapter(scheduleList)
        rvSchedules.adapter = scheduleAdapter

        // Floating Action Button to Add New Schedule
        val fabAddSchedule = findViewById<FloatingActionButton>(R.id.fabAddSchedule)
        fabAddSchedule.setOnClickListener {
            val addScheduleBottomSheet = AddScheduleBottomSheetFragment()
            addScheduleBottomSheet.show(supportFragmentManager, addScheduleBottomSheet.tag)
        }

        // Button to Reshuffle Schedule
        val fabReshuffleSchedule = findViewById<MaterialButton>(R.id.btnReshuffle)
        fabReshuffleSchedule.setOnClickListener {
            reshuffleSchedule()
        }

        // Set up CalendarView to handle date selection
        setupCalendarView()

        // Load the saved schedule from Firestore
        loadSavedSchedule()
    }

    private fun setupCalendarView() {
        val calendarView = findViewById<CalendarView>(R.id.calendar)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)

            // Get the day of the week (1=Sunday, 2=Monday,..., 7=Saturday)
            val dayOfWeek = selectedDate.get(Calendar.DAY_OF_WEEK)
            val dayMapping = mapDayOfWeekToFirestoreKey(dayOfWeek)

            // Load schedules for the mapped day (e.g., "Day 1" for Monday)
            loadScheduleForDay(dayMapping)
        }
    }

    private fun mapDayOfWeekToFirestoreKey(dayOfWeek: Int): String {
        // Assuming the schedule is stored as "Day 1" for Monday, "Day 7" for Sunday
        return when (dayOfWeek) {
            Calendar.MONDAY -> "Day 1"
            Calendar.TUESDAY -> "Day 2"
            Calendar.WEDNESDAY -> "Day 3"
            Calendar.THURSDAY -> "Day 4"
            Calendar.FRIDAY -> "Day 5"
            Calendar.SATURDAY -> "Day 6"
            Calendar.SUNDAY -> "Day 7"
            else -> ""
        }
    }

    private fun loadSavedSchedule() {
        val userId = auth.currentUser?.uid ?: return

        // Fetch courses from "courses" collection
        db.collection("users").document(userId).collection("courses")
            .get()
            .addOnSuccessListener { courseSnapshot ->
                val courseList = courseSnapshot.toObjects(Course::class.java)

                // Fetch the schedule from "schedule" collection
                db.collection("users").document(userId).collection("schedule")
                    .document("studyPlan")
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val distributedSchedule = document.get("distributedSchedule") as Map<String, List<String>>

                            // Clear the scheduleList to avoid duplications
                            scheduleList.clear()

                            // Set up RecyclerView for the current day initially (e.g., today)
                            val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                            val todayKey = mapDayOfWeekToFirestoreKey(today)
                            loadScheduleForDay(todayKey)
                        }
                    }
                    .addOnFailureListener {
                        // Handle error loading schedule
                        Toast.makeText(this, "Failed to load schedule", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                // Handle error loading courses
                Toast.makeText(this, "Failed to load courses", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadScheduleForDay(dayKey: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("schedule")
            .document("studyPlan")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val distributedSchedule = document.get("distributedSchedule") as Map<String, List<String>>

                    // Clear the scheduleList for new data
                    scheduleList.clear()

                    // Find and add the courses for the selected day
                    val coursesForDay = distributedSchedule[dayKey] ?: emptyList()
                    coursesForDay.forEach { courseName ->
                        // Fetch course details from courseList (You may need to adapt this to match your data structure)
                        db.collection("users").document(userId).collection("courses")
                            .whereEqualTo("courseName", courseName)
                            .get()
                            .addOnSuccessListener { courseSnapshot ->
                                val courseList = courseSnapshot.toObjects(Course::class.java)
                                courseList.forEach { course ->
                                    val progress = if (course.totalStudyHours > 0) {
                                        (course.completedStudyHours / course.totalStudyHours) * 100
                                    } else {
                                        0f
                                    }

                                    // Add schedule entry to the list
                                    scheduleList.add(
                                        Schedule(
                                            course.courseName,
                                            dayKey,
                                            course.dailyStudyHours.toFloat(),
                                            "$progress%"
                                        )
                                    )
                                }

                                // Notify adapter of changes
                                scheduleAdapter.notifyDataSetChanged()
                            }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load schedule for the selected day", Toast.LENGTH_SHORT).show()
            }
    }

    private fun reshuffleSchedule() {
        val userId = auth.currentUser?.uid ?: return

        // Fetch the current schedule from Firestore
        db.collection("users").document(userId).collection("schedule")
            .document("studyPlan")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val distributedSchedule = document.get("distributedSchedule") as Map<String, List<String>>

                    // Flatten the current schedule into a single list of all courses for the week
                    val allCourses = distributedSchedule.values.flatten().toMutableList()

                    // Shuffle the entire list of courses
                    allCourses.shuffle()

                    // Retrieve the coursesPerDay setting
                    val coursesPerDay = document.getLong("coursesPerDay")?.toInt() ?: 0

                    // Redistribute the shuffled courses across the days
                    val reshuffledSchedule = mutableMapOf<String, List<String>>()
                    var currentCourseIndex = 0

                    for (day in 1..7) {
                        val dayKey = "Day $day"
                        val coursesForDay = mutableListOf<String>()

                        // Assign the correct number of courses for this day
                        while (coursesForDay.size < coursesPerDay && currentCourseIndex < allCourses.size) {
                            coursesForDay.add(allCourses[currentCourseIndex])
                            currentCourseIndex++
                        }

                        // Add the reshuffled courses to the schedule
                        reshuffledSchedule[dayKey] = coursesForDay
                    }

                    // Save the reshuffled schedule back to Firestore
                    db.collection("users").document(userId).collection("schedule")
                        .document("studyPlan")
                        .update("distributedSchedule", reshuffledSchedule)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Schedule reshuffled successfully", Toast.LENGTH_SHORT).show()

                            // Reload the schedule after reshuffling
                            loadSavedSchedule()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to reshuffle schedule", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load current schedule for reshuffling", Toast.LENGTH_SHORT).show()
            }
    }

}
