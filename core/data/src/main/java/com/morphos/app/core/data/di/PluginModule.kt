package com.morphos.app.core.data.di

import com.morphos.app.core.data.plugin.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class PluginModule {

    @Binds
    @IntoSet
    abstract fun bindCalendarPlugin(plugin: CalendarPlugin): DataPlugin

    @Binds
    @IntoSet
    abstract fun bindWeatherPlugin(plugin: WeatherPlugin): DataPlugin

    @Binds
    @IntoSet
    abstract fun bindBatteryPlugin(plugin: BatteryPlugin): DataPlugin

    @Binds
    @IntoSet
    abstract fun bindStepCountPlugin(plugin: StepCountPlugin): DataPlugin

    @Binds
    @IntoSet
    abstract fun bindNotificationPlugin(plugin: NotificationPlugin): DataPlugin

    @Binds
    @IntoSet
    abstract fun bindClockPlugin(plugin: ClockPlugin): DataPlugin

    @Binds
    @IntoSet
    abstract fun bindCountdownPlugin(plugin: CountdownPlugin): DataPlugin

    @Binds
    @IntoSet
    abstract fun bindNewsHeadlinePlugin(plugin: NewsHeadlinePlugin): DataPlugin

    @Binds
    @IntoSet
    abstract fun bindReminderPlugin(plugin: ReminderPlugin): DataPlugin

    @Binds
    @IntoSet
    abstract fun bindUsageStatsPlugin(plugin: UsageStatsPlugin): DataPlugin
}
