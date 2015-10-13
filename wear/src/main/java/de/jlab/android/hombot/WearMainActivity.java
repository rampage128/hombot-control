package de.jlab.android.hombot;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.prefs.Preferences;

import de.jlab.android.hombot.common.core.HombotStatus;
import de.jlab.android.hombot.common.core.RequestEngine;
import de.jlab.android.hombot.common.settings.SharedSettings;
import de.jlab.android.hombot.common.utils.JoyTouchListener;
import de.jlab.android.hombot.core.WearRequestEngine;

public class WearMainActivity extends WearableActivity implements RequestEngine.RequestListener {

    private static class ViewHolder {
        RelativeLayout container;

        View joyTop;
        View joyRight;
        View joyBottom;
        View joyLeft;
        View joyCenter;

        TextView textBottom;

        ImageView arrowTop;
        ImageView arrowRight;
        ImageView arrowBottom;
        ImageView arrowLeft;

        ProgressBar loader;
    }
    private ViewHolder mViewHolder;

    private WearRequestEngine mWearRequestEngine;
    private HombotStatus mBotStatus;

    private JoyTouchListener mJoyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setAmbientEnabled();

        mWearRequestEngine = new WearRequestEngine();

        mViewHolder = new ViewHolder();
        mViewHolder.container = (RelativeLayout) findViewById(R.id.container);
        mViewHolder.joyTop = findViewById(R.id.joy_top);
        mViewHolder.joyRight = findViewById(R.id.joy_right);
        mViewHolder.joyBottom = findViewById(R.id.joy_bottom);
        mViewHolder.joyLeft = findViewById(R.id.joy_left);
        mViewHolder.joyCenter = findViewById(R.id.joy_center);
        mViewHolder.textBottom = (TextView)findViewById(R.id.text_bottom);
        mViewHolder.arrowTop = (ImageView)findViewById(R.id.arrow_up);
        mViewHolder.arrowRight = (ImageView)findViewById(R.id.arrow_right);
        mViewHolder.arrowBottom = (ImageView)findViewById(R.id.arrow_bottom);
        mViewHolder.arrowLeft = (ImageView)findViewById(R.id.arrow_left);
        mViewHolder.loader = (ProgressBar)findViewById(R.id.loader);

