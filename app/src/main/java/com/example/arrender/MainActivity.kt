package com.example.arrender

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CAMERA = 1000
    private lateinit var mCameraModule: CameraModule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mCameraModule = CameraModule(this);
    }

    override fun onResume() {
        super.onResume()
        // 권한 확인 후 카메라 시작
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            mCameraModule.startCamera()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mCameraModule.startCamera()
            } else {
                Toast.makeText(this, "카메라 사용을 위해 접근 권한 허용이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}