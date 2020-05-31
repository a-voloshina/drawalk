package ru.nsu.fit.android.drawalk.modules.map

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import ru.nsu.fit.android.drawalk.R
import ru.nsu.fit.android.drawalk.modules.permission.OnPermissionCallback
import ru.nsu.fit.android.drawalk.modules.permission.PermissionHelper
import ru.nsu.fit.android.drawalk.modules.permission.PermissionResultHandler

class LocationPermissionCallback(
    private val context: Activity,
    private val resultHandler: PermissionResultHandler
) : OnPermissionCallback {

    companion object {
        const val LOCATION_REQUEST_CODE = 23
    }

    private val permissionHelper = PermissionHelper(this, context)

    override fun onPermissionGranted(permissionName: String) {
        resultHandler.handleSuccessfullyGetPermission()
    }

    override fun onPermissionDeclined(permissionName: String) {
        resultHandler.handleCantGetPermission()
    }

    override fun onPermissionNeedExplanation(permissionName: String, explanationMessage: String) {
        AlertDialog.Builder(context)
            .setPositiveButton(R.string.allow) { _, _ ->
                permissionHelper.requestAfterExplanation(permissionName, LOCATION_REQUEST_CODE)
            }
            .setNegativeButton(R.string.decline) { _, _ ->
                resultHandler.handleCantGetPermission()
            }
            .setTitle(R.string.permission_denied)
            .setMessage(explanationMessage)
            .show()
    }

    override fun onPermissionReallyDeclined(permissionName: String, explanationMessage: String) {
        AlertDialog.Builder(context)
            .setPositiveButton(R.string.allow) { _, _ ->
                openSettingsScreen()
            }
            .setNegativeButton(R.string.decline) { _, _ ->
                resultHandler.handleCantGetPermission()
            }
            .setTitle(permissionName)
            .setMessage(explanationMessage)
            .show()
    }

    fun requestPermission(permissionName: String, explanationMessage: String) {
        permissionHelper.request(permissionName, LOCATION_REQUEST_CODE, explanationMessage)
    }

    private fun openSettingsScreen() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.parse("package:" + context.packageName)
        intent.data = uri
        context.startActivity(intent)
    }
}