package com.dom.healthcompanion.ui.featureList

import android.util.Log
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
                    FeatureItem(R.string.feature_breathing) {
                        navigator.navigateTo(NavItem.BREATHING)
                        Log.d("FeatureListViewModel", "Breathing clicked!")
                    },
                )
        }
    }
