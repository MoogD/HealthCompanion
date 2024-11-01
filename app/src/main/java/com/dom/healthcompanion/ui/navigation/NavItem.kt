package com.dom.healthcompanion.ui.navigation

import com.dom.healthcompanion.R
import com.dom.healthcompanion.utils.TextString

enum class NavItem(val navName: String, val title: TextString) {
    FEATURE_LIST("featureList", TextString.Res(R.string.feature_list_screen_title)),
    BREATHING("breathing", TextString.Res(R.string.breathing_screen_title)), ;

    companion object {
        fun fromNavName(navName: String) =
            entries.firstOrNull { it.navName == navName }
    }
}
