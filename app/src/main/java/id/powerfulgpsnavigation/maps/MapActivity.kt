package id.powerfulgpsnavigation.maps

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mapbox.api.directions.v5.DirectionsCriteria.AnnotationCriteria
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.engine.LocationEngineRequest
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import com.mapbox.services.android.navigation.ui.v5.route.NavigationRoute
import com.mapbox.services.android.navigation.v5.models.DirectionsCriteria
import com.mapbox.services.android.navigation.v5.models.DirectionsRoute
import com.mapbox.services.android.navigation.v5.navigation.NavigationMapRoute
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import kotlin.properties.Delegates


class MapActivity : AppCompatActivity(), LocationListener {
    private lateinit var icon: Icon
    private var originLocation: Location? = null
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2

    private lateinit var drawerLayout: DrawerLayout

    // Declare a variable for MapView
    private lateinit var mapView: MapView
    private lateinit var maplibreMap: MapboxMap
    private var lastLocation: Location? = null
    private var locationComponent: LocationComponent? = null

    // Declare a UI Kit
    private lateinit var search: EditText
    private lateinit var menuDashboard: Button
    private lateinit var searchButton: Button
    private lateinit var backToMapBtn: Button
    private lateinit var backToSearchListBtn: Button
    private lateinit var locateButton: Button

    //Declare a Location Address
    private lateinit var geocoder: Geocoder
    private var address: String? = null
    private var locationAddress: Address? = null

    // Declare a Location Address List
    private lateinit var locationrecyclerview: ListView
    private lateinit var locAddressListView: CardView
    private lateinit var emptyLocAddress: TextView

    // Declare a Location Address in ListView
    private lateinit var adapter: MyArrayAdapter
    private var maxAddressLineIndex: Int = 0
    private lateinit var rowAddress: View
    private lateinit var addressLine: TextView

    // Declare a Address Coordinates
    private var latitude by Delegates.notNull<Double>()
    private var longitude by Delegates.notNull<Double>()

    // Declare a Location Info
    private lateinit var locationInfo: CoordinatorLayout
    private lateinit var locAddress: TextView
    private lateinit var locCoords: TextView
    private lateinit var placeInfoOnMap: TextView
    private lateinit var getDirectionsButton: Button
    private lateinit var markerOptions: MarkerOptions
    private lateinit var marker: Marker
    private lateinit var streetName: String

    // Get Directions
    private lateinit var origin: Point
    private lateinit var destination: Point
    private lateinit var navigationRouteBuilder: NavigationRoute.Builder
    private var navigationMapRoute: NavigationMapRoute? = null
    private lateinit var route: DirectionsRoute
    private lateinit var route_1: DirectionsRoute

    // route responses
    private lateinit var maplibreResponse: com.mapbox.services.android.navigation.v5.models.DirectionsResponse

    // Declare a Route Info
    private lateinit var routeInfo: CoordinatorLayout
    private lateinit var routeOpt1: LinearLayout
    private lateinit var routeDuration_0: TextView
    private lateinit var routeDistance_0: TextView
    private lateinit var routeOpt2: LinearLayout
    private lateinit var routeDuration_1: TextView
    private lateinit var routeDistance_1: TextView
    private lateinit var startNavigationBtn1: Button
    private lateinit var startNavigationBtn2: Button
    private lateinit var demoNavigationBtn1 : Button
    private lateinit var demoNavigationBtn2 : Button
    private lateinit var cancelRouteBtn: Button

    // navigation route distance format
    private lateinit var kilometers: String
    private lateinit var meters: String
    private lateinit var kilometers_1: String
    private lateinit var meters_1: String

    // Vehicle type and route choice
    private lateinit var vehicleRoute: LinearLayout
    private lateinit var car: Button
    private lateinit var pedestrian: Button
    private var vehicleType = DirectionsCriteria.PROFILE_DRIVING_TRAFFIC

    @SuppressLint("MissingPermission", "CutPasteId", "UseCompatLoadingForDrawables", "SetTextI18n",
        "InflateParams"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this,onBackPressedCallbackToExitApp)

        val key = "C975JZ2tW8Fxe2xPdPzz"

        // Find other maps in https://cloud.maptiler.com/maps/
        val mapId = "streets-v2"

        val styleUrl = "https://api.maptiler.com/maps/$mapId/style.json?key=$key"

        // Init MapLibre
        Mapbox.getInstance(this)

        // Init layout view
        val inflater = LayoutInflater.from(this)
        val rootView = inflater.inflate(R.layout.activity_map, null)
        setContentView(rootView)

        // Init the MapView
        mapView = rootView.findViewById(R.id.mapView)

        // Init the UI Kit
        menuDashboard = findViewById(R.id.dashboard_menu_map)
        drawerLayout = findViewById(R.id.menu_drawer_layout)
        backToMapBtn = findViewById(R.id.back_to_map_btn)
        backToSearchListBtn = findViewById(R.id.back_to_searchlist_btn)
        search = findViewById(R.id.searchLocation)
        searchButton = findViewById(R.id.searchLocationButton)
        locateButton = findViewById(R.id.locateButton)

        // Init the Location Address
        locAddressListView = findViewById(R.id.locAddressListInRecyclerView)

