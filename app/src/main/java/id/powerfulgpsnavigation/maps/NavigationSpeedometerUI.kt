package id.powerfulgpsnavigation.maps

import android.annotation.SuppressLint
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment


class NavigationSpeedometerUI : Fragment(), LocationListener {

    private lateinit var locationManager: LocationManager
    private lateinit var speedometer: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.navigation_speedometer, container, false)
        speedometer = view.findViewById(R.id.navigation_location_speed)
        enableLocation()

        return view
    }

    fun enableLocation(){
        locationManager = requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager
        @Suppress("DEPRECATED_IDENTITY_EQUALS")
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) !==
            PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
    }

    override fun onLocationChanged(location: Location) {
        val navSpeed = (location.speed * 3.6).toInt()
        speedometer.text = navSpeed.toString()
    }
}