package com.example.arrender;

import android.opengl.GLES32;
import android.util.Log;

/**
 * Shader Manager
 */
public class ShaderManager {
    public int ID;                              ///< Shader program ID
    private final String TAG = "ShaderManager"; ///< TAG


    /**
     * Shader Manager Constructor
     *
     * @param vertexCode    Vertex code
     * @param fragmentCode  Fragment code
     */
    ShaderManager(String vertexCode, String fragmentCode) {
        int vertex = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);
        GLES32.glShaderSource(vertex, vertexCode);
        GLES32.glCompileShader(vertex);
        checkCompileErrors(vertex, "VERTEX");

        int fragment = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);
        GLES32.glShaderSource(fragment, fragmentCode);
        GLES32.glCompileShader(fragment);
        checkCompileErrors(fragment, "FRAGMENT");

        ID = GLES32.glCreateProgram();
        GLES32.glAttachShader(ID, vertex);
        GLES32.glAttachShader(ID, fragment);

        // 개별적으로 컴파일된 세이더 객체(vertex, fragment etc,)들을 하나로 묶어
        // GPU 가 실제로 실행할 수 있는 '최종 실행 프로그램' 으로 조립하는 함수
        GLES32.glLinkProgram(ID);
        checkCompileErrors(ID,"PROGRAM");

        GLES32.glDeleteShader(vertex);
        GLES32.glDeleteShader(fragment);
    }

    /**
     * Activate this program
     */
    public void use() {
        GLES32.glUseProgram(ID);
    }

    /**
     * Checks whether a shader compiled, or a program linked, successfully,
     * and logs the GL info log if it did not.
     *
     * Shader objects and program objects are validated through two different
     * GL query pairs: glGetShaderiv + GL_COMPILE_STATUS for a shader, versus
     * glGetProgramiv + GL_LINK_STATUS for a program. GL_COMPILE_STATUS is not
     * a valid query for a program object, and GL_LINK_STATUS is not a valid
     * query for a shader object - they only exist on their matching object
     * type, so a single call can't check both at once. Since this method is
     * shared by both the per-shader compile check and the final program link
     * check, the if/else branches on "type" to pick the matching GL query
     * pair for whichever kind of object was actually passed in.
     *
     * @param shader the shader or program object ID to check
     * @param type   "PROGRAM" to validate a linked program; "VERTEX" or
     *               "FRAGMENT" (or any other label) to validate a compiled shader
     */
    private void checkCompileErrors(int shader, String type) {
        int[] compiled = new int[1];
        if(!type.equals("PROGRAM")) {
            // Shader branch: ask the shader object for its GL_COMPILE_STATUS
            GLES32.glGetShaderiv(shader,GLES32.GL_COMPILE_STATUS, compiled, 0);
            if(compiled[0] != GLES32.GL_TRUE) {
                String infoLog = GLES32.glGetShaderInfoLog(shader);
                Log.e(TAG, "ERROR::SHADER_COMPILATION_ERROR of type:" + type + "\n " + infoLog);
            }
        } else {
            // Program branch: ask the linked program for its GL_LINK_STATUS
            GLES32.glGetProgramiv(shader, GLES32.GL_LINK_STATUS, compiled, 0);
            if(compiled[0] != GLES32.GL_TRUE) {
                String infoLog = GLES32.glGetProgramInfoLog(shader);
                Log.e(TAG, "ERROR::PROGRAM_LINKING_ERROR of type: " + type + "\n " + infoLog);
            }
        }
    }
}
