package com.dom.healthcompanion.data.database.breathing

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.dom.healthcompanion.domain.breathing.model.BreathingSummary
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@ProvidedTypeConverter
class BreathingConverter {
    private val listType = object : TypeToken<List<BreathingSummary.BreathingRoundSummary>>() {}.type

    @TypeConverter
    fun listToJson(value: List<BreathingSummary.BreathingRoundSummary>) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String): List<BreathingSummary.BreathingRoundSummary> =
        Gson().fromJson(value, listType)
}
