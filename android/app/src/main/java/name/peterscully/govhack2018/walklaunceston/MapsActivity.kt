package name.peterscully.govhack2018.walklaunceston

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

class MapsActivity : AppCompatActivity() {

    private lateinit var map: GoogleMap

    private val defaultMapArea = LatLngBounds(LatLng(-41.4933, 147.0787), LatLng(-41.2291, 147.2219))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap -> onMapReady(googleMap = googleMap) }
    }

    fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.apply {
            setOnMapLoadedCallback { onMapLoaded() }
//            setOnMarkerClickListener { marker -> onMarkerClick(marker = marker) }
//            setOnInfoWindowClickListener { marker -> onInfoWindowClick(marker = marker) }
        }
    }

    fun onMapLoaded() {
        map.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                defaultMapArea,
                resources.getDimensionPixelSize(R.dimen.map_padding)
            )
        )
    }
}
