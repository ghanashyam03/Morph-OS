package com.morphos.app.core.testing

import com.morphos.app.core.common.AppDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
object TestAppDispatchers {
    fun create(testDispatcher: TestDispatcher = StandardTestDispatcher()): AppDispatchers {
        return AppDispatchers(
            main = testDispatcher,
            io = testDispatcher,
            default = testDispatcher,
            unconfined = testDispatcher
        )
    }
}
