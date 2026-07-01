package com.morphos.app.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

private val Context.userPrefsDataStore by preferencesDataStore(name = "user_preferences")
private val Context.memProfileDataStore by preferencesDataStore(name = "memory_profile")

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    @Named("user_preferences")
    fun provideUserPreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.userPrefsDataStore
    }

    @Provides
    @Singleton
    @Named("memory_profile")
    fun provideMemoryProfileDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.memProfileDataStore
    }
}