        // getting the recyclerview by its id
        locationrecyclerview = findViewById(R.id.locationAddressList)

        emptyLocAddress = findViewById(R.id.emptyLocationAddress)

        geocoder = Geocoder(this)

        //for API Level 9 or higher
        if (!Geocoder.isPresent()) {
            Toast.makeText(
                this@MapActivity,
                "Sorry! Geocoder service not Present.",
                Toast.LENGTH_LONG
            ).show()
        }

        // Get Directions to start Navigation Route
        locationInfo = findViewById(R.id.locationInfo)
        locAddress = findViewById(R.id.locationAddress)
        locCoords = findViewById(R.id.addressCoordinates)
        getDirectionsButton = findViewById(R.id.getDirections)
        placeInfoOnMap = findViewById(R.id.PlaceDetailsOnMap)

        // Get Route Info
        routeInfo = findViewById(R.id.RouteInfo)

        // Get Alternatives Routes 1
        routeOpt1 = findViewById(R.id.routeOptions1)
        routeDistance_0 = findViewById(R.id.routeDistance)
        routeDuration_0 = findViewById(R.id.routeDuration)

        // Get Alternatives Routes 2
        routeOpt2 = findViewById(R.id.routeOptions2)
        routeDistance_1 = findViewById(R.id.routeDistance_1)
        routeDuration_1 = findViewById(R.id.routeDuration_1)

        // Start Navigation Route or Cancel Route
        startNavigationBtn1 = findViewById(R.id.startNavigationButton1)
        startNavigationBtn2 = findViewById(R.id.startNavigationButton2)
        demoNavigationBtn1 = findViewById(R.id.btn_navigation_demo)
        demoNavigationBtn2 = findViewById(R.id.btn_navigation_demo_1)
        cancelRouteBtn = findViewById(R.id.cancelRouteButton)

        // Select Vehicle Type
        vehicleRoute = findViewById(R.id.vehicleRouteType)
        car = findViewById(R.id.drivingRoute)
        pedestrian = findViewById(R.id.walkingRoute)

