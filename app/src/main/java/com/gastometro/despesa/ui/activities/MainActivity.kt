package com.gastometro.despesa.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.ui.setupWithNavController
import com.gastometro.despesa.R
import com.gastometro.despesa.databinding.ActivityMainBinding
import com.gastometro.despesa.notification.MyForegroundService


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews(binding)

        val serviceIntent = Intent(this, MyForegroundService::class.java)
        startService(serviceIntent)
        
    }

    private fun initViews(binding: ActivityMainBinding) {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.apply {
            setupWithNavController(navController)
            setOnNavigationItemReselectedListener { }
        }
    }


    fun hideBottomNavigation() {
        binding.bottomAppBar.visibility = View.GONE
        binding.bottomNavigation.visibility = View.GONE
        binding.fab.visibility = View.GONE
    }

    fun showBottomNavigation() {
        binding.bottomAppBar.visibility = View.VISIBLE
        binding.bottomNavigation.visibility = View.VISIBLE
        binding.fab.visibility = View.VISIBLE
    }
}