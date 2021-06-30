package com.servirunplatomas.puntoplato.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import kotlin.collections.ArrayList
import kotlinx.android.synthetic.main.fragment_ubication.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterManager
import com.servirunplatomas.puntoplato.R
import com.servirunplatomas.puntoplato.common.ClusterMap
import com.servirunplatomas.puntoplato.common.ClusterMarker
import com.servirunplatomas.puntoplato.data.model.Ubication
import com.servirunplatomas.puntoplato.data.repository.FirestoreImpl
import com.servirunplatomas.puntoplato.domain.UseCaseImpl
import com.servirunplatomas.puntoplato.ui.MainViewModel
import com.servirunplatomas.puntoplato.ui.MainViewModelFactory
import com.servirunplatomas.puntoplato.vo.State

@ExperimentalCoroutinesApi
class UbicationFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    // GOOGLE MAPS & CURRENT LOCATION
    private var mMap: GoogleMap? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var locationRequest: LocationRequest? = null
    private var location: Location? = null

    // LIST OF POINTS & MARKERS
    private val listPoints = mutableListOf<String>()
    private val clusterManager by lazy { ClusterManager<ClusterMap>(context, mMap) }

    // VIEW MODEL
    private val viewModel by lazy { ViewModelProvider(this, MainViewModelFactory(UseCaseImpl(FirestoreImpl())))
        .get(MainViewModel::class.java) }

    companion object { private const val MY_PERMISSIONS_REQUEST_UBICATION = 1 }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ubication, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        observeData()
    }

    private fun observeData() {

        viewModel.liveDataPoints.observe(viewLifecycleOwner, { result ->

            when(result) {

                is State.Loading -> {
                    showProgress()
                }

                is State.Success -> {
                    getPoints(result.data)
                    hideProgress()
                }

                is State.Failure -> {
                    hideProgress()
                    showError()
                }
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap?) {

        mMap = googleMap

        mMap?.uiSettings?.isZoomControlsEnabled = true
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(-34.588817, -58.450779)))
        mMap?.animateCamera(CameraUpdateFactory.zoomTo(11f))
        mMap?.setOnCameraIdleListener(clusterManager)
        mMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.custom_map))
        mMap?.setOnMarkerClickListener(this)

        getLocationPermission()
    }

    private fun getLocationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkLocationPermission()) {

                mMap?.isMyLocationEnabled = true

                locationRequest()
                locationCallback()

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
                fusedLocationProviderClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            }

        } else {

            mMap?.isMyLocationEnabled = true

            locationRequest()
            locationCallback()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
            fusedLocationProviderClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    private fun checkLocationPermission(): Boolean {

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_UBICATION)

            } else {

                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_UBICATION)
            }

            return false

        } else {

            return true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        when (requestCode) { MY_PERMISSIONS_REQUEST_UBICATION -> {

            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                if (checkLocationPermission()) {

                    mMap?.isMyLocationEnabled = true

                    locationRequest()
                    locationCallback()

                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
                    fusedLocationProviderClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                }
            }
        }
        }
    }

    private fun locationRequest() {

        locationRequest = LocationRequest()
        locationRequest?.interval = 25000
        locationRequest?.fastestInterval = 20000
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun locationCallback() {

        locationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult?) {

                location = locationResult!!.locations[locationResult.locations.size-1]

                if (locationResult.locations.isNotEmpty()) {

                    val geopoint = location?.latitude?.let { location?.longitude?.let { it1 -> LatLng(it, it1) } }
                    mMap?.moveCamera(CameraUpdateFactory.newLatLng(geopoint))
                    mMap?.animateCamera(CameraUpdateFactory.zoomTo(15f))
                }
            }
        }
    }

    private fun getPoints(result: List<String>) {

        mMap?.clear()
        clusterManager.clearItems()
        listPoints.clear()

        clusterManager.renderer = ClusterMarker(requireContext(), mMap!!, clusterManager)

        val ubication = Ubication()
        val arrayList: ArrayList<ClusterMap> = ArrayList()

        for (points in result) {

            listPoints.add(points)

            ubication.name = points.substringAfter("name=").substringBefore(',').replace("}","")
            ubication.latitude = points.substringAfter("latitude=").substringBefore(',').replace("}","")
            ubication.longitude = points.substringAfter("longitude=").substringBefore(',').replace("}","")

            val point = ClusterMap(ubication.name, LatLng(ubication.latitude.toDouble(), ubication.longitude.toDouble()))
            arrayList.add(point)
        }

        clusterManager.addItems(arrayList)
        clusterManager.cluster()
        arrayList.clear()

        hideProgress()
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        sendDataMarker(p0)
        return true
    }

    private fun sendDataMarker(p0: Marker?) {

        if (!listPoints.isNullOrEmpty()) {

            for (points in listPoints) {

                val name = points.substringAfter("name=").substringBefore(',').replace("}","")
                val address = points.substringAfter("address=").substringBefore(',').replace("}","")
                val instagram = points.substringAfter("instagram=").substringBefore(',').replace("}","")
                val schedule = points.substringAfter("schedule=").substringBefore(',').replace("}","")
                val image = points.substringAfter("image=").substringBefore(',').replace("}","")

                if (p0?.title == name) {

                    val bundle = Bundle()
                    bundle.putString("name",name)
                    bundle.putString("address",address)
                    bundle.putString("instagram",instagram)
                    bundle.putString("schedule",schedule)
                    bundle.putString("image",image)

                    val ubicationDetailFragment = UbicationDetailFragment()
                    ubicationDetailFragment.arguments = bundle

                    findNavController().navigate(R.id.menuUbicationDetailFragment,bundle)
                }
            }
        }
    }

    private fun showProgress() {
        layoutProgressUbication.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        layoutProgressUbication.visibility = View.GONE
    }

    private fun showError() {
        Toast.makeText(context,"Por favor, revise su conexión!",Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
        }
        super.onStop()
    }

    override fun onResume() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
        super.onResume()
    }
}