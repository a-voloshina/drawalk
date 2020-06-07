package ru.nsu.fit.android.drawalk.modules.profile

import ru.nsu.fit.android.drawalk.utils.FirebaseHolder

class MyProfilePresenter(private val view: IMyProfileActivity): IMyProfilePresenter {
    override fun updateName(name: String) {
        FirebaseHolder.changeUsername(name, {
            view.closeAfterDeletion()
        }) {
            view.showError(it)
        }
    }

    override fun deleteAccount() {
        FirebaseHolder.deleteAccount({
            view.closeAfterDeletion()
        }, {
            view.showError(it)
        }) {
            view.onUserInfoDeleted()
        }
    }

    override fun start() {}
}