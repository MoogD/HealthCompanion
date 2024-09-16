package com.dom.healthcompanion.ui.navigation

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Based on https://proandroiddev.com/jetpack-compose-navigation-architecture-with-viewmodels-1de467f19e1c
 * */

@Singleton
class Navigator
    @Inject
    constructor() {
        private val _navTarget = MutableSharedFlow<NavItem>(extraBufferCapacity = 1)
        val navTarget = _navTarget.asSharedFlow()

        fun navigateTo(navItem: NavItem) {
            _navTarget.tryEmit(navItem)
        }
    }
