package com.example.diashield

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var textViewTitle: TextView
    private lateinit var buttonHeartRate: Button
    private lateinit var buttonRespiratoryRate: Button
    private lateinit var textViewHeartRate: TextView
    private lateinit var textViewRespiratoryRate: TextView
    private lateinit var buttonSymptoms: Button
    private lateinit var buttonUploadSigns: Button

    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private val VIDEO_CAPTURE_REQUEST_CODE = 200

    // SensorManager for respiratory rate measurement
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private val accelValuesX = mutableListOf<Float>()
    private val accelValuesY = mutableListOf<Float>()
    private val accelValuesZ = mutableListOf<Float>()
    private var isCollectingData = false
    private lateinit var countDownTimer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        initializeViews()
        setupWindowInsets()
        initializeSensors()  // Initialize sensors for respiratory rate
    }

    private fun initializeViews() {
        textViewTitle = findViewById(R.id.textView)
        buttonHeartRate = findViewById(R.id.btnHeartRate)
        buttonRespiratoryRate = findViewById(R.id.btnRespiratoryRate)
        textViewHeartRate = findViewById(R.id.tvHeartRate)
        textViewRespiratoryRate = findViewById(R.id.tvRespiratoryRate)
        buttonSymptoms = findViewById(R.id.btnSymptoms)

        buttonHeartRate.setOnClickListener {
            requestCameraPermissionAndStartVideoCapture()
        }

        buttonRespiratoryRate.setOnClickListener {
            startRespiratoryRateMeasurement()
        }

        buttonSymptoms.setOnClickListener {
            val intent = Intent(this, SymptomsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private fun requestCameraPermissionAndStartVideoCapture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            startVideoCapture()
        }
    }

    private fun startVideoCapture() {
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
            takeVideoIntent.resolveActivity(packageManager)?.also {
                // Attempt to enable flash for all video recordings
                takeVideoIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", false)
                takeVideoIntent.putExtra("android.intent.extra.FLASH_MODE", "torch")

                // Some devices might use different extra keys for flash
                takeVideoIntent.putExtra("camera_flash", "on")
                takeVideoIntent.putExtra("flash.mode", "torch")

                Toast.makeText(this, "Starting video capture with flash. This may not work on all devices.", Toast.LENGTH_LONG).show()
                startActivityForResult(takeVideoIntent, VIDEO_CAPTURE_REQUEST_CODE)
            }
        }
    }

    private fun startRespiratoryRateMeasurement() {
        Toast.makeText(this, "Please lay down and place the phone on your chest for 45 seconds", Toast.LENGTH_LONG).show()
        isCollectingData = true
        accelValuesX.clear()
        accelValuesY.clear()
        accelValuesZ.clear()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

        countDownTimer = object : CountDownTimer(45000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                buttonRespiratoryRate.text = "Measuring... ${secondsRemaining}s"
            }

            override fun onFinish() {
                stopRespiratoryRateMeasurement()
            }
        }.start()
    }

    private fun stopRespiratoryRateMeasurement() {
        isCollectingData = false
        sensorManager.unregisterListener(this)
        buttonRespiratoryRate.text = "Respiratory Rate"

        val respiratoryRate = respiratoryRateCalculator(accelValuesX, accelValuesY, accelValuesZ)
        textViewRespiratoryRate.text = "Respiratory Rate: $respiratoryRate breaths/min"
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVideoCapture()
            } else {
                Toast.makeText(this, "Camera permission is required to measure heart rate", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VIDEO_CAPTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            val videoUri: Uri? = data?.data
            videoUri?.let { uri ->
                Toast.makeText(this, "Processing heart rate, please wait...", Toast.LENGTH_LONG).show()
                CoroutineScope(Dispatchers.Main).launch {
                    val heartRate = heartRateCalculator(uri, contentResolver)
                    textViewHeartRate.text = "Heart Rate: $heartRate bpm"
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (isCollectingData && event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            accelValuesX.add(event.values[0])
            accelValuesY.add(event.values[1])
            accelValuesZ.add(event.values[2])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used in this implementation
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }
}