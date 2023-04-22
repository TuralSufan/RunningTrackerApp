package com.example.runningtracker.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.runningtracker.R
import com.example.runningtracker.databinding.FragmentSettingsBinding
import com.example.runningtracker.databinding.FragmentSetupBinding
import com.example.runningtracker.other.Constants.KEY_NAME
import com.example.runningtracker.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadFieldsFromSharedPref()

        binding.btnApply.setOnClickListener {
            val success = applyChangesToSharedPref()
            if (success){
                Snackbar.make(view,"Saved changes", Snackbar.LENGTH_LONG).show()
                loadFieldsFromSharedPref()
            } else {
                Snackbar.make(view, "Please fill out all the fields", Snackbar.LENGTH_LONG).show()
            }
        }
    }


    private fun loadFieldsFromSharedPref() {
        val name = sharedPref.getString(KEY_NAME, "")
        val weight = sharedPref.getFloat(KEY_WEIGHT, 80f)
        binding.etName.setText(name)
        binding.etWeight.setText(weight.toString())
    }

    private fun applyChangesToSharedPref(): Boolean {
        val nameText = binding.etName.text.toString()
        val weightText = binding.etWeight.text.toString()
        if (nameText.isEmpty() || weightText.isEmpty()){
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME, nameText)
            .putFloat(KEY_WEIGHT, weightText.toFloat())
            .apply()
        return true
    }
}