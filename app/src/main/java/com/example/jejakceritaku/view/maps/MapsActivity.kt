package com.example.jejakceritaku.view.maps

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.example.jejakceritaku.R
import com.example.jejakceritaku.data.response.ListStoryItem
import com.example.jejakceritaku.databinding.ActivityMapsBinding
import com.example.jejakceritaku.view.ViewModelFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val viewModel: MapsViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private lateinit var googleMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.title = "JejakCeritaku Map"

        initializeMap()
        observeViewModelData()
    }

    private fun initializeMap() {
        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        configureMapSettings()
        checkLocationPermission()
    }

    private fun configureMapSettings() {
        googleMap.uiSettings.run {
            isZoomControlsEnabled = true
            isIndoorLevelPickerEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }

        googleMap.setOnMapLongClickListener { latLng ->
            addCustomMarker(latLng, "New Marker", "Lat: ${latLng.latitude} Long: ${latLng.longitude}", R.drawable.ic_andro, Color.parseColor("#3DDC84"))
        }

        googleMap.setOnPoiClickListener { poi ->
            googleMap.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
            )?.showInfoWindow()
        }
    }

    private fun addCustomMarker(latLng: LatLng, title: String, snippet: String, @DrawableRes iconRes: Int, @ColorInt color: Int) {
        googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
                .snippet(snippet)
                .icon(convertVectorToBitmap(iconRes, color))
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_maps, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        googleMap.mapType = when (item.itemId) {
            R.id.normal_type-> GoogleMap.MAP_TYPE_NORMAL
            R.id.satellite_type -> GoogleMap.MAP_TYPE_SATELLITE
            R.id.terrain_type -> GoogleMap.MAP_TYPE_TERRAIN
            R.id.hybrid_type -> GoogleMap.MAP_TYPE_HYBRID
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun convertVectorToBitmap(@DrawableRes id: Int, @ColorInt color: Int): BitmapDescriptor {
        val vectorDrawable = ResourcesCompat.getDrawable(resources, id, null) ?: run {
            Log.e("BitmapHelper", "Resource not found")
            return BitmapDescriptorFactory.defaultMarker()
        }
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        DrawableCompat.setTint(vectorDrawable, color)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) enableUserLocation()
        }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation()
        } else {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
        }
    }

    private fun observeViewModelData() {
        viewModel.getSession().observe(this) { session ->
            viewModel.getMapsStory(session.token) { result ->
                result.onSuccess { storyList ->
                    viewModel.setStoryMaps(storyList)
                    displayMarkers(storyList)
                }.onFailure {
                    Log.e("MapsActivity", "Error: ${it.message}")
                }
            }
        }

        viewModel.storyMaps.observe(this) { storyList ->
            displayMarkers(storyList)
        }
    }

    private fun displayMarkers(storyList: List<ListStoryItem>) {
        storyList.forEach { story ->
            val latLng = LatLng(story.lat!!, story.lon!!)
            googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(story.name)
                    .snippet(story.description)
            )
        }
    }
}

