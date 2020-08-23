package tech.yunze.withu.LocationStufff

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tech.yunze.withu.R

class LocationApapter: RecyclerView.Adapter<LocationApapter.NoteHolder>(){

     var LocationList = ArrayList<Location>()


     inner class NoteHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
         var title:TextView
         var lat: TextView
         var lon: TextView

        init {
            title = itemView.findViewById(R.id.title)
           lat = itemView.findViewById(R.id.lat)
           lon = itemView.findViewById(R.id.lon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {

        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_item,parent,false)
        Log.i("Info:", "Now on create view holder")
        return NoteHolder(itemView)
    }

    override fun getItemCount(): Int {
        Log.i("Info:", "Now getItemCount" + LocationList.size)
        return LocationList.size
    }
    override fun onBindViewHolder(holder: NoteHolder, position: Int) {
        Log.i("Info:", "Now onBindViewHolder")
        val currentLocation = LocationList!!.get(position)
        holder.title.setText(currentLocation.title)
        holder.lat.setText(currentLocation.lat.toString())
        holder.lon.setText(currentLocation.lon.toString())
    }
    fun notifyMe(title:String,lat: Double, long: Double){
        LocationList!!.add(
            Location(
                title,
                lat,
                long
            )
        )
        notifyDataSetChanged()
    }
}