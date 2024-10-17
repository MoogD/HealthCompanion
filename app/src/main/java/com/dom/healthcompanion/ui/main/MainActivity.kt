package com.dom.healthcompanion.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.dom.healthcompanion.R
import com.dom.healthcompanion.ui.navigation.NavigationComponent
import com.dom.healthcompanion.ui.navigation.Navigator
import com.dom.healthcompanion.ui.theme.HealthCompanionTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HealthCompanionTheme {
                Column {
                    AppTopBar(stringResource(R.string.app_name)) { }
                    NavigationComponent(navController = rememberNavController(), navigator)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HealthCompanionTheme {
//        Greeting("Android")
    }
}
