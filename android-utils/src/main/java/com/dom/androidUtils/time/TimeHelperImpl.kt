package com.dom.androidUtils.time

import java.util.Calendar

class TimeHelperImpl : TimeHelper {
    override fun getCurrentTimeMillis(): Long {
        return Calendar.getInstance().timeInMillis
    }
}
