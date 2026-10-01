package com.example.arrender;

import android.graphics.SurfaceTexture;
import android.opengl.GLES32;
import android.view.Surface;

import java.nio.IntBuffer;

/**
 * Manages the SurfaceTexture and Surface used for background rendering.
 */
public class BackgroundRenderable implements SurfaceTexture.OnFrameAvailableListener {

    private Surface mSurface;
    private SurfaceTexture mSurfaceTexture;
    public int mTextureID = 0;
    static final int textureWidth = 960;
    static final int textureHeight = 720;


    /**
     * Initializes the OpenGL texture used by the SurfaceTexture.
     */
    public void initialize() {
        IntBuffer textureID = IntBuffer.allocate(1);
        GLES32.glGenTextures(1, textureID);
        mTextureID = textureID.get(0);
    }

    /**
     * Creates and returns the SurfaceTexture for camera output.
     *
     * @return SurfaceTexture used for camera output
     */
    public SurfaceTexture getSurfaceTexture() {

        if(mSurfaceTexture == null || mSurfaceTexture.isReleased())
        {
            mSurfaceTexture = new SurfaceTexture(mTextureID);
            mSurfaceTexture.setDefaultBufferSize(textureWidth, textureHeight);
            mSurfaceTexture.setOnFrameAvailableListener(this);
            synchronized(this) {
//                this.isUpdateTexture = false;
            }
        }

        return mSurfaceTexture;
    }

    /**
     * Creates and returns the Surface used for camera output.
     *
     * @return Surface used for camera output
     */
    public Surface getSurface () {
        if(mSurface == null)
            mSurface = new Surface(getSurfaceTexture());

        return mSurface;
    }

    /**
     *
     * @param surfaceTexture
     */
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }
}
