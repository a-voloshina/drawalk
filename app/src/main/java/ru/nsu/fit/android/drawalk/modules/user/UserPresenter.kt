package ru.nsu.fit.android.drawalk.modules.user

import ru.nsu.fit.android.drawalk.model.firebase.UserData
import ru.nsu.fit.android.drawalk.utils.FirebaseHolder

class UserPresenter(
    private val userId: String?,
    private val view: IUserActivity
): IUserPresenter {
    override fun start() {
        if (userId == null) {
            view.showError(RuntimeException("User not found"))
            return
        }
        FirebaseHolder.USERS.document(userId).get().addOnFailureListener {
            view.showError(it)
        }.addOnSuccessListener {
            view.setUserData(it.toObject(UserData::class.java) ?: run {
                view.showError(RuntimeException("Cannot cast data to user data type"))
                return@addOnSuccessListener
            })
        }
    }
}