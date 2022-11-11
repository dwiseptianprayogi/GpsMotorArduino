package com.example.gpsarduino

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import me.ibrahimsn.lib.Speedometer
import java.io.IOException
import java.lang.Float
import java.util.*
import kotlin.Array
import kotlin.Int
import kotlin.IntArray
import kotlin.String
import kotlin.apply
import kotlin.arrayOf
import kotlin.toString


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Homefragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Homefragment : Fragment(), OnMapReadyCallback, LocationListener {

    private lateinit var mMap: GoogleMap
    private lateinit var database: DatabaseReference
    private lateinit var database1: DatabaseReference
    private lateinit var firebaseDatabase: FirebaseDatabase

    private lateinit var tvLokasi: TextView
    private lateinit var mapView : MapView

    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_homefragment, container, false)
    }

    companion object {

        fun newInstance() : Homefragment {
            val fragment = Homefragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val view: View = inflater.inflate(R.layout.fragment_map, container, false)

        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

//        val mapFragment = requireFragmentManager()
//            .findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment!!.getMapAsync(this)

        val bottomSheetFragment = BottomSheetFragment()

        BottomSheetBehavior.from(view.findViewById(R.id.sheet)).apply {
            peekHeight=750
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        database1 = Firebase.database.reference

        val tvLolkasi: TextView = view.findViewById(R.id.tvLokasiValue)

        getData()
        getLocation()


        //added because new access fine location policies, imported class..
        //ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);


        //check permission
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            ActivityCompat.requestPermissions(
                requireContext() as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1000
            )
        } else {

            //start the program if permission is granted
            doStuff()
        }

        val btnImage: Button = view.findViewById(R.id.btnDialog)
        btnImage.setOnClickListener {
            var dialog = imageFragment()
            dialog.show(requireFragmentManager(), "customDialog")
        }
        val btn : Button = view.findViewById(R.id.btnURLMaps)

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

        firebaseDatabase.getReference("gps/state").addValueEventListener(
            object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.value.toString()
                    if (value == "1"){
                        getLocation()
                    }else{
                        Toast.makeText(context, "value state gps no get LatLng", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "error get state gps", Toast.LENGTH_SHORT).show()
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
//                finish()
            }
        }
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun doStuff() {
        val lm = requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
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
        Toast.makeText(context, "Waiting for GPS connection!", Toast.LENGTH_SHORT).show()
    }

    private fun getLocation() {
        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(requireContext() as Activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
    }
    override fun onLocationChanged(location: Location) {
//        tvGpsLocation = findViewById(R.id.textView)
//        tvGpsLocation.text = "Latitude: " + location.latitude + " , Longitude: " + location.longitude
//        Toast.makeText(context,"Latlong: "+location.latitude+","+location.longitude, Toast.LENGTH_LONG).show()

        FirebaseDatabase.getInstance().reference.child("gps/latLng")
            .setValue(location.latitude.toString()+","+location.longitude.toString())

        FirebaseDatabase.getInstance().reference.child("gps/key").get().addOnSuccessListener {
            val key = it.value.toString()
            FirebaseDatabase.getInstance().reference.child("gps/lokasi").child(key.toString()).child("latLng")
                .setValue(location.latitude.toString()+","+location.longitude.toString())
        }

        FirebaseDatabase.getInstance().reference.child("gps/state").setValue("0")

        val speedometer = view?.findViewById<Speedometer>(R.id.speedometer)

        val nCurrentSpeed = location.speed * 3.6f
        if (speedometer != null) {
            speedometer.setSpeed(nCurrentSpeed.toInt(), 1000L)
        }

    }

    private fun getData() {

        firebaseDatabase = FirebaseDatabase.getInstance()
        database = firebaseDatabase.getReference("gps/latLng")
        database .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot){
                val latitude = dataSnapshot.value.toString()
                val tvLolkasi: TextView = view!!.findViewById(R.id.tvLokasiValue)
//                tvLolkasi.text = latitude.toString()

                var latlong = latitude.split(',')
                var lat = Float.parseFloat(latlong[0])
                var lng = Float.parseFloat(latlong[1])

                val geocoder = Geocoder(context)
                val list = geocoder.getFromLocation(lat.toDouble(), lng.toDouble(), 1)
                val addresses = list.get(0).getAddressLine(0);
                tvLolkasi.text = addresses

                val mGeocoder = Geocoder(context, Locale.getDefault())
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
                    Toast.makeText(context,"Unable connect to Geocoder", Toast.LENGTH_LONG).show()
                }

            }
            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Toast.makeText(context, "erroe getting data latlng", Toast.LENGTH_SHORT).show()
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })

    }

    override fun onMapReady(googleMap: GoogleMap) {
        getData()
        mMap = googleMap

        firebaseDatabase = FirebaseDatabase.getInstance()
        database = firebaseDatabase.getReference("gps/latLng")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mMap.clear()
                val latitude = snapshot.value.toString()

                var latlong = latitude.split(',')
                var lat = Float.parseFloat(latlong[0])
                var lng = Float.parseFloat(latlong[1])

                val sydney = LatLng(lat.toDouble(), lng.toDouble())
                mMap.addMarker(MarkerOptions().position(sydney).title("Lokasi Motor"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16.0f))
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed load data maps", Toast.LENGTH_LONG).show()
            }

        })
    }
}