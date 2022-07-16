package com.example.gpsarduino

import android.content.ContentValues.TAG
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.IOException
import java.lang.Float.parseFloat
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var database: DatabaseReference
    private lateinit var database1: DatabaseReference
    private lateinit var firebaseDatabase: FirebaseDatabase

    private lateinit var tvLokasi:TextView
    private lateinit var mapView : MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val bottomSheetFragment = BottomSheetFragment()

        BottomSheetBehavior.from(findViewById(R.id.sheet)).apply {
            peekHeight=700
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }

//        firebaseDatabase = FirebaseDatabase.getInstance()
//        database = firebaseDatabase.getReference("latLng")

        database1 = Firebase.database.reference

        val tvLolkasi: TextView = findViewById(R.id.tvLokasiValue)


        getData()

        val lat = database.child("lat").get()
        val lng =  database.child("lng").get()

        val btnImage:Button = findViewById(R.id.btnDialog)
        btnImage.setOnClickListener {
            var dialog = imageFragment()
            dialog.show(supportFragmentManager, "customDialog")
        }
        val btn : Button = findViewById(R.id.btnURLMaps)

        database1.child("url").get().addOnSuccessListener {
            var url ="${it.value}"
//            findViewById<TextView>(R.id.tvSpeedometer).text = url
            Log.i("firebase", "Got value ${it.value}")
//                val gmmIntentUri = Uri.parse(url)
//                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
//                mapIntent.setPackage("com.google.android.apps.maps")
//                startActivity(mapIntent)
//                Toast.makeText(this@MapsActivity, url, Toast.LENGTH_SHORT).show()
            btn.setOnClickListener {
//                val url1 = url
//                val i = Intent(Intent.ACTION_VIEW)
//                i.data = Uri.parse(url1)
//                startActivity(i)
                val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(url))
                startActivity(intent)
            }
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }
//        bottomSheetFragment.show(supportFragmentManager, "hahahaha")


//        database.child("latLng").get().addOnSuccessListener {
//            Log.i("firebase", "Got value ${it.value}")
//            var lokasi = "${it.value}"
//
//
//
//        }.addOnFailureListener{
//            Log.e("firebase", "Error getting data", it)
//        }
//        database.child("url").get().addOnSuccessListener {
//            var url ="${it.value}"
//            Log.i("firebase", "Got value ${it.value}")
//        }.addOnFailureListener{
//            Log.e("firebase", "Error getting data", it)
//        }
        firebaseDatabase = FirebaseDatabase.getInstance()
        database = firebaseDatabase.getReference("speed")
        database .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val speedVal = dataSnapshot.value.toString()
                val tvSpeed = findViewById<TextView>(R.id.tvSpeedometerValue)
                tvSpeed.text = speedVal
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun getData() {

        firebaseDatabase = FirebaseDatabase.getInstance()
        database = firebaseDatabase.getReference("latLng")
        database .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot){
                val latitude = dataSnapshot.value.toString()
                val tvLolkasi: TextView = findViewById(R.id.tvLokasiValue)
//                tvLolkasi.text = latitude.toString()

            var latlong = latitude.split(',')
            var lat = parseFloat(latlong[0])
            var lng = parseFloat(latlong[1])

            val geocoder = Geocoder(this@MapsActivity)
            val list = geocoder.getFromLocation(lat.toDouble(), lng.toDouble(), 1)
            val addresses = list.get(0).getAddressLine(0);
            tvLolkasi.text = addresses

            val mGeocoder = Geocoder(applicationContext, Locale.getDefault())
            var addressString = ""
            try {
                val addressList: List<Address> = mGeocoder.getFromLocation(lat.toDouble(), lng.toDouble(), 1)

                if (addressList.isNotEmpty()){
                    val address = addressList[0]
                    val sb = StringBuilder()
                    for (i in 0 until address.maxAddressLineIndex){
                        sb.append(address.getAddressLine(i)).append("\n")
                    }
                    if (address.premises != null){
                        sb.append(address.premises).append(", ")
                        sb.append(address.locality).append(", ")
                        sb.append(address.adminArea).append(", ")
                        sb.append(address.countryName).append(", ")
                        sb.append(address.postalCode)
                        addressString = sb.toString()

                    }
                }
            } catch (e: IOException){
                Toast.makeText(applicationContext,"Unable connect to Geocoder", Toast.LENGTH_LONG).show()
            }

            }
            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
        
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        getData()
        mMap = googleMap

        firebaseDatabase = FirebaseDatabase.getInstance()
        database = firebaseDatabase.getReference("latLng")
        database .addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                mMap.clear()
                val latitude = snapshot.value.toString()

                var latlong = latitude.split(',')
                var lat = parseFloat(latlong[0])
                var lng = parseFloat(latlong[1])

                val sydney = LatLng(lat.toDouble(), lng.toDouble())
                mMap.addMarker( MarkerOptions().position(sydney).title("Lokasi Motor"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,16.0f))
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext,"Failed load data maps", Toast.LENGTH_LONG).show()
            }

        })



//        database.child("latLng").get().addOnSuccessListener {
//            var url ="${it.value}"
//
//            var latlong =  url.split(',')
//            var latitude = parseFloat(latlong[0])
//            var longitude = parseFloat(latlong[1])
//
//            val sydney = LatLng(latitude.toDouble(), longitude.toDouble())
//            mMap.addMarker(MarkerOptions().position(sydney).title("Lokasi Motor"))
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,16.0f))
//
//            Log.i("firebase", "Got value ${it.value}")
//        }.addOnFailureListener{
//            Log.e("firebase", "Error getting data", it)
//        }
    }
}