package com.example.gpsarduino

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class GpsAdapter (val gpsCntx : Context, val LayoutResId : Int, val gpsList:List<GPS>):ArrayAdapter<GPS>(gpsCntx,
    LayoutResId,gpsList) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater : LayoutInflater = LayoutInflater.from(gpsCntx)

        val view : View = layoutInflater.inflate(LayoutResId, null)

        val tvLokasi : TextView = view.findViewById(R.id.tvLokasiValue)

        tvLokasi.text = GPS.latlng

        return view
    }

}