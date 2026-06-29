package com.morphos.app.core.domain.usecase

import kotlinx.coroutines.flow.Flow

abstract class FlowUseCase<in P, out R> {
    abstract operator fun invoke(parameters: P): Flow<Result<R>>
}
