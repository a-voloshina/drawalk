package ru.nsu.fit.android.drawalk.modules.base

import androidx.appcompat.app.AppCompatActivity

abstract class IViewActivity<T: IPresenter>: IView<T>, AppCompatActivity() {
    protected lateinit var presenter: T

    override fun onResume() {
        super.onResume()
        presenter.start()
    }
}