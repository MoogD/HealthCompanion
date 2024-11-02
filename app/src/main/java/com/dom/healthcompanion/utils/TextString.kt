package com.dom.healthcompanion.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class TextString {
    data class Res(val resId: Int) : TextString()

    data class String(val text: kotlin.String) : TextString()
}

@Composable
fun TextString.getAsString(): String =
    when (this) {
        is TextString.Res -> stringResource(id = this.resId)
        is TextString.String -> this.text
    }
