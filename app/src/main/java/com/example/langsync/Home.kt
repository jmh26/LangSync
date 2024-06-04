package com.example.langsync

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class Home : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_admin)

        val navView = findViewById<BottomNavigationView>(R.id.navigation_bar)
        val navController = findNavController(R.id.nav_host_fragment_activity_home_admin)
        navView.setupWithNavController(navController)
    }
}
