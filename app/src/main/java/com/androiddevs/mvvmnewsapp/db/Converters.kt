package com.androiddevs.mvvmnewsapp.db

import androidx.room.TypeConverter
import com.androiddevs.mvvmnewsapp.models.Source

class Converters {
    @TypeConverter
    fun fromSource(source: Source) = source.name
    @TypeConverter
    fun toSource(name: String) = Source(name,name)
}