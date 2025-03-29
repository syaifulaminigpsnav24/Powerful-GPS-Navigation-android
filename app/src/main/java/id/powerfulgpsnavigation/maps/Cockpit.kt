package id.powerfulgpsnavigation.maps

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Cockpit : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private lateinit var tiltUpAndDown: TextView
    private lateinit var tiltRightAndLeft: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cockpit)
    }

    override fun onSensorChanged(event: SensorEvent?) {

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}