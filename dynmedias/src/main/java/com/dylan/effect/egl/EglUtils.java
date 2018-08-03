package com.dylan.effect.egl;

import android.content.res.Resources;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

public final class EglUtils {
    private static String readText(Resources mRes, String path) {
        StringBuilder result = new StringBuilder();
        try {
            InputStream is = mRes.getAssets().open(path);
            byte[] buffer = new byte[1024];
            int read = is.read(buffer);
            while (read > 0) {
                result.append(new String(buffer, 0, read));
                read = is.read(buffer);
            }
        } catch (Exception e) {
            return null;
        }
        return result.toString().replaceAll("\\r\\n", "\n");
    }
    private static int loadShader(int shaderType, String source) {
        if (source == null) {
            Log.e("dylan", "Shader source ==null : shaderType =" + shaderType);
            return 0;
        }
        int shader = GLES20.glCreateShader(shaderType);
        if (shader == 0) return shader;
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e("dylan", "Could not compile shader:" + shaderType);
            Log.e("dylan", "GLES20 Error:" + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    public static int createGLProgram(String vertexSource, String fragmentSource) {
        int vertex = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertex == 0) return 0;
        int fragment = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (fragment == 0) return 0;
        int program = GLES20.glCreateProgram();
        if (program == 0) return program;
        GLES20.glAttachShader(program, vertex);
        GLES20.glAttachShader(program, fragment);
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e("dylan", "Could not link program:" + GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }
    public static int createGLProgramFromAssets(Resources res, String vertex, String fragment) {
        return createGLProgram(readText(res, vertex), readText(res, fragment));
    }

    public static int createTextureID(boolean isOes) {
        int target = GLES20.GL_TEXTURE_2D;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            target = isOes ? GLES11Ext.GL_TEXTURE_EXTERNAL_OES : GLES20.GL_TEXTURE_2D;
        }
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(target, texture[0]);
        GLES20.glTexParameterf(target, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(target, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(target, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(target, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
    }
}
