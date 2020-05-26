package ru.nsu.fit.android.drawalk.common

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlin.coroutines.CoroutineContext

abstract class UseCase<RQ, RS>(var request: RQ) {
    private var parentJob: Job = Job()

    var scope: CoroutineScope = GlobalScope
    var backgroundContext: CoroutineContext = IO
    var foregroundContext: CoroutineContext = Main
    var onComplete: (RS) -> Unit = {}
    var onError: (Throwable) -> Unit = {}

    fun request(rq: RQ) = this.apply {
        request = rq
    }

    fun scope(cs: CoroutineScope) = this.apply {
        scope = cs
    }

    fun backgroundContext(ctx: CoroutineContext) = this.apply {
        backgroundContext = ctx
    }

    fun foregroundContext(ctx: CoroutineContext) = this.apply {
        foregroundContext = ctx
    }

    fun onComplete(callback: (RS) -> Unit) = this.apply {
        onComplete = callback
    }

    fun onError(callback: (Throwable) -> Unit) = this.apply {
        onError = callback
    }

    protected abstract suspend fun executeOnBackground(): RS

    fun execute() {
        parentJob.cancel()
        parentJob = scope.launch(foregroundContext) {
            try {
                val result = withContext(backgroundContext) {
                    executeOnBackground()
                }
                onComplete.invoke(result)
            } catch (e: CancellationException) {
                Log.d("UseCase", "canceled by user")
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun cancel() {
        parentJob.cancel()
    }
}