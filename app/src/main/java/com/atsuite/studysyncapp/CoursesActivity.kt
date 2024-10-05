package com.atsuite.studysyncapp

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.snapshotFlow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CoursesActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var courseAdapter: CourseAdapter
    private val courseList = mutableListOf<Course>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_courses)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setSubtitle("Courses")
        setSupportActionBar(toolbar)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Set up RecyclerView
        val rvCourses = findViewById<RecyclerView>(R.id.rvCourses)
        rvCourses.layoutManager = LinearLayoutManager(this)

        // Pass edit and delete actions to the adapter
        courseAdapter = CourseAdapter(courseList,
            onEditClick = { course -> editCourse(course) },
            onDeleteClick = { course -> deleteCourse(course) }
        )
        rvCourses.adapter = courseAdapter

        // Floating Action Button to Add New Course
        val fabAddCourse = findViewById<FloatingActionButton>(R.id.fabAddCourse)
        fabAddCourse.setOnClickListener {
            val addCourseBottomSheet = AddCourseBottomSheetFragment()
            addCourseBottomSheet.show(supportFragmentManager, addCourseBottomSheet.tag)
        }

        // Load courses from Firebase
        loadCourses()
    }

    private fun loadCourses() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("courses")
            .addSnapshotListener{ snapshots, error ->
                if (error != null){
                    return@addSnapshotListener
                }
                if (snapshots !=null && !snapshots.isEmpty){
                    val courses = snapshots.documents.map { document ->
                        document.toObject(Course::class.java)!!.apply {
                            documentId = document.id
                        }
                    }
                    courseAdapter.updateCourses(courses)
                }
            }

        /*db.collection("users").document(userId).collection("courses")
            .get()
            .addOnSuccessListener { result ->
                val courses = result.map { document ->
                    document.toObject(Course::class.java).apply {
                        documentId = document.id
                    }
                }
                // Update the RecyclerView with the list of courses
                courseAdapter.updateCourses(courses)
            }
            .addOnFailureListener { e ->
                // Handle errors here
            }*/
    }

    private fun editCourse(course: Course) {
        val editCourseBottomSheet = EditCourseBottomSheetFragment.newInstance(course)
        editCourseBottomSheet.show(supportFragmentManager, editCourseBottomSheet.tag)
    }

    private fun deleteCourse(course: Course) {
        val userId = auth.currentUser?.uid ?: return

        // Confirm deletion with an alert dialog
        AlertDialog.Builder(this)
            .setTitle("Delete Course")
            .setMessage("Are you sure you want to delete ${course.courseName}?")
            .setPositiveButton("Delete") { dialog, _ ->
                // Delete the course from Firestore
                db.collection("users").document(userId).collection("courses")
                    .document(course.documentId)
                    .delete()
                    .addOnSuccessListener {
                        loadCourses() // Reload courses after deletion
                    }
                    .addOnFailureListener { e ->
                        // Handle deletion error
                    }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
