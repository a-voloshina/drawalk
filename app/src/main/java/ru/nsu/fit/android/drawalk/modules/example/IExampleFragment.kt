package ru.nsu.fit.android.drawalk.modules.example

import ru.nsu.fit.android.drawalk.modules.base.IView
import ru.nsu.fit.android.drawalk.modules.base.IViewFragment

abstract class IExampleFragment: IViewFragment<IExamplePresenter>() {
    abstract fun setExampleText(text: String)
}