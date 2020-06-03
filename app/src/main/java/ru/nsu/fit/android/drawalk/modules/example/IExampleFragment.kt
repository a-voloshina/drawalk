package ru.nsu.fit.android.drawalk.modules.example

import ru.nsu.fit.android.drawalk.modules.base.IViewFragment

abstract class IExampleFragment(layoutId: Int): IViewFragment<IExamplePresenter>(layoutId) {
    abstract fun setExampleText(text: String)
}