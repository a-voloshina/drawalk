package ru.nsu.fit.android.drawalk.modules.permission

interface PermissionResultHandler {
    fun handleSuccessfullyGetPermission()
    fun handleCantGetPermission()
}