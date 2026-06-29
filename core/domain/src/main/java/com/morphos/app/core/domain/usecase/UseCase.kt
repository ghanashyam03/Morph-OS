package com.morphos.app.core.domain.usecase

abstract class UseCase<in P, out R> {
    abstract suspend operator fun invoke(parameters: P): Result<R>
}
