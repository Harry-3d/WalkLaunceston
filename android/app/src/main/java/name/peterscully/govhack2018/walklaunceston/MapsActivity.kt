package name.peterscully.govhack2018.walklaunceston

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.data.kml.KmlLayer
import java.util.regex.Pattern


class MapsActivity : AppCompatActivity() {

    private lateinit var map: GoogleMap

    private val defaultMapArea = LatLngBounds(LatLng(-41.460828, 147.097281), LatLng(-41.430178, 147.138681))

    private val heritagePlaces: MutableList<HeritagePlace> = mutableListOf()
    private val publicSeating: MutableList<PublicSeat> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap -> onMapReady(googleMap = googleMap) }
    }

    private fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.apply {
            mapType = GoogleMap.MAP_TYPE_TERRAIN
            setOnMapLoadedCallback { onMapLoaded() }
            setOnMarkerClickListener { marker -> onMarkerClick(marker = marker) }
            setOnInfoWindowClickListener { marker -> onInfoWindowClick(marker = marker) }
        }
    }

    private fun onMapLoaded() {
        map.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                defaultMapArea,
                resources.getDimensionPixelSize(R.dimen.map_padding)
            )
        )
        loadHeritagePlaces()
        loadPublicSeating()
        loadTrails()
    }

    private fun loadHeritagePlaces() {
        Log.d("loadHeritagePlaces", "")
        val inputStream = this.resources.openRawResource(R.raw.heritage_places_custom)

        inputStream.bufferedReader()
            .use { it.readText() }
            .split(Pattern.compile("\n"))
            .forEach { it ->
                val values = it.split(Pattern.compile(","))
                try {
                    heritagePlaces.add(
                        HeritagePlace(
                            lat = values[1].toDouble(),
                            long = values[0].toDouble(),
                            id = values[2].toLong(),
                            desc = values[6],
                            url = values[8]
                        )
                    )
                } catch (e: Exception) {
                    Log.e("loadHeritagePlaces", "Exception", e)
                }
            }
        heritagePlaces.forEach { it: HeritagePlace ->
            val location = LatLng(it.lat, it.long)
            val marker = map.addMarker(MarkerOptions().apply {
                position(location)
                title(it.desc)
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
            })
            marker.tag = "Heritage" + it.id
        }
    }

    private fun loadPublicSeating() {
        Log.d("loadPublicSeating", "")
        val inputStream = this.resources.openRawResource(R.raw.public_seating_custom)

        inputStream.bufferedReader()
            .use { it.readText() }
            .split(Pattern.compile("\n"))
            .forEach { it ->
                val values = it.split(Pattern.compile(","))
                try {
                    publicSeating.add(
                        PublicSeat(
                            lat = values[1].toDouble(),
                            long = values[0].toDouble(),
                            id = values[2].toLong(),
                            desc = values[3]
                        )
                    )
                } catch (e: Exception) {
                    Log.e("loadPublicSeating", "Exception", e)
                }
            }
        publicSeating.forEach { it: PublicSeat ->
            val location = LatLng(it.lat, it.long)
            val marker = map.addMarker(MarkerOptions().apply {
                position(location)
                title(it.desc)
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            })
            marker.tag = "Seat" + it.id
        }
    }

    private fun loadTrails() {
        val layer = KmlLayer(map, R.raw.recreational_trails, applicationContext)
        layer.addLayerToMap()
    }

    fun onMarkerClick(marker: Marker?): Boolean {
        marker?.let {
        }
        return false
    }

    fun onInfoWindowClick(marker: Marker?) {
        marker?.let {
            val tag = marker.tag as String
            if (tag.startsWith("Heritage")) {
                val id = tag.removePrefix("Heritage").toLong()
                val url = heritagePlaces.first { place -> place.id == id }.url
                if (url.isNotBlank()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }
            }
        }
    }

    private fun doActionWithPermissions(permission: String, requestCode: Int, action: () -> Unit) {
        when (ContextCompat.checkSelfPermission(this, permission)) {
            PackageManager.PERMISSION_GRANTED -> {
                action()
            }
            PackageManager.PERMISSION_DENIED -> {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    // TODO: Show rationale, *asynchronously*
                    showMessageOkCancel("Permission is needed to read storage.",
                        DialogInterface.OnClickListener { _, _ ->
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(permission),
                                requestCode
                            )
                        })
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(permission),
                        requestCode
                    )
                }
            }
            else -> {
                Log.wtf("doActionWithPermissions", "WTF - Unknown permission status!")
            }
        }
    }

    private fun showMessageOkCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadHeritagePlaces()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_READ_EXTERNAL_STORAGE = 1
    }
}
