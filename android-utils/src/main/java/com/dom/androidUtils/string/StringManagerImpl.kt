package com.dom.androidUtils.string

import android.content.Context

class StringManagerImpl(private val context: Context) : StringManager {
    override fun getString(resId: Int): String {
        return context.getString(resId)
    }
}
