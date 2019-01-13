package com.offtime.test.moonballon;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by skn on 2015-08-01.
 */
public class PureGolden extends View
{
    private Paint mCirclePaint;
    private Paint mPointerPaint;
    private Paint mPointerHaloPaint;

    private Path mOuterCirclePath;
    private RectF mOuterCircleRect;
    private float mOuterCircleStartAngle;
    private float mOuterCircleEndAngle;
    private float mOuterCircleTotalDegrees;
    private boolean is_day_time = false;

    private Drawable day_drawable;
    private Drawable night_drawable;

    private float mViewWidth;
    private float mViewHeight;
    private float mViewMinDistance;
    private float mFirstCoordinates[] = {0f, 0f};
    private float mLastCoordinates[] = {0f, 0f};

    private boolean is_first_pointer_pressed = false;
    private boolean is_last_pointer_pressed = false;

    private float OUTER_CIRCLE_DISTANCE_RATIO = (0.7f); //circle is 70% of total rect width & height
    private static final float MAX_POINTER_DISTANCE  = 50.0f;
    private static final float MAX_TOTAL_ANGLE = 355.0f;
    private static final float POINTER_SIZE = 9.0f;
    private static final float POINTER_HOLLOW_SIZE = 50.0f;
    private static final float FIRST_INITIAL_ANGLE  = 0.0f;
    private static final int POINTER_COLOR = 0xff70ff70;
    private static final int POINTER_HOLLOW_COLOR = 0xff70ff70;
    private static final int CIRCLE_PATH_COLOR = 0xffffffff;
    private static final String TAG = PureGolden.class.getSimpleName();


    public PureGolden(Context context)
    {
        super(context);
        initPureGolden();
		
		int a = -1;
    }

    public PureGolden(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initPureGolden();
		
		int main_val = 333;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
    {
        //int height = getDefaultSize(minHeight, heightMeasureSpec);
        //int width = getDefaultSize(minWidth, widthMeasureSpec);

        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);
        mViewWidth = w;

        int minh = getPaddingBottom() + getPaddingTop() + getSuggestedMinimumHeight();
        int h = resolveSizeAndState(minh, heightMeasureSpec, 1);
        mViewHeight = h;

        if(mViewWidth >= mViewHeight)
        {
            mViewMinDistance = mViewHeight;
        }
        else
        {
            mViewMinDistance = mViewWidth;
        }

        recalculatePaths();

        reinitResources();

        setMeasuredDimension(w, h);

    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        canvas.drawColor(Color.WHITE);

        getFirstAndLastPoint();

        if(is_day_time == true)
        {
            day_drawable.draw(canvas);
        }
        else
        {
            night_drawable.draw(canvas);
        }

        canvas.drawPath(mOuterCirclePath, mCirclePaint);

        if ((is_first_pointer_pressed == true) || (is_last_pointer_pressed == true))
        {
            mPointerHaloPaint.setColor(0xffff0000);

            if(is_first_pointer_pressed == true)
            {
                canvas.drawCircle(mFirstCoordinates[0], mFirstCoordinates[1], POINTER_SIZE, mPointerPaint);
                canvas.drawCircle(mFirstCoordinates[0], mFirstCoordinates[1], POINTER_HOLLOW_SIZE, mPointerHaloPaint);
            }
            else if(is_last_pointer_pressed == true)
            {
                canvas.drawCircle(mLastCoordinates[0], mLastCoordinates[1], POINTER_SIZE, mPointerPaint);
                canvas.drawCircle(mLastCoordinates[0], mLastCoordinates[1], POINTER_HOLLOW_SIZE, mPointerHaloPaint);
            }
            else
            {
                //default first pointer is shown
                canvas.drawCircle(mFirstCoordinates[0], mFirstCoordinates[1], POINTER_SIZE, mPointerPaint);
                canvas.drawCircle(mFirstCoordinates[0], mFirstCoordinates[1], POINTER_HOLLOW_SIZE, mPointerHaloPaint);
            }
        }
        else
        {
            mPointerHaloPaint.setColor(0xff00ff00);

            canvas.drawCircle(mFirstCoordinates[0], mFirstCoordinates[1], POINTER_SIZE, mPointerPaint);
            canvas.drawCircle(mFirstCoordinates[0], mFirstCoordinates[1], POINTER_HOLLOW_SIZE, mPointerHaloPaint);
        }
    }


