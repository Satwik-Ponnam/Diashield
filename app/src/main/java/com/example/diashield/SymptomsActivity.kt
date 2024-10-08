package com.example.diashield

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SymptomsActivity : AppCompatActivity() {

    private lateinit var submitButton: Button
    private lateinit var ratingNausea: RatingBar
    private lateinit var ratingHeadache: RatingBar
    private lateinit var ratingDiarrhea: RatingBar
    private lateinit var ratingSoreThroat: RatingBar
    private lateinit var ratingFever: RatingBar
    private lateinit var ratingMuscleAche: RatingBar
    private lateinit var ratingLossOfSmell: RatingBar
    private lateinit var ratingCough: RatingBar
    private lateinit var ratingShortnessOfBreath: RatingBar
    private lateinit var ratingFeelingTired: RatingBar

    private lateinit var database: HealthInformationDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptoms)

        // Initialize database
        database = Room.databaseBuilder(
            applicationContext,
            HealthInformationDatabase::class.java,
            "health_information_db"
        ).build()

        // Initialize RatingBars
        ratingNausea = findViewById(R.id.rating_nausea)
        ratingHeadache = findViewById(R.id.rating_headache)
        ratingDiarrhea = findViewById(R.id.rating_diarrhea)
        ratingSoreThroat = findViewById(R.id.rating_sore_throat)
        ratingFever = findViewById(R.id.rating_fever)
        ratingMuscleAche = findViewById(R.id.rating_muscle_ache)
        ratingLossOfSmell = findViewById(R.id.rating_loss_of_smell)
        ratingCough = findViewById(R.id.rating_cough)
        ratingShortnessOfBreath = findViewById(R.id.rating_shortness_of_breath)
        ratingFeelingTired = findViewById(R.id.rating_feeling_tired)

        submitButton = findViewById(R.id.submitButton)

        submitButton.setOnClickListener {
            saveSymptoms()
        }
    }

    private fun saveSymptoms() {
        val symptoms = Symptoms(
            id = 0, // Room will auto-generate the ID
            nausea = ratingNausea.rating.toInt(),
            headache = ratingHeadache.rating.toInt(),
            diarrhea = ratingDiarrhea.rating.toInt(),
            soreThroat = ratingSoreThroat.rating.toInt(),
            fever = ratingFever.rating.toInt(),
            muscleAche = ratingMuscleAche.rating.toInt(),
            lossOfSmell = ratingLossOfSmell.rating.toInt(),
            cough = ratingCough.rating.toInt(),
            shortnessOfBreath = ratingShortnessOfBreath.rating.toInt(),
            feelingTired = ratingFeelingTired.rating.toInt()
        )

        lifecycleScope.launch(Dispatchers.IO) {
            database.dao.upsertHealthInfo(symptoms)

            launch(Dispatchers.Main) {
                Toast.makeText(this@SymptomsActivity, "Symptoms saved to database", Toast.LENGTH_SHORT).show()

                // Create a map of symptom ratings to pass back to MainActivity
                val symptomRatings = mapOf(
                    "Nausea" to symptoms.nausea,
                    "Headache" to symptoms.headache,
                    "Diarrhea" to symptoms.diarrhea,
                    "Sore Throat" to symptoms.soreThroat,
                    "Fever" to symptoms.fever,
                    "Muscle Ache" to symptoms.muscleAche,
                    "Loss of Smell or Taste" to symptoms.lossOfSmell,
                    "Cough" to symptoms.cough,
                    "Shortness of Breath" to symptoms.shortnessOfBreath,
                    "Feeling Tired" to symptoms.feelingTired
                )

                val resultIntent = Intent()
                resultIntent.putExtra("symptomRatings", HashMap(symptomRatings))
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }
}