package com.project.fatigueddetection.modulevision;

import android.graphics.PointF;
import android.os.SystemClock;

public class EyeTracker {
    private final long TIME_MS = 1000;

    private final float FRICTION_COEFF = 2.2f;
    private final float GRAVITY_COEFF = 0.5f;

    private final float BOUNCE_MUL = 0.8f;

    private final float ZERO_TOLERANCE = 0.001f; //converge to zero more quickly

    private long mLastUpdateTimeMs = SystemClock.elapsedRealtime();

    private PointF mEyePosition;
    private float mEyeRadius;

    private PointF mIrisPosition;
    private float mIrisRadius;

    private float vx = 0.0f;  //Scaling based on eye size
    private float vy = 0.0f;

    private int mConsecutiveBounces = 0; //keeping track, iris movement when too fast

    PointF nextIrisPosition(PointF eyePosition, float eyeRadius, float irisRadius) {
        mEyePosition = eyePosition;
        mEyeRadius = eyeRadius;

        if (mIrisPosition == null) {
            mIrisPosition = eyePosition;
        }
        mIrisRadius = irisRadius;

        long nowMs = SystemClock.elapsedRealtime();
        long elapsedTimeMs = nowMs - mLastUpdateTimeMs;
        float simulationRate = (float) elapsedTimeMs / TIME_MS;
        mLastUpdateTimeMs = nowMs;

        if (!isStopped()) {
            vy += GRAVITY_COEFF * simulationRate;
        }

        vx = applyFriction(vx, simulationRate);
        vy = applyFriction(vy, simulationRate);

        float x = mIrisPosition.x + (vx * mIrisRadius * simulationRate);
        float y = mIrisPosition.y + (vy * mIrisRadius * simulationRate);
        mIrisPosition = new PointF(x, y);

        makeIrisInBounds(simulationRate);
        return mIrisPosition;
    }

    private float applyFriction(float velocity, float simulationRate) {
        if (isZero(velocity)) {
            velocity = 0.0f;
        } else if (velocity > 0) {
            velocity = Math.max(0.0f, velocity - (FRICTION_COEFF * simulationRate));
        } else {
            velocity = Math.min(0.0f, velocity + (FRICTION_COEFF * simulationRate));
        }
        return velocity;
    }

   //To keep iris inside the eye, with fast movement, iris may go out of bounds
    private void makeIrisInBounds(float simulationRate) {
        float irisOffsetX = mIrisPosition.x - mEyePosition.x;
        float irisOffsetY = mIrisPosition.y - mEyePosition.y;

        float maxDistance = mEyeRadius - mIrisRadius;
        float distance = (float) Math.sqrt(Math.pow(irisOffsetX, 2) + Math.pow(irisOffsetY, 2));
        if (distance <= maxDistance) {
           //no correction
            mConsecutiveBounces = 0;
            return;
        }

       //dampen the momentum of fast movement
        mConsecutiveBounces++;


        float ratio = maxDistance / distance;
        float x = mEyePosition.x + (ratio * irisOffsetX);
        float y = mEyePosition.y + (ratio * irisOffsetY);


        float dx = x - mIrisPosition.x;
        vx = applyBounce(vx, dx, simulationRate) / mConsecutiveBounces;

        float dy = y - mIrisPosition.y;
        vy = applyBounce(vy, dy, simulationRate) / mConsecutiveBounces;

        mIrisPosition = new PointF(x, y);
    }


    private float applyBounce(float velocity, float distOutOfBounds, float simulationRate) {
        if (isZero(distOutOfBounds)) {

            return velocity;
        }

        velocity *= -1;

        float bounce = BOUNCE_MUL * Math.abs(distOutOfBounds / mIrisRadius);
        if (velocity > 0) {
            velocity += bounce * simulationRate;
        } else {
            velocity -= bounce * simulationRate;
        }
        return velocity;
    }

    private boolean isStopped() {
        if (mEyePosition.y >= mIrisPosition.y) {
            return false;
        }

        float irisOffsetY = mIrisPosition.y - mEyePosition.y;
        float maxDistance = mEyeRadius - mIrisRadius;
        if (irisOffsetY < maxDistance) {
            return false;
        }
        return (isZero(vx) && isZero(vy));
    }

    //ZeroTolerance
    private boolean isZero(float num) {
        return ((num < ZERO_TOLERANCE) && (num > -1 * ZERO_TOLERANCE));
    }
}
