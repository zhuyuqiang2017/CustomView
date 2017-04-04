package com.example.navigationview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhuyuqiang on 2017/3/14.
 */

public class DecoderView extends View {

    private Context mContext;
    private Paint mBitmapPaint, mBoundPaint;
    private int DEFAULT_BOUND_COLOR = Color.RED;
    private final int DEFAULT_WIDTH = 600;
    private final int DEFAULT_HEIGHT = 600;
    private Rect mViewPort = new Rect();
    private int mOriginWidth, mOriginHeight;
    private BitmapFactory.Options mDecoderOption = new BitmapFactory.Options();
    private InputStream mSourceInputStream = null;
    private String mSourcePath;
    private float mDownX, mDownY;
    private BitmapRegionDecoder mDecoder = null;
    private int mCurrentWidth, mCurrentHeight;
    private onViewPortPositionChangeListener mListener;

    public interface onViewPortPositionChangeListener {
        void getViewPortRect(Rect mViewPortRect);
    }

    public DecoderView(Context context) {
        this(context, null);
    }

    public DecoderView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DecoderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DecoderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        initPaints();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getCustomMeasuteWidth(widthMeasureSpec), getCustomMeasuteHeight(heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawPartImage(canvas);
        Log.i("zyq", "width =" + getWidth() + " height=" + getHeight());
    }

    private void drawPartImage(Canvas canvas) {
        Log.i("zyq", "drawPartImage = " + (mSourceInputStream != null));
        if (mSourceInputStream != null) {
            try {
                Log.i("zyq", "mSourceInputStream != null");
                if (mDecoder == null) {
                    mDecoder = BitmapRegionDecoder.newInstance(mSourceInputStream, true);

                }
            } catch (IOException e) {
                Log.i("zyq", "e=" + e.toString());
            }
        }
        if (mSourcePath != null) {
            try {
                Log.i("zyq", "mSourceInputStream != null");
                if (mDecoder == null) {
                    mDecoder = BitmapRegionDecoder.newInstance(mSourceInputStream, true);
                }
            } catch (IOException e) {
                Log.i("zyq", "e=" + e.toString());
            }
        }
        if (mDecoder != null) {
            Bitmap b = mDecoder.decodeRegion(mViewPort, mDecoderOption);
            canvas.drawBitmap(b, getPaddingLeft(), getPaddingTop(), mBitmapPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                Log.i("zyq", "MotionEvent.ACTION_DOWN: mDownX="+mDownX+" mDownY="+mDownY);
//                mDownX = event.getX();
//                mDownY = event.getY();
////                break;
//            case MotionEvent.ACTION_MOVE:
//                float mMoveX = event.getX();
//                float mMoveY = event.getY();
//                Log.i("zyq", "MotionEvent.ACTION_MOVE mMoveX="+mMoveX+" mMoveY="+mMoveY);
//                Log.i("zyq", "mMoveX - mDownX="+(mMoveX - mDownX)+" mMoveY - mDownY="+(mMoveY - mDownY));
//                updateViewPort(-(mMoveX - mDownX), -(mMoveY - mDownY));
//                invalidate();
////                break;
//            case MotionEvent.ACTION_UP:
//                Log.i("zyq", "MotionEvent.ACTION_UP");
////                break;
//        }
        mDetector.onTouchEvent(event);
        return true;
    }

    private void updateViewPort(float width, float height) {
//        Log.i("zyq", "width=" + width + " height=" + height);
        mViewPort.left = mViewPort.left + (int) width;
        mViewPort.right = mViewPort.right + (int) width;
        mViewPort.top = mViewPort.top + (int) height;
        mViewPort.bottom = mViewPort.bottom + (int) height;
        if (mViewPort.left < 0) {
            mViewPort.left = 0;
            mViewPort.right = mCurrentWidth;
        }
        if (mViewPort.top < 0) {
            mViewPort.top = 0;
            mViewPort.bottom = mCurrentHeight;
        }
        if (mViewPort.right > mOriginWidth) {
            mViewPort.right = mOriginWidth;
            mViewPort.left = mOriginWidth - mCurrentWidth;
        }
        if (mViewPort.bottom > mOriginHeight) {
            mViewPort.bottom = mOriginHeight;
            mViewPort.top = mOriginHeight - mCurrentHeight;
        }
        if (mListener != null) {
            mListener.getViewPortRect(mViewPort);
        }

    }

    public void setViewPortChangeListener(onViewPortPositionChangeListener listener) {
        this.mListener = listener;
    }

    public void setImageResource(String filePath) {
        if (mSourceInputStream != null) {
            throw new SecurityException("已经设置过图片资源");
        }
        mDecoderOption.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, mDecoderOption);
        mOriginHeight = mDecoderOption.outHeight;
        mOriginWidth = mDecoderOption.outWidth;
        Log.i("qqq","width = "+mOriginWidth+" height = "+mOriginHeight);
        mSourcePath = filePath;
        mDecoderOption.inJustDecodeBounds = false;
        mDecoderOption.inPreferredConfig = Bitmap.Config.ARGB_8888;
        invalidate();
    }

    public void setImageResource(InputStream is) {
        if (mSourcePath != null) {
            throw new SecurityException("已经设置过图片资源");
        }
        mDecoderOption.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, mDecoderOption);
        mOriginHeight = mDecoderOption.outHeight;
        mOriginWidth = mDecoderOption.outWidth;
        Log.i("qqq","width = "+mOriginWidth+" height = "+mOriginHeight);
        mSourceInputStream = is;
        mDecoderOption.inJustDecodeBounds = false;
        mDecoderOption.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Log.i("zyq", "set Image source");
        invalidate();
    }

