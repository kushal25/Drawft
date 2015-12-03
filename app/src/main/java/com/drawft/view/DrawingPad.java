package com.drawft.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

//import com.groupdrawft.data.DrawingData;

public class DrawingPad extends PaintView {

    Context context;
    public OnTouchEventListener listener;
    private final String TAG = DrawingPad.class.getSimpleName();
    private final int duration = Toast.LENGTH_SHORT;
    private float FINGER_WIDTH = 25.0F;

    protected int mBkColor = 0;
    /* protected Canvas mCanvas;
    public Bitmap mBitmap;
     protected Paint mPaint = new Paint();
     protected Path mPath = new Path();*/
    private boolean mChanged;

    protected ArrayList<ArrayList> __allPaths = new ArrayList<ArrayList>();
    protected ArrayList<Float> __myPathPoints = new ArrayList<Float>();
    float curX;
    float curY;

    /*int viewW = 0, viewH = 0;
    float drawftTop, drawftBottom, drawftLeft, drawftRight;*/
    boolean hasData = false;

    public DrawingPad(Context paramContext, int w, int h) {
        super(paramContext, w, h);
        this.context = paramContext;
        this.viewW = w;
        this.viewH = h;
        //setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public DrawingPad(Context paramContext, AttributeSet attr) {
        super(paramContext, attr);
    }

    public void initialize(int paramInt1, int paramInt2) {
        super.initialize(paramInt1, paramInt2);
    }

    public ArrayList<ArrayList> getPathsData() {
        return this.__allPaths;
    }

    public ArrayList<Float> getCurPathData() {
        return this.__myPathPoints;
    }

    public void setPathsData(ArrayList<ArrayList> pathsData) {
        Log.d(TAG, "Loop is Starting");
        for (ArrayList list : pathsData) {
            for (Object o : list) {
                Log.d(TAG, "Loop is running");
            }
        }
        Log.d(TAG, "Loop is End");

    }

    public void setOnTouchEventListener(OnTouchEventListener paramOnTouchEventListener) {
        this.listener = paramOnTouchEventListener;
    }

    public int getBackgroundColor() {
        return this.mBkColor;
    }


    /*protected void onDraw(Canvas paramCanvas) {
        paramCanvas.drawBitmap(this.mBitmap, 0.0F, 0.0F, this.mPaint);
    }*/

    public boolean onTouchEvent(MotionEvent paramMotionEvent) {

        CharSequence text;
        if (paramMotionEvent.getPointerCount() > 1) {
            return false;
        }
        try {
            float x = paramMotionEvent.getX();
            float y = paramMotionEvent.getY();
            switch (paramMotionEvent.getAction()) {
                case 0:
                    this.setPaintColor(this.curColor);
                    onTouchDown(x, y);
                    this.listener.onActionDown(paramMotionEvent);
                    super.invalidate();
                    break;
                case 1:
                    onTouchUp();
                    this.listener.onActionUp(paramMotionEvent);
                    break;
                case 2:
                    // resetDirtyRect(x, y);
                    int historySize = paramMotionEvent.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = paramMotionEvent.getHistoricalX(i);
                        float historicalY = paramMotionEvent.getHistoricalY(i);
                        //expandDirtyRect(x, y);
                        onTouchMove(historicalX, historicalY);
                    }


                    onTouchMove(x, y);
                    /*invalidate(
                            (int) (dirtyRect.left - HALF_STROKE_WIDTH),
                            (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                            (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                            (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));*/
                    super.invalidate();
                    break;

            }
            return true;
        } catch (Exception localException) {
           /* text = "Error!";
            Toast.makeText(this.context, text, duration).show();*/
        }

