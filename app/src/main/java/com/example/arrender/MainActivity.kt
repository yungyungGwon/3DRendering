package com.example.arrender

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.arrender.databinding.ActivityMainBinding

/**
 * This class is main activity and gl renderer
 */
class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"                    // Log label
    private val PERMISSION_REQUEST_CAMERA = 1000        // Camera permission request code
    private var isRequested = false                     // Notice toast message just once
    private lateinit var binding: ActivityMainBinding   // View binding
    private lateinit var cameraModule: CameraModule     // Camera module

    /**
     * Called when the activity is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraModule = CameraModule(this)
        binding.renderView.setOnSurfaceReadyListener { surface ->
            cameraModule.setSurface(surface)
            cameraModule.startCamera()
        }
    }

    /**
     * Called when the activity is resumed.
     */
    override fun onResume() {
        super.onResume()
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraModule.startCamera()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
        }
    }

    /**
     * Called when the activity is paused.
     */
    override fun onPause() {
        super.onPause()
    }

    /**
     * Called when the permission request result is returned.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraModule.startCamera()
            } else {
                if(!isRequested) {
                    Toast.makeText(this, "카메라 사용을 위해 접근 권한 허용이 필요합니다.", Toast.LENGTH_SHORT).show()
                    Log.w(TAG, "카메라 사용을 위해 접근 권한 허용이 필요합니다.");
                    isRequested = true
                }
            }
        }
    }
}