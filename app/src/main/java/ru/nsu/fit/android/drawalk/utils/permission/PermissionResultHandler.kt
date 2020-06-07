package ru.nsu.fit.android.drawalk.utils.permission

interface PermissionResultHandler {
    fun handleSuccessfullyGetPermission()
    fun handleCantGetPermission()
}