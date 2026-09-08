package com.example.arrender;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Handler;
import android.os.HandlerThread;
import android.widget.Toast;

import android.graphics.SurfaceTexture;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class CameraModule {
    static String TAG = "CameraModule";
    private final Context appContext;
    private final CameraManager cameraManager;

    private CameraDevice cameraDevice;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private boolean isFrontCamera = false;

    private SurfaceTexture mSurfaceTexture;

    public CameraModule (Context context) {
        this.appContext = context;
        this.cameraManager = (CameraManager) appContext.getSystemService(Context.CAMERA_SERVICE);
    }

    public void startCamera() {
        startBackgroundThread();

        int facing = isFrontCamera ?
                CameraCharacteristics.LENS_FACING_FRONT :
                CameraCharacteristics.LENS_FACING_BACK;

        String cameraId = getCameraId(facing);
        if (cameraId != null) {
            try {
                if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this.appContext, "카메라 사용을 위해 접근 권한 허용이 필요합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                cameraManager.openCamera(cameraId, stateCallback, backgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
            // TODO: 미리보기 세션 시작
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
//            closeCamera();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
//            closeCamera();
        }
    };

    private String getCameraId(int facing) {
        try {
            for (String id : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (lensFacing != null && lensFacing == facing) {
                    return id;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraModuleThread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void createCameraPreview() {
        // TODO: 카메라 이미지 캡쳐 >> 이미지 버퍼에 담기 >> 이미지 버퍼 GL에서 읽기 >> 읽은 이미지 처리하기 >> 결과 값 view에 표츌하기
        //        mSurfaceTexture = new SurfaceTexture()
    }
}
