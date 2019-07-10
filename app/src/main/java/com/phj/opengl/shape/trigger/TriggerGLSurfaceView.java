package com.phj.opengl.shape.trigger;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.phj.opengl.R;
import com.phj.opengl.ibo.IBOTriggerRender;
import com.phj.opengl.image.ImageRender;


/**
 * 描述：画三角形的GLSurfaceView
 * Created by PHJ on 2019/7/10.
 */

public class TriggerGLSurfaceView extends GLSurfaceView {

    private GLSurfaceView.Renderer mRender;

    public TriggerGLSurfaceView(Context context) {
        this(context,null);
    }

    public TriggerGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        mRender = new ImageRender(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
        setRenderer(mRender);

        //只有在绘制数据改变时才绘制view，可以防止GLSurfaceView帧重绘
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
