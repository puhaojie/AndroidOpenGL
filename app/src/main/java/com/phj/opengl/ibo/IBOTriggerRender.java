package com.phj.opengl.ibo;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 描述：采用IBO + glDrawElements 方式绘制三角形
 * Created by PHJ on 2019/7/10.
 */

public class IBOTriggerRender implements GLSurfaceView.Renderer {

    private final static String TAG = IBOTriggerRender.class.getSimpleName();

    // 三维的顶点坐标，有方向的
    private static final float triangleCoords[] = {
            -0.5f, 1f, 0.0f,  // bottom right
            -1f, -1f, 0.0f, // bottom left
            0.5f, 1f, 0.0f, // top
    };


    private static final short indices[] = {
            0,1,2
    };

    // 颜色
    private static final  float colors[] = {0.8f, 0.4f, 0.1f, 0f};

    // 顶点着色器code
    private static final String vertexShaderCode =
            "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = vPosition;" +
            "}";

    // 片元着色器code
    private static final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "    gl_FragColor = vColor;" +
            "}";

    // 顶点buffer
    private FloatBuffer vertexBuffer;
    private ShortBuffer indiceBuffer;
    private int mProgram; //
    private int mPositionHandle; //顶点
    private int mColorHandle; // 颜色
    private int iboId; // IBO的ID

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        // 1、存储顶点坐标
        vertexBuffer = initBuffer(triangleCoords,4);
        ByteBuffer mbb = ByteBuffer.allocateDirect(indices.length * 2);
        // 数组排列用nativeOrder
        mbb.order(ByteOrder.nativeOrder());
        indiceBuffer = mbb.asShortBuffer();
        indiceBuffer.put(indices);
        indiceBuffer.flip();

        // 创建program
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentShaderCode);

        //创建一个空的OpenGLES程序
        mProgram = GLES20.glCreateProgram();

        Log.e(TAG, "onSurfaceCreated: vertexShader="+vertexShader+"  fragmentShader="+fragmentShader+"  mProgram="+mProgram);
        //将顶点着色器加入到程序
        GLES20.glAttachShader(mProgram, vertexShader);
        //将片元着色器加入到程序中
        GLES20.glAttachShader(mProgram, fragmentShader);
        //连接到着色器程序
        GLES20.glLinkProgram(mProgram);

        // 创建VBO
        int[] ibos = new int[1];
        GLES20.glGenBuffers(ibos.length, ibos, 0);

        iboId = ibos[0];
        // 绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, iboId);

        //赋值
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER,indices.length * 2,indiceBuffer,GLES20.GL_STATIC_DRAW);

        //解绑
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // 裁剪大小
        GLES20.glViewport(0, 0, width, height);
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

        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        Log.e(TAG, "onDrawFrame: mPositionHandle="+mPositionHandle+"  mColorHandle="+mColorHandle);
        //设置绘制三角形的颜色
        GLES20.glUniform4fv(mColorHandle, 1, colors, 0);

        // 绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, iboId);
        Log.e(TAG, "onDrawFrame: iboId="+iboId);
        //绘制三角形
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, 3,GLES20.GL_UNSIGNED_SHORT,0);
        // 绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    /**
     * 加载shader
     * @param type 片元、顶点
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
    private static FloatBuffer initBuffer(float[] buffers,int len) {
        // 先初始化buffer,数组的长度*4,因为一个float占4个字节
        ByteBuffer mbb = ByteBuffer.allocateDirect(buffers.length * len);
        // 数组排列用nativeOrder
        mbb.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = mbb.asFloatBuffer();
        floatBuffer.put(buffers);
        floatBuffer.flip();
        return floatBuffer;
    }
}
