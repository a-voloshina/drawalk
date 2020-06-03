package ru.nsu.fit.android.drawalk.modules.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

abstract class IViewFragment<T: IPresenter>(private val layoutId: Int): IView<T>, Fragment() {
    lateinit var presenter: T

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutId, container, false)
    }

    override fun onResume() {
        super.onResume()
        presenter.start()
    }
}