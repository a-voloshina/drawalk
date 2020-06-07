package ru.nsu.fit.android.drawalk.utils.permission

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import ru.nsu.fit.android.drawalk.DraWalkApplication

class PermissionHelper(
    private val onPermissionCallback: OnPermissionCallback,
    private val context: Activity
) {

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isPermissionBanned(permissionsName: String): Boolean {
        return ((context.checkSelfPermission(permissionsName) ==
                PackageManager.PERMISSION_DENIED) && (!isExplanationNeeded(permissionsName)))
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isPermissionDeclined(permissionsName: String): Boolean {
        return context.checkSelfPermission(permissionsName) !=
                PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isExplanationNeeded(permissionName: String): Boolean {
        return context.shouldShowRequestPermissionRationale(permissionName)
    }

    fun request(
        permissionName: String,
        requestCode: Int,
        explanationMessage: String
    ): PermissionHelper {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val prefKeyFirstAskingPermission = "pref_first_perm_ask"
            if(DraWalkApplication.preferencesManager().getBoolean(prefKeyFirstAskingPermission)){
                DraWalkApplication.preferencesManager().putBoolean(prefKeyFirstAskingPermission, false)
                context.requestPermissions(arrayOf(permissionName), requestCode)
            } else {
                if (context.checkSelfPermission(permissionName) == PackageManager.PERMISSION_GRANTED) {
                    onPermissionCallback.onPermissionGranted(permissionName)
                } else {
                    if (isExplanationNeeded(permissionName)) {
                        onPermissionCallback.onPermissionNeedExplanation(
                            permissionName,
                            explanationMessage
                        )
                    } else {
                        onPermissionCallback.onPermissionReallyDeclined(permissionName, explanationMessage)
                    }
                }
            }

        } else {
            //NoPermissionNeeded
        }
        return this
    }

    fun requestAfterExplanation(permissionName: String, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(permissionName) != PackageManager.PERMISSION_GRANTED) {
                context.requestPermissions(arrayOf(permissionName), requestCode)
            } else {
                onPermissionCallback.onPermissionGranted(permissionName)
            }
        }
    }

}