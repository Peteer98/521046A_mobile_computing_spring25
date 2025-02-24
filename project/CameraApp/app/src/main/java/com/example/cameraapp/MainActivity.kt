package com.example.cameraapp

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val REQUEST_PERMISSION_CODE = 200

    private lateinit var photoView: ImageView
    private lateinit var videoView: VideoView
    private lateinit var takePhotoButton: Button
    private lateinit var playVideoButton: Button
    private lateinit var recordButton: Button
    private lateinit var pickPhotoButton: Button // New button to pick an image

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        photoView = findViewById(R.id.photoView)
        videoView = findViewById(R.id.videoView)
        takePhotoButton = findViewById(R.id.takePhotoButton)
        playVideoButton = findViewById(R.id.playVideoButton)
        recordButton = findViewById(R.id.recordButton)
        pickPhotoButton = findViewById(R.id.pickPhotoButton) // New button

        // Request necessary permissions
        requestPermissions()

        // Take a photo
        takePhotoButton.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePhotoLauncher.launch(cameraIntent)
        }

        // Record a video
        recordButton.setOnClickListener {
            val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            recordVideoLauncher.launch(videoIntent)
        }

        // Pick a video from storage
        playVideoButton.setOnClickListener {
            pickVideoLauncher.launch("video/*")
        }

        // Pick a photo from storage
        pickPhotoButton.setOnClickListener {
            pickPhotoLauncher.launch("image/*")
        }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (!permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CODE)
        }
    }

    // Photo capture launcher
    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val photoBitmap = result.data?.extras?.get("data") as Bitmap?
            photoBitmap?.let {
                saveImageToStorage(it) // Save to storage
                showPhoto(it) // Display in ImageView
            }
        }
    }

    // Video recording launcher
    private val recordVideoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val videoUri: Uri? = result.data?.data
            videoUri?.let {
                playVideo(it)
            }
        }
    }

    // Video picker launcher
    private val pickVideoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            playVideo(it)
        }
    }

    // Photo picker launcher
    private val pickPhotoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoView.setImageURI(it)
        }
    }

    private fun showPhoto(bitmap: Bitmap) {
        videoView.visibility = View.GONE
        photoView.visibility = View.VISIBLE
        photoView.setImageBitmap(bitmap)
    }

    private fun playVideo(uri: Uri) {
        photoView.visibility = View.GONE
        videoView.visibility = View.VISIBLE
        videoView.setVideoURI(uri)
        videoView.start()
    }

    // Save captured image to storage
    private fun saveImageToStorage(bitmap: Bitmap) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyCameraApp")
        }

        val uri: Uri? = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            try {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
                Toast.makeText(this, "Photo saved!", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to save photo!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
