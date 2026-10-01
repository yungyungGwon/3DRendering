package com.example.arrender;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Surface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * Manages RenderView operations.
 */
public class RenderView extends GLSurfaceView implements GLSurfaceView.Renderer {
    public BackgroundRenderable backgroundRenderable = new BackgroundRenderable();  // Handles background rendering
    private OnSurfaceReadyListener readyListener;                                   // Listener for Surface readiness

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
    }

    /**
     * Sets the listener to be notified when the Surface is ready.
     *
     * @param listener listener to receive the Surface ready callback
     */
    public void setOnSurfaceReadyListener(OnSurfaceReadyListener listener) {
        this.readyListener = listener;
    }

    /**
     *
     * @param gl10
     */
    @Override
    public void onDrawFrame(GL10 gl10) {

    }

    /**
     *
     * @param gl
     * @param Width
     * @param height
     */
    public void onSurfaceChanged(GL10 gl, int Width, int height) {
        // Todo: 카메라 캘리브레이션 값이랑 서페이스 값 활용해서 가로 세로 비융 지정하고, 서페이스 회전하기 위한 코드
    }

    /**
     *
     * @param gl10
     * @param eglConfig
     */
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        backgroundRenderable.initialize();
        if (readyListener != null) {
            readyListener.onSurfaceReady(backgroundRenderable.getSurface());
        }
    }

    /**
     * Interface for separating camera module responsibilities.
     * and notifying the camera when the Surface is ready.
     */
    public interface OnSurfaceReadyListener {
        void onSurfaceReady(Surface surface);
    }
}

