package ru.nsu.fit.android.drawalk.modules.navigation.fragments.myarts

import ru.nsu.fit.android.drawalk.R
import ru.nsu.fit.android.drawalk.modules.user.UserArtsFeedFragment
import ru.nsu.fit.android.drawalk.utils.FirebaseHolder

class MyArtsFeedFragment: UserArtsFeedFragment(FirebaseHolder.AUTH.currentUser?.uid) {
    override val noDataText by lazy { getString(R.string.no_my_arts_text) }
}