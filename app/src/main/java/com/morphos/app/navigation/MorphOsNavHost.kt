package com.morphos.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.morphos.app.core.domain.repository.SettingsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.Serializable

sealed interface MorphOsRoute {
    @Serializable object Onboarding : MorphOsRoute
    @Serializable object Dashboard : MorphOsRoute
    @Serializable object WidgetCreator : MorphOsRoute
    @Serializable data class WidgetEditor(val widgetId: String) : MorphOsRoute
    @Serializable object Settings : MorphOsRoute
    @Serializable object Privacy : MorphOsRoute
    @Serializable object AiSettings : MorphOsRoute
    @Serializable object Permissions : MorphOsRoute
    @Serializable object About : MorphOsRoute
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NavHostEntryPoint {
    fun settingsRepository(): SettingsRepository
}

@Composable
fun MorphOsNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    val settingsRepository = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            NavHostEntryPoint::class.java
        ).settingsRepository()
    }

    var startDestination by remember { mutableStateOf<MorphOsRoute?>(null) }

    LaunchedEffect(Unit) {
        val completed = settingsRepository.isOnboardingComplete()
        startDestination = if (completed) MorphOsRoute.Dashboard else MorphOsRoute.Onboarding
    }

    if (startDestination == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {
        composable<MorphOsRoute.Onboarding> {
            com.morphos.app.feature.onboarding.OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(MorphOsRoute.Dashboard) {
                        popUpTo(MorphOsRoute.Onboarding) { inclusive = true }
                    }
                }
            )
        }
        composable<MorphOsRoute.Dashboard> {
            com.morphos.app.feature.dashboard.DashboardScreen(
                onNavigateToSettings = { navController.navigate(MorphOsRoute.Settings) },
                onNavigateToWidgetCreator = { navController.navigate(MorphOsRoute.WidgetCreator) },
                onNavigateToWidgetEditor = { widgetId ->
                    navController.navigate(MorphOsRoute.WidgetEditor(widgetId))
                }
            )
        }
        composable<MorphOsRoute.WidgetCreator> {
            com.morphos.app.feature.widgetcreator.WidgetCreatorScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<MorphOsRoute.WidgetEditor> {
            // WidgetCreatorScreen handles editor logic when editor mode is supported or popBackStack fallback
            com.morphos.app.feature.widgetcreator.WidgetCreatorScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<MorphOsRoute.Settings> {
            com.morphos.app.feature.settings.SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPrivacy = { navController.navigate(MorphOsRoute.Privacy) },
                onNavigateToAiSettings = { navController.navigate(MorphOsRoute.AiSettings) },
                onNavigateToPermissions = { navController.navigate(MorphOsRoute.Permissions) }
            )
        }
        composable<MorphOsRoute.Privacy> {
            com.morphos.app.feature.settings.PrivacyScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<MorphOsRoute.AiSettings> {
            com.morphos.app.feature.settings.AiSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<MorphOsRoute.Permissions> {
            com.morphos.app.feature.settings.PermissionsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
