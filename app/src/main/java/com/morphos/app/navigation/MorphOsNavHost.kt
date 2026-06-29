package com.morphos.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

sealed interface MorphOsRoute {
    @Serializable object Onboarding
    @Serializable object Dashboard
    @Serializable object WidgetCreator
    @Serializable data class WidgetEditor(val widgetId: String)
    @Serializable object Settings
    @Serializable object Privacy
    @Serializable object AiSettings
    @Serializable object Permissions
    @Serializable object About
}

@Composable
fun MorphOsNavHost() {
    val navController = rememberNavController()
    
    val isOnboardingComplete = runBlocking {
        // TODO: Read from userPreferences DataStore in Prompt 2
        false
    }
    
    val startDestination = if (isOnboardingComplete) {
        MorphOsRoute.Dashboard
    } else {
        MorphOsRoute.Onboarding
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
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
                onNavigateToWidgetCreator = { navController.navigate(MorphOsRoute.WidgetCreator) }
            )
        }
        composable<MorphOsRoute.WidgetCreator> {
            com.morphos.app.feature.widgetcreator.WidgetCreatorScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<MorphOsRoute.WidgetEditor> {
            // Stubbed
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
