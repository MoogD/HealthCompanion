package com.dom.healthcompanion.ui.featureList

import androidx.lifecycle.ViewModel
import com.dom.healthcompanion.R
import com.dom.healthcompanion.ui.navigation.NavItem
import com.dom.healthcompanion.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class FeatureListViewModel
    @Inject
    constructor(private val navigator: Navigator) : ViewModel() {
        private val _featureItems = MutableStateFlow(emptyList<FeatureItem>())
        val featureItems: StateFlow<List<FeatureItem>>
            get() = _featureItems

        init {
            _featureItems.value =
                listOf(
                    FeatureItem(R.string.breathing_screen_title) {
                        navigator.navigateTo(NavItem.BREATHING)
                    },
                )
        }
    }
