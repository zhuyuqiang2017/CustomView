package com.example.administrator.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by zhuyuqiang on 2017/3/13.
 */

public class CustomView extends View {

    private final int WHITE_COLOR = Color.WHITE;
    private final int ORANGE_COLOR = 0xffffa800;
    //确定叶子旋转方向
    private final int ROTATE_RIGHT = 0;
    private final int ROTATE_LEFT = 1;
    private final int ROTATE_DEGREE = 5;
    //确定叶子垂直方向的振幅大小
    private final int SMALL_AMPLITUDE = 0;
    private final int MIDDLE_AMPLITUDE = 1;
    private final int BIG_AMPLITUDE = 2;
    private final int SMALL_Y = 1;
    private final int MIDDLE_Y = 2;
    private final int BIG_Y = 3;
    //确定叶子水平方向的移动大小
    private final int SMALL_TRANSITION = 0;
    private final int MIDDLE_TRANSITION = 1;
    private final int BIG_TRANSITION = 2;
    private final int SMALL_X = 5;
    private final int MIDDLE_X = 10;
    private final int BIG_X = 15;
    private Paint mWhitePaint,mOrangePaint,mBitmapPaint;
    private int mPadding_left,mPadding_top,mPadding_right,mPadding_bottom;
    private Point mLeftCircleCenter,mRightCircleCenter,mFengshanPoint;
    private BitmapContainer mLeafContainer,mFengShanContainer,mBg_Container;
    private Matrix mFengShanRotate,mLeafMatrix,mTextMatrix;
    private int startDegree = 0;
    private int mProgressValue = 0;
    private int radius;
    private final float MAX_PROGRESS_VALUE = 100;
    private RectF mLeftArcRect;
    private Rect mTextRect;
    private final int max = 10;
    private int y_step = 2;
    private final String mFinish= "100%";
    private float mTextScale = 0.1f;
    private int mTextSize = 42;
    private List<Leaf> mLeaves = new ArrayList<>();
    private onProgressChangedListener listener;
    public interface onProgressChangedListener{
        void onProgressValueChanged(int progressValue);
    }

