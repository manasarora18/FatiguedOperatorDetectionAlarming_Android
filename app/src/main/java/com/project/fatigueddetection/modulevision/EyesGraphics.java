package com.project.fatigueddetection.modulevision;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.MediaPlayer;
import android.util.Log;

import com.project.fatigueddetection.R;

public class EyesGraphics extends GraphicOverlay.Graphic {
    private static final float EYE_RADIUS_PROPORTION = 0.45f;
    private static final float IRIS_RADIUS_PROPORTION = EYE_RADIUS_PROPORTION / 2.0f;
    private Paint mEyeWhitesPaint;
    private Paint mEyeIrisPaint;
    private Paint mEyeOutlinePaint;
    private Paint mEyeLidPaint;
    private EyeTracker mLeftPhysics = new EyeTracker();
    private EyeTracker mRightPhysics = new EyeTracker();

    private volatile PointF mLeftPosition;
    private volatile boolean mLeftOpen;
    private volatile PointF mRightPosition;
    private volatile boolean mRightOpen;
    private Context context;
    private MediaPlayer mediaPlayer = null;

    //Eye
    EyesGraphics(GraphicOverlay overlay, Context context) {
        super(overlay);
        this.context = context;

        mEyeWhitesPaint = new Paint();
        mEyeWhitesPaint.setColor(Color.TRANSPARENT);
        mEyeWhitesPaint.setStyle(Paint.Style.FILL);

        mEyeLidPaint = new Paint();
        mEyeLidPaint.setColor(Color.RED);
        mEyeLidPaint.setStyle(Paint.Style.FILL);

        mEyeIrisPaint = new Paint();
        mEyeIrisPaint.setColor(Color.BLUE);
        mEyeIrisPaint.setStyle(Paint.Style.STROKE);
        mEyeIrisPaint.setStrokeWidth(2);

        mEyeOutlinePaint = new Paint();
        mEyeOutlinePaint.setColor(Color.BLACK);
        mEyeOutlinePaint.setStyle(Paint.Style.STROKE);
        mEyeOutlinePaint.setStrokeWidth(3);


    }

    //RecentEyesPosition
    void updateEyes(PointF leftPosition, boolean leftOpen,
                    PointF rightPosition, boolean rightOpen) {
        mLeftPosition = leftPosition;
        mLeftOpen = leftOpen;

        mRightPosition = rightPosition;
        mRightOpen = rightOpen;

        postInvalidate();
    }

    //CurrentEyeCanvas
    @Override
    public void draw(Canvas canvas) {
        PointF detectLeftPosition = mLeftPosition;
        PointF detectRightPosition = mRightPosition;
        if ((detectLeftPosition == null) || (detectRightPosition == null)) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            return;
        }

        PointF leftPosition =
                new PointF(translateX(detectLeftPosition.x), translateY(detectLeftPosition.y));
        PointF rightPosition =
                new PointF(translateX(detectRightPosition.x), translateY(detectRightPosition.y));

        // inter-eye distance as the size of the eyes.
        float distance = (float) Math.sqrt(
                Math.pow(rightPosition.x - leftPosition.x, 2) +
                        Math.pow(rightPosition.y - leftPosition.y, 2));
        float eyeRadius = EYE_RADIUS_PROPORTION * distance;
        float irisRadius = IRIS_RADIUS_PROPORTION * distance;

        // update current iris position, draw left eye
        PointF leftIrisPosition =
                mLeftPhysics.nextIrisPosition(leftPosition, eyeRadius, irisRadius);
        drawEye(canvas, leftPosition, eyeRadius, leftIrisPosition, irisRadius, mLeftOpen);

        // update current iris position, draw right eye
        PointF rightIrisPosition =
                mRightPhysics.nextIrisPosition(rightPosition, eyeRadius, irisRadius);
        drawEye(canvas, rightPosition, eyeRadius, rightIrisPosition, irisRadius, mRightOpen);
    }

    //EyeCanvas drawing
    private void drawEye(Canvas canvas, PointF eyePosition, float eyeRadius,
                         PointF irisPosition, float irisRadius, boolean isOpen) {

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.beep);
        }

        if (isOpen) {
            canvas.drawCircle(eyePosition.x, eyePosition.y, eyeRadius, mEyeWhitesPaint);
            canvas.drawCircle(irisPosition.x, irisPosition.y, irisRadius, mEyeIrisPaint);
            Log.i("MEDIA", "INOpen Method but not in stop");
            if (mediaPlayer.isPlaying()) {
                Log.i("MEDIA", "Stop");
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }

        } else {
            canvas.drawCircle(eyePosition.x, eyePosition.y, eyeRadius, mEyeLidPaint);
            float y = eyePosition.y;
            float start = eyePosition.x - eyeRadius;
            float end = eyePosition.x + eyeRadius;
            canvas.drawLine(start, y, end, y, mEyeOutlinePaint);
            Log.i("MEDIA", "Start");
            mediaPlayer.start();
        }
        canvas.drawCircle(eyePosition.x, eyePosition.y, eyeRadius, mEyeOutlinePaint);
    }
}