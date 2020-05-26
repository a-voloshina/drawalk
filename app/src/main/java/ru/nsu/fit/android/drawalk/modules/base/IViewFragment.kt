package ru.nsu.fit.android.drawalk.modules.base

import androidx.fragment.app.Fragment

abstract class IViewFragment<T: IPresenter>: IView<T>, Fragment() {
    lateinit var presenter: T

    override fun onResume() {
        super.onResume()
        presenter.start()
    }
}