    public CustomView(Context context) {
        this(context,null);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttrs();
        initBitmaps();
        initPaints();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getCustomMeasureWidth(widthMeasureSpec),getCustomMeasureHeight(heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initKeyPoints();
        initMatrix();
        generateLeaves();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLeafs(canvas);
        drawProgress(canvas);
        canvas.drawBitmap(mBg_Container.src,mPadding_left,mPadding_top,mBitmapPaint);
        drawFengShan(canvas);
        postInvalidateDelayed(50);
    }

    private void drawLeafs(Canvas canvas) {
        int mProgressWidth = (int) ((mProgressValue/MAX_PROGRESS_VALUE)*(getWidth()-mPadding_left-mPadding_right-radius));
        for (Leaf mLeaf:mLeaves){
            canvas.save();
            mLeafMatrix.reset();
            mLeafMatrix.preRotate(mLeaf.degree,mLeafContainer.width/2,mLeafContainer.height/2);
            switch (mLeaf.rotateDirection){
                case ROTATE_LEFT:
                    mLeaf.degree = mLeaf.degree-ROTATE_DEGREE;
                    break;
                case ROTATE_RIGHT:
                    mLeaf.degree = mLeaf.degree+ROTATE_DEGREE;
                    break;
            }
            mLeafMatrix.postTranslate(mLeaf.position.x-50,mLeaf.position.y-mLeafContainer.height/2);
            switch (mLeaf.transition){
                case SMALL_TRANSITION:
                    mLeaf.position.x = mLeaf.position.x - SMALL_X;
                    break;
                case MIDDLE_TRANSITION:
                    mLeaf.position.x = mLeaf.position.x - MIDDLE_X;
                    break;
                case BIG_TRANSITION:
                    mLeaf.position.x = mLeaf.position.x - BIG_X;
                    break;
            }
            switch (mLeaf.transition){
                case SMALL_AMPLITUDE:
                    if(mLeaf.position.y<(mRightCircleCenter.y-max)){
                        y_step = -SMALL_Y;
                    }else if (mLeaf.position.y>(mRightCircleCenter.y+max)){
                        y_step = SMALL_Y;
                    }
                    break;
                case MIDDLE_AMPLITUDE:
                    if(mLeaf.position.y<(mRightCircleCenter.y-max)){
                        y_step = -MIDDLE_Y;
                    }else if (mLeaf.position.y>(mRightCircleCenter.y+max)){
                        y_step = MIDDLE_Y;
                    }
                    break;
                case BIG_AMPLITUDE:
                    if(mLeaf.position.y<(mRightCircleCenter.y-max)){
                        y_step = -BIG_Y;
                    }else if (mLeaf.position.y>(mRightCircleCenter.y+max)){
                        y_step = BIG_Y;
                    }
                    break;
            }

            mLeaf.position.y = mLeaf.position.y - y_step;
            if(mLeaf.position.x > mProgressWidth+mPadding_left+10){
                canvas.drawBitmap(mLeafContainer.src,mLeafMatrix,mBitmapPaint);
            }else{
                mLeaf.position.set(mRightCircleCenter.x-radius,mRightCircleCenter.y-mLeafContainer.height/2);
                mLeaf.transition = new Random().nextInt(3);
                mLeaf.degree = new Random().nextInt(360);
                mLeaf.rotateDirection = new Random().nextInt(2);
                mLeaf.amplitude = new Random().nextInt(3);
            }
            canvas.restore();
        }
    }

    public void setProgressValue(int value){
        this.mProgressValue = value;
        invalidate();
    }

    public int getProgressValue(){
        return mProgressValue;
    }

    public void setProgressChangeListener(onProgressChangedListener listener){
        this.listener = listener;
    }

    private void drawProgress(Canvas canvas) {

        int shouldDrawWidth = (int) ((mProgressValue/MAX_PROGRESS_VALUE)*(getWidth()-mPadding_left-mPadding_right-radius));
        Log.d("zyq_progress","shouldDrawWidth = "+shouldDrawWidth);
        if(shouldDrawWidth<(mLeftCircleCenter.x-mPadding_left)){
            float degree = (float) Math.toDegrees(Math.acos((radius - shouldDrawWidth)
                    / (float) radius));
            canvas.save();
            canvas.drawArc(mLeftArcRect,180-degree,2*degree,false,mOrangePaint);
        }else{
            if (shouldDrawWidth>mRightCircleCenter.x){
                shouldDrawWidth = mRightCircleCenter.x;
            }
            canvas.save();
            canvas.drawArc(mLeftArcRect,90,180,true,mOrangePaint);
            canvas.drawRect(radius+mPadding_left,mPadding_top,shouldDrawWidth,2*radius+mPadding_top,mOrangePaint);
            canvas.restore();
        }
        if(listener != null){
            listener.onProgressValueChanged(mProgressValue);
        }
    }

    private void drawFengShan(Canvas canvas){
        if(mProgressValue<100){
            startDegree = startDegree+15;
            canvas.save();
            mFengShanRotate.reset();
            mFengShanRotate.preRotate(startDegree,mFengShanContainer.width/2,mFengShanContainer.height/2);
            mFengShanRotate.postTranslate(mFengshanPoint.x,mFengshanPoint.y);
            Log.i("zyq","mFengshanPoint.x ="+mFengshanPoint.x+" mFengshanPoint.y="+mFengshanPoint.y);
            canvas.drawBitmap(mFengShanContainer.src,mFengShanRotate,mBitmapPaint);
            canvas.restore();
        }else{
            canvas.save();
            mTextMatrix.reset();
            if(mTextScale<1){
                mWhitePaint.setTextSize(mTextSize*mTextScale);
                mWhitePaint.getTextBounds(mFinish,0,mFinish.length(),mTextRect);
            }else{
                mWhitePaint.setTextSize(mTextSize);
                mWhitePaint.getTextBounds(mFinish,0,mFinish.length(),mTextRect);
            }
            canvas.drawText(mFinish,0,mFinish.length(),mRightCircleCenter.x-((mTextRect.width())/2),
                    mRightCircleCenter.y+(mTextRect.height()/2),mWhitePaint);
            mTextScale = mTextScale+0.1f;
            canvas.restore();
        }

    }

    private void initKeyPoints(){
        mLeftCircleCenter = new Point();
        radius = getHeight()/2-mPadding_bottom/2-mPadding_top/2;
        mLeftCircleCenter.y = radius+mPadding_top;
        mLeftCircleCenter.x = radius+mPadding_right;
        Log.d("zyq_point","View,height="+getHeight()+" mLeftCircleCenter.x = "+mLeftCircleCenter.x+"mLeftCircleCenter.y="+mLeftCircleCenter.y);
        mRightCircleCenter = new Point();
        mRightCircleCenter.y = radius+mPadding_top;
        mRightCircleCenter.x = getWidth()-radius-mPadding_right;
        Log.d("zyq_point","mRightCircleCenter.x = "+mRightCircleCenter.x+"mRightCircleCenter.y="+mRightCircleCenter.y);
        mFengshanPoint = new Point();
        mFengshanPoint.x = mRightCircleCenter.x-mFengShanContainer.width/2;
        mFengshanPoint.y = mRightCircleCenter.y-mFengShanContainer.height/2;
        Log.d("zyq_point","mFengshanPoint.x = "+mFengshanPoint.x+"mFengshanPoint.y="+mFengshanPoint.y);
    }

    private void initPaints(){
        mWhitePaint = new Paint();
        mWhitePaint.setAntiAlias(true);
        mWhitePaint.setDither(true);
        mWhitePaint.setTextSize(32);
        mWhitePaint.setTextAlign(Paint.Align.LEFT);
        mWhitePaint.setStrokeWidth(6f);
        mWhitePaint.setColor(WHITE_COLOR);


        mOrangePaint = new Paint();
        mOrangePaint.setAntiAlias(true);
        mOrangePaint.setDither(true);
        mOrangePaint.setColor(ORANGE_COLOR);
        mOrangePaint.setStyle(Paint.Style.FILL);

        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setDither(true);
        mBitmapPaint.setFilterBitmap(true);
    }

    private void initBitmaps(){
        Bitmap mLeaf = ((BitmapDrawable)getResources().getDrawable(R.drawable.leaf_drawable,null)).getBitmap();
        Bitmap mFengShan = ((BitmapDrawable)getResources().getDrawable(R.drawable.fengshan_drawable,null)).getBitmap();
        Bitmap mbg_kuang = ((BitmapDrawable)getResources().getDrawable(R.drawable.leaf_kuang_drawable,null)).getBitmap();
        mLeafContainer = new BitmapContainer();
        mLeafContainer.src = mLeaf;
        mLeafContainer.width = mLeaf.getWidth();
        mLeafContainer.height = mLeaf.getHeight();

        mFengShanContainer = new BitmapContainer();
        mFengShanContainer.src = mFengShan;
        mFengShanContainer.width = mFengShan.getWidth();
        mFengShanContainer.height = mFengShan.getHeight();
        Log.i("zyq","fen:width="+mFengShanContainer.width+" height="+mFengShanContainer.height);

        mBg_Container = new BitmapContainer();
        mBg_Container.src = mbg_kuang;
        mBg_Container.width = mbg_kuang.getWidth();
        mBg_Container.height = mbg_kuang.getHeight();
        Log.i("zyq_Bg_Container","mBg_Container:width="+mBg_Container.width+" height="+mBg_Container.height);
    }

    private void initMatrix(){
        mFengShanRotate = new Matrix();
        mLeafMatrix = new Matrix();
        mTextMatrix = new Matrix();
        calculatePosition();
    }

    private void calculatePosition(){
        mLeftArcRect = new RectF();
        mLeftArcRect.left = mPadding_left;
        mLeftArcRect.top = mPadding_top;
        mLeftArcRect.right = 2*radius+mPadding_left;
        mLeftArcRect.bottom = 2*radius+mPadding_top;
        mTextRect = new Rect();
        mWhitePaint.getTextBounds(mFinish,0,mFinish.length(), mTextRect);
    }

    private class BitmapContainer{
        Bitmap src;
        int width;
        int height;
    }

    private class Leaf{
        Point position;
        int degree;
        long startTime;
        int rotateDirection;
        int amplitude;
        int transition;
        public Leaf(){
            position = new Point();
            position.set(mRightCircleCenter.x-radius,mRightCircleCenter.y);
            degree = new Random().nextInt(360);
            startTime = System.currentTimeMillis();
            rotateDirection = new Random().nextInt(2);
            amplitude = new Random().nextInt(3);
            transition = new Random().nextInt(3);
        }
        public Leaf(int transition){
            position = new Point();
            position.set(mRightCircleCenter.x-radius,mRightCircleCenter.y);
            degree = new Random().nextInt(360);
            startTime = System.currentTimeMillis();
            rotateDirection = new Random().nextInt(2);
            amplitude = new Random().nextInt(3);
            this.transition = transition;
        }
    }

    private void generateLeaves() {
        if(mLeaves.isEmpty()){
            for(int i = 0;i<3;i++){
                Leaf l = new Leaf(i);
                mLeaves.add(l);
            }
        }
    }

    private int getCustomMeasureWidth(int widthMeasureSpec){
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        if (mode == MeasureSpec.EXACTLY){
            Log.i("zyq_view","view.size = "+size);
            return size;
        }else{
            Log.i("zyq_view","view.width = "+(mBg_Container.width+getPaddingLeft()+getPaddingRight()));
            return mBg_Container.width+getPaddingLeft()+getPaddingRight();
        }
    }

    private int getCustomMeasureHeight(int heightMeasureSpec){
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int size = MeasureSpec.getSize(heightMeasureSpec);
        if (mode == MeasureSpec.EXACTLY){
            return size;
        }else{
            return mBg_Container.height+getPaddingTop()+getPaddingBottom();
        }
    }

    private void initAttrs() {
        mPadding_left = getPaddingLeft();
        mPadding_top = getPaddingTop();
        mPadding_right = getPaddingRight();
        mPadding_bottom = getPaddingBottom();
        Log.i("zyq_pad","left="+mPadding_left+" top="+mPadding_top+" right="+mPadding_right+" bottom="+mPadding_bottom);
    }
}
