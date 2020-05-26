package ru.nsu.fit.android.drawalk.modules.example

import kotlinx.coroutines.delay
import ru.nsu.fit.android.drawalk.common.UseCase

class CalcStub: UseCase<String, String>("") {
    override suspend fun executeOnBackground(): String {
        delay(1000)
        return "Calculation completed: $request"
    }
}