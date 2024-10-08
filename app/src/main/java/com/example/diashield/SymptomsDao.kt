package com.example.diashield

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomsDao {

    @Upsert
    suspend fun upsertHealthInfo(healthInformation: Symptoms)

    @Delete
    suspend fun delete(healthInformation: Symptoms)

    @Query("SELECT * FROM symptoms")
    fun getAllHealthInformation() : Flow<List<Symptoms>>

    @Query("DELETE FROM symptoms")
    suspend fun deleteAll()

    @Query("SELECT * FROM symptoms ORDER BY id DESC LIMIT 1")
    suspend fun getLatestHealthInfo(): Symptoms?

    @Query("UPDATE symptoms SET heartRate = :heartRate, respiratoryRate = :respiratoryRate WHERE id = (SELECT id FROM symptoms ORDER BY id DESC LIMIT 1)")
    suspend fun updateLatestVitalSigns(heartRate: Int, respiratoryRate: Int)
}