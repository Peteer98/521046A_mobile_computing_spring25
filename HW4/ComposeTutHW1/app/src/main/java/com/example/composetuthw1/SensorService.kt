    package com.example.composetuthw1

    import android.app.*
    import android.content.Context
    import android.content.Intent
    import android.hardware.Sensor
    import android.hardware.SensorEvent
    import android.hardware.SensorEventListener
    import android.hardware.SensorManager
    import android.os.IBinder
    import android.util.Log
    import androidx.core.app.NotificationCompat

    class SensorService : Service(), SensorEventListener {
        private var lastX = 0f
        private var lastY = 0f
        private var lastZ = 0f

        private lateinit var sensorManager: SensorManager
        private var accelerometer: Sensor? = null

        override fun onCreate() {
            super.onCreate()
            Log.d("SensorService", "Service created")
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
                Log.d("SensorService", "Accelerometer registered")
            } else {
                Log.d("SensorService", "No accelerometer found")
            }


            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

            val notification = NotificationCompat.Builder(this, "sensor_channel")
                .setContentTitle("Sensor Active")
                .setContentText("Monitoring accelerometer changes.")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()

            startForeground(1, notification)
            Log.d("SensorService", "Foreground service started")
        }

        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]

                // Define a threshold for significant movement
                val threshold = 0.2f

                if (Math.abs(x - lastX) > threshold || Math.abs(y - lastY) > threshold || Math.abs(z - lastZ) > threshold) {
                    sendNotification("Accel Data: x=$x, y=$y, z=$z")

                    // Update last known values
                    lastX = x
                    lastY = y
                    lastZ = z
                }
            }
        }


        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        private fun sendNotification(message: String) {
            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val notification = NotificationCompat.Builder(this, "sensor_channel")
                .setContentTitle("Sensor Update")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val manager = getSystemService(NotificationManager::class.java)
            manager.notify(2, notification)
        }

        override fun onBind(intent: Intent?): IBinder? {
            return null
        }
    }
