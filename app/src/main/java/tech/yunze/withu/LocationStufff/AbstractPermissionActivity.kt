package tech.yunze.withu.LocationStufff

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

abstract class AbstractPermissionActivity :Activity() {
    protected abstract fun getDesiredPermissions(): Array<String>?
    protected abstract fun onPermissionDenied()
    protected abstract  fun onReady()

    private val REQUEST_PERMISSION = 61125
    private val STATE_IN_PERMISSION ="inPermission"
    private var isInPermission = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState != null){
            isInPermission =
                savedInstanceState.getBoolean(STATE_IN_PERMISSION,false)

        }
        if (hasAllPermissions(getDesiredPermissions())) {
            onReady()
        } else if (!isInPermission) {
            isInPermission = true
            ActivityCompat
                .requestPermissions(
                    this,
                    netPermissions(getDesiredPermissions()),
                   REQUEST_PERMISSION
                )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        isInPermission = false
        if(requestCode ==REQUEST_PERMISSION){
            if(hasAllPermissions(getDesiredPermissions())){
                onReady()
            }else{
                onPermissionDenied()
            }
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_IN_PERMISSION, isInPermission)
    }
    fun hasAllPermissions(perms: Array<String>?): Boolean{
        for(perm in perms!!){
            if(!hasPermission(perm)){
                return false
            }
        }
        return true

    }

    fun hasPermission(perm: String): Boolean{
        return ContextCompat.checkSelfPermission(this, perm) ==
                PackageManager.PERMISSION_GRANTED

    }
    fun netPermissions(wanted: Array<String>?): Array<String>{
        val result = ArrayList<String>()
        for(perm in wanted!!){
            if(!hasPermission(perm)){
                result.add(perm)
            }
        }
        return result.toTypedArray()
    }

}