    private void getFirstAndLastPoint()
    {
        PathMeasure pm = new PathMeasure(mOuterCirclePath, false);

        pm.getPosTan(pm.getLength() * 0.0f, mFirstCoordinates, null);
        pm.getPosTan(pm.getLength() * 1.0f, mLastCoordinates, null);
    }

    private final void initPureGolden()
    {
        setPadding(3, 3, 3, 3);

        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setDither(true);
        mCirclePaint.setColor(CIRCLE_PATH_COLOR);
        mCirclePaint.setStrokeWidth(10.0f);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeJoin(Paint.Join.ROUND);
        mCirclePaint.setStrokeCap(Paint.Cap.ROUND);

        mOuterCircleRect = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
        mOuterCircleStartAngle = FIRST_INITIAL_ANGLE;
        mOuterCircleEndAngle = MAX_TOTAL_ANGLE;
        mOuterCircleTotalDegrees = MAX_TOTAL_ANGLE;

        mPointerPaint = new Paint();
        mPointerPaint.setAntiAlias(true);
        mPointerPaint.setDither(true);
        mPointerPaint.setStyle(Paint.Style.FILL);
        mPointerPaint.setColor(POINTER_COLOR);
        mPointerPaint.setStrokeWidth(5.0f);

        mPointerHaloPaint = new Paint();
        mPointerHaloPaint.setColor(POINTER_HOLLOW_COLOR);
        mPointerHaloPaint.setAlpha(0x70);
        mPointerHaloPaint.setStyle(Paint.Style.STROKE);
        mPointerHaloPaint.setStrokeWidth(10.0f);
    }

    private void initPaths()
    {
        mOuterCirclePath = new Path();

        mOuterCirclePath.addArc(mOuterCircleRect, mOuterCircleStartAngle, mOuterCircleTotalDegrees);
    }

    private void recalculatePaths()
    {
        mOuterCirclePath = new Path();
        mOuterCircleRect.set((mViewMinDistance*(1-OUTER_CIRCLE_DISTANCE_RATIO))/2, (mViewHeight - (mViewMinDistance*OUTER_CIRCLE_DISTANCE_RATIO))/2 ,mViewMinDistance - (mViewMinDistance*(1-OUTER_CIRCLE_DISTANCE_RATIO))/2, (mViewHeight + (mViewMinDistance*OUTER_CIRCLE_DISTANCE_RATIO))/2 );
        mOuterCirclePath.addArc(mOuterCircleRect, mOuterCircleStartAngle, mOuterCircleTotalDegrees);
    }

    private void reinitResources()
    {
        day_drawable = getResources().getDrawable(R.drawable.day_scene);
        day_drawable.setAlpha(0x70);
        day_drawable.setBounds(0, 0, (int) mViewWidth, (int) mViewHeight);

        night_drawable = getResources().getDrawable(R.drawable.night_scene);
        night_drawable.setAlpha(0x70);
        night_drawable.setBounds(0, 0, (int) mViewWidth, (int) mViewHeight);
    }

