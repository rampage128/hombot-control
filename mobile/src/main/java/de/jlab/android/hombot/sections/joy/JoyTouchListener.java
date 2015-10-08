package de.jlab.android.hombot.sections.joy;

import android.graphics.Point;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Vector;

import de.jlab.android.hombot.core.RequestEngine;
import de.jlab.android.hombot.utils.RepeatListener;

/**
 * Created by frede_000 on 03.10.2015.
 */
public class JoyTouchListener implements View.OnTouchListener {

    private static final int INTERVAL_NO_REPEAT = -1;

    private Handler handler = new Handler();

    private int mTurnInterval = INTERVAL_NO_REPEAT;
    private int mDriveInterval = INTERVAL_NO_REPEAT;

    private PushListener mPushListeners[];

    private PushListener mCurrentAction;
    private int mCurrentInterval = INTERVAL_NO_REPEAT;

    private Runnable handlerRunnable = new Runnable() {
        @Override
        public void run() {
            if (mCurrentInterval != INTERVAL_NO_REPEAT) {
                handler.postDelayed(this, mCurrentInterval);
            }
            mCurrentAction.onPush();
        }
    };

    public JoyTouchListener(int driveInterval, int turnInterval, PushListener[] pushListeners) {
        this.mDriveInterval = driveInterval;
        this.mTurnInterval = turnInterval;

        mPushListeners = pushListeners;
    }

    private Point lSize = new Point(), lCenter = new Point(), lVec = new Point(), lRef = new Point(0, -1);
    private float lLen = 0, lAng = 0;
    private MotionEvent.PointerCoords lCoords = new MotionEvent.PointerCoords();
    private PushListener lNewAction;

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        motionEvent.getPointerCoords(0, lCoords);

        lSize.set(view.getMeasuredWidth(), view.getMeasuredHeight());
        lCenter.set(lSize.x / 2, lSize.y / 2);
        lVec.set((int) lCoords.getAxisValue(MotionEvent.AXIS_X) - lCenter.x, (int) lCoords.getAxisValue(MotionEvent.AXIS_Y) - lCenter.y);
        lLen = (float) Math.sqrt(lVec.x * lVec.x + lVec.y * lVec.y);

        if (lLen < lSize.x / 4) { // CENTER
            lNewAction = mPushListeners[4];
            mCurrentInterval = INTERVAL_NO_REPEAT;
        } else {
            lAng = (float)(Math.acos((lVec.x * lRef.x + lVec.y * lRef.y) / (lLen * 1))  * (180 / Math.PI)); // acos(DOT / (LEN1 * LEN2))
            if (lAng < 45) { // TOP
                lNewAction = mPushListeners[0];
                mCurrentInterval = mDriveInterval;
            } else if (lAng > 135) { // BOTTOM
                lNewAction = mPushListeners[2];
                mCurrentInterval = mDriveInterval;
            } else if (lVec.x < 0) { // LEFT
                lNewAction = mPushListeners[3];
                mCurrentInterval = mTurnInterval;
            } else if (lVec.x > 0) { // RIGHT
                lNewAction = mPushListeners[1];
                mCurrentInterval = mTurnInterval;
            }
        }

        if (lNewAction != mCurrentAction && mCurrentAction != null) {
            mCurrentAction.onRelease();
        }
        mCurrentAction = lNewAction;

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handler.removeCallbacks(handlerRunnable);
                if (mCurrentInterval != INTERVAL_NO_REPEAT) {
                    handler.postDelayed(handlerRunnable, mCurrentInterval);
                }
                mCurrentAction.onPush();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handler.removeCallbacks(handlerRunnable);
                mCurrentAction.onRelease();
                mCurrentAction = null;
                return true;
        }

        return false;
    }

    public interface PushListener {
        void onPush();
        void onRelease();
    }

}
