package com.example.mapfilterapplication

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.mapfilterapplication.R

class MapsFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var restaurantDetailsCard: LinearLayout
    private lateinit var restaurantImage: ImageView
    private lateinit var restaurantName: TextView
    private lateinit var restaurantAddress: TextView

    data class Restaurant(val name: String, val address: String, val latLng: LatLng, val imageRes: Int)

    private val restaurants = listOf(
        Restaurant(
            "Biryani Junction – Faisalabad",
            "Satiana Road, Faisalabad",
            LatLng(31.2675, 72.3198),
            R.drawable.afghanburrito
        ),
        Restaurant(
            "Spice Garden – Madina Town",
            "Susan Road, Madina Town",
            LatLng(31.2702, 72.3429),
            R.drawable.banner_image
        ),
        Restaurant(
            "Karahi Palace – D-Ground",
            "D-Ground, Faisalabad",
            LatLng(31.2548, 72.3302),
            R.drawable.banner_image
        ),
        Restaurant(
            "Tandoori Nights – People's Colony",
            "Peoples Colony No. 1, Faisalabad",
            LatLng(31.2605, 72.3627),
            R.drawable.banner_image
        ),
        Restaurant(
            "The Burger House – Kohinoor",
            "Kohinoor City, Faisalabad",
            LatLng(31.2451, 72.3399),
            R.drawable.img
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        restaurantDetailsCard = view.findViewById(R.id.restaurantDetailsCard)
        restaurantImage = view.findViewById(R.id.restaurantImage)
        restaurantName = view.findViewById(R.id.restaurantName)
        restaurantAddress = view.findViewById(R.id.restaurantAddress)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        enableMyLocation()
        addRestaurantMarkers()
        mMap.setOnMarkerClickListener { marker ->
            val restaurant = marker.tag as? Restaurant
            restaurant?.let {
                restaurantDetailsCard.visibility = View.VISIBLE
                restaurantImage.setImageResource(it.imageRes)
                restaurantName.text = it.name
                restaurantAddress.text = it.address
            }
            true
        }
    }

    private fun addRestaurantMarkers() {
        for (restaurant in restaurants) {
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(restaurant.latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title(restaurant.name)
            )
            marker?.tag = restaurant
            android.util.Log.d("MapsFragment", "Added marker for ${restaurant.name} at ${restaurant.latLng}")
        }
        android.util.Log.d("MapsFragment", "Added ${restaurants.size} markers total")
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                    android.util.Log.d("MapsFragment", "Moved camera to user location: $userLatLng")
                } ?: run {
                    // Fallback to a default location if user location is not available
                    val defaultLocation = LatLng(40.7128, -74.0060) // New York
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))
                    android.util.Log.d("MapsFragment", "Moved camera to default location: $defaultLocation")
                }
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            // Fallback to a default location while waiting for permission
            val defaultLocation = LatLng(40.7128, -74.0060) // New York
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))
            android.util.Log.d("MapsFragment", "Moved camera to default location while waiting for permission: $defaultLocation")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        }
    }
}