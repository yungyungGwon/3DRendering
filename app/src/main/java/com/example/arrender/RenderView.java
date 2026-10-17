package com.example.arrender;

import android.content.Context;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * Manages RenderView operations
 */
public class RenderView extends GLSurfaceView implements GLSurfaceView.Renderer {
    public BackgroundRenderable backgroundRenderable = new BackgroundRenderable();  // Handles background rendering
    private CameraController cameraModule;

    public final String TAG = "RenderView";

    /**
     * Initializes the render view
     * @param context Activity context
     * @param attrs Attribute set
     */
    public RenderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setEGLContextClientVersion(3);
        getHolder().addCallback(this);
        setRenderer(this);
        cameraModule = new CameraController(context);
    }

    /**
     *
     * @param gl10
     * @param eglConfig
     */
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        backgroundRenderable.initialize();
        cameraModule.setSurface(new Surface(backgroundRenderable.getSurfaceTexture()));
        cameraModule.startCamera();
    }

    /**
     *
     * @param gl
     * @param width
     * @param height
     */
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES32.glViewport(0,0, width, height);
        Log.w(TAG, "w:" + width + ", h: " + height);
        // Todo: 카메라 캘리브레이션 값이랑 서페이스 값 활용해서 가로 세로 비융 지정하고, 서페이스 회전하기 위한 코드
    }

    /**
     *
     * @param gl10
     */
    @Override
    public void onDrawFrame(GL10 gl10) {
        backgroundRenderable.render();
    }
}

