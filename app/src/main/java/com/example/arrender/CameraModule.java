package com.example.arrender;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Range;
import android.view.Surface;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.concurrent.Executor;

/**
 * Manages Camera operations.
 */
public class CameraModule {
    private String TAG = "CameraModule";
    private final Context appContext;
    private final CameraManager cameraManager;

    private CameraDevice cameraDevice;
    private String cameraId;
    private Range<Integer> frameFPS;
    private float focalDistance = 0;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private boolean isFrontCamera = false;
    private CameraCaptureSession cameraCaptureSession;
    private Surface mSurface;
    private boolean isRequested = false;

    /**
     * Initializes the camera module.
     *
     * @param context application context
     */
    public CameraModule (Context context) {
        this.appContext = context;
        this.cameraManager = (CameraManager) appContext.getSystemService(Context.CAMERA_SERVICE);
    }

    /**
     * Starts the camera and begins the preview session.
     */
    public void startCamera() {
        if(mSurface == null) return ;

        try {
            // Get camera ID
            int facing = isFrontCamera ?
                    CameraCharacteristics.LENS_FACING_FRONT :
                    CameraCharacteristics.LENS_FACING_BACK;

            cameraId = getCameraId(facing);

            if(cameraId == null) return;
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            Range<Integer>[] fpsRange = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);

            // Get fps range from camera characteristics
            if(fpsRange == null) return;
            frameFPS = fpsRange[fpsRange.length - 1];
            Float minFocusDistance = characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
            // Consider a legacy hw level
            // If we have a low level device, we can not control detail focal config
            focalDistance = (minFocusDistance != null ? minFocusDistance : 0f) * 0.15f;

            // Check the camera permission
            if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if(!isRequested) {
                    Toast.makeText(appContext, "카메라 사용을 위해 접근 권한 허용이 필요합니다.", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "카메라 사용을 위해 접근 권한 허용이 필요합니다.");
                    isRequested = true;
                }
                return;
            }

            // Open the camera
            cameraManager.openCamera(cameraId, stateCallback, backgroundHandler);

            // Start background thread
            startBackgroundThread();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a capture request for camera preview.
     *
     * @return capture request for the camera preview, or null if creation fails
     */
    private CaptureRequest createCaptureRequest() {
        try {
            int deviceType = CameraDevice.TEMPLATE_PREVIEW;

            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(deviceType);

            builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, frameFPS);
            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, focalDistance);

            builder.addTarget(mSurface);

            return builder.build();
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Handles camera capture session state changes.
     */
    private final CameraCaptureSession.StateCallback sessionStateCallback = new CameraCaptureSession.StateCallback() {

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

        }

        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            cameraCaptureSession = session;

            try {
                session.setRepeatingRequest(createCaptureRequest(), null, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Handles camera device state changes.
     */
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            try {
                ArrayList<OutputConfiguration> outputConfigAll = new ArrayList<>();
                outputConfigAll.add(new OutputConfiguration(mSurface));
                Executor executor = new Executor() {
                    @Override
                    public void execute(Runnable runnable) {
                        runnable.run();
                        //Todo: how to use the execute. we need to check the function

                    }
                };

                SessionConfiguration sessionConfiguration = new SessionConfiguration(SessionConfiguration.SESSION_REGULAR, outputConfigAll, executor, sessionStateCallback);
                cameraDevice.createCaptureSession(sessionConfiguration);

            } catch (CameraAccessException e) {
                throw new RuntimeException(e);
            }
            // TODO: 미리보기 세션 시작
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
        }
    };

    /**
     * Finds the camera ID matching the specified lens facing.
     *
     * @param facing lens facing direction
     * @return matching camera ID, or null if no camera is found
     */
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

    /**
     * Sets the Surface used for camera preview.
     *
     * @param surface Surface for camera output
     */
    public void setSurface(Surface surface) {
        mSurface = surface;
    }

    /**
     * Starts the background thread for camera operations.
     */
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraModuleThread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }
}
