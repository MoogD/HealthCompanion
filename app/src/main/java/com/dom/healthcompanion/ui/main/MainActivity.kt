package com.dom.healthcompanion.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.compose.rememberNavController
import com.dom.healthcompanion.ui.ScreenEvent
import com.dom.healthcompanion.ui.navigation.NavItem
import com.dom.healthcompanion.ui.navigation.NavigationComponent
import com.dom.healthcompanion.ui.navigation.Navigator
import com.dom.healthcompanion.ui.theme.HealthCompanionTheme
import com.dom.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var logger: Logger

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HealthCompanionTheme {
                Column {
                    val navController = rememberNavController()
                    AppTopBar(viewModel.topBarTitleFlow, viewModel.topBarIconFlow)
                    NavigationComponent(navController = navController, navigator, logger)
                    navController.addOnDestinationChangedListener { _, destination, _ ->
                        onDestinationChanged(destination)
                    }
                }
            }
        }
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.screenEventFlow.collect {
                    handleScreenEvent(it)
                }
            }
        }
    }

    private fun handleScreenEvent(event: ScreenEvent) {
        when (event) {
            is MainScreenEvent -> handleMainScreenEvent(event)
        }
    }

    private fun handleMainScreenEvent(event: MainScreenEvent) {
        when (event) {
            MainScreenEvent.OnBackPressed -> onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun onDestinationChanged(destination: NavDestination) {
        val navItem = NavItem.fromNavName(destination.route ?: "") ?: return
        viewModel.onNavigationDestinationChanged(navItem)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HealthCompanionTheme {
//        Greeting("Android")
    }
}
