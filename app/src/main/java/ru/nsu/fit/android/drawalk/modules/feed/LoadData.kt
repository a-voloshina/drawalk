package ru.nsu.fit.android.drawalk.modules.feed

import kotlinx.coroutines.delay
import ru.nsu.fit.android.drawalk.common.UseCase
import ru.nsu.fit.android.drawalk.model.GpsArt
import java.security.SecureRandom


class LoadData(request: LoadRequest): UseCase<LoadData.LoadRequest, Unit>(request) {
    private val random = SecureRandom()

    override suspend fun executeOnBackground() {
        val data = request.data
        data.removeAt(data.lastIndex)   //removes "loading" null
        val index: Int = data.size
        val end = index + request.pageSize
        for (i in index until end) {
            delay(500) //simulates working
            val contact = GpsArt(genId(), genId(), mutableListOf())
            data.add(contact)
        }
    }

    private fun genId(): String {
        val low = 100000000
        val high = 999999999
        val randomNumber = random.nextInt(high - low) + low
        return "0$randomNumber"
    }

    data class LoadRequest(
        val data: MutableList<GpsArt?>,
        val pageSize: Int
    )
}