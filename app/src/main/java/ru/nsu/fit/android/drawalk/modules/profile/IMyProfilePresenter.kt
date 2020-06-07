package ru.nsu.fit.android.drawalk.modules.profile

import ru.nsu.fit.android.drawalk.modules.base.IPresenter

interface IMyProfilePresenter: IPresenter {
    fun updateName(name: String)
    fun deleteAccount()
}