    public Rect getViewPortRect() {
        return mViewPort;
    }

    public void setViewPortRectPosition(float x, float y) {
        int positionX = (int) (x * mOriginWidth);
        int positionY = (int) (y * mOriginHeight);
        int tran_X = positionX - mViewPort.left;
        int tran_y = positionY - mViewPort.top;
        updateViewPort(tran_X, tran_y);
        invalidate();
    }

    private void initPaints() {
        Log.i("zyq", "init Paints");
        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setDither(true);
        mBitmapPaint.setFilterBitmap(true);

        mBoundPaint = new Paint();
        mBoundPaint.setAntiAlias(true);
        mBoundPaint.setDither(true);
        mBoundPaint.setColor(DEFAULT_BOUND_COLOR);
    }

    private int getCustomMeasuteWidth(int widthMeasureSpec) {
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            mViewPort.left = getPaddingLeft();
            mViewPort.right = size - getPaddingRight();
            mCurrentWidth = size - getPaddingRight() - getPaddingLeft();
            Log.i("zyq", "getCustomMeasuteHeight: MeasureSpec.EXACTLY");
            return size;
        } else {
            Log.i("zyq", "getCustomMeasuteHeight: MeasureSpec.*");
            mViewPort.left = getPaddingLeft();
            mViewPort.right = DEFAULT_WIDTH + getPaddingLeft();
            return DEFAULT_WIDTH + getPaddingRight() + getPaddingRight();
        }
    }

    private int getCustomMeasuteHeight(int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int size = MeasureSpec.getSize(heightMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            Log.i("zyq", "getCustomMeasuteHeight: MeasureSpec.EXACTLY");
            mViewPort.top = getPaddingTop();
            mViewPort.bottom = size - getPaddingBottom();
            mCurrentHeight = size - getPaddingBottom() - getPaddingTop();
            return size;
        } else {
            Log.i("zyq", "getCustomMeasuteHeight: MeasureSpec.*");
            mViewPort.top = getPaddingTop();
            mViewPort.bottom = DEFAULT_HEIGHT + getPaddingTop();
            return DEFAULT_HEIGHT + getPaddingTop() + getPaddingBottom();
        }
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
            updateViewPort(distanceX, distanceY);
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
}

