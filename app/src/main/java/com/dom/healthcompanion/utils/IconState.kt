package com.dom.healthcompanion.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.ui.graphics.vector.ImageVector

data class IconState(val isVisible: Boolean, val iconType: Type, val onClick: () -> Unit) {
    enum class Type(val vector: ImageVector) {
        BACK(Icons.AutoMirrored.Filled.ArrowBack),
        LIST(Icons.AutoMirrored.Filled.List),
    }
}
