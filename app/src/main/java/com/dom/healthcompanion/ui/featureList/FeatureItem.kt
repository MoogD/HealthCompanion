package com.dom.healthcompanion.ui.featureList

import androidx.annotation.StringRes

data class FeatureItem(
    @StringRes val textRes: Int,
    val onClick: () -> Unit,
)