        mViewHolder.loader.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        mViewHolder.container.setOnTouchListener(null);
    }

    @Override
    public void statusUpdate(HombotStatus status) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        int intervalDrive = Integer.parseInt(sp.getString(SharedSettings.PREF_JOY_INTERVAL_DRIVE, "800"));
        int intervalTurn = Integer.parseInt(sp.getString(SharedSettings.PREF_JOY_INTERVAL_TURN, "800"));

        if (mBotStatus == null || mBotStatus.getStatus() != status.getStatus()) {
            if (mJoyListener != null) {
                mJoyListener.stop();
            }
            if (HombotStatus.Status.CHARGING.equals(status.getStatus()) || HombotStatus.Status.WORKING.equals(status.getStatus())) {
                mViewHolder.joyTop.setVisibility(View.INVISIBLE);
                mViewHolder.joyRight.setVisibility(View.INVISIBLE);
                mViewHolder.joyLeft.setVisibility(View.INVISIBLE);
                mViewHolder.joyBottom.setVisibility(View.VISIBLE);
                mViewHolder.loader.setVisibility(View.INVISIBLE);
                mViewHolder.textBottom.setVisibility(View.VISIBLE);

                mViewHolder.arrowTop.setVisibility(View.INVISIBLE);
                mViewHolder.arrowRight.setVisibility(View.INVISIBLE);
                mViewHolder.arrowBottom.setVisibility(View.INVISIBLE);
                mViewHolder.arrowLeft.setVisibility(View.INVISIBLE);

                if (HombotStatus.Status.CHARGING.equals(status.getStatus())) {
                    mViewHolder.textBottom.setText(getString(R.string.undock));
                    mJoyListener = new DockedTouchListener(intervalDrive, intervalTurn);
                } else {
                    mViewHolder.loader.setVisibility(View.VISIBLE);
                    mViewHolder.textBottom.setText(getString(R.string.stop_cleaning, status.getMode().name()));
                    mJoyListener = new WorkingTouchListener(intervalDrive, intervalTurn);
                }


            } else if (HombotStatus.Status.OFFLINE.equals(status.getStatus()) || HombotStatus.Status.DOCKING.equals(status.getStatus()) || HombotStatus.Status.UNDOCKING.equals(status.getStatus())) {
                mViewHolder.joyTop.setVisibility(View.INVISIBLE);
                mViewHolder.joyRight.setVisibility(View.INVISIBLE);
                mViewHolder.joyLeft.setVisibility(View.INVISIBLE);
                mViewHolder.joyBottom.setVisibility(View.INVISIBLE);
                mViewHolder.textBottom.setVisibility(View.INVISIBLE);
                mViewHolder.loader.setVisibility(View.VISIBLE);
                if (HombotStatus.Status.OFFLINE.equals(status.getStatus())) {
                    mViewHolder.loader.setVisibility(View.INVISIBLE);
                } else {
                    mViewHolder.loader.setVisibility(View.VISIBLE);
                }
                mJoyListener = null;
            } else {

                mViewHolder.joyTop.setVisibility(View.VISIBLE);
                mViewHolder.joyRight.setVisibility(View.VISIBLE);
                mViewHolder.joyLeft.setVisibility(View.VISIBLE);
                mViewHolder.joyBottom.setVisibility(View.VISIBLE);
                mViewHolder.loader.setVisibility(View.INVISIBLE);
                mViewHolder.textBottom.setVisibility(View.INVISIBLE);

                mViewHolder.arrowTop.setVisibility(View.VISIBLE);
                mViewHolder.arrowRight.setVisibility(View.VISIBLE);
                mViewHolder.arrowBottom.setVisibility(View.VISIBLE);
                mViewHolder.arrowLeft.setVisibility(View.VISIBLE);

                mJoyListener = new UndockedTouchListener(intervalDrive, intervalTurn);
            }
        }
        mBotStatus = status;
        mViewHolder.container.setOnTouchListener(mJoyListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mWearRequestEngine.connect(this);
        mWearRequestEngine.start(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mWearRequestEngine.disconnect();
        mWearRequestEngine.stop();
    }

      ////////////////////////////////////
     /// TOUCH LISTENERS ////////////////
    ///////////////////////////////////

    private class DockedTouchListener extends JoyTouchListener {

        public DockedTouchListener(int driveInterval, int turnInterval) {
            super(driveInterval, turnInterval, new JoyTouchListener.PushListener[] {
                    new JoyTouchListener.PushListener() {
                        @Override
                        public void onPush() {}

                        @Override
                        public void onRelease() {}
                    },
                    new JoyTouchListener.PushListener() {
                        @Override
                        public void onPush() {}

                        @Override
                        public void onRelease() {}
                    },
                    new JoyTouchListener.PushListener() {
                        @Override
                        public void onPush() {
                            mWearRequestEngine.sendCommand(RequestEngine.Command.JOY_BACK);
                            Log.d("MOT", "B");
                        }

                        @Override
                        public void onRelease() {}
                    },
                    new JoyTouchListener.PushListener() {
                        @Override
                        public void onPush() {}

                        @Override
                        public void onRelease() {}
                    },
                    new JoyTouchListener.PushListener() {
                        @Override
                        public void onPush() {}

                        @Override
                        public void onRelease() { /* NO RELEASE FOR CENTER COMMAND */ }
                    }
            });
        }
    }

    private class UndockedTouchListener extends JoyTouchListener {

        public UndockedTouchListener(int driveInterval, int turnInterval) {
            super(driveInterval, turnInterval, new JoyTouchListener.PushListener[] {
                    new JoyTouchListener.PushListener() {
                        @Override
                        public void onPush() {
                            mWearRequestEngine.sendCommand(RequestEngine.Command.JOY_FORWARD);
                            Log.d("MOT", "F");
                        }

                        @Override
                        public void onRelease() {
                            mWearRequestEngine.sendCommand(RequestEngine.Command.JOY_RELEASE);
                            Log.d("MOT", "-");
                        }
                    },
                    new JoyTouchListener.PushListener() {
                        @Override
                        public void onPush() {
                            mWearRequestEngine.sendCommand(RequestEngine.Command.JOY_RIGHT);
                            Log.d("MOT", "R");
                        }

                        @Override
                        public void onRelease() {
                            mWearRequestEngine.sendCommand(RequestEngine.Command.JOY_RELEASE);
                            Log.d("MOT", "-");
                        }
                    },
                    new JoyTouchListener.PushListener() {
                        @Override
                        public void onPush() {
                            mWearRequestEngine.sendCommand(RequestEngine.Command.JOY_BACK);
                            Log.d("MOT", "B");
                        }

                        @Override
                        public void onRelease() {
                            mWearRequestEngine.sendCommand(RequestEngine.Command.JOY_RELEASE);
                            Log.d("MOT", "-");
                        }
                    },
                    new JoyTouchListener.PushListener() {
                        @Override
                        public void onPush() {
                            mWearRequestEngine.sendCommand(RequestEngine.Command.JOY_LEFT);
                            Log.d("MOT", "L");
                        }

                        @Override
                        public void onRelease() {
                            mWearRequestEngine.sendCommand(RequestEngine.Command.JOY_RELEASE);
                            Log.d("MOT", "-");
                        }
                    },
                    new JoyTouchListener.PushListener() {
                        @Override
                        public void onPush() {
                            Log.d("MOT", "SHOW OVERFLOW");
                        }

                        @Override
                        public void onRelease() { /* NO RELEASE FOR CENTER COMMAND */ }
                    }
            });
        }
    }

    private class WorkingTouchListener extends JoyTouchListener {

        public WorkingTouchListener(int driveInterval, int turnInterval) {
            super(driveInterval, turnInterval, new JoyTouchListener.PushListener[] {
                    new JoyTouchListener.PushListener() {
                        @Override
                        public void onPush() {}

                        @Override
                        public void onRelease() {}
                    },
                    new JoyTouchListener.PushListener() {
                        @Override
                        public void onPush() {}

                        @Override
                        public void onRelease() {}
                    },
                    new JoyTouchListener.PushListener() {
                        @Override
                        public void onPush() {
                            mWearRequestEngine.sendCommand(RequestEngine.Command.PAUSE);
                        }

                        @Override
                        public void onRelease() {}
                    },
                    new JoyTouchListener.PushListener() {
                        @Override
                        public void onPush() {}

                        @Override
                        public void onRelease() {}
                    },
                    new JoyTouchListener.PushListener() {
                        @Override
                        public void onPush() {}

                        @Override
                        public void onRelease() {}
                    }
            });
        }
    }

}