        mapView.getMapAsync { map ->
            maplibreMap = map
            navigationMapRoute = NavigationMapRoute(mapView, map)

            maplibreMap.setStyle(styleUrl) { style: Style ->
                locationComponent = maplibreMap.locationComponent
                val locationComponentOptions =
                    LocationComponentOptions.builder(this@MapActivity)
                        .pulseEnabled(true)
                        .build()
                val locationComponentActivationOptions =
                    buildLocationComponentActivationOptions(style, locationComponentOptions)
                locationComponent!!.activateLocationComponent(locationComponentActivationOptions)
                locationComponent!!.isLocationComponentEnabled = true
                locationComponent!!.cameraMode = CameraMode.TRACKING
                locationComponent!!.forceLocationUpdate(lastLocation)

                menuDashboard.setOnClickListener {
                    onBackPressedDispatcher.addCallback(this,onBackPressedCallbackToCloseDrawer)

                    drawerLayout.openDrawer(GravityCompat.START, true)
                }

                locateButton.setOnClickListener {
                    locationComponent!!.cameraMode = CameraMode.TRACKING
                    onBackPressedDispatcher.addCallback(this,onBackPressedCallbackToExitApp)
                }

                search.setOnFocusChangeListener { _: View, _: Boolean ->
                    mapView.visibility = View.INVISIBLE
                    locateButton.visibility = View.INVISIBLE
                    menuDashboard.visibility = View.INVISIBLE
                    backToMapBtn.visibility = View.VISIBLE
                    locAddressListView.visibility = View.VISIBLE

                    onBackPressedDispatcher.addCallback(this,onBackPressedCallback)
                }

                search.setOnClickListener {
                    mapView.visibility = View.INVISIBLE
                    locateButton.visibility = View.INVISIBLE
                    menuDashboard.visibility = View.INVISIBLE
                    backToMapBtn.visibility = View.VISIBLE
                    locAddressListView.visibility = View.VISIBLE

                    onBackPressedDispatcher.addCallback(this,onBackPressedCallback)
                }

                backToMapBtn.setOnClickListener {
                    mapView.visibility = View.VISIBLE
                    locateButton.visibility = View.VISIBLE
                    menuDashboard.visibility = View.VISIBLE
                    backToMapBtn.visibility = View.INVISIBLE
                    searchButton.visibility = View.VISIBLE
                    placeInfoOnMap.visibility = View.INVISIBLE
                    locAddressListView.visibility = View.INVISIBLE
                    locationrecyclerview.visibility = View.INVISIBLE
                    emptyLocAddress.visibility = View.VISIBLE
                    search.setText("")
                    search.visibility = View.VISIBLE

                    // on below line getting current view.
                    val view: View? = this.currentFocus

                    // on below line checking if view is not null.
                    if (view != null) {
                        // on below line we are creating a variable
                        // for input manager and initializing it.
                        val inputMethodManager =
                            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

                        // on below line hiding our keyboard.
                        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
                    }

                    locationComponent!!.cameraMode = CameraMode.TRACKING

                    onBackPressedDispatcher.addCallback(this,onBackPressedCallbackToExitApp)
                }

                searchButton.setOnClickListener(searchButtonOnClickListener)

                locationrecyclerview.setOnItemClickListener { _, _, position, _ ->
                    val locationAddressSelected = adapter.getItem(position)
                    val lat = locationAddressSelected!!.latitude
                    val lng = locationAddressSelected.longitude
                    locationAddress = geocoder.getFromLocation(lat, lng, 1)!![0]
                    streetName = locationAddress!!.getAddressLine(0)

                    latitude = locationAddress!!.latitude
                    longitude = locationAddress!!.longitude
                    locAddress.text = streetName
                    locCoords.text = "$latitude, $longitude"


                    val bounds = mutableListOf<LatLng>()

                    // Get bitmaps for marker icon
                    val infoIconDrawable = ResourcesCompat.getDrawable(
                        this.resources,
                        // Intentionally specify package name
                        // This makes copy from another project easier
                        com.mapbox.mapboxsdk.R.drawable.maplibre_marker_icon_default,
                        null
                    )!!

                    val bitmapMarker = infoIconDrawable.toBitmap()

                    icon = IconFactory.getInstance(this)
                        .fromBitmap(bitmapMarker)

                    val addressLatLng = LatLng(latitude, longitude)
                    bounds.add(addressLatLng)

                    // Use MarkerOptions and addMarker() to add a new marker in map
                    markerOptions = MarkerOptions()
                        .position(addressLatLng)
                        .icon(icon)
                    marker = maplibreMap.addMarker(markerOptions)

                    // Move camera to newly added annotations
                    maplibreMap.getCameraForLatLngBounds(LatLngBounds.fromLatLngs(bounds))?.let {
                        val newCameraPosition = CameraPosition.Builder()
                            .target(it.target)
                            .zoom(14.0)
                            .build()
                        maplibreMap.cameraPosition = newCameraPosition
                    }

                    // on below line getting current view.
                    val view: View? = this.currentFocus

                    // on below line checking if view is not null.
                    if (view != null) {
                        // on below line we are creating a variable
                        // for input manager and initializing it.
                        val inputMethodManager =
                            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

                        // on below line hiding our keyboard.
                        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
                    }

                    locateButton.visibility = View.INVISIBLE
                    search.visibility = View.INVISIBLE
                    locationInfo.visibility = View.VISIBLE
                    searchButton.visibility = View.INVISIBLE
                    placeInfoOnMap.visibility = View.VISIBLE
                    locAddressListView.visibility = View.INVISIBLE
                    mapView.visibility = View.VISIBLE
                    backToSearchListBtn.visibility = View.VISIBLE
                    backToMapBtn.visibility = View.INVISIBLE

                    // adding onbackpressed callback listener.
                    onBackPressedDispatcher.addCallback(this,onBackPressedCallbackToSearchList)
                }

                backToSearchListBtn.setOnClickListener {
                    mapView.visibility = View.INVISIBLE
                    locateButton.visibility = View.INVISIBLE
                    menuDashboard.visibility = View.INVISIBLE
                    backToMapBtn.visibility = View.VISIBLE
                    locAddressListView.visibility = View.VISIBLE
                    backToSearchListBtn.visibility = View.INVISIBLE
                    locationInfo.visibility = View.INVISIBLE
                    search.visibility = View.VISIBLE
                    searchButton.visibility = View.VISIBLE
                    placeInfoOnMap.visibility = View.INVISIBLE

                    map.removeMarker(marker)

                    onBackPressedDispatcher.addCallback(this,onBackPressedCallback)
                }

                getDirectionsButton.setOnClickListener {
                    originLocation = maplibreMap.locationComponent.lastKnownLocation

                    origin = Point.fromLngLat(originLocation!!.longitude, originLocation!!.latitude)
                    destination = Point.fromLngLat(longitude, latitude)

                    navigationRouteBuilder = NavigationRoute.builder(this).apply {
                        this.accessToken(getString(R.string.mapbox_access_token))
                        this.origin(origin)
                        this.destination(destination)
                        this.voiceUnits(DirectionsCriteria.METRIC)
                        this.alternatives(true)
                        this.profile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
                        this.baseUrl(getString(R.string.base_url))
                    }

                    navigationRouteBuilder.build().getRoute(directionsResponse)

                    val originPosition = LatLng(originLocation!!.latitude, originLocation!!.longitude)
                    val destPosition = LatLng(latitude, longitude)

                    val latLngBounds: LatLngBounds = LatLngBounds.Builder()
                        .include(originPosition)
                        .include(destPosition)
                        .build()

                    map.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 250), 500)

                    routeOpt1.background = ContextCompat.getDrawable(this@MapActivity, com.mapbox.services.android.navigation.ui.v5.R.color.md_grey_700)
                    routeOpt2.background = ContextCompat.getDrawable(this@MapActivity, com.mapbox.services.android.navigation.ui.v5.R.color.md_grey_700)
                    routeOpt2.visibility = View.VISIBLE

                    routeInfo.visibility = View.VISIBLE
                    locationInfo.visibility = View.INVISIBLE
                    cancelRouteBtn.visibility = View.VISIBLE
                    placeInfoOnMap.visibility = View.INVISIBLE

                    car.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.white))

                    onBackPressedDispatcher.addCallback(this, onBackPressedCallbackToCancelRoute)
                    backToSearchListBtn.visibility = View.INVISIBLE
                }

                car.setOnClickListener {
                    vehicleType = DirectionsCriteria.PROFILE_DRIVING_TRAFFIC
                    car.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.white))
                    pedestrian.setBackgroundTintList(ContextCompat.getColorStateList(this,
                        com.mapbox.services.android.navigation.ui.v5.R.color.md_grey_700))

                    navigationRouteBuilder = NavigationRoute.builder(this).apply {
                        this.accessToken(getString(R.string.mapbox_access_token))
                        this.origin(origin)
                        this.destination(destination)
                        this.voiceUnits(DirectionsCriteria.METRIC)
                        this.alternatives(true)
                        this.profile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
                        this.baseUrl(getString(R.string.base_url))
                    }

                    navigationRouteBuilder.build().getRoute(directionsResponse)
                    navigationMapRoute?.addRoute(route)
                }

                pedestrian.setOnClickListener {
                    vehicleType = DirectionsCriteria.PROFILE_WALKING
                    pedestrian.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.white))
                    car.setBackgroundTintList(ContextCompat.getColorStateList(this,
                        com.mapbox.services.android.navigation.ui.v5.R.color.md_grey_700))

                    navigationRouteBuilder = NavigationRoute.builder(this).apply {
                        this.accessToken(getString(R.string.mapbox_access_token))
                        this.origin(origin)
                        this.destination(destination)
                        this.voiceUnits(DirectionsCriteria.METRIC)
                        this.alternatives(true)
                        this.profile(DirectionsCriteria.PROFILE_WALKING)
                        this.baseUrl(getString(R.string.base_url))
                    }

                    navigationRouteBuilder.build().getRoute(directionsResponse)
                    navigationMapRoute?.addRoute(route)
                }

                cancelRouteBtn.setOnClickListener {
                    routeInfo.visibility = View.INVISIBLE
                    cancelRouteBtn.visibility = View.INVISIBLE
                    vehicleRoute.visibility = View.INVISIBLE
                    locationrecyclerview.visibility = View.INVISIBLE
                    locateButton.visibility = View.VISIBLE
                    menuDashboard.visibility = View.VISIBLE
                    emptyLocAddress.visibility = View.VISIBLE
                    search.setText("")
                    search.visibility = View.VISIBLE
                    searchButton.visibility = View.VISIBLE

                    startNavigationBtn1.visibility = View.INVISIBLE
                    demoNavigationBtn1.visibility = View.INVISIBLE
                    startNavigationBtn2.visibility = View.INVISIBLE
                    demoNavigationBtn2.visibility = View.INVISIBLE

                    map.removeMarker(marker)
                    navigationMapRoute?.removeRoute()
                    routeDistance_0.text = "Calculating route..."
                    routeDuration_0.text = ""
                    routeDistance_1.text = "Calculating alternative route..."
                    routeDuration_1.text = ""

                    car.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.white))
                    pedestrian.setBackgroundTintList(ContextCompat.getColorStateList(this,
                        com.mapbox.services.android.navigation.ui.v5.R.color.md_grey_700))

                    locationComponent!!.cameraMode = CameraMode.TRACKING

                    onBackPressedDispatcher.addCallback(this,onBackPressedCallbackToExitApp)
                }
            }
            maplibreMap.cameraPosition = CameraPosition.Builder().target(LatLng(-6.200000,106.816666)).zoom(9.0).build()
        }

        checkPermissions()
    }

    private val onBackPressedCallbackToCloseDrawer = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            drawerLayout.closeDrawers()

            onBackPressedDispatcher.addCallback(this@MapActivity,onBackPressedCallbackToExitApp)
        }
    }

    private val onBackPressedCallbackToExitApp = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            //showing dialog and then closing the application..
            exitApp()
        }
    }

    private fun exitApp() {
        MaterialAlertDialogBuilder(this).apply {
            setTitle("Exit Powerful GPS Navigation")
            setMessage("are you sure?")
            setPositiveButton("Yes") { _, _ -> finish() }
            setNegativeButton("No", null)
            show()
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            //showing dialog and then closing the application..
            backToMap()
        }
    }

    private val onBackPressedCallbackToSearchList = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            //showing dialog and then closing the application..
            backToSearchList()
        }
    }

    private fun backToMap() {
        mapView.visibility = View.VISIBLE
        locateButton.visibility = View.VISIBLE
        menuDashboard.visibility = View.VISIBLE
        backToMapBtn.visibility = View.INVISIBLE
        locAddressListView.visibility = View.INVISIBLE
        locationrecyclerview.visibility = View.INVISIBLE
        emptyLocAddress.visibility = View.VISIBLE
        search.setText("")
        search.visibility = View.VISIBLE

        // on below line getting current view.
        val view: View? = this.currentFocus

        // on below line checking if view is not null.
        if (view != null) {
            // on below line we are creating a variable
            // for input manager and initializing it.
            val inputMethodManager =
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

            // on below line hiding our keyboard.
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
        locationComponent!!.cameraMode = CameraMode.TRACKING

        onBackPressedDispatcher.addCallback(this,onBackPressedCallbackToExitApp)
    }

    private fun backToSearchList() {
        mapView.visibility = View.INVISIBLE
        locateButton.visibility = View.INVISIBLE
        menuDashboard.visibility = View.INVISIBLE
        backToMapBtn.visibility = View.VISIBLE
        backToSearchListBtn.visibility = View.INVISIBLE
        locAddressListView.visibility = View.VISIBLE
        search.visibility = View.VISIBLE
        placeInfoOnMap.visibility = View.INVISIBLE
        searchButton.visibility = View.VISIBLE
        locationInfo.visibility = View.INVISIBLE

        maplibreMap.removeMarker(marker)

        onBackPressedDispatcher.addCallback(this,onBackPressedCallback)
    }

    private val onBackPressedCallbackToCancelRoute = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            //showing dialog and then closing the application..
            cancelRoute()
        }
    }

    private fun cancelRoute() {
        routeInfo.visibility = View.INVISIBLE
        cancelRouteBtn.visibility = View.INVISIBLE
        vehicleRoute.visibility = View.INVISIBLE
        locationrecyclerview.visibility = View.INVISIBLE
        locateButton.visibility = View.VISIBLE
        menuDashboard.visibility = View.VISIBLE
        emptyLocAddress.visibility = View.VISIBLE
        search.setText("")
        search.visibility = View.VISIBLE
        searchButton.visibility = View.VISIBLE

        startNavigationBtn1.visibility = View.INVISIBLE
        demoNavigationBtn1.visibility = View.INVISIBLE
        startNavigationBtn2.visibility = View.INVISIBLE
        demoNavigationBtn2.visibility = View.INVISIBLE

        maplibreMap.removeMarker(marker)
        navigationMapRoute?.removeRoute()
        routeDistance_0.text = "Calculating route..."
        routeDuration_0.text = ""
        routeDistance_1.text = "Calculating alternative route..."
        routeDuration_1.text = ""

        car.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.white))
        pedestrian.setBackgroundTintList(ContextCompat.getColorStateList(this,
            com.mapbox.services.android.navigation.ui.v5.R.color.md_grey_700))

        locationComponent!!.cameraMode = CameraMode.TRACKING

        onBackPressedDispatcher.addCallback(this,onBackPressedCallbackToExitApp)
    }

    var searchButtonOnClickListener = View.OnClickListener { // TODO Auto-generated method stub
        val searchString = search.text.toString()
        searchFromLocationName(searchString)

        locationrecyclerview.visibility = View.VISIBLE
        emptyLocAddress.visibility = View.INVISIBLE
        mapView.visibility = View.INVISIBLE
        locateButton.visibility = View.INVISIBLE
        menuDashboard.visibility = View.INVISIBLE
        backToMapBtn.visibility = View.VISIBLE
        locAddressListView.visibility = View.VISIBLE

        onBackPressedDispatcher.addCallback(this,onBackPressedCallback)
    }

    private fun searchFromLocationName(name: String) {
        try {
            val result = geocoder.getFromLocationName(name, MAX_RESULT)
            if (result == null || result.isEmpty()) {
                Toast.makeText(
                    this@MapActivity,
                    "No matches were found or there is no backend service!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                adapter = MyArrayAdapter(
                    this,
                    android.R.layout.activity_list_item, result
                )
                locationrecyclerview.setAdapter(adapter)
            }
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            Toast.makeText(
                this@MapActivity,
                "The network is unavailable or any other I/O problem occurs!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    inner class MyArrayAdapter // TODO Auto-generated constructor stub
        (
        var mycontext: Activity, textViewResourceId: Int,
        objects: List<Address?>?
    ) :
        ArrayAdapter<Address?>(mycontext, textViewResourceId, objects!!) {
        @SuppressLint("ViewHolder", "InflateParams")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val inflater = mycontext.layoutInflater
            rowAddress = inflater.inflate(R.layout.location_list_card_view_design, null, true)
            addressLine = rowAddress.findViewById<View?>(R.id.location_address) as TextView
            rowAddress.findViewById<ImageView>(R.id.icon)!!

            maxAddressLineIndex = getItem(position)!!.maxAddressLineIndex
            for (j in 0 .. maxAddressLineIndex) {
                addressLine.text = getItem(position)!!.getAddressLine(j)
            }

            return rowAddress
        }
    }

    companion object {
        const val MAX_RESULT = 10
    }

    private val directionsResponse: Callback<DirectionsResponse> = object : Callback<DirectionsResponse> {
        override fun onResponse(
            call: Call<DirectionsResponse>,
            response: Response<DirectionsResponse>,
        ) {
            response.body()?.let { response ->
                if (response.routes().isNotEmpty()) {
                    maplibreResponse =
                        com.mapbox.services.android.navigation.v5.models.DirectionsResponse.fromJson(
                            response.toJson()
                        );

                    if (maplibreResponse.routes().size == 1) {
                        this@MapActivity.route = maplibreResponse.routes().first()

                        // meters and kilometers format
                        val km = (route.distance() / 1000).toInt()
                        val m = (route.distance() / 10).toInt()

                        // route distance
                        meters = m.toString()
                        kilometers = km.toString()

                        // route time
                        val minutes = (route.duration() / 60 % 60).toInt()
                        val hours = (route.duration() / 60 / 60).toInt()
                        val days = (route.duration() / 60 / 60 / 24).toInt()

                        if (hours > 24) {
                            routeDuration_0.text =
                                days.toString() + "d" + (hours % 24).toString() + "h" + minutes.toString() + "min"
                        } else {
                            routeDuration_0.text =
                                (hours % 24).toString() + "h" + minutes.toString() + "min"
                        }

                        if (m > 10) {
                            routeDistance_0.text = kilometers + "km"
                        } else {
                            routeDistance_0.text = meters + "m"
                        }

                        routeOpt1.background = ContextCompat.getDrawable(this@MapActivity, R.color.blue)
                        routeOpt2.background = ContextCompat.getDrawable(this@MapActivity, com.mapbox.services.android.navigation.ui.v5.R.color.md_grey_700)
                        routeOpt2.visibility = View.INVISIBLE
                        startNavigationBtn1.visibility = View.VISIBLE
                        demoNavigationBtn1.visibility = View.VISIBLE
                        startNavigationBtn2.visibility = View.INVISIBLE
                        demoNavigationBtn2.visibility = View.INVISIBLE

                        routeOpt1.setOnClickListener(clickMainRouteOptions1)
                        startNavigationBtn1.setOnClickListener(clickMainRouteToStartNav)
                        demoNavigationBtn1.setOnClickListener(clickMainRouteToDemoNav)

                        vehicleRoute.visibility = View.VISIBLE

                        navigationMapRoute?.addRoute(route)
                    } else {
                        this@MapActivity.route = maplibreResponse.routes().first()
                        this@MapActivity.route_1 = maplibreResponse.routes().get(1)

                        // meters and kilometers format
                        val km = (route.distance() / 1000).toInt()
                        val m = (route.distance() / 10).toInt()

                        // route distance
                        meters = m.toString()
                        kilometers = km.toString()

                        // route time
                        val minutes = (route.duration() / 60 % 60).toInt()
                        val hours = (route.duration() / 60 / 60).toInt()
                        val days = (route.duration() / 60 / 60 / 24).toInt()

                        if (hours > 24) {
                            routeDuration_0.text =
                                days.toString() + "d" + (hours % 24).toString() + "h" + minutes.toString() + "min"
                        } else {
                            routeDuration_0.text =
                                (hours % 24).toString() + "h" + minutes.toString() + "min"
                        }

                        if (m > 10) {
                            routeDistance_0.text = kilometers + "km"
                        } else {
                            routeDistance_0.text = meters + "m"
                        }

                        // meters and kilometers format
                        val km_1 = (route_1.distance() / 1000).toInt()
                        val m_1 = (route_1.distance() / 10).toInt()

                        // route distance
                        meters_1 = m_1.toString()
                        kilometers_1 = km_1.toString()

                        // route time
                        val minutes_1 = (route_1.duration() / 60 % 60).toInt()
                        val hours_1 = (route_1.duration() / 60 / 60).toInt()
                        val days_1 = (route_1.duration() / 60 / 60 / 24).toInt()

                        if (hours_1 > 24) {
                            routeDuration_1.text =
                                days_1.toString() + "d" + (hours_1 % 24).toString() + "h" + minutes_1.toString() + "min"
                        } else {
                            routeDuration_1.text =
                                (hours_1 % 24).toString() + "h" + minutes_1.toString() + "min"
                        }

                        if (m_1 > 10) {
                            routeDistance_1.text = kilometers_1 + "km"
                        } else {
                            routeDistance_1.text = meters_1 + "m"
                        }

                        routeOpt1.background = ContextCompat.getDrawable(this@MapActivity, R.color.blue)
                        routeOpt2.background = ContextCompat.getDrawable(this@MapActivity, com.mapbox.services.android.navigation.ui.v5.R.color.md_grey_700)
                        routeOpt2.visibility = View.VISIBLE
                        startNavigationBtn1.visibility = View.VISIBLE
                        demoNavigationBtn1.visibility = View.VISIBLE
                        startNavigationBtn2.visibility = View.INVISIBLE
                        demoNavigationBtn2.visibility = View.INVISIBLE

                        routeOpt1.setOnClickListener(clickMainRouteOptions1)
                        startNavigationBtn1.setOnClickListener(clickMainRouteToStartNav)
                        demoNavigationBtn1.setOnClickListener(clickMainRouteToDemoNav)

                        routeOpt2.setOnClickListener(clickAlternativeRouteOptions2)
                        startNavigationBtn2.setOnClickListener(clickAlternativeRouteToStartNav)
                        demoNavigationBtn2.setOnClickListener(clickAlternativeRouteToDemoNav)

                        vehicleRoute.visibility = View.VISIBLE

                        navigationMapRoute?.addRoute(route)
                    }
                }
            }
        }

        override fun onFailure(call: Call<DirectionsResponse>, throwable: Throwable) {

        }
    }

    private val clickMainRouteOptions1 = View.OnClickListener {
        routeOpt1.background = ContextCompat.getDrawable(this, R.color.blue)
        routeOpt2.background = ContextCompat.getDrawable(this, com.mapbox.services.android.navigation.ui.v5.R.color.md_grey_700)
        startNavigationBtn1.visibility = View.VISIBLE
        demoNavigationBtn1.visibility = View.VISIBLE
        startNavigationBtn2.visibility = View.INVISIBLE
        demoNavigationBtn2.visibility = View.INVISIBLE

        navigationMapRoute?.addRoute(route)
    }

    private val clickAlternativeRouteOptions2 = View.OnClickListener {
        routeOpt1.background = ContextCompat.getDrawable(this, com.mapbox.services.android.navigation.ui.v5.R.color.md_grey_700)
        routeOpt2.background = ContextCompat.getDrawable(this, R.color.blue)
        startNavigationBtn1.visibility = View.INVISIBLE
        demoNavigationBtn1.visibility = View.INVISIBLE
        startNavigationBtn2.visibility = View.VISIBLE
        demoNavigationBtn2.visibility = View.VISIBLE

        navigationMapRoute?.addRoute(route_1)
    }

    private val clickMainRouteToStartNav = View.OnClickListener {
        route.let { route ->
            val userLocation = maplibreMap.locationComponent.lastKnownLocation ?: return@let

            val options = NavigationLauncherOptions.builder()
                .directionsRoute(route)
                .shouldSimulateRoute(false)
                .initialMapCameraPosition(CameraPosition.Builder().target(LatLng(userLocation.latitude, userLocation.longitude)).build())
                .lightThemeResId(R.style.TestNavigationViewLight)
                .darkThemeResId(R.style.TestNavigationViewDark)
                .build()
            NavigationLauncher.startNavigation(this@MapActivity, options)
        }

        routeInfo.visibility = View.INVISIBLE
        cancelRouteBtn.visibility = View.INVISIBLE
        vehicleRoute.visibility = View.INVISIBLE
        locationrecyclerview.visibility = View.INVISIBLE
        locateButton.visibility = View.VISIBLE
        menuDashboard.visibility = View.VISIBLE
        emptyLocAddress.visibility = View.VISIBLE
        search.setText("")
        search.visibility = View.VISIBLE
        searchButton.visibility = View.VISIBLE

        maplibreMap.removeMarker(marker)
        navigationMapRoute?.removeRoute()

        startNavigationBtn1.visibility = View.INVISIBLE
        demoNavigationBtn1.visibility = View.INVISIBLE
        startNavigationBtn2.visibility = View.INVISIBLE
        demoNavigationBtn2.visibility = View.INVISIBLE

        car.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.white))
        pedestrian.setBackgroundTintList(ContextCompat.getColorStateList(this,
            com.mapbox.services.android.navigation.ui.v5.R.color.md_grey_700))

        routeDistance_0.text = "Calculating route..."
        routeDuration_0.text = ""
        routeDistance_1.text = "Calculating alternative route..."
        routeDuration_1.text = ""

        locationComponent!!.cameraMode = CameraMode.TRACKING

        onBackPressedDispatcher.addCallback(this,onBackPressedCallbackToExitApp)
    }

    private val clickAlternativeRouteToStartNav = View.OnClickListener {
        route_1.let { alternativeRoute ->
            val userLocation = maplibreMap.locationComponent.lastKnownLocation ?: return@let

            val options = NavigationLauncherOptions.builder()
                .directionsRoute(alternativeRoute)
                .shouldSimulateRoute(false)
                .initialMapCameraPosition(CameraPosition.Builder().target(LatLng(userLocation.latitude, userLocation.longitude)).build())
                .lightThemeResId(R.style.TestNavigationViewLight)
                .darkThemeResId(R.style.TestNavigationViewDark)
                .build()
            NavigationLauncher.startNavigation(this@MapActivity, options)
        }

        routeInfo.visibility = View.INVISIBLE
        cancelRouteBtn.visibility = View.INVISIBLE
        vehicleRoute.visibility = View.INVISIBLE
        locationrecyclerview.visibility = View.INVISIBLE
        locateButton.visibility = View.VISIBLE
        menuDashboard.visibility = View.VISIBLE
        emptyLocAddress.visibility = View.VISIBLE
        search.setText("")
        search.visibility = View.VISIBLE
        searchButton.visibility = View.VISIBLE

        maplibreMap.removeMarker(marker)
        navigationMapRoute?.removeRoute()

        startNavigationBtn1.visibility = View.INVISIBLE
        demoNavigationBtn1.visibility = View.INVISIBLE
        startNavigationBtn2.visibility = View.INVISIBLE
        demoNavigationBtn2.visibility = View.INVISIBLE

        car.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.white))
        pedestrian.setBackgroundTintList(ContextCompat.getColorStateList(this,
            com.mapbox.services.android.navigation.ui.v5.R.color.md_grey_700))

        routeDistance_0.text = "Calculating route..."
        routeDuration_0.text = ""
        routeDistance_1.text = "Calculating alternative route..."
        routeDuration_1.text = ""

        locationComponent!!.cameraMode = CameraMode.TRACKING

        onBackPressedDispatcher.addCallback(this,onBackPressedCallbackToExitApp)
    }

    private val clickMainRouteToDemoNav = View.OnClickListener {
        route.let { routeDemo ->
            val userLocation = maplibreMap.locationComponent.lastKnownLocation ?: return@let

            val options = NavigationLauncherOptions.builder()
                .directionsRoute(routeDemo)
                .shouldSimulateRoute(true)
                .initialMapCameraPosition(CameraPosition.Builder().target(LatLng(userLocation.latitude, userLocation.longitude)).build())
                .lightThemeResId(R.style.TestNavigationViewLight)
                .darkThemeResId(R.style.TestNavigationViewDark)
                .build()
            NavigationLauncher.startNavigation(this@MapActivity, options)
        }

        routeInfo.visibility = View.INVISIBLE
        cancelRouteBtn.visibility = View.INVISIBLE
        vehicleRoute.visibility = View.INVISIBLE
        locationrecyclerview.visibility = View.INVISIBLE
        locateButton.visibility = View.VISIBLE
        menuDashboard.visibility = View.VISIBLE
        emptyLocAddress.visibility = View.VISIBLE
        search.setText("")
        search.visibility = View.VISIBLE
        searchButton.visibility = View.VISIBLE

        maplibreMap.removeMarker(marker)
        navigationMapRoute?.removeRoute()

        startNavigationBtn1.visibility = View.INVISIBLE
        demoNavigationBtn1.visibility = View.INVISIBLE
        startNavigationBtn2.visibility = View.INVISIBLE
        demoNavigationBtn2.visibility = View.INVISIBLE

        car.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.white))
        pedestrian.setBackgroundTintList(ContextCompat.getColorStateList(this,
            com.mapbox.services.android.navigation.ui.v5.R.color.md_grey_700))

        routeDistance_0.text = "Calculating route..."
        routeDuration_0.text = ""
        routeDistance_1.text = "Calculating alternative route..."
        routeDuration_1.text = ""

        locationComponent!!.cameraMode = CameraMode.TRACKING

        onBackPressedDispatcher.addCallback(this,onBackPressedCallbackToExitApp)
    }

    private val clickAlternativeRouteToDemoNav = View.OnClickListener {
        route_1.let { alternativeRouteDemo ->
            val userLocation = maplibreMap.locationComponent.lastKnownLocation ?: return@let

            val options = NavigationLauncherOptions.builder()
                .directionsRoute(alternativeRouteDemo)
                .shouldSimulateRoute(true)
                .initialMapCameraPosition(CameraPosition.Builder().target(LatLng(userLocation.latitude, userLocation.longitude)).build())
                .lightThemeResId(R.style.TestNavigationViewLight)
                .darkThemeResId(R.style.TestNavigationViewDark)
                .build()
            NavigationLauncher.startNavigation(this@MapActivity, options)
        }

        routeInfo.visibility = View.INVISIBLE
        cancelRouteBtn.visibility = View.INVISIBLE
        vehicleRoute.visibility = View.INVISIBLE
        locationrecyclerview.visibility = View.INVISIBLE
        locateButton.visibility = View.VISIBLE
        menuDashboard.visibility = View.VISIBLE
        emptyLocAddress.visibility = View.VISIBLE
        search.setText("")
        search.visibility = View.VISIBLE
        searchButton.visibility = View.VISIBLE

        maplibreMap.removeMarker(marker)
        navigationMapRoute?.removeRoute()

        startNavigationBtn1.visibility = View.INVISIBLE
        demoNavigationBtn1.visibility = View.INVISIBLE
        startNavigationBtn2.visibility = View.INVISIBLE
        demoNavigationBtn2.visibility = View.INVISIBLE

        car.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.white))
        pedestrian.setBackgroundTintList(ContextCompat.getColorStateList(this,
            com.mapbox.services.android.navigation.ui.v5.R.color.md_grey_700))

        routeDistance_0.text = "Calculating route..."
        routeDuration_0.text = ""
        routeDistance_1.text = "Calculating alternative route..."
        routeDuration_1.text = ""

        locationComponent!!.cameraMode = CameraMode.TRACKING

        onBackPressedDispatcher.addCallback(this,onBackPressedCallbackToExitApp)
    }

    private fun checkPermissions() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        @Suppress("DEPRECATED_IDENTITY_EQUALS")
        if (ContextCompat.checkSelfPermission(this@MapActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !==
            PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MapActivity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this@MapActivity,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                ActivityCompat.requestPermissions(this@MapActivity,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
    }

    override fun onLocationChanged(location: Location) {
        lastLocation?.latitude = location.latitude
        lastLocation?.longitude = location.longitude
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buildLocationComponentActivationOptions(
        style: Style,
        locationComponentOptions: LocationComponentOptions
    ): LocationComponentActivationOptions {
        return LocationComponentActivationOptions
            .builder(this, style)
            .locationComponentOptions(locationComponentOptions)
            .useDefaultLocationEngine(true)
            .locationEngineRequest(
                LocationEngineRequest.Builder(750)
                    .setFastestInterval(750)
                    .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                    .build()
            )
            .build()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
