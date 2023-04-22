package com.example.runningtracker.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CancelTrackingDialog:DialogFragment() {

    private var yesListener: (() -> Unit)? = null

    fun setYesListener(listener: () -> Unit){
        yesListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(
            requireContext(), R.style.AlertDialog_AppCompat
        ).setTitle("Cancel the Run?")
            .setMessage("Are you sure to cancel the current run and delete all its data?")
            .setIcon(com.example.runningtracker.R.drawable.ic_delete_24).setPositiveButton("Yes") { _, _ ->
                yesListener?.let {yes ->
                    yes()
                }
            }.setNegativeButton("No", null).create()
    }
}