package com.atsuite.studysyncapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Dashboard1Activity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard1)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
                R.id.menu_courses -> {
                    // Navigate to courses activity
                    val intent = Intent(this, CoursesActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_schedules -> {
                    // Navigate to assignments activity
                    val intent = Intent(this, ScheduleOverviewActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_assignments -> {
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
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.menu_profile -> {
               /* val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)*/
                true
            }
            R.id.menu_settings -> {
                /*val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)*/
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }
}