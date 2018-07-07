package network.b.bnet.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

/**
 * Created by Administrator on
 */
class PermissionUtils(private val context: Activity) {

    private var mHasPermissionRunnable: Runnable? = null
    private var mNoPermissionRunnable: Runnable? = null
    private var REQUEST_CODE_PERMISSION = 1000

    fun checkStoragePermission(hasPermissionDo: Runnable, ipd: IPermissionDialog) {
        var permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        checkPermission(permission, hasPermissionDo, Runnable {
            if (ipd != null) {
                ipd.showPermissionDialog("need to store~")
            }
        })
    }

    fun checkCameraPermission(hasPermissionDo: Runnable, ipd: IPermissionDialog) {
        var permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        checkPermission(permission, hasPermissionDo, Runnable {
            if (ipd != null) {
                ipd.showPermissionDialog("Need  take photos ~")
            }
        })
    }

    fun reRequestPermissions(hasPermissionDo: Runnable, ipd: IPermissionDialog) {
        var permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        mNoPermissionRunnable = Runnable {
            if (ipd != null)
                ipd.showPermissionDialog("Need  take photos ~~")
        }
        requestPermissions(permission)
    }

    private fun requestPermissions(permissions: Array<out String>) {
        ActivityCompat.requestPermissions(context, permissions, REQUEST_CODE_PERMISSION)
    }

    fun checkPermission(permissions: Array<out String>, hasPermissionDo: Runnable, noPermissionDo: Runnable) {
        mHasPermissionRunnable = null
        mNoPermissionRunnable = null
        if (isPermissionsGranted(permissions))
            hasPermissionDo.run()
        else if (ActivityCompat.shouldShowRequestPermissionRationale(context, permissions.get(0))) {
            noPermissionDo.run()
        } else {
            mHasPermissionRunnable = hasPermissionDo
            mNoPermissionRunnable = noPermissionDo
            requestPermissions(permissions)
        }
    }


    fun isPermissionsGranted(permissions: Array<out String>): Boolean {
        for (it in permissions) {
            if (ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED)
                return false
        }
        return true
    }

    private fun isAllGranted(grantResults: IntArray): Boolean {
        for (it in grantResults) {
            if (it != PackageManager.PERMISSION_GRANTED) return false
        }
        return true
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (isAllGranted(grantResults))
                mHasPermissionRunnable?.run()
            else mNoPermissionRunnable?.run()
        }
    }
}

