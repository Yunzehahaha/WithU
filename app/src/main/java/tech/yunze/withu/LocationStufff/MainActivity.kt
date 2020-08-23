package tech.yunze.withu.LocationStufff

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import tech.yunze.withu.R

class MainActivity : AbstractPermissionActivity() , LocationListener{
    private var mgr: LocationManager? = null
    val PERMS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    val LocationAdapter = LocationApapter()
    val listener: LocationListener? = this
    override fun getDesiredPermissions(): Array<String>? {
        return PERMS
    }

    override fun onPermissionDenied() {
        Log.d("Info:" , " Permission application is failed")
    }
    @SuppressWarnings("MissingPermission")
    override fun onReady() {
//        getLocation()
        Log.d("Info:" , " Permission is ready")
//       mgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//       mgr!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3600000, 1000f, this)


    }
    @SuppressWarnings("MissingPermission")
    fun getLocation() = runBlocking {
        coroutineScope{
            launch {
                mgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                mgr!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,100,12f,listener)
                Log.d("Info:", "first request")
                delay(200)
                mgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                mgr!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,100,12f,listener)
                mgr!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,100,12f,listener)
                mgr!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,100,12f,listener)
                mgr!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,100,12f,listener)
                Log.d("Info:", "sceond request")
                //delay(2000)

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.setLayoutManager(LinearLayoutManager(this))
        recyclerView.setHasFixedSize(true)
        //LocationAdapter.notifyMe("From GPS"1.2,1.3)
        recyclerView.adapter = LocationAdapter
        getLocation()
//        LocationAdapter.notifyMe("lication1",1.0,1.0)
//        LocationAdapter.notifyMe("lication2",2.0,2.0)
//        LocationAdapter.notifyMe("lication3",2.0,2.0)
        Log.d("Info:","Main activity on create finished")



        println("Yunze: ${WorkManager.singletonHolder.getInstance("HeiHei")}")


        println( "Yunze: ${WorkManager.singletonHolder.get()!!.name}")

        //WorkManager.singletonHolder.get()!!.applicationContext
        println("Yunze: ${WorkManager.singletonHolder.get()!!.applicationContext}")
        println("Yunze: ")
        println("Yunze: ")
        println("Yunze: ${WorkManager.singletonHolder.get()!!.applicationContext}")
        println("Yunze: ${WorkManager.singletonHolder.get()!!.applicationContext}")


    }

    override fun onLocationChanged(location: Location?) {
        val roundedLat =
            Math.round(location!!.latitude * 10000.0).toDouble() / 10000.0
        val roundedLon =
            Math.round(location.longitude * 10000.0).toDouble() / 10000.0
        LocationAdapter.notifyMe("from GPS",roundedLat,roundedLon)
        Log.d("Lat: ", roundedLat.toString())
        Log.d("Long: ", roundedLon.toString())
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderDisabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}


