package com.atsuite.studysyncapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddScheduleBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var courseList: MutableList<Course> = mutableListOf()  // Initialize to an empty list

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_add_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etCoursesPerDay = view.findViewById<EditText>(R.id.etCoursesPerDay)
        val btnAddSchedule = view.findViewById<Button>(R.id.btnAddSchedule)

        btnAddSchedule.setOnClickListener {
            val coursesPerDay = etCoursesPerDay.text.toString().trim().toIntOrNull()

            if (coursesPerDay != null) {
                // Load courses and distribute them after courses are fetched
                fetchCoursesAndDistribute(coursesPerDay)
                //Toast.makeText(context, "Number of courses per day added", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please enter the number of courses per day", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchCoursesAndDistribute(coursesPerDay: Int) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("courses")
            .get()
            .addOnSuccessListener { result ->
                courseList = result.documents.mapNotNull { document ->
                    document.toObject(Course::class.java)
                }.toMutableList()

                // Check if course list is empty
                if (courseList.isNotEmpty()) {
                    // Distribute courses only if we have courses
                    distributeCourses(coursesPerDay)
                    //Toast.makeText(context, "Courses distributed successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No courses available to distribute", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load courses", Toast.LENGTH_SHORT).show()
            }
    }

    private fun distributeCourses(coursesPerDay: Int) {
        val userId = auth.currentUser?.uid ?: return

        // Debugging statement: Log the coursesPerDay
        Log.d("ScheduleDebug", "Distributing $coursesPerDay courses per day.")

        // Distribute the courses across the week
        val scheduleMap = distributeCoursesAcrossDays(coursesPerDay)

        // Convert the schedule map to a format that can be saved in Firestore
        val scheduleData = scheduleMap.mapValues { dayCourses ->
            dayCourses.value.map { it.courseName }  // Save course names for each day
        }

        // Save both courses per day and the distributed schedule in Firestore
        val userScheduleData = mapOf(
            "coursesPerDay" to coursesPerDay,
            "distributedSchedule" to scheduleData
        )

        db.collection("users").document(userId)
            .collection("schedule")
            .document("studyPlan")
            .set(userScheduleData)
            .addOnSuccessListener {
                Log.d("ScheduleDebug", "Schedule saved successfully.")
                Toast.makeText(context, "Schedule saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Log.e("ScheduleDebug", "Failed to save schedule.")
                Toast.makeText(context, "Failed to save schedule", Toast.LENGTH_SHORT).show()
            }
    }

    private fun distributeCoursesAcrossDays(coursesPerDay: Int): Map<String, List<Course>> {
        val scheduleMap = mutableMapOf<String, MutableList<Course>>()
        val totalStudyDays = 7  // Assuming a 7-day week
        var courseIndex = 0

        // Track the number of days each course has been assigned
        val courseStudyDayCount = mutableMapOf<String, Int>()  // Map courseName to number of days it has been assigned

        // Initialize course study day count
        for (course in courseList) {
            courseStudyDayCount[course.courseName] = 0
        }

        // Track how many courses are fully assigned to their study days
        var fullyAssignedCourses = 0

        // Rotate through the days and assign courses
        for (day in 1..totalStudyDays) {
            val coursesForDay = mutableListOf<Course>()
            var remainingCourses = coursesPerDay

            // Debugging statement: Log which day we are assigning courses to
            Log.d("ScheduleDebug", "Assigning courses for Day $day.")

            // Keep assigning courses until the number of courses per day is filled
            while (remainingCourses > 0 && courseList.isNotEmpty()) {
                val course = courseList[courseIndex]

                // Check if the course has been assigned the number of days it is supposed to
                val assignedDays = courseStudyDayCount[course.courseName] ?: 0
                if (assignedDays < course.studyDaysPerWeek) {
                    // Assign the current course
                    coursesForDay.add(course)

                    // Update the course's assigned day count
                    courseStudyDayCount[course.courseName] = assignedDays + 1
                    remainingCourses--

                    // Debugging statement: Log the course being added and how many days it has been assigned
                    Log.d("ScheduleDebug", "Assigned ${course.courseName} to Day $day. It has now been assigned ${assignedDays + 1} days.")
                }

                // Check if the course is fully assigned
                if (assignedDays + 1 == course.studyDaysPerWeek) {
                    fullyAssignedCourses++
                }

                // If all courses are fully assigned, break out of the loop
                if (fullyAssignedCourses == courseList.size) {
                    Log.d("ScheduleDebug", "All courses have been fully assigned. Stopping distribution.")
                    break
                }

                // Move to the next course in the list
                courseIndex = (courseIndex + 1) % courseList.size
            }

            // Assign the courses to the day
            scheduleMap["Day $day"] = coursesForDay

            // Debugging statement: Log how many courses were assigned to the day
            Log.d("ScheduleDebug", "Day $day: Assigned ${coursesForDay.size} courses.")
        }

        return scheduleMap
    }

}
