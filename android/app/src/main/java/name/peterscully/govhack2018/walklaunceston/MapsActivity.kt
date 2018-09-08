package name.peterscully.govhack2018.walklaunceston

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import java.util.regex.Pattern


class MapsActivity : AppCompatActivity() {

    private lateinit var map: GoogleMap

    // Recreational Trails
//    private val defaultMapArea = LatLngBounds(LatLng(-41.4933, 147.0787), LatLng(-41.2291, 147.2219))

    // 	Heritage Places
    private val defaultMapArea = LatLngBounds(LatLng(-41.5219, 146.9826), LatLng(-41.2159, 147.4671))
    private val heritagePlaces: MutableList<HeritagePlace> = arrayListOf()
    private val publicSeating: MutableList<PublicSeat> = arrayListOf()

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
        doActionWithPermissions(
            permission = READ_EXTERNAL_STORAGE,
            requestCode = REQUEST_READ_EXTERNAL_STORAGE
        )
        { loadHeritagePlaces() }
        doActionWithPermissions(
            permission = READ_EXTERNAL_STORAGE,
            requestCode = REQUEST_READ_EXTERNAL_STORAGE
        )
        { loadPublicSeating() }
    }

    fun loadHeritagePlaces() {
        Log.d("loadHeritagePlaces", "")
        val inputStream = this.resources.openRawResource(R.raw.heritage_places)

        inputStream.bufferedReader()
            .use { it.readText() }
            .split(Pattern.compile("\n"))
            .forEach { it ->
                val values = it.split(Pattern.compile(","))
                try {
                    heritagePlaces.add(
                        HeritagePlace(
                            values[1].toDouble(),
                            values[0].toDouble(),
                            values[2].toLong(),
                            values[6]
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
            })
            marker.tag = "Heritage" + it.id
        }
    }

    fun loadPublicSeating() {
        Log.d("loadPublicSeating", "")
        val inputStream = this.resources.openRawResource(R.raw.public_seating)

        inputStream.bufferedReader()
            .use { it.readText() }
            .split(Pattern.compile("\n"))
            .forEach { it ->
                val values = it.split(Pattern.compile(","))
                try {
                    publicSeating.add(
                        PublicSeat(
                            values[1].toDouble(),
                            values[0].toDouble(),
                            values[2].toLong(),
                            values[3]
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
