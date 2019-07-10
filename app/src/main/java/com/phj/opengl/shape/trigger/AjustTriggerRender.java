package com.phj.opengl.shape.trigger;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 描述：调节大小的三角形
 * Created by PHJ on 2019/7/10.
 */

public class AjustTriggerRender implements GLSurfaceView.Renderer {

    private final static String TAG = AjustTriggerRender.class.getSimpleName();

    // 三维的顶点坐标，有方向的
    private static final float triangleCoords[] = {
            -0.5f, 1f, 0.0f,  // bottom right
            -1f, -1f, 0.0f, // bottom left
            0.5f, 1f, 0.0f, // top
    };

    // 颜色
    private static final float colors[] = {
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
    };

    // 顶点着色器code
    private static final String vertexShaderCode =
            "attribute vec4 vPosition;" + // 顶点
            "uniform mat4 vMatrix;"+   // 矩阵校正
            "attribute vec4 aColor;" + // 颜色
            "varying vec4 vColor;" +
            "void main() {" +
            "  gl_Position = vMatrix*vPosition;" +
            "  vColor=aColor;" +
            "}";

    // 片元着色器code
    private static final String fragmentShaderCode =
            "precision mediump float;" +
            "varying vec4 vColor;" +
            "void main() {" +
            "    gl_FragColor = vColor;" +
            "}";

    // 顶点buffer
    private FloatBuffer vertexBuffer, colorBuffer;
    private int mProgram; //
    private int mPositionHandle; //顶点
    private int mColorHandle; // 颜色
    private int mMatrixHandle; // 矩阵

    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        // 1、存储顶点坐标
        vertexBuffer = initBuffer(triangleCoords);
        colorBuffer = initBuffer(colors);
        // 创建program
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        //创建一个空的OpenGLES程序
        mProgram = GLES20.glCreateProgram();

        Log.e(TAG, "onSurfaceCreated: vertexShader=" + vertexShader + "  fragmentShader=" + fragmentShader + "  mProgram=" + mProgram);
        //将顶点着色器加入到程序
        GLES20.glAttachShader(mProgram, vertexShader);
        //将片元着色器加入到程序中
        GLES20.glAttachShader(mProgram, fragmentShader);
        //连接到着色器程序
        GLES20.glLinkProgram(mProgram);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // 裁剪大小
        GLES20.glViewport(0, 0, width, height);
        //计算宽高比
        float ratio=(float)width/height;
        //设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glUseProgram(mProgram);
        GLES20.glClearColor(0, 0, 0, 1);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST); // 当我们需要绘制透明图片时，就需要关闭它
        // 填充数据
        //获取顶点着色器的vPosition成员句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        //启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                12, vertexBuffer);

        mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");

        //设置绘制三角形的颜色
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle, 4,
                GLES20.GL_FLOAT, false,
                0, colorBuffer);

        mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        GLES20.glUniformMatrix4fv(mMatrixHandle,1,false,mMVPMatrix,0);

        Log.e(TAG, "onDrawFrame: mPositionHandle=" + mPositionHandle + "  mColorHandle=" + mColorHandle+"  mMatrixHandle="+mMatrixHandle);
        //绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 3);
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    /**
     * 加载shader
     *
     * @param type       片元、顶点
     * @param shaderCode Code
     * @return int
     */
    private int loadShader(int type, String shaderCode) {
        //根据type创建顶点着色器或者片元着色器
        int shader = GLES20.glCreateShader(type);
        //将资源加入到着色器中，并编译
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    // 初始化buffer
    private static FloatBuffer initBuffer(float[] buffers) {
        // 先初始化buffer,数组的长度*4,因为一个float占4个字节
        ByteBuffer mbb = ByteBuffer.allocateDirect(buffers.length * 4);
        // 数组排列用nativeOrder
        mbb.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = mbb.asFloatBuffer();
        floatBuffer.put(buffers);
        floatBuffer.flip();
        return floatBuffer;
    }
}

