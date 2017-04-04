package com.example.navigationview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by zhuyuqiang on 2017/3/15.
 */

public class CustomNavigationView extends View {

    private Context mContext;
    private int mViewWidth, mViewHeight;
    private int mSourceWidth, mSourceHeight;
    private int BOUNDS_COLOR = Color.RED;
    private int VIEW_DEFAULT_WIDTH = 400;
    private int VIEW_DEFAULT_HEIGHT = 400;
    private int sample = 1;
    private Bitmap mNavigationMap;
    private Paint mBitmapPaint, mBoundsPaint;
    private int mSourceId;
    private int mImageWidth, mImageHeight;
    private float density = 1f;
    private Point mViewPortPosition = new Point();
    private Rect mBoundRect = new Rect();
    private Rect mViewPortRect = null;
    private int mBoundWidth, mBoundHeight;
    private onBoundsChangedListener mListener;

    public interface onBoundsChangedListener {
        void deliveryViewAndBoundsRect(int width, int height, Rect mBoundsRect);
    }

    public CustomNavigationView(Context context) {
        this(context, null);
    }

    public CustomNavigationView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomNavigationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CustomNavigationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Log.i("zyq", "CustomNavigationView:construction");
        initBitmapPaint();
        mContext = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.i("zyq", "CustomNavigationView:onMeasure");
        setMeasuredDimension(getCustomMeasureWidth(widthMeasureSpec), getCustomMeasureHeight(heightMeasureSpec));
        initNavigationMap();
        calculateBoundRect(mViewPortRect);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mNavigationMap != null) {
            Log.i("zyq", "onDraw:mNavigationMap");
            canvas.drawBitmap(mNavigationMap, mViewPortPosition.x, mViewPortPosition.y, mBitmapPaint);
            drawBounds(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        return true;
    }

    public void setBoundsRect(Rect boundsRect) {
        mViewPortRect = boundsRect;
        calculateBoundRect(boundsRect);

    }

    public void setOnBoundsRectChangeListener(onBoundsChangedListener listener) {
        this.mListener = listener;
    }

    private void calculateBoundRect(Rect mViewPortRect) {
        if (mViewWidth > 0 && mViewPortRect != null) {
            mBoundRect.left = (mViewPortRect.left * mImageWidth / mSourceWidth);
            mBoundRect.right = (mViewPortRect.right * mImageWidth / mSourceWidth);
            mBoundRect.top = (mViewPortRect.top * mImageHeight / mSourceHeight);
            mBoundRect.bottom = (mViewPortRect.bottom * mImageHeight / mSourceHeight);
            mBoundWidth = mBoundRect.width();
            mBoundHeight = mBoundRect.height();
            Log.i("zyq_bound", "reset rect:left" + mBoundRect.left + " top=" + mBoundRect.top + " right=" + mBoundRect.right + " bottom=" + mBoundRect.bottom + "\n" +
                    "viewPort:left=" + mViewPortRect.left + " top=" + mViewPortRect.top + " right=" + mViewPortRect.right + " bottom=" + mViewPortRect.bottom);
        }

        invalidate();
    }

    public void setDrawableId(int sourceId) {
        this.mSourceId = sourceId;
    }

    private void initNavigationMap() {
        density = getResources().getDisplayMetrics().density;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), mSourceId, options);
        mSourceWidth = options.outWidth;
        mSourceHeight = options.outHeight;
        while (mSourceWidth / sample > (mViewWidth / density)
                || mSourceHeight / sample > (mViewHeight / density)) {
            sample *= 2;
        }
        Log.i("zyq", "sample =" + sample);
        options.inJustDecodeBounds = false;
        options.inSampleSize = sample;
        mNavigationMap = BitmapFactory.decodeResource(getResources(), mSourceId, options);
        mImageWidth = mNavigationMap.getWidth();
        mImageHeight = mNavigationMap.getHeight();
        mViewPortPosition.x = getPaddingLeft() + (mViewWidth - mImageWidth) / 2;
        mViewPortPosition.y = getPaddingTop() + (mViewHeight - mImageHeight) / 2;
        Log.i("zyq", "mViewWidth=" + mViewWidth + " mViewHeight=" + mViewHeight + " mImageWidth=" + mImageWidth + " mImageHeight=" + mImageHeight);
    }

    private void initBitmapPaint() {
        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setDither(true);
        mBitmapPaint.setFilterBitmap(true);

        mBoundsPaint = new Paint();
        mBoundsPaint.setAntiAlias(true);
        mBoundsPaint.setDither(true);
        mBoundsPaint.setColor(BOUNDS_COLOR);
        mBoundsPaint.setStrokeWidth(2.0f);
        mBoundsPaint.setStyle(Paint.Style.STROKE);
    }

    private int getCustomMeasureWidth(int measureWidth) {
        int mode = MeasureSpec.getMode(measureWidth);
        int size = MeasureSpec.getSize(measureWidth);
        Log.i("zyq", "mode=" + mode + " EXACTLY=" + MeasureSpec.EXACTLY + " UNSPECIFIED=" + MeasureSpec.UNSPECIFIED + " AT_MOST=" + MeasureSpec.AT_MOST);
        if (mode == MeasureSpec.EXACTLY) {
            mViewWidth = size - getPaddingLeft() - getPaddingRight();
            return size;
        } else {
            mViewWidth = VIEW_DEFAULT_WIDTH;
            return VIEW_DEFAULT_WIDTH + getPaddingRight() + getPaddingLeft();
        }
    }

    private int getCustomMeasureHeight(int measureHeight) {
        int mode = MeasureSpec.getMode(measureHeight);
        int size = MeasureSpec.getSize(measureHeight);
        Log.i("zyq", "mode=" + mode + " EXACTLY=" + MeasureSpec.EXACTLY + " UNSPECIFIED=" + MeasureSpec.UNSPECIFIED + " AT_MOST=" + MeasureSpec.AT_MOST);
        if (mode == MeasureSpec.EXACTLY) {
            mViewHeight = size - getPaddingTop() - getPaddingBottom();
            return size;
        } else {
            mViewHeight = VIEW_DEFAULT_HEIGHT;
            return VIEW_DEFAULT_HEIGHT + getPaddingTop() + getPaddingBottom();
        }
    }

    private void drawBounds(Canvas canvas) {
        canvas.save();
        if (mBoundRect != null) {
            Log.i("zyq_bound", "drawBounds");
//            canvas.drawRect(mBoundRect,mBoundsPaint);
            canvas.drawRect(mBoundRect.left + mViewPortPosition.x, mBoundRect.top + mViewPortPosition.y, mBoundRect.right + mViewPortPosition.x,
                    mBoundRect.bottom + mViewPortPosition.y, mBoundsPaint);
        }
        canvas.restore();
    }

    private GestureDetector mDetector = new GestureDetector(mContext, new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            updateBoundRect(-distanceX, -distanceY);
//            postInvalidateDelayed(50);
            invalidate();
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    });

    private void updateBoundRect(float width, float height) {
        Log.i("zyq", "width=" + width + " height=" + height);
        mBoundRect.left = mBoundRect.left + (int) width;
        mBoundRect.right = mBoundRect.right + (int) width;
        mBoundRect.top = mBoundRect.top + (int) height;
        mBoundRect.bottom = mBoundRect.bottom + (int) height;
        if (mBoundRect.left < 0) {
            mBoundRect.left = 0;
            mBoundRect.right = mBoundWidth;
        }
        if (mBoundRect.top < 0) {
            mBoundRect.top = 0;
            mBoundRect.bottom = mBoundHeight;
        }
        if (mBoundRect.right > (mImageWidth)) {
            mBoundRect.right = (mImageWidth);
            mBoundRect.left = (mImageWidth) - mBoundWidth;
        }
        if (mBoundRect.bottom > (mImageHeight)) {
            mBoundRect.bottom = (mImageHeight);
            mBoundRect.top = (mImageHeight) - mBoundHeight;
        }
        if (mListener != null) {
            mListener.deliveryViewAndBoundsRect(mImageWidth, mImageHeight, mBoundRect);
        }
        Log.i("zyq_bound", "reset rect:left" + mBoundRect.left + " top=" + mBoundRect.top + " right=" + mBoundRect.right + " bottom=" + mBoundRect.bottom + "\n" +
                "mViewPortPosition.y+mImageHeight=" + (mViewPortPosition.y + mImageHeight) + " mViewPortPosition.x+mImageWidth" + (mViewPortPosition.x + mImageWidth));
    }
}
