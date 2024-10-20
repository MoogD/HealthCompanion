package com.dom.testUtils

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag

fun ComposeContentTestRule.getProgressIndicatorProgress(): Float {
    val nodes =
        this
            .onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
            .fetchSemanticsNode()
    return nodes.config[SemanticsProperties.ProgressBarRangeInfo].current
}

fun ComposeContentTestRule.assertCorrectTextShown(
    text: String,
    testTag: String,
) {
    this
        .onNodeWithTag(testTag)
        .assertTextEquals(text)
}

fun ComposeContentTestRule.assertCountForTag(
    count: Int,
    testTag: String,
) {
    this
        .onAllNodesWithTag(testTag)
        .assertCountEquals(count)
}
