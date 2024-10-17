package com.dom.healthcompanion.ui.breathing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.dom.healthcompanion.R
import com.dom.healthcompanion.domain.breathing.model.BreathingExercise
import com.dom.healthcompanion.ui.TestTags
import com.dom.healthcompanion.ui.theme.PurpleGrey80
import com.dom.healthcompanion.utils.ButtonState
import com.dom.healthcompanion.utils.Text
import com.dom.healthcompanion.utils.getAsString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

@Composable
fun BreathingScreen(
    titleFlow: Flow<Text>,
    timerStateFlow: Flow<TimerState>,
    startButtonStateFlow: Flow<ButtonState>,
    onStopClick: () -> Unit,
) {
    val timerState =
        timerStateFlow.collectAsState(
            initial =
                TimerState(
                    BreathingExercise.RoundType.FINISHED,
                    BreathingViewModel.STARTING_TIME_STRING,
                    BreathingViewModel.STARTING_TIME_STRING,
                    0f,
                ),
        )
    val title = titleFlow.collectAsState(initial = Text.TextRes(R.string.breathing_screen_default_title))
    val buttonState = startButtonStateFlow.collectAsState(initial = ButtonState(Text.TextRes(R.string.btnStartText), {}))
    ConstraintLayout(
        modifier =
            Modifier
                .fillMaxSize()
                .background(PurpleGrey80),
    ) {
        val (txtTitle, txtRoundType, txtCurrentTime, progress, roundOverviewList, txtTotal, btnStart, btnStop) = createRefs()
        val bottomGuideline = createGuidelineFromBottom(0.2f)
        val horizontalCenterGuideLine = createGuidelineFromEnd(0.5f)
        val titleString = title.value.getAsString()
        Text(
            text = titleString,
            fontSize = 24.sp,
            modifier =
                Modifier
                    .testTag(TestTags.TITLE_TEXT_TAG)
                    .constrainAs(txtTitle) {
                        top.linkTo(parent.top, margin = 24.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
        )
        Text(
            text = timerState.value.type.name,
            fontSize = 24.sp,
            modifier =
                Modifier
                    .testTag(TestTags.TIMER_TYPE_TEXT_TAG)
                    .constrainAs(txtRoundType) {
                        top.linkTo(txtTitle.top, margin = 48.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
        )
        Text(
            text = timerState.value.currentTimeText,
            fontSize = 24.sp,
            modifier =
                Modifier
                    .testTag(TestTags.TIMER_CURRENT_TIME_TEXT_TAG)
                    .constrainAs(txtCurrentTime) {
                        top.linkTo(txtRoundType.bottom, margin = 96.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
        )
        CircularProgressIndicator(
            progress = { timerState.value.progress },
            color = Color.Green,
            trackColor = Color.Transparent,
            modifier =
                Modifier.constrainAs(progress) {
                    top.linkTo(txtCurrentTime.top, margin = (-36).dp)
                    start.linkTo(txtCurrentTime.start, margin = (-24).dp)
                    end.linkTo(txtCurrentTime.end, margin = (-24).dp)
                    bottom.linkTo(txtCurrentTime.bottom, margin = (36).dp)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                },
        )
        val listState = rememberLazyListState()
        LazyColumn(
            modifier =
                Modifier
                    .constrainAs(roundOverviewList) {
                        top.linkTo(txtCurrentTime.bottom, margin = (96).dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(txtTotal.top, margin = 24.dp)
                        height = Dimension.fillToConstraints
                        width = Dimension.fillToConstraints
                    }
                    .background(Color.Gray),
            state = listState,
        ) {
            items(timerState.value.laps) {
                ListItem(it)
            }
        }
        LaunchedEffect(timerState.value.laps) {
            if (timerState.value.laps.isNotEmpty()) {
                listState.scrollToItem(timerState.value.laps.lastIndex)
            }
        }
        Text(
            text = timerState.value.totalTimeText,
            fontSize = 24.sp,
            modifier =
                Modifier
                    .testTag(TestTags.TIMER_TOTAL_TIME_TEXT_TAG)
                    .constrainAs(txtTotal) {
                        bottom.linkTo(bottomGuideline)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
        )
        Button(
            onClick = buttonState.value.onClick,
            modifier =
                Modifier.constrainAs(btnStart) {
                    top.linkTo(bottomGuideline, margin = 24.dp)
                    end.linkTo(horizontalCenterGuideLine, margin = 24.dp)
                },
        ) {
            Text(buttonState.value.text.getAsString())
        }
        Button(
            onClick = onStopClick,
            modifier =
                Modifier.constrainAs(btnStop) {
                    top.linkTo(bottomGuideline, margin = 24.dp)
                    start.linkTo(horizontalCenterGuideLine, margin = 24.dp)
                },
        ) {
            Text(stringResource(R.string.btnStopText))
        }
    }
}

@Composable
fun ListItem(data: TimerLap) {
    Row {
        Text(
            text = "${data.index}: ${data.time}",
            modifier =
                Modifier
                    .testTag(TestTags.TIMER_LAP_LIST_TEXT_ITEM_TAG)
                    .padding(16.dp, 2.dp),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BreathingScreenPreview() {
    BreathingScreen(
        titleFlow = flowOf(Text.TextRes(R.string.buteyko_breathing_title)),
        timerStateFlow =
            MutableStateFlow(
                TimerState(
                    BreathingExercise.RoundType.NORMAL_BREATHING,
                    BreathingViewModel.STARTING_TIME_STRING,
                    BreathingViewModel.STARTING_TIME_STRING,
                    0.5f,
                ),
            ),
        startButtonStateFlow = flowOf(ButtonState(Text.TextRes(R.string.btnStartText), {})),
        onStopClick = {},
    )
}
