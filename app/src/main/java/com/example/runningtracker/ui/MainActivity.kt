package com.example.runningtracker.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.runningtracker.R
import com.example.runningtracker.databinding.ActivityMainBinding
import com.example.runningtracker.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)

        navigateToTrackingFragmentIfNeeded(intent)

        val navController = Navigation.findNavController(this, R.id.fragmentContainerView)
        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.setOnItemReselectedListener { /*NO-OP*/ }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment -> binding.bottomNavigationView.visibility =
                    View.VISIBLE

                else -> binding.bottomNavigationView.visibility = View.GONE
            }

        }


    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            findNavController(R.id.fragmentContainerView).navigate(R.id.action_global_TrackingFragment)

        }

    }


}