        return super.onTouchEvent(paramMotionEvent);

    }

    /*private void expandDirtyRect(float historicalX, float historicalY) {
        if (historicalX < dirtyRect.left) {
            dirtyRect.left = historicalX;
        } else if (historicalX > dirtyRect.right) {
            dirtyRect.right = historicalX;
        }
        if (historicalY < dirtyRect.top) {
            dirtyRect.top = historicalY;
        } else if (historicalY > dirtyRect.bottom) {
            dirtyRect.bottom = historicalY;
        }
    }*/

    /*private void resetDirtyRect(float eventX, float eventY) {

        // The lastTouchX and lastTouchY were set when the ACTION_DOWN
        // motion event occurred.
        dirtyRect.left = Math.min(this.curX, eventX);
        dirtyRect.right = Math.max(this.curX, eventX);
        dirtyRect.top = Math.min(this.curY, eventY);
        dirtyRect.bottom = Math.max(this.curY, eventY);
    }*/

    protected void recordPoints(float paramFloat1, float paramFloat2) {
        if (this.__myPathPoints != null) {
            this.__myPathPoints.add(paramFloat1);
            this.__myPathPoints.add(paramFloat2);
            this.hasData = true;
        }
    }

    //Record left,top,right and bottom dimensions
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

    protected void recordPath() {
        if (this.__allPaths != null && this.__myPathPoints != null) {
            //addToAllPaths();
            this.__allPaths.add(this.__myPathPoints);
            /*this.__myPathPoints.clear();*/
        }
    }


    public void empty() {
        if (this.mBitmap != null && !this.mBitmap.isRecycled()) {
            //mBitmap.recycle();
            this.mBitmap.eraseColor(getBackgroundColor());
            this.__allPaths.clear();
            /*this.mBitmap.recycle();
            this.mBitmap = null;*/
            invalidate();
            this.hasData = false;
            resetDimensions();
        }
    }

    public void clear() {
        if (this.__myPathPoints != null) {
            this.__myPathPoints.clear();
        }
    }

    public boolean hasData() {
        return this.hasData;
    }

    public void drawUndoPath(HashMap pathInfo) {
        /*if (k == l-1) {
            return;
        }*/
        ArrayList path = (ArrayList) pathInfo.get("path");

        //Toast.makeText(this.context, "Size Up List = " + this.__myPathPoints.size(), duration).show();
        int i = path.size();
        try {
            if (i > 1) {
                //this.mPaint = preparePaint((Integer) pathInfo.get("path_info"));
                //if (k != l-1) {
                this.mPaint.setColor((Integer) pathInfo.get("path_info"));
                // }
                float curX = ((Float) path.get(0));
                float curY = ((Float) path.get(1));
                recordPoints(curX, curY);
                resetDimensions(curX, curY);
                int j = 2;
                for (; ; ) {

                    if (j >= i) {
                        this.__myPathPoints.clear();
                        break;
                    }
                    this.mPath.reset();
                    this.mPath.moveTo(curX, curY);
                    float toX = ((Float) path.get(j));
                    float toY = ((Float) path.get(j + 1));
                    this.mPath.lineTo(toX, toY);
                    this.mPath.close();
                    this.mCanvas.drawPath(this.mPath, this.mPaint);
                    recordPoints(toX, toY);
                    resetDimensions(toX, toY);
                    curX = toX;
                    curY = toY;
                    j = j + 2;
                    invalidate();

                }
                // recordUndoPath();
                //invalidate();
                //addToAllPaths();

            }

        } catch (Exception e) {
            this.__myPathPoints.clear();
        }


    }

    protected void onTouchDown(float paramFloat1, float paramFloat2) {
       /* CharSequence text;
        text = "Motion Down!(" + paramFloat1 + "," + paramFloat2 + ")";
        Toast.makeText(this.context, text, duration).show();*/
        /*Log.d(TAG, "Size Up List = " + this.__myPathPoints.size());
        Toast.makeText(this.context, "Size Up List = " + this.__myPathPoints.size(), duration).show();*/
        this.curX = paramFloat1;
        this.curY = paramFloat2;
        this.mPath.reset();
        this.mPath.moveTo(this.curX, this.curY);
        this.mPath.lineTo(paramFloat1, paramFloat2);
        this.mPath.close();
        this.mCanvas.drawPath(this.mPath, this.mPaint);
       /* Log.d(TAG, "Loop is Starting");
        Log.d(TAG, "Loop is Starting");*/
        recordPoints(paramFloat1, paramFloat2);
        resetDimensions(paramFloat1, paramFloat2);
       /* this.mPath.reset();
        this.mPath.moveTo(100, 100);
        this.mPath.lineTo(200, 200);
        this.mPath.close();
        this.mCanvas.drawPath(this.mPath, this.mPaint);
        invalidate();*/
    }

    protected void onTouchMove(float paramFloat1, float paramFloat2) {
        /*CharSequence text;
        text = "Motion move!(" + paramFloat1 + "," + paramFloat2 + ")";
        Toast.makeText(this.context, text, duration).show();*/
        // return;
        this.mPath.reset();
        this.mPath.moveTo(this.curX, this.curY);
        this.mPath.lineTo(paramFloat1, paramFloat2);
        this.mPath.close();
        this.mCanvas.drawPath(this.mPath, this.mPaint);
        this.curX = paramFloat1;
        this.curY = paramFloat2;

        recordPoints(paramFloat1, paramFloat2);
        resetDimensions(paramFloat1, paramFloat2);

    }

    protected void onTouchUp() {
       /* CharSequence text;
        text = "Motion Up!";
        Toast.makeText(this.context, text, duration).show();*/
        recordPath();

    }

    public void onDestroy() {
        super.onDestroy();
        try {
            this.__allPaths = null;
            this.__myPathPoints = null;
        } catch (Exception e) {

        }

    }

    public static abstract interface OnTouchEventListener {
        public abstract void onActionUp(MotionEvent paramMotionEvent);

        public abstract void onActionDown(MotionEvent paramMotionEvent);
    }


}
