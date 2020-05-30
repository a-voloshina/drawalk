package ru.nsu.fit.android.drawalk.modules.arts

import kotlinx.coroutines.delay
import ru.nsu.fit.android.drawalk.common.UseCase
import ru.nsu.fit.android.drawalk.model.GpsArt
import java.security.SecureRandom


class LoadData(request: Int): UseCase<Int, List<GpsArt>>(request) {
    private val random = SecureRandom()

    override suspend fun executeOnBackground(): List<GpsArt> {
        val out = mutableListOf<GpsArt>()
        for (i in 0 until request) {
            delay(500) //simulates working
            val contact = GpsArt(genId(), genId(), mutableListOf())
            out.add(contact)
        }
        return out
    }

    private fun genId(): String {
        val low = 100000000
        val high = 999999999
        val randomNumber = random.nextInt(high - low) + low
        return "0$randomNumber"
    }
}