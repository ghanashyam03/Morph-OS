package com.morphos.app.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        TODO("Not yet implemented")
    }

    @Provides
    @Singleton
    fun provideMemoryProfileDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        TODO("Not yet implemented")
    }
}
