/*
 * Copyright (C) 2012 Neo Visionaries Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.neovisionaries.android.opengl.sample.triangle;


import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import com.neovisionaries.android.opengl.ArrayDrawer;
import com.neovisionaries.android.opengl.Attribute;
import com.neovisionaries.android.opengl.BufferCreator;
import com.neovisionaries.android.opengl.ColorBuffer;
import com.neovisionaries.android.opengl.DrawingMode;
import com.neovisionaries.android.opengl.FragmentShader;
import com.neovisionaries.android.opengl.GLES;
import com.neovisionaries.android.opengl.GLESException;
import com.neovisionaries.android.opengl.GLESRenderer;
import com.neovisionaries.android.opengl.GLESSurfaceView;
import com.neovisionaries.android.opengl.Program;
import com.neovisionaries.android.opengl.Shader;
import com.neovisionaries.android.opengl.Transform;
import com.neovisionaries.android.opengl.Uniform;
import com.neovisionaries.android.opengl.VertexShader;
import com.neovisionaries.android.opengl.Viewport;
import android.graphics.Color;
import android.view.MotionEvent;


public class TriangleRenderer extends GLESRenderer
{
    private Program program;
    private Uniform u_mvp_matrix;
    private Attribute a_position;
    private FloatBuffer positions;
    private ArrayDrawer drawer;
    private Transform projection = new Transform();
    private Transform camera = new Transform();
    private Transform mvp = new Transform();
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float angle;
    private float previous_x;
    private float previous_y;


    @Override
    public void onSurfaceCreated(GLES gles, EGLConfig config) throws GLESException
    {
        // Set the color used to clear the color buffer.
        ColorBuffer.setClearColor(Color.GRAY);

        try
        {
            // Create a vertex shader and a fragment shader.
            Shader<?> vs = new VertexShader(openAssetFile("triangle/triangle.vert"), true);
            Shader<?> fs = new FragmentShader(openAssetFile("triangle/triangle.frag"), true);

            // Make the shaders delete themselves when they are
            // detached from the last program.
            vs.setAutoDeleted(true);
            fs.setAutoDeleted(true);

            // Create a shader program and link it.
            program = new Program(vs, fs).link();
        }
        catch (IOException e)
        {
            throw new GLESException("Failed to create shaders.", e);
        }


        // Create an accessor to the uniform variable 'u_mvp_matrix'.
        u_mvp_matrix = program.getUniform("u_mvp_matrix");

        // Create an accessor to the attribute variable 'a_position'.
        a_position = program.getAttribute("a_position");

        // Create data of triangle vertex positions.
        positions = BufferCreator.createFloatBuffer(new float[]
        {
            //  X, Y, Z
            -0.5f, -0.25f,        0,
             0.5f, -0.25f,        0,
             0.0f,  0.559016994f, 0
        });

        // Create a drawer to draw a triangle.
        drawer = new ArrayDrawer(DrawingMode.TRIANGLES, 0, 3);
    }


    @Override
    public void onSurfaceChanged(GLES gles, int width, int height) throws GLESException
    {
        // Set the viewport.
        Viewport.set(width, height);

        // The aspect ratio of the viewport.
        float ratio = (float)width / height;

        // Compute the projection matrix.
        projection.setFrustum(-ratio, ratio, -1, 1, 3, 7);

        // Compute the camera matrix.
        camera.setLookAt(0, 0, -3, 0, 0, 0, 0, 1, 0);
    }


    @Override
    public void onDrawFrame(GLES gles) throws GLESException
    {
        // Clear the color buffer.
        ColorBuffer.clear();

        // Use the program.
        program.use();

        // Compute the model-view-projection matrix and set the value
        // to the corresponding uniform variable.
        mvp.setIdentity()
           .multiply(projection)
           .multiply(camera)
           .rotate(angle, 0, 0, 1)
           .setTo(u_mvp_matrix);

        // Set the vertex positions to the corresponding attribute variable.
        a_position.setArray3(positions);

        // Draw a triangle.
        drawer.draw();
    }


    @Override
    public boolean onTouchEvent(GLESSurfaceView view, MotionEvent event)
    {
        float x = event.getX();
        float y = event.getY();

        if (event.getAction() == MotionEvent.ACTION_MOVE)
        {
            float dx = x - previous_x;
            float dy = y - previous_y;

            // reverse direction of rotation above the mid-line
            if (y > view.getHeight() / 2)
            {
                dx = dx * -1;
            }

            // reverse direction of rotation to left of the mid-line
            if (x < view.getWidth() / 2)
            {
                dy = dy * -1;
            }

            angle += (dx + dy) * TOUCH_SCALE_FACTOR;

            view.requestRender();
        }

        previous_x = x;
        previous_y = y;

        return true;
    }


    @Override
    public void onPause(GLESSurfaceView view)
    {
        if (program != null)
        {
            program.delete();
            program = null;
        }
    }


    private InputStream openAssetFile(String fileName) throws IOException
    {
        return this.getContext().getAssets().open(fileName);
    }
}
