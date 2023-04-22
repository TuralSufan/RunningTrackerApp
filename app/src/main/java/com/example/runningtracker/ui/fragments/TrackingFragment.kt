package com.example.runningtracker.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.runningtracker.R
import com.example.runningtracker.databinding.FragmentTrackingBinding
import com.example.runningtracker.db.Run
import com.example.runningtracker.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningtracker.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningtracker.other.Constants.ACTION_STOP_SERVICE
import com.example.runningtracker.other.Constants.CANCEL_TRACKING_DIALOG_TAG
import com.example.runningtracker.other.Constants.MAP_ZOOM
import com.example.runningtracker.other.Constants.POLYLINE_WITH
import com.example.runningtracker.other.TrackingUtils
import com.example.runningtracker.services.Polyline
import com.example.runningtracker.services.TrackingService
import com.example.runningtracker.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {
    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    private var map: GoogleMap? = null

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var curTimeInMillis = 0L

    private var fab: FloatingActionButton? = null

    @set:Inject
    var weight = 80f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        if (savedInstanceState != null){
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG_TAG) as CancelTrackingDialog?
            cancelTrackingDialog?.setYesListener {
                stopRun()
            }
        }

        binding.mapView.getMapAsync {
            map = it
            addAllPolylines()
        }
        binding.btnFinishRun.setOnClickListener {
            zoomToSeeFullTrack()
            endRunandSaveToDB()
        }

        subscribeToObservers()
        fab = binding.fab

        binding.fab.setOnClickListener {
            showCancelTrackingDialog()
        }
    }


    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyLine()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis = it
            val formattedTime = TrackingUtils.getFormattedStopWatchTime(curTimeInMillis, true)
            binding.tvTimer.text = formattedTime
        })
    }


    private fun toggleRun() {
        if (isTracking) {
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }

    }

    private fun showCancelTrackingDialog() {

        CancelTrackingDialog().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
    }

    private fun stopRun() {
        binding.tvTimer.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        fab?.visibility = View.INVISIBLE
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && curTimeInMillis > 0L) {
            binding.btnToggleRun.text = "Start"
            binding.btnFinishRun.visibility = View.VISIBLE
            fab?.visibility = View.INVISIBLE
        } else if (isTracking){
            binding.btnToggleRun.text = "Stop"
            binding.btnFinishRun.visibility = View.GONE
            fab?.visibility = View.VISIBLE
        }
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(), MAP_ZOOM
                )
            )
        }
    }

    private fun zoomToSeeFullTrack() {
        val bounds = LatLngBounds.builder()
        for (polyline in pathPoints) {
            for (pos in polyline) {
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunandSaveToDB() {
        map?.snapshot { bmp ->
            var distanceInMeters = 0
            for (polyline in pathPoints) {
                distanceInMeters += TrackingUtils.calculatePolylineLength(polyline).toInt()
            }
            val avgSpeed =
                round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) / 10) * 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000F) * weight).toInt()

            val run =
                Run(bmp, dateTimeStamp, avgSpeed, distanceInMeters, curTimeInMillis, caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polylineOptions =
                PolylineOptions().color(R.color.colorTracking).width(POLYLINE_WITH).addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyLine() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val latestLatLng = pathPoints.last().last()
            val polyLineOption = PolylineOptions().color(R.color.colorTracking).width(POLYLINE_WITH)
                .add(preLastLatLng).add(latestLatLng)
            map?.addPolyline(polyLineOption)
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    override fun onStart() {
        super.onStart()
        binding.mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView?.onStop()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView?.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView?.onLowMemory()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView?.onPause()
    }


}








