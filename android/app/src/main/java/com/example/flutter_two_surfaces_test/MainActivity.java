package com.example.flutter_two_surfaces_test;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import io.flutter.embedding.android.FlutterSurfaceView;
import io.flutter.embedding.android.FlutterView;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.dart.DartExecutor;

@TargetApi(Build.VERSION_CODES.FROYO)
class GlRenderer implements GLSurfaceView.Renderer {
    private int width = 0;
    private int height = 0;
    private int frame = 0;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;
    }

    private void drawRect(Rect r, int c) {
        GLES20.glViewport(r.left, r.top, r.width(), r.height());
        GLES20.glScissor(r.left, r.top, r.width(), r.height());
        GLES20.glClearColor(Color.red(c) / 255.f, Color.green(c) / 255.f, Color.blue(c) / 255.f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        drawRect(new Rect(0, 0, width, height), Color.WHITE);
        drawRect(new Rect(frame % width, 0, frame % width + 1, height), Color.BLACK);
        drawRect(new Rect(100, 100, 300, 300), frame % 2 == 0 ? Color.RED : Color.BLUE);
        frame++;
    }

    public static GLSurfaceView createView(Context context) {
        GLSurfaceView view = new GLSurfaceView(context);
        view.setEGLContextClientVersion(2);
        view.setEGLConfigChooser(8, 8, 8, 0, 16, 0);

        view.setRenderer(new GlRenderer());
        view.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        return view;
    }
}

@TargetApi(Build.VERSION_CODES.FROYO)
public class MainActivity extends Activity {
    private FlutterEngine flutterEngine;
    private FlutterView flutterView;
    private GLSurfaceView glView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setBackgroundColor(Color.MAGENTA);
        glView = GlRenderer.createView(this);

        flutterEngine = new FlutterEngine(this);
        flutterEngine.getDartExecutor().executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault());
        FlutterSurfaceView flutterSurfaceView = new FlutterSurfaceView(this, true);
        flutterSurfaceView.setZOrderOnTop(true);
        flutterView = new FlutterView(this, flutterSurfaceView);
        flutterView.attachToFlutterEngine(flutterEngine);

        FrameLayout layout = new FrameLayout(this);
        FrameLayout.LayoutParams matchParent = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        layout.addView(glView, matchParent);
        layout.addView(flutterView, matchParent);
        setContentView(layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        flutterEngine.getLifecycleChannel().appIsResumed();
        glView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        flutterEngine.getLifecycleChannel().appIsInactive();
        glView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        flutterEngine.getLifecycleChannel().appIsPaused();
    }

    @Override
    protected void onDestroy() {
        flutterView.detachFromFlutterEngine();
        super.onDestroy();
    }
}
