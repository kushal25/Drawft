package com.drawft.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.drawft.R;

import java.util.ArrayList;
import java.util.HashMap;


public class PaintView extends View {

    Context context;
    protected Paint mPaint = new Paint();
    protected Path mPath = new Path();
    public Bitmap mBitmap;
    protected Canvas mCanvas;
    protected int mBkColor = 0;
    int viewW = 0, viewH = 0;
    public float FINGER_WIDTH = 25.0F;
    float drawftTop, drawftBottom, drawftLeft, drawftRight;
    public int curColor = getResources().getColor(R.color.paint_color_green);

    public PaintView(Context paramContext, int w, int h) {
        super(paramContext, null);
        /*if (android.os.Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }*/
        this.context = paramContext;
        this.viewW = w;
        this.viewH = h;
        setFocusable(true);
        setFocusableInTouchMode(true);
        // setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public PaintView(Context paramContext) {
        super(paramContext, null);
        /*if (android.os.Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }*/
        this.context = paramContext;

    }

    /*public void setBitmapSize() {
        this.mBitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888);
        int i = this.mBitmap.getWidth();
    }*/


    public PaintView(Context paramContext, AttributeSet attr) {
        super(paramContext, attr);
    }

    public void setImageDimensions(int paramInt1, int paramInt2) {
        try {
            if (paramInt1 > this.viewW || paramInt2 > this.viewH) {
                this.viewW = paramInt1;
                this.viewH = paramInt2;
                initialize(this.viewW, this.viewH);
            }
        } catch (Exception e) {

        }

    }
    public void initialize(int paramInt1, int paramInt2) {
        try {
            CharSequence text;
            if (paramInt1 > 0 && paramInt2 > 0) {
                this.viewW = paramInt1;
                this.viewH = paramInt2;
            }

            this.mBitmap = Bitmap.createBitmap(this.viewW, this.viewH, Bitmap.Config.ARGB_8888);
            this.mBitmap.eraseColor(getBackgroundColor());
            this.mCanvas = new Canvas();
            this.mCanvas.setBitmap(this.mBitmap);
            this.mPaint = new Paint();

            /*this.mPaint.setStyle(Paint.Style.STROKE);
            this.mPaint.setStrokeWidth(this.FINGER_WIDTH);
            this.mPaint.setAntiAlias(true);
            this.mPaint.setColor(Color.RED);
            this.mPaint.setStrokeCap(Paint.Cap.ROUND);
            this.mPaint.setStrokeJoin(Paint.Join.ROUND);
            this.mPaint.setShadowLayer(2, 0, 0, Color.RED);
            this.mPaint.setFilterBitmap(true);*/


            this.mPaint.setStrokeWidth(this.FINGER_WIDTH * 1f);
            this.mPaint.setAntiAlias(true);
            this.mPaint.setFilterBitmap(true);
            this.mPaint.setDither(true);
            this.mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            this.mPaint.setColor(curColor);
            this.mPaint.setStyle(Paint.Style.STROKE);
            this.mPaint.setStrokeJoin(Paint.Join.ROUND);
            this.mPaint.setStrokeCap(Paint.Cap.ROUND);
            /*this.mPaint.setShadowLayer(6, 0, 0, Color.RED);*/
            /*this.mPaint.setPathEffect(new CornerPathEffect(15));*/
            this.mPath = new Path();
            resetDimensions();

        } catch (OutOfMemoryError localOutOfMemoryError) {
        }
    }


    public void setPaintColor(int color) {

        if (this.mPaint != null) {
            this.mPaint.setColor(color);
            this.curColor = color;
        }
    }

    public int getPaintColor() {
        return this.curColor;
    }


    protected void onDraw(Canvas paramCanvas) {
        if (this.mBitmap != null && this.mPaint != null) {
            paramCanvas.drawBitmap(this.mBitmap, 0, 0, this.mPaint);
        }
    }

    public boolean onTouchEvent(MotionEvent paramMotionEvent) {
        return true;
    }

    public void drawPath(HashMap pathInfo) {
        ArrayList path = (ArrayList) pathInfo.get("path");
        int i = path.size();
        int color = ((Long) pathInfo.get("path_info")).intValue();
        try {
            if (i > 1) {
                this.mPaint.setColor(color);
                float curX = ((Double) path.get(0)).floatValue();
                float curY = ((Double) path.get(1)).floatValue();
                resetDimensions(curX, curY);
                int j = 2;
                for (; ; ) {

                    if ((j + 1) == i) {
                        break;
                    }
                    this.mPath.reset();
                    this.mPath.moveTo(curX, curY);
                    float toX = ((Double) path.get(j)).floatValue();
                    float toY = ((Double) path.get(j + 1)).floatValue();
                    this.mPath.lineTo(toX, toY);
                    this.mPath.close();
                    this.mCanvas.drawPath(this.mPath, this.mPaint);
                    resetDimensions(toX, toY);
                    curX = toX;
                    curY = toY;
                    j = j + 2;
                }
                //invalidate();
            }

        } catch (Exception e) {
        }


    }

    public String getDimensionsString() {
        float w = (this.viewW - (this.drawftRight + this.drawftLeft));
        float h = (this.viewH - (this.drawftBottom + this.drawftTop));
        return w + "-" + h;
    }

    public int[] getIntersectionPoint() {
        int[] coord = {0, 0};
        if (Math.round(this.drawftLeft) <= 0) {
            coord[0] = 0;
        } else {
            coord[0] = Math.round(this.drawftLeft);
        }
        if (Math.round(this.drawftTop) <= 0) {
            coord[1] = 0;
        } else {
            coord[1] = Math.round(this.drawftTop);
        }
        return coord;
    }

    public void resetDimensions() {
        this.drawftTop = 100000.0F;
        this.drawftLeft = 100000.0F;
        this.drawftRight = 100000.0F;
        this.drawftBottom = 100000.0F;
    }

    protected void resetDimensions(Float x, Float y) {
        if (this.drawftLeft > x) {
            this.drawftLeft = x;
        }
        if (this.drawftTop > y) {
            this.drawftTop = y;
        }
        if (this.drawftRight > (this.viewW - x)) {
            this.drawftRight = (this.viewW - x);
        }
        if (this.drawftBottom > (this.viewH - y)) {
            this.drawftBottom = (this.viewH - y);
        }
    }

    public int getBackgroundColor() {
        return this.mBkColor;
    }

    public void empty() {
        if (this.mBitmap != null && !this.mBitmap.isRecycled()) {
            this.mBitmap.eraseColor(getBackgroundColor());

            invalidate();
            resetDimensions();
        }
    }

    public void onDestroy() {
        super.destroyDrawingCache();
        try {
            this.context = null;
            if (mBitmap != null && !mBitmap.isRecycled()) {
                mBitmap.recycle();
                this.mBitmap = null;
            }
            this.mCanvas = null;
            this.mPaint = null;
            this.mPath = null;
        } catch (Exception e) {

        }
    }
}
