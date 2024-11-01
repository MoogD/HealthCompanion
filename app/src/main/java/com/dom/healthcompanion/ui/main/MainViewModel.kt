package com.dom.healthcompanion.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dom.healthcompanion.R
import com.dom.healthcompanion.ui.ScreenEvent
import com.dom.healthcompanion.ui.navigation.NavItem
import com.dom.healthcompanion.utils.IconState
import com.dom.healthcompanion.utils.TextString
import com.dom.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel
    @Inject
    constructor(private val logger: Logger) : ViewModel() {
        private val _topBarTitleFlow = MutableStateFlow<TextString>(TextString.Res(R.string.app_name))
        val topBarTitleFlow: StateFlow<TextString>
            get() = _topBarTitleFlow
        private val _topBarIconFlow = MutableStateFlow(IconState(false, IconState.Type.Back, ::onBackPressed))
        val topBarIconFlow: StateFlow<IconState>
            get() = _topBarIconFlow

        // TODO: Add re-usable implementation for screenEventHandling
        private val screenEventChannel = Channel<ScreenEvent>(Channel.BUFFERED)
        val screenEventFlow = screenEventChannel.receiveAsFlow()

        private fun onBackPressed() {
            sendEvent(MainScreenEvent.OnBackPressed)
        }

        fun onNavigationDestinationChanged(destination: NavItem) {
            _topBarTitleFlow.value = destination.title
            _topBarIconFlow.value = _topBarIconFlow.value.copy(isVisible = destination != NavItem.FEATURE_LIST)
            logger.d("updateToolbarTitle for ${destination.navName} with icon visibility: ${destination != NavItem.FEATURE_LIST}")
        }

        private fun sendEvent(event: ScreenEvent) {
            viewModelScope.launch {
                screenEventChannel.send(event)
            }
        }
    }
