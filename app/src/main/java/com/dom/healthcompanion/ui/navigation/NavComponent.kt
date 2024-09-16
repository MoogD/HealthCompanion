package com.dom.healthcompanion.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dom.healthcompanion.ui.featureList.FeatureListScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun NavigationComponent(
    navController: NavHostController,
    navigator: Navigator,
) {
    LaunchedEffect("navigation") {
        navigator.navTarget.onEach {
            navController.navigate(it.navName)
        }.launchIn(this)
    }
    NavHost(navController = navController, startDestination = NavItem.FEATURE_LIST.navName) {
        composable(NavItem.FEATURE_LIST.navName) { FeatureListScreen() }
    }
}
