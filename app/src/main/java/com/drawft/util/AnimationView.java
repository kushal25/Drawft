package com.drawft.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class AnimationView extends View {
    private Movie mMovie;
    private long mMovieStart;
    private static final boolean DECODE_STREAM = true;

    public AnimationView(final Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        InputStream is;
        /*is = context.getResources().openRawResource(R.raw.gif_image);
        if (DECODE_STREAM) {
            mMovie = Movie.decodeStream(is);
        } else {
            byte[] array = streamToBytes(is);
            mMovie = Movie.decodeByteArray(array, 0, array.length);
        }*/
    }

    private byte[] streamToBytes(InputStream is) {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = is.read(buffer)) >= 0) {
                os.write(buffer, 0, len);
            }
        } catch (java.io.IOException e) {
        }
        return os.toByteArray();
    }

    public void onDraw(Canvas canvas) {
        long now = android.os.SystemClock.uptimeMillis();
        if (mMovieStart == 0) {
            mMovieStart = now;
        }
        if (mMovie != null) {
            int dur = mMovie.duration();
            if (dur == 0) {
                dur = 1000;
            }
            int relTime = (int) ((now - mMovieStart) % dur);
            mMovie.setTime(relTime);
            canvas.scale((float) getWidth() / (float) mMovie.width(), (float) getHeight() / (float) mMovie.height());
            mMovie.draw(canvas, 0, 0);
            invalidate();
        }
    }
}