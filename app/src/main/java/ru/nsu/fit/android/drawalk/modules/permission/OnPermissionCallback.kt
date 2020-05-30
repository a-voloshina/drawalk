package ru.nsu.fit.android.drawalk.modules.permission

interface OnPermissionCallback {
    fun onPermissionGranted(permissionName: String)
    fun onPermissionDeclined(permissionName: String)
    fun onPermissionNeedExplanation(permissionName: String, explanationMessage: String)
    fun onPermissionReallyDeclined(permissionName: String, explanationMessage: String)
}