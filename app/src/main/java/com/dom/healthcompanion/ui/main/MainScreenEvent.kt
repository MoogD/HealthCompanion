package com.dom.healthcompanion.ui.main

import com.dom.healthcompanion.ui.ScreenEvent

sealed class MainScreenEvent : ScreenEvent {
    data object OnBackPressed : MainScreenEvent()
}
