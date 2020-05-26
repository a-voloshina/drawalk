package ru.nsu.fit.android.drawalk.modules.example

import ru.nsu.fit.android.drawalk.modules.base.IPresenter

interface IExamplePresenter: IPresenter {
    fun calcWithCancel(text: String)
    fun calcWithoutCancel(text: String)
}