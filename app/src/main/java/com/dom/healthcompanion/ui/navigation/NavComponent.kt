package com.dom.healthcompanion.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dom.healthcompanion.ui.breathing.BreathingScreen
import com.dom.healthcompanion.ui.breathing.BreathingViewModel
import com.dom.healthcompanion.ui.featureList.FeatureListScreen
import com.dom.healthcompanion.ui.featureList.FeatureListViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun NavigationComponent(
    navController: NavHostController,
    navigator: Navigator,
) {
    val featureListViewModel: FeatureListViewModel = viewModel()
    LaunchedEffect("navigation") {
        navigator.navTarget.onEach {
            navController.navigate(it.navName)
        }.launchIn(this)
    }
    NavHost(navController = navController, startDestination = NavItem.FEATURE_LIST.navName) {
        composable(NavItem.FEATURE_LIST.navName) {
            FeatureListScreen(featureListViewModel.featureItems)
        }
        composable(NavItem.BREATHING.navName) {
            val breathingViewModel: BreathingViewModel = viewModel()
            BreathingScreen(
                breathingViewModel.titleFlow,
                breathingViewModel.timerStateFlow,
                breathingViewModel.buttonStateFlow,
//                breathingViewModel::onStopClicked,
                {},
            )
        }
    }
}
