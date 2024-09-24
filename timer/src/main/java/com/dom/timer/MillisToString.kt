package com.dom.timer

@Suppress("DefaultLocale")
fun Long.millisToMinutesAndSeconds(): String {
    if (this < 0) return "00:00"
    val minutes = this / 1000 / 60
    val seconds = this / 1000 % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Suppress("DefaultLocale")
fun Long.millisToHoursAndMinutesAndSeconds(): String {
    if (this < 0) return "00:00:00"
    val hours = this / 1000 / 60 / 60
    val minutes = this / 1000 / 60 % 60
    val seconds = this / 1000 % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
