package com.xinyu.joystick;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by zhang on 2017/4/2.
 */

public class Joystick extends View {
    private int innerColor;
    private int outerColor;
    private boolean isThrottle;
    private final static int INNER_COLOR_DEFAULT = Color.parseColor("#000000");
    private final static int OUTER_COLOR_DEFAULT = Color.parseColor("#FFFFFF");
    private final static int TEXT_COLOR_DEFAULT = Color.parseColor("#11DD05");
    private final static int LINE_COLOR_DEFAULT = Color.parseColor("#11DD05");
    private int OUTER_WIDTH_SIZE;
    private int OUTER_HEIGHT_SIZE;
    private int realWidth;//绘图使用的宽
    private int realHeight;//绘图使用的高
    private float innerCenterX;
    private float innerCenterY;
    private float outRadius;
    private float innerRadius;
    private Paint outerPaint;
    private Paint innerPaint;
    private Paint linePaint;
    private Paint textPaint;
    private onAngleAndStrengthListenner mCallBack = null;
    private float yawUnLockLength;

    public interface onAngleAndStrengthListenner{
        public void onAngleAndStrength(double angle, double strengthX, double strengthY);
    }
    public Joystick(Context context) {
        this(context,null);
    }

    public Joystick(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = getResources().obtainAttributes(attrs,R.styleable.Joystick);
        innerColor = ta.getColor(R.styleable.Joystick_InnerColor,INNER_COLOR_DEFAULT);
        outerColor = ta.getColor(R.styleable.Joystick_OuterColor,OUTER_COLOR_DEFAULT);
        isThrottle = ta.getBoolean(R.styleable.Joystick_IsThrottle, false);
        ta.recycle();

        OUTER_WIDTH_SIZE = dip2px(context,125.0f);
        OUTER_HEIGHT_SIZE = dip2px(context,125.0f);

        outerPaint = new Paint();
        innerPaint = new Paint();
        textPaint = new Paint();
        linePaint = new Paint();

        outerPaint.setColor(outerColor);
        outerPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        innerPaint.setColor(innerColor);
        innerPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        textPaint.setColor(TEXT_COLOR_DEFAULT);
        textPaint.setTextSize(24.0f);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        linePaint.setColor(LINE_COLOR_DEFAULT);
        linePaint.setStrokeWidth(2);
        linePaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(width,height);
    }

    private int measureWidth(int widthMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthVal = MeasureSpec.getSize(widthMeasureSpec);
        //处理三种模式
        if(widthMode==MeasureSpec.EXACTLY){
            return widthVal+getPaddingLeft()+getPaddingRight();
        }else if(widthMode==MeasureSpec.UNSPECIFIED){
            return OUTER_WIDTH_SIZE;
        }else{
            return Math.min(OUTER_WIDTH_SIZE,widthVal);
        }
    }
    private int measureHeight(int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightVal = MeasureSpec.getSize(heightMeasureSpec);
        //处理三种模式
        if(heightMode==MeasureSpec.EXACTLY){
            return heightVal+getPaddingTop()+getPaddingBottom();
        }else if(heightMode==MeasureSpec.UNSPECIFIED){
            return OUTER_HEIGHT_SIZE;
        }else{
            return Math.min(OUTER_HEIGHT_SIZE,heightVal);
        }
    }

    private void updateAngleAndStrength(){
        if(mCallBack!=null){
            double innerWidthHalf = realWidth / 2 - innerRadius;
            double innerHeightHalf = realHeight / 2 - innerRadius;

            double angle = Math.toDegrees(Math.atan(Math.abs(innerCenterY - realHeight / 2) / Math.abs(innerCenterX - realWidth / 2)));    // 角度
            double strengthX = Math.abs(innerCenterX - (realWidth / 2)) / innerWidthHalf;    // X 轴强度
            double strengthY = Math.abs(innerCenterY - (realHeight) / 2) / innerHeightHalf;   // Y 轴强度
            if(isThrottle)
                strengthY = 1 - (Math.abs(innerCenterY - innerRadius) / (innerHeightHalf * 2));   // Y 轴强度

            // 第一二象限
            if(innerCenterY < realHeight / 2){
                // 第一象限
                if(innerCenterX > realWidth / 2){

                }
                // 第二象限
                else if(innerCenterX < realWidth / 2) {
                    angle = 180 - angle;
                }
                // 90°
                else {
                    angle = 90;
                }
            }
            // 第三四象限
            else if(innerCenterY > realHeight / 2){
                // 第三象限
                if(innerCenterX < realWidth / 2){
                    angle = 180 + angle;
                }
                // 第四象限
                else if(innerCenterX > realWidth / 2){
                    angle = 360 - angle;
                }
                // 270°
                else {
                    angle = 270;
                }
            }
            // 0°或180°
            else {
                if(innerCenterX < realWidth / 2)
                    angle = 180;
                else if(innerCenterX > realWidth / 2)
                    angle = 0;
                else
                    angle = 0;
            }

            mCallBack.onAngleAndStrength(angle, strengthX, strengthY);
        }
    }

