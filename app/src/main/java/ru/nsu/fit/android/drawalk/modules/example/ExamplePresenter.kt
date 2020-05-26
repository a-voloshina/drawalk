package ru.nsu.fit.android.drawalk.modules.example

class ExamplePresenter(private val view: IExampleFragment) : IExamplePresenter {
    private val cancellableStub = CalcStub()

    override fun calcWithCancel(text: String) {
        cancellableStub
            .request(text)
            .onComplete {
                view.setExampleText(it)
            }.onError {
                view.setExampleText("ERROR ON CANCELLABLE")
            }.execute()
    }

    override fun calcWithoutCancel(text: String) {
        CalcStub()
            .request(text)
            .onComplete {
                view.setExampleText(it)
            }.onError {
                view.setExampleText("ERROR ON NOT CANCELLABLE")
            }.execute()
    }

    override fun start() {}
}