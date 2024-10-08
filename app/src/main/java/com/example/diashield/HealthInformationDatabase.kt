package com.example.diashield

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Symptoms::class],
    version = 1,
    exportSchema = false
)
abstract class HealthInformationDatabase : RoomDatabase() {
    abstract val dao: SymptomsDao
}