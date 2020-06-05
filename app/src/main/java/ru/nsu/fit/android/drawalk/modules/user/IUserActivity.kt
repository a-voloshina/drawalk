package ru.nsu.fit.android.drawalk.modules.user

import ru.nsu.fit.android.drawalk.model.firebase.UserData
import ru.nsu.fit.android.drawalk.modules.base.IViewActivity

abstract class IUserActivity: IViewActivity<IUserPresenter>() {
    abstract fun setUserData(userData: UserData)
}