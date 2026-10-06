package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.LeadSource
import com.example.data.model.LeadStage
import com.example.data.model.UserRole

class GQTypeConverters {
    @TypeConverter
    fun fromUserRole(value: UserRole?): String = value?.name ?: UserRole.EXECUTIVE.name

    @TypeConverter
    fun toUserRole(value: String?): UserRole =
        value?.let { runCatching { UserRole.valueOf(it) }.getOrNull() } ?: UserRole.EXECUTIVE

    @TypeConverter
    fun fromLeadSource(value: LeadSource?): String = value?.name ?: LeadSource.OTHER.name

    @TypeConverter
    fun toLeadSource(value: String?): LeadSource =
        value?.let { runCatching { LeadSource.valueOf(it) }.getOrNull() } ?: LeadSource.OTHER

    @TypeConverter
    fun fromLeadStage(value: LeadStage?): String = value?.name ?: LeadStage.NEW.name

    @TypeConverter
    fun toLeadStage(value: String?): LeadStage =
        value?.let { runCatching { LeadStage.valueOf(it) }.getOrNull() } ?: LeadStage.NEW
}
