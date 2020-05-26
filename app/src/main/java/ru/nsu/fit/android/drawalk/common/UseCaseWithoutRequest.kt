package ru.nsu.fit.android.drawalk.common

abstract class UseCaseWithoutRequest<RS>: UseCase<Unit, RS>(Unit)