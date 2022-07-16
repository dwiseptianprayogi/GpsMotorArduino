package com.example.gpsarduino

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private  lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvLat: TextView = findViewById(R.id.tvLat)
        val tvLng: TextView = findViewById(R.id.tvLng)
        val tvLatValue: TextView = findViewById(R.id.tvLatValue)
        val tvLngValue: TextView = findViewById(R.id.tvLngValue)
        val tvUrlValue: TextView = findViewById(R.id.tvURLValue)
        val btnImage:Button = findViewById(R.id.btnDialog)


        database = Firebase.database.reference
        val lat = database.child("lat").get()
        val lng =  database.child("lng").get()

        database.child("lat").get().addOnSuccessListener {
            tvLatValue.text = "${it.value}"
            Log.i("firebase", "Got value ${it.value}")
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }
        database.child("lng").get().addOnSuccessListener {
            tvLngValue.text = "${it.value}"
            Log.i("firebase", "Got value ${it.value}")
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }
        database.child("url").get().addOnSuccessListener {
            var url ="${it.value}"
//            tvUrlValue.text = url
            val btnURL: Button = findViewById(R.id.btnURL)
            Log.i("firebase", "Got value ${it.value}")

            btnURL.setOnClickListener {
//                val openURL = Intent(android.content.Intent.ACTION_VIEW)
//                openURL.data = Uri.parse(url)
//                startActivity(openURL)
                val gmmIntentUri = Uri.parse(url)
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)

                Toast.makeText(this@MainActivity, url, Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }

        btnImage.setOnClickListener {
            var dialog = imageFragment()
            dialog.show(supportFragmentManager, "customDialog")
        }

    }

    class LocationAddress {

    }

}

private fun Geocoder.getFromLocation(latitude: Task<DataSnapshot>, longitude: Task<DataSnapshot>, i: Int) {

}


