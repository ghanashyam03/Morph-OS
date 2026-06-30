package com.morphos.app.core.data.agent

import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.domain.model.NotificationPriority
import com.morphos.app.core.domain.model.PrioritizedNotification
import com.morphos.app.core.domain.repository.NotificationRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class NotificationAgentImplTest {

    @MockK
    lateinit var notificationRepository: NotificationRepository

    private lateinit var agent: NotificationAgentImpl
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testDispatchers = AppDispatchers(
        main = testDispatcher,
        io = testDispatcher,
        default = testDispatcher,
        unconfined = testDispatcher
    )

    private val repoFlow = MutableStateFlow<List<PrioritizedNotification>>(emptyList())

    @BeforeEach
    fun setUp() {
        mockkStatic(Calendar::class)
        every { notificationRepository.getPrioritizedNotifications() } returns repoFlow
        agent = NotificationAgentImpl(notificationRepository, testDispatchers)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    private fun createNotification(
        id: String,
        packageName: String,
        title: String = "Title",
        text: String = "Text",
        priority: NotificationPriority = NotificationPriority.MEDIUM
    ): PrioritizedNotification {
        return PrioritizedNotification(
            id = id,
            packageName = packageName,
            appName = "App",
            title = title,
            text = text,
            timestamp = System.currentTimeMillis(),
            priorityScore = 0.5f,
            priority = priority
        )
    }

    @Test
    fun systemNotification_assignedLowPriority() = runTest {
        val mockCal = mockk<Calendar>()
        every { Calendar.getInstance() } returns mockCal
        every { mockCal.get(Calendar.HOUR_OF_DAY) } returns 12 // daytime

        val notif = createNotification("1", "com.android.settings")
        repoFlow.value = listOf(notif)

        agent.start()

        val list = agent.getPrioritizedNotifications().value
        assertEquals(1, list.size)
        assertEquals(NotificationPriority.LOW, list[0].priority)

        agent.stop()
    }

    @Test
    fun socialNotification_assignedHighPriority_duringActiveHours() = runTest {
        val mockCal = mockk<Calendar>()
        every { Calendar.getInstance() } returns mockCal
        every { mockCal.get(Calendar.HOUR_OF_DAY) } returns 12 // daytime

        val notif = createNotification("1", "com.whatsapp")
        repoFlow.value = listOf(notif)

        agent.start()

        val list = agent.getPrioritizedNotifications().value
        assertEquals(1, list.size)
        assertEquals(NotificationPriority.HIGH, list[0].priority)

        agent.stop()
    }

    @Test
    fun promoNotification_suppressed() = runTest {
        val mockCal = mockk<Calendar>()
        every { Calendar.getInstance() } returns mockCal
        every { mockCal.get(Calendar.HOUR_OF_DAY) } returns 12

        val notif = createNotification("1", "com.shopping", title = "50% off promo code")
        repoFlow.value = listOf(notif)

        agent.start()

        val list = agent.getPrioritizedNotifications().value
        // Promo notifications are suppressed (removed from filtered list)
        assertTrue(list.isEmpty())

        agent.stop()
    }

    @Test
    fun notifications_sortedByPriority() = runTest {
        val mockCal = mockk<Calendar>()
        every { Calendar.getInstance() } returns mockCal
        every { mockCal.get(Calendar.HOUR_OF_DAY) } returns 12

        val notifLow = createNotification("1", "com.android.settings") // will become LOW
        val notifHigh = createNotification("2", "com.whatsapp")       // will become HIGH
        val notifMedium = createNotification("3", "com.other", priority = NotificationPriority.MEDIUM)

        repoFlow.value = listOf(notifLow, notifHigh, notifMedium)

        agent.start()

        val list = agent.getPrioritizedNotifications().value
        assertEquals(3, list.size)
        assertEquals(NotificationPriority.HIGH, list[0].priority)
        assertEquals(NotificationPriority.MEDIUM, list[1].priority)
        assertEquals(NotificationPriority.LOW, list[2].priority)

        agent.stop()
    }
}