    private void drawScale(Canvas canvas){
        float innerHeight = realHeight - (innerRadius * 2);
        float innerWidth = realWidth - (innerRadius * 2);
        float halfInnerHeight = (realHeight - (innerRadius)) / 2;
        float halfInnerWidth = (realWidth - (innerRadius)) / 2;
        float quarterInnerHeight = (realHeight - (innerRadius * 2)) / 4;
        float quarterInnerWidth = (realWidth - (innerRadius * 2)) / 4;

        float innerPaddingBottom = realHeight - innerRadius;
        float innerPaddingRight = realWidth - innerRadius;

        // X轴0%
        canvas.drawLine(0, innerPaddingBottom, innerRadius, innerPaddingBottom, linePaint);
        canvas.drawLine(innerPaddingRight, innerPaddingBottom, realWidth, innerPaddingBottom, linePaint);
        // X轴 25%
        canvas.drawLine(0, innerPaddingBottom - quarterInnerHeight, innerRadius / 2, innerPaddingBottom - quarterInnerHeight, linePaint);
        canvas.drawLine(innerPaddingRight + (innerRadius / 2), innerPaddingBottom - quarterInnerHeight, realWidth, innerPaddingBottom - quarterInnerHeight, linePaint);
        // X轴50%
        canvas.drawLine(0, realHeight / 2, innerRadius, realHeight / 2, linePaint);
        canvas.drawLine(innerPaddingRight, realHeight / 2, realWidth, realHeight / 2, linePaint);
        // X轴 75%
        canvas.drawLine(0, innerRadius + quarterInnerHeight, innerRadius / 2, innerRadius + quarterInnerHeight, linePaint);
        canvas.drawLine(innerPaddingRight + (innerRadius / 2), innerRadius + quarterInnerHeight, realWidth, innerRadius + quarterInnerHeight, linePaint);
        // X轴100%
        canvas.drawLine(0, innerRadius, innerRadius, innerRadius, linePaint);
        canvas.drawLine(innerPaddingRight, innerRadius, realWidth, innerRadius, linePaint);

        // Y轴0%
        canvas.drawLine(innerRadius, 0, innerRadius, innerRadius, linePaint);
        canvas.drawLine(innerRadius, innerPaddingBottom, innerRadius, realHeight, linePaint);
        // Y轴 25%
        canvas.drawLine(innerRadius + quarterInnerWidth, 0, innerRadius + quarterInnerWidth, innerRadius / 2, linePaint);
        canvas.drawLine(innerRadius + quarterInnerWidth, innerPaddingBottom + (innerRadius / 2), innerRadius + quarterInnerWidth, realHeight, linePaint);
        // Y轴50%
        canvas.drawLine(realWidth / 2, 0, realWidth / 2, innerRadius, linePaint);
        canvas.drawLine(realWidth / 2, innerPaddingBottom, realWidth / 2, realHeight, linePaint);
        // Y轴 75%
        canvas.drawLine(innerPaddingRight - quarterInnerWidth, 0, innerPaddingRight - quarterInnerWidth, innerRadius / 2, linePaint);
        canvas.drawLine(innerPaddingRight - quarterInnerWidth, innerPaddingBottom + (innerRadius / 2), innerPaddingRight - quarterInnerWidth, realHeight, linePaint);
        // Y轴100%
        canvas.drawLine(innerPaddingRight, 0, innerPaddingRight, innerRadius, linePaint);
        canvas.drawLine(innerPaddingRight, innerPaddingBottom, innerPaddingRight, realHeight, linePaint);

        // 文字
        if(isThrottle){
            canvas.drawText("0%", 0, innerPaddingBottom, textPaint);
            canvas.drawText("0%", innerPaddingRight, innerPaddingBottom, textPaint);

            canvas.drawText("25%", 0, innerPaddingBottom - quarterInnerHeight, textPaint);
            canvas.drawText("25%", innerPaddingBottom, innerPaddingBottom - quarterInnerHeight, textPaint);

            canvas.drawText("50%", 0, realHeight / 2, textPaint);
            canvas.drawText("50%", innerPaddingRight, realHeight / 2, textPaint);

            canvas.drawText("75%", 0, innerRadius + quarterInnerHeight, textPaint);
            canvas.drawText("75%", innerPaddingRight, innerRadius + quarterInnerHeight, textPaint);

            canvas.drawText("100%", 0, innerRadius, textPaint);
            canvas.drawText("100%", innerPaddingRight, innerRadius, textPaint);
        } else {
            canvas.drawText("0%", 0, realHeight / 2, textPaint);
            canvas.drawText("0%", innerPaddingRight, realHeight / 2, textPaint);

            canvas.drawText("50%", 0, innerRadius + quarterInnerHeight, textPaint);
            canvas.drawText("50%", innerPaddingRight, innerRadius + quarterInnerHeight, textPaint);
            canvas.drawText("50%", 0, innerPaddingBottom - quarterInnerHeight, textPaint);
            canvas.drawText("50%", innerPaddingBottom, innerPaddingBottom - quarterInnerHeight, textPaint);

            canvas.drawText("100%", 0, innerRadius, textPaint);
            canvas.drawText("100%", innerPaddingRight, innerRadius, textPaint);
            canvas.drawText("100%", 0, innerPaddingBottom, textPaint);
            canvas.drawText("100%", innerPaddingRight, innerPaddingBottom, textPaint);
        }
        canvas.drawText("0%", realWidth / 2, innerRadius, textPaint);
        canvas.drawText("0%", realWidth / 2, realHeight, textPaint);

        canvas.drawText("50%", innerRadius + quarterInnerWidth, innerRadius, textPaint);
        canvas.drawText("50%", innerRadius + quarterInnerWidth, realHeight, textPaint);
        canvas.drawText("50%", innerPaddingRight - quarterInnerWidth, innerRadius, textPaint);
        canvas.drawText("50%", innerPaddingRight - quarterInnerWidth, realHeight, textPaint);

        canvas.drawText("100%", innerRadius, innerRadius, textPaint);
        canvas.drawText("100%", innerRadius, realHeight, textPaint);
        canvas.drawText("100%", innerPaddingRight, innerRadius, textPaint);
        canvas.drawText("100%", innerPaddingRight, realHeight, textPaint);

        // 指示线
        canvas.drawLine(0, innerCenterY, realWidth, innerCenterY, linePaint);
        canvas.drawLine(innerCenterX, 0, innerCenterX, realHeight, linePaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        realWidth = w;
        realHeight = h;

        outRadius = Math.min(Math.min(realWidth/2-getPaddingLeft(),realWidth/2-getPaddingRight()),Math.min(realHeight/2-getPaddingTop(),realHeight/2-getPaddingBottom()));
        innerRadius = outRadius*0.25f;

        innerCenterX = realWidth/2;
        // 如果是油门摇杆，油门默认最小
        if(isThrottle) {
            innerCenterY = realHeight - innerRadius;
            yawUnLockLength = realWidth * 0.1f;
        }
        else {
            innerCenterY = realHeight / 2;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画外部容器
        canvas.drawRect(0, 0, realWidth, realHeight, outerPaint);
        //内部圆
        canvas.drawCircle(innerCenterX,innerCenterY,innerRadius,innerPaint);
        drawScale(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            changeInnerCirclePosition(event);
        }
        if(event.getAction()==MotionEvent.ACTION_MOVE){
            changeInnerCirclePosition(event);
        }
        if(event.getAction()==MotionEvent.ACTION_UP){
            innerCenterX = realWidth/2;
            if(!isThrottle) {
                innerCenterY = realHeight / 2;
            }

            updateAngleAndStrength();
            invalidate();
        }
        return true;
    }

    /**
     * 改变内部小圆的位置
     * @param e
     * @see MotionEvent
     */
    private void changeInnerCirclePosition(MotionEvent e) {
        //第一步，确定有效的触摸点集
        float X = e.getX();
        float Y = e.getY();

        // 如果触摸点在自由区域(外部矩形区 - 内部圆半径)内
        boolean isPointInFree = ((X > innerRadius) && (X < (realWidth - innerRadius))) && ((Y > innerRadius) && (Y < (realHeight - innerRadius)));
        if(isPointInFree){
            // 如果偏航摇杆偏移小于偏航距离的10%，不响应偏航操作（提高体验）
            if(isThrottle){
                if((X > (realWidth / 2 - yawUnLockLength)) && (X < (realWidth / 2 + yawUnLockLength))){
                    innerCenterX = realWidth / 2;
                }else{
                    if(X < realWidth / 2 )
                        innerCenterX = X + yawUnLockLength;
                    else
                        innerCenterX = X - yawUnLockLength;
                }
            }else {
                innerCenterX = X;
            }

            innerCenterY = Y;
        }else{
            // 如果是油门摇杆，
            if(isThrottle){
                if(X < (innerRadius - yawUnLockLength))
                    innerCenterX = innerRadius;
                else if (X < innerRadius && X > (innerRadius - yawUnLockLength))
                    innerCenterX = X + yawUnLockLength;
                else if(X > (realWidth - innerRadius) && X < (realWidth - innerRadius + yawUnLockLength))
                    innerCenterX = X - yawUnLockLength;
                else if (X > (realWidth - innerRadius + yawUnLockLength))
                    innerCenterX = realWidth - innerRadius;
                else
                    innerCenterX = X;
            } else {
                if (X < innerRadius)
                    innerCenterX = innerRadius;
                else if (X > realWidth - innerRadius)
                    innerCenterX = realWidth - innerRadius;
                else
                    innerCenterX = X;
            }

            if(Y < innerRadius)
                innerCenterY = innerRadius;
            else if(Y > realHeight - innerRadius)
                innerCenterY = realHeight - innerRadius;
            else
                innerCenterY = Y;
        }

        // 更新角度和强度
        updateAngleAndStrength();
        invalidate();
    }
    public void setOnNavAndSpeedListener(onAngleAndStrengthListenner listener){
        mCallBack = listener;
    }
    public static int dip2px(Context context, float dpValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue*scale +0.5f);
    }
}
