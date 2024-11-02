package com.dom.healthcompanion.ui.main

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.dom.healthcompanion.R
import com.dom.healthcompanion.utils.IconState
import com.dom.healthcompanion.utils.TextString
import com.dom.healthcompanion.utils.getAsString
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    topAppBarTextFlow: Flow<TextString>,
    iconStateFlow: Flow<IconState>,
) {
    val title = topAppBarTextFlow.collectAsState(initial = TextString.Res(R.string.app_name)).value
    val iconState = iconStateFlow.collectAsState(initial = IconState(false, IconState.Type.BACK) {}).value
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title.getAsString(),
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .testTag("topBarTitle")
                        .wrapContentWidth()
                        .wrapContentSize(Alignment.Center),
            )
        },
        navigationIcon = {
            if (iconState.isVisible) {
                IconButton(
                    onClick = iconState.onClick,
                ) {
                    Icon(
                        iconState.iconType.vector,
                        contentDescription = "topBarIcon",
                        modifier = Modifier.testTag(iconState.iconType.vector.toString()),
                    )
                }
            }
        },
    )
}
