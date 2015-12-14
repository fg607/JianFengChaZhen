package com.hardwork.fg607.jianfengchazhen.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.hardwork.fg607.jianfengchazhen.R;
import com.hardwork.fg607.jianfengchazhen.utils.BitmapUtil;
import com.hardwork.fg607.jianfengchazhen.utils.DensityUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fg607 on 15-12-11.
 */
public class MyView extends View implements Runnable,View.OnTouchListener{

    private HashMap<Bitmap,Integer> mNailHashMap;
    private boolean mIsRotate = false;
    private Paint mPaint = new Paint();
    private Context mContext;
    private  Matrix mMatrix = new Matrix();;
    private float mViewSize;
    private boolean mIsConflict = false;
    private int mNailWidth  = 16;//dip
    private int mNailHeight = 100;//dip
    private int mCircleRadius = 30;//dip
    private int mNailNumerTextSize = 19;//dip
    private int mFailedTextSize = 21;//dip
    private int mConflictCount = 0;
    private Bitmap mBitmap;
    private int mRotate;
    private boolean mIsRuning = false;
    private int mMinRotate = 1;
    private int mSleepTime = 12;
    private boolean mIsAddNewNail = false;
    private boolean mIsDrawing = false;

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();

        //用线程来刷新界面
        Thread thread=new Thread(this);
        thread.start();
    }

    public void init(){

        //dip转化为px
        mNailWidth = DensityUtil.dip2px(mContext, mNailWidth);
        mNailHeight = DensityUtil.dip2px(mContext, mNailHeight);
        mCircleRadius = DensityUtil.dip2px(mContext,mCircleRadius);
        mNailNumerTextSize = DensityUtil.dip2px(mContext,mNailNumerTextSize);
        mFailedTextSize = DensityUtil.dip2px(mContext,mFailedTextSize);

        mIsRuning = true;
        mIsRotate = true;

        mNailHashMap = new HashMap<>();
        mViewSize =  mContext.getResources().getDisplayMetrics().widthPixels;
        mPaint.setTextAlign(Paint.Align.CENTER);

        setOnTouchListener(this);
    }

    public void addNail(){

        Bitmap bitmap = BitmapUtil.readBitmapById(mContext, R.drawable.nail);
        bitmap = BitmapUtil.scaleImage(bitmap,mNailWidth,mNailHeight);
        mNailHashMap.put(bitmap, 0);
    }

    public void clickScreen(){

        if(!isRotated()){
            mNailHashMap.clear();
            startRotate();
        }else {
            mIsAddNewNail = true;
        }

    }

 /* @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

           setMeasuredDimension(mViewSize, mViewSize);
    }*/

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        prepareDraw();

        drawing(canvas);

        afterDraw();


    }

    public void drawing(Canvas canvas) {

        mPaint.setColor(Color.rgb(0, 0, 0));

        //画中心圆
        canvas.drawCircle(mViewSize / 2, mViewSize / 2, mCircleRadius, mPaint);
        //画底下小圆
        canvas.drawCircle(mViewSize / 2, mViewSize / 2 + mNailHeight + mCircleRadius / 2 + mNailWidth, mNailWidth / 2, mPaint);

        drawNails(canvas);

        drawNailsNumber(canvas);

        if(mIsConflict) {
            drawFailedInfo(canvas);
        }




    }

    private void drawFailedInfo(Canvas canvas) {
        drawFailedText(canvas);
        drawConflictNail(canvas);
    }

    private void drawConflictNail(Canvas canvas) {

        Bitmap bitmap = BitmapUtil.readBitmapById(mContext, R.drawable.conflict_nail);
        bitmap = BitmapUtil.scaleImage(bitmap,mNailWidth,mNailHeight);

        mMatrix.setTranslate(mViewSize / 2 - bitmap.getWidth() / 2, mViewSize / 2);
        mMatrix.postRotate(0, mViewSize / 2, mViewSize / 2);

        canvas.drawBitmap(bitmap, mMatrix, null);
    }

    public void prepareDraw() {

        if(mIsAddNewNail){
            addNail();
        }
    }

    public void afterDraw() {

        if(mIsAddNewNail){
            mIsAddNewNail = false;
        }

        if(mIsConflict){

            stopRotate();
            mIsConflict = false;

        }else {

            updateNails();
        }

        mIsDrawing = false;
    }

    public void updateNails(){

        //遍历HashMap改变角度
        for(Map.Entry<Bitmap,Integer> entry:mNailHashMap.entrySet()){
            mBitmap = entry.getKey();
            mRotate = entry.getValue();

            if (mRotate == 360) {
                mRotate = 0;
            }

            mRotate += mMinRotate;
            mNailHashMap.put(mBitmap, mRotate);
        }

    }
    public void drawFailedText(Canvas canvas) {
            mPaint.setColor(Color.RED);
            mPaint.setTextSize(mFailedTextSize);
            canvas.drawText("你输了,再接再厉哦！",mViewSize / 2, + mPaint.getTextSize(),mPaint);
    }

    public void drawNailsNumber(Canvas canvas) {
        mPaint.setColor(Color.rgb(255, 255, 255));
        mPaint.setTextSize(mNailNumerTextSize);
        canvas.drawText("" + mNailHashMap.size(), mViewSize / 2, mViewSize / 2 + mPaint.getTextSize()/2, mPaint);
    }

    public void drawNails(Canvas canvas) {

        //清空碰撞检测标志
        mConflictCount = 0;

        for(Map.Entry<Bitmap,Integer> entry:mNailHashMap.entrySet()){

            Bitmap bitmap = entry.getKey();
            int rotate = entry.getValue();

            if(mIsAddNewNail){
                checkConflict(rotate);
            }


            mMatrix.setTranslate(mViewSize / 2 - bitmap.getWidth() / 2, mViewSize / 2);
            mMatrix.postRotate(rotate, mViewSize / 2, mViewSize / 2);

            canvas.drawBitmap(bitmap, mMatrix, null);
        }
    }


    public void startRotate(){

        this.mIsRotate=true;

    }

    public void stopRotate(){

        this.mIsRotate=false;

    }

    public boolean isRotated(){

        return mIsRotate;
    }

    @Override
    public void run() {

        while(mIsRuning){

            if(mIsRotate && !mIsDrawing){
                //刷新View
                this.postInvalidate();
                mIsDrawing = true;
            }

            try {
                Thread.sleep(mSleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    public void checkConflict(int rotate) {

        //根据小球的宽度和高度计算出角度在10-350之间的nail会与新添加nail产生碰撞（包含新添加nail,角度为0）
        if (rotate <= 10 || rotate >= 350) {

            mConflictCount++;

            if (mConflictCount >= 2 ) {

                mIsConflict = true;
            }
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_DOWN){

            clickScreen();
        }
        return false;
    }

    @Override
    public void setVisibility(int visibility) {

        if(visibility == VISIBLE){
            mIsRuning = true;
        }else {

            mIsRuning = false;
        }
        super.setVisibility(visibility);
    }

    @Override
    protected void finalize() throws Throwable {
        mIsRuning = false;
        super.finalize();

    }
}
