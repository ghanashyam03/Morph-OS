package com.morphos.app.core.domain.usecase

import com.morphos.app.core.common.AppResult
import kotlinx.coroutines.flow.Flow

abstract class FlowUseCase<in P, out R> {
    abstract operator fun invoke(params: P): Flow<AppResult<R>>
}
