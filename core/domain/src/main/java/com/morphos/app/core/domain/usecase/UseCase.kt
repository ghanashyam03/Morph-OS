package com.morphos.app.core.domain.usecase

import com.morphos.app.core.common.AppResult

abstract class UseCase<in P, out R> {
    abstract suspend operator fun invoke(params: P): AppResult<R>
}

object NoParams
