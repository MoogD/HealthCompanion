package com.dom.healthcompanion.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dom.healthcompanion.ui.breathing.BreathingScreen
import com.dom.healthcompanion.ui.breathing.BreathingViewModel
import com.dom.healthcompanion.ui.featureList.FeatureListScreen
import com.dom.healthcompanion.ui.featureList.FeatureListViewModel
import com.dom.logger.Logger
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun NavigationComponent(
    navController: NavHostController,
    navigator: Navigator,
    logger: Logger,
) {
    // match lifecycle of NavigationComponent
    LaunchedEffect(true) {
        navigator.navTarget.onEach {
            logger.d("navigate to ${it.navName}")
            navController.navigate(it.navName)
        }.launchIn(this)
    }
    NavHost(navController = navController, startDestination = NavItem.FEATURE_LIST.navName) {
        composable(NavItem.FEATURE_LIST.navName) {
            val featureListViewModel: FeatureListViewModel = hiltViewModel()
            FeatureListScreen(featureListViewModel.featureItems)
        }
        composable(NavItem.BREATHING.navName) {
            val breathingViewModel: BreathingViewModel = hiltViewModel()
            BreathingScreen(
                breathingViewModel.titleFlow,
                breathingViewModel.timerStateFlow,
                breathingViewModel.buttonStateFlow,
                breathingViewModel::onStopClicked,
                logger,
            )
        }
    }
}