    private float getAngle(float center_x, float center_y, float new_x, float new_y)
    {
        float angle = (float) Math.toDegrees(Math.atan2(new_y - center_y, new_x - center_x));

        if(angle < 0){
            angle += 360;
        }

        return angle;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float angle = 0.0f;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                boolean ret = check_first_pointer_pressed(x, y);
                if(ret == true)
                {
                    is_first_pointer_pressed = true;
                    invalidate();
                }
                else
                {
                    ret = check_last_pointer_pressed(x, y);

                    if(ret == true)
                    {
                        is_last_pointer_pressed = true;
                    }
                    else
                    {
                        is_last_pointer_pressed = false;
                    }
                    is_first_pointer_pressed = false;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //get_proper_near_pointer_position(x, y);
                if(is_first_pointer_pressed == true)
                {
                    angle = getAngle(mOuterCircleRect.centerX(), mOuterCircleRect.centerY(), x, y);
                    mOuterCircleStartAngle = angle;
                    mOuterCircleTotalDegrees = mOuterCircleEndAngle - mOuterCircleStartAngle;

                    mOuterCircleTotalDegrees = (mOuterCircleTotalDegrees < 0 ? 360f + mOuterCircleTotalDegrees : mOuterCircleTotalDegrees);
                    //Log.d(TAG, String.format("start:%d end:%d", (int) mOuterCircleStartAngle, (int)mOuterCircleEndAngle));

                    if((angle >= 0) && (angle <180))
                    {
                        is_day_time = true;
                    }
                    else
                    {
                        is_day_time = false;
                    }
                }
                else if(is_last_pointer_pressed == true)
                {
                    angle = getAngle(mOuterCircleRect.centerX(), mOuterCircleRect.centerY(), x, y);
                    mOuterCircleEndAngle = angle;
                    mOuterCircleTotalDegrees = mOuterCircleEndAngle - mOuterCircleStartAngle;
                    mOuterCircleTotalDegrees = (mOuterCircleTotalDegrees < 0 ? 360f + mOuterCircleTotalDegrees : mOuterCircleTotalDegrees);
                    //Log.d(TAG, String.format("start:%d end:%d", (int) mOuterCircleStartAngle, (int)mOuterCircleEndAngle));

                    if((angle >= 0) && (angle <180))
                    {
                        is_day_time = true;
                    }
                    else
                    {
                        is_day_time = false;
                    }
                }
                initPaths();
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                is_first_pointer_pressed = false;
                is_last_pointer_pressed = false;
                if(is_first_pointer_pressed == true)
                {
                    angle = getAngle(mOuterCircleRect.centerX(), mOuterCircleRect.centerY(), x, y);
                    mOuterCircleStartAngle = angle;
                    mOuterCircleTotalDegrees = mOuterCircleEndAngle - mOuterCircleStartAngle;
                    mOuterCircleTotalDegrees = (mOuterCircleTotalDegrees < 0 ? 360f + mOuterCircleTotalDegrees : mOuterCircleTotalDegrees);
                }
                else if(is_last_pointer_pressed == true)
                {
                    angle = getAngle(mOuterCircleRect.centerX(), mOuterCircleRect.centerY(), x, y);

                    mOuterCircleEndAngle = angle;
                    mOuterCircleTotalDegrees = mOuterCircleEndAngle - mOuterCircleStartAngle;
                    mOuterCircleTotalDegrees = (mOuterCircleTotalDegrees < 0 ? 360f + mOuterCircleTotalDegrees : mOuterCircleTotalDegrees);
                }
                initPaths();
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        return true;
    }

    private boolean check_first_pointer_pressed(float x, float y)
    {
        float pointer_x = mFirstCoordinates[0];
        float pointer_y = mFirstCoordinates[1];

        float dist = (float) Math.sqrt( Math.pow(x - pointer_x, 2) + Math.pow(y - pointer_y, 2) );
        if(dist <= MAX_POINTER_DISTANCE)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private boolean check_last_pointer_pressed(float x, float y)
    {
        float pointer_x = mLastCoordinates[0];
        float pointer_y = mLastCoordinates[1];

        float dist = (float) Math.sqrt( Math.pow(x - pointer_x, 2) + Math.pow(y - pointer_y, 2) );
        if(dist <= MAX_POINTER_DISTANCE)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

}



