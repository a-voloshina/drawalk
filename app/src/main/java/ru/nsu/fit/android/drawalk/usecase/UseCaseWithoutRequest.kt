package ru.nsu.fit.android.drawalk.usecase

abstract class UseCaseWithoutRequest<RS>: UseCase<Unit, RS>(Unit)