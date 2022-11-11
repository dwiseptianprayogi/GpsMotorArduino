package com.example.gpsarduino

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import me.ibrahimsn.lib.Speedometer
import java.io.IOException
import java.lang.Float.parseFloat
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    private lateinit var mMap: GoogleMap
    private lateinit var database: DatabaseReference
    private lateinit var database1: DatabaseReference
    private lateinit var firebaseDatabase: FirebaseDatabase

    private lateinit var tvLokasi:TextView
    private lateinit var mapView : MapView

    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val bottomSheetFragment = BottomSheetFragment()

        BottomSheetBehavior.from(findViewById(R.id.sheet)).apply {
            peekHeight=1000
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        database1 = Firebase.database.reference

        val tvLolkasi: TextView = findViewById(R.id.tvLokasiValue)

        getData()
        getLocation()

        //added because new access fine location policies, imported class..
        //ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);


        //check permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1000
            )
        } else {

            //start the program if permission is granted
            doStuff()
        }

//        val speedometer = findViewById<Speedometer>(R.id.speedometer)
//
//        speedometer.setSpeed(80, 1000L)

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
            btn.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(url))
                startActivity(intent)
            }
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }

        firebaseDatabase = FirebaseDatabase.getInstance()
        database = firebaseDatabase.getReference("gps/speed")
        database .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val speedVal = dataSnapshot.value.toString()
                val tvSpeed = findViewById<TextView>(R.id.tvSpeedometerValue)
                tvSpeed.text = speedVal
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MapsActivity, "value speed is null", Toast.LENGTH_SHORT).show()
            }
        })

        firebaseDatabase.getReference("gps/state").addValueEventListener(
            object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.value.toString()
                    if (value == "1"){
                        getLocation()
                    }else{
                        Toast.makeText(this@MapsActivity, "value state gps no get LatLng", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MapsActivity, "error get state gps", Toast.LENGTH_SHORT).show()
                }

            }
        )


    }




    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doStuff()
            } else {
                finish()
            }
        }
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun doStuff() {
        val lm = this.getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        lm?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
        Toast.makeText(this, "Waiting for GPS connection!", Toast.LENGTH_SHORT).show()
    }

    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
    }
    override fun onLocationChanged(location: Location) {
//        tvGpsLocation = findViewById(R.id.textView)
//        tvGpsLocation.text = "Latitude: " + location.latitude + " , Longitude: " + location.longitude
        Toast.makeText(this,"Latlong: "+location.latitude+","+location.longitude, Toast.LENGTH_LONG).show()
        FirebaseDatabase.getInstance().reference.child("gps/latLng")
            .setValue(location.latitude.toString()+","+location.longitude.toString())

        FirebaseDatabase.getInstance().reference.child("gps/key").get().addOnSuccessListener {
            val key = it.value.toString()
            FirebaseDatabase.getInstance().reference.child("gps/lokasi").child(key.toString()).child("latLng")
                .setValue(location.latitude.toString()+","+location.longitude.toString())
        }

//        Toast.makeText(this, "key: $key", Toast.LENGTH_LONG).show()

//        FirebaseDatabase.getInstance().reference.child("gps/lokasi").push()
//            .setValue(location.latitude.toString()+","+location.longitude.toString())

        FirebaseDatabase.getInstance().reference.child("gps/state").setValue("0")
        val txt = findViewById<View>(R.id.tvSpeedometerValue) as TextView
        val speedometer = findViewById<Speedometer>(R.id.speedometer)

        if (location == null) {
            txt.text = "-.- km/h"
            speedometer.setSpeed(0, 1000L)
        } else {
            val nCurrentSpeed = location.speed * 3.6f
            txt.text = String.format("%.2f", nCurrentSpeed) + " km/h"
            speedometer.setSpeed(nCurrentSpeed.toInt(), 1000L)
        }

    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == locationPermissionCode) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
//            }
//            else {
//                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }


    private fun getData() {

        firebaseDatabase = FirebaseDatabase.getInstance()
        database = firebaseDatabase.getReference("gps/latLng")
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
                Toast.makeText(applicationContext, "erroe getting data latlng", Toast.LENGTH_SHORT).show()
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
        database = firebaseDatabase.getReference("gps/latLng")
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