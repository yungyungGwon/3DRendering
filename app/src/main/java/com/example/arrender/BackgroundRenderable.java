package com.example.arrender;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES32;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Manages the SurfaceTexture and Surface used for background rendering.
 */

// Todo: This class need to reflect rotation matrix. Currently we used to default rotation matrix
public class BackgroundRenderable implements SurfaceTexture.OnFrameAvailableListener {

    static final int textureWidth = 960;
    static final int textureHeight = 720;

    /**
     * Initializes the OpenGL texture used by the SurfaceTexture.
     */
    public void initialize() {
        shaderManager = new ShaderManager(vertexShaderCode, fragmentShaderCode);

        // VAO: stores the vertex attribute setup (which buffer/offset/stride each
        // attribute reads from) as a single reusable state object
        IntBuffer vaoBuffer = IntBuffer.allocate(1);
        // VBO: will hold per-vertex data (position + UV)
        IntBuffer vboBuffer = IntBuffer.allocate(1);
        // EBO: will hold the index data describing triangle winding order
        IntBuffer eboBuffer = IntBuffer.allocate(1);

        GLES32.glGenVertexArrays(1, vaoBuffer);
        mVAO = vaoBuffer.get(0);
        GLES32.glBindVertexArray(mVAO);

        GLES32.glGenBuffers(1, vboBuffer); // reserve a buffer object name for vertex data
        GLES32.glGenBuffers(1, eboBuffer); // reserve a buffer object name for index data
        mVBO = vboBuffer.get(0);
        mEBO = eboBuffer.get(0);

        // Interleaved vertex data for a fullscreen quad:
        // first 4 triples are clip-space positions (x, y, z),
        // followed by 4 pairs of UV coordinates (u, v) for those same 4 vertices
        float[] vertices = {
                1.0f,  1.0f, 0.0f,
                1.0f, -1.0f, 0.0f,
                -1.0f, -1.0f, 0.0f,
                -1.0f,  1.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 1.0f,
                0.0f, 0.0f
        };
        // Bind the VBO to GL_ARRAY_BUFFER, then copy the vertex array from CPU
        // memory into GPU memory. This copy only happens here (initialization),
        // not every frame, since this geometry never changes.
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, mVBO);
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, vertices.length * Float.BYTES, FloatBuffer.wrap(vertices), GLES32.GL_DYNAMIC_DRAW);

        // Index data: two triangles (sharing vertices 1 and 3) forming the quad
        int[] indices = new int[] {
                0, 1, 3,
                1, 2, 3
        };
        // Bind the EBO to GL_ELEMENT_ARRAY_BUFFER (the target glDrawElements reads
        // indices from), then copy the index array from CPU memory into GPU memory.
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, mEBO);
        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, indices.length * Integer.BYTES, IntBuffer.wrap(indices), GLES32.GL_DYNAMIC_DRAW);
        mAmountOfIndices = indices.length;


        // Reserve a texture object name. Its image content is NOT uploaded here
        // (no glTexImage2D): for GL_TEXTURE_EXTERNAL_OES, the image storage is
        // owned and filled by the camera pipeline via SurfaceTexture.updateTexImage().
        IntBuffer textureID = IntBuffer.allocate(1);
        GLES32.glGenTextures(1, textureID);

        mTextureID = textureID.get(0);

        // Wrap mode: controls how out-of-[0,1] UV samples are resolved.
        // GL_TEXTURE_EXTERNAL_OES only supports CLAMP_TO_EDGE (GL_REPEAT /
        // GL_MIRRORED_REPEAT are not legal for this target) - clamps to the
        // nearest edge texel instead of tiling.
        GLES32.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_CLAMP_TO_EDGE);
        GLES32.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_CLAMP_TO_EDGE);

        // Filtering: controls how in-between-texel positions are sampled when the
        // texture is minified or magnified. GL_LINEAR blends the nearest texels
        // (bilinear interpolation) for a smooth result instead of GL_NEAREST's
        // blocky look. Mipmap filters are not available here since external OES
        // textures don't support mipmapping.
        GLES32.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR);
        GLES32.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR);

        // Position attribute
        int positionAttribute = GLES32.glGetAttribLocation(shaderManager.ID,"aPos");
        GLES32.glEnableVertexAttribArray(positionAttribute);
        GLES32.glVertexAttribPointer(positionAttribute, 3, GLES32.GL_FLOAT, false, 3 * Float.BYTES, 0);

        // Texture attribute
        int textureAttribute = GLES32.glGetAttribLocation(shaderManager.ID, "aTexCoord");
        GLES32.glEnableVertexAttribArray(textureAttribute);
        GLES32.glVertexAttribPointer(textureAttribute, 2, GLES32.GL_FLOAT, false, 2 * Float.BYTES, 12 * Float.BYTES);

        shaderManager.use();
        GLES32.glBindVertexArray(0);
    }

    /**
     *
     * @param surfaceTexture
     */
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    /**
     * Creates and returns the SurfaceTexture for camera output.
     *
     * @return SurfaceTexture used for camera output
     */
    public SurfaceTexture getSurfaceTexture() {

        if(surfaceTexture == null || surfaceTexture.isReleased())
        {
            surfaceTexture = new SurfaceTexture(mTextureID);
            surfaceTexture.setDefaultBufferSize(textureWidth, textureHeight);
            surfaceTexture.setOnFrameAvailableListener(this);
            synchronized(this) {
//                this.isUpdateTexture = false;
            }
        }

        return surfaceTexture;
    }

    /**
     * Free all buffers and puts surfaceTexture into the abandoned state
     */
    public void release() {
        if(surfaceTexture!= null && surfaceTexture.isReleased())
            surfaceTexture.release();
        if(mTextureID != 0)
            GLES32.glDeleteTextures(1, new int[]{mTextureID}, 0);
    }

    /**
     *
     */
    public void render() {
        if(mTextureID == 0) return;

        if(surfaceTexture != null) {
            surfaceTexture.updateTexImage();
        }

        // GL_TEXTURE0 is for activity
        // So never ever input the mTextureID of background renderable
        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        shaderManager.use();
        GLES32.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);

        // Bind vertex array to opengl es
        GLES32.glBindVertexArray(mVAO);
        // GL_TRIANGLE_STRIP 는 처음 2개 정점 이후, 새 정점 하나가 들어올 때마다 직전 2개와 묶여 삼각형을 만듦
        //  -> 변을 공유하는 "띠" 형태라 인덱스/정점 수를 아낄 수 있음
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, mAmountOfIndices, GLES32.GL_UNSIGNED_INT, 0);
        GLES32.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);
        GLES32.glBindVertexArray(0);
    }

    String vertexShaderCode = "#version 300 es\n" +
            "layout (location = 0) in vec3 aPos;\n" +
            "layout (location = 1) in vec2 aTexCoord;\n" +
            "out vec2 TexCoord;\n" +
            "void main()\n" +
            "{\n" +
            "   TexCoord = aTexCoord;\n" +
            "   gl_Position = vec4(aPos, 1.0);\n" +
            "}";
    String fragmentShaderCode = "#version 300 es\n" +
            "#extension GL_OES_EGL_image_external_essl3 : require\n" +
            "precision mediump float;\n" +
            "uniform samplerExternalOES outTexture;\n" +
            "in vec2 TexCoord;\n" +
            "out vec4 glFragColor;\n" +
            "void main()\n" +
            "{\n" +
            "   glFragColor = texture(outTexture, TexCoord);\n" +
            "}";

    ShaderManager shaderManager;
    private SurfaceTexture surfaceTexture;
    int mVBO = 0;   ///< Vertex Buffer Object ID
    int mVAO = 0;   ///< Vertex Array Object ID
    int mEBO = 0;   ///< Element Buffer Object ID
    public int mTextureID = 0;  ///< TextureID
    int mAmountOfIndices = 0;   ///< Indices data length
}
