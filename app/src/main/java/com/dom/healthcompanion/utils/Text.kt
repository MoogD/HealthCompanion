package com.dom.healthcompanion.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class Text {
    data class TextRes(val resId: Int) : Text()

    data class TextString(val text: String) : Text()
}

@Composable
fun Text.getAsString(): String =
    when (this) {
        is Text.TextRes -> stringResource(id = this.resId)
        is Text.TextString -> this.text
    }
