package com.example.diashield
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "symptoms")
data class Symptoms(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val nausea: Int = 0,
    val headache: Int = 0,
    val diarrhea: Int,
    val soreThroat: Int,
    val fever: Int,
    val muscleAche: Int,
    val lossOfSmell: Int,
    val cough: Int,
    val shortnessOfBreath: Int,
    val feelingTired: Int,
    val heartRate: Int? = null,
    val respiratoryRate: Int? = null
)