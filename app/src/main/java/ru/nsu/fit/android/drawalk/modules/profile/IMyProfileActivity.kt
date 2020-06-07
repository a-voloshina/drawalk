package ru.nsu.fit.android.drawalk.modules.profile

import ru.nsu.fit.android.drawalk.modules.base.IViewActivity

abstract class IMyProfileActivity: IViewActivity<IMyProfilePresenter>() {
    abstract fun updateUserData()
    abstract fun onUserInfoDeleted()
    abstract fun closeAfterDeletion()
}