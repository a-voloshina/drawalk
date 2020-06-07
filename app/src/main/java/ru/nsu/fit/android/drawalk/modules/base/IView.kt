package ru.nsu.fit.android.drawalk.modules.base

interface IView<T: IPresenter> {
    fun showError(cause: Throwable)
}