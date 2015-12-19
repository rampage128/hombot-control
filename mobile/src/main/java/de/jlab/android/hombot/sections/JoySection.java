package de.jlab.android.hombot.sections;

import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.jlab.android.hombot.R;
import de.jlab.android.hombot.SectionFragment;
import de.jlab.android.hombot.SettingsActivity;
import de.jlab.android.hombot.common.settings.SharedSettings;
import de.jlab.android.hombot.core.HttpRequestEngine;
import de.jlab.android.hombot.common.utils.JoyTouchListener;
import de.jlab.android.hombot.utils.Colorizer;

/**
 * A {@link SectionFragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SectionFragment.SectionInteractionListener} interface
 * to handle interaction events.
 * Use the {@link JoySection#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JoySection extends SectionFragment {

    private static class ViewHolder {
        Button commandMySpace;
        Button commandSpiral;
        Button commandTurbo;
        Button commandHome;
        View joy;
        TextView joyLabel;
    }

    private ViewHolder mViewHolder;

    public static JoySection newInstance(int sectionNumber) {
        JoySection fragment = new JoySection();
        fragment.register(sectionNumber);
        return fragment;
    }

    private class InstaCleanHandler extends Handler {

        public static final int SPIRAL = 1;

        private boolean mInsta;

        public InstaCleanHandler(boolean insta) {
            mInsta = insta;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    sendCommand(HttpRequestEngine.Command.MODE_SPIRAL);
                    if (mInsta) {
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException ignored) {}
                        sendCommand(HttpRequestEngine.Command.START);
                    }
            }
        }
    }
    private InstaCleanHandler mInstaCleanHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_section_joy, container, false);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mInstaCleanHandler = new InstaCleanHandler(sp.getBoolean(SettingsActivity.PREF_JOY_INSTACLEAN, false));

        mViewHolder = new ViewHolder();
        mViewHolder.commandMySpace = (Button) view.findViewById(R.id.cm_mode_myspace);
        mViewHolder.commandSpiral = (Button) view.findViewById(R.id.cm_mode_spiral);
        mViewHolder.commandTurbo = (Button) view.findViewById(R.id.cm_turbo);
        mViewHolder.commandHome = (Button) view.findViewById(R.id.cm_home);
        mViewHolder.joy = view.findViewById(R.id.ct_joy);
        mViewHolder.joyLabel = (TextView)view.findViewById(R.id.ct_joy_label);


        mViewHolder.commandMySpace.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand(HttpRequestEngine.Command.MODE_MYSPACE);
            }
        });

        mViewHolder.commandSpiral.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!mInstaCleanHandler.hasMessages(InstaCleanHandler.SPIRAL)) {
                    mInstaCleanHandler.sendEmptyMessage(InstaCleanHandler.SPIRAL);
                }
            }
        });

        mViewHolder.commandTurbo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand(HttpRequestEngine.Command.TURBO);
            }
        });

        mViewHolder.commandHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand(HttpRequestEngine.Command.HOME);
            }
        });

        int intervalDrive = Integer.parseInt(sp.getString(SharedSettings.PREF_JOY_INTERVAL_DRIVE, "800"));
        int intervalTurn = Integer.parseInt(sp.getString(SharedSettings.PREF_JOY_INTERVAL_TURN, "800"));

        mViewHolder.joy.setOnTouchListener(new JoyTouchListener(intervalDrive, intervalTurn, new JoyTouchListener.PushListener[]{
                new JoyTouchListener.PushListener() {
                    @Override
                    public void onPush() {
                        sendCommand(HttpRequestEngine.Command.JOY_FORWARD);
                        Log.d("MOT", "F");
                    }

                    @Override
                    public void onRelease() {
                        sendCommand(HttpRequestEngine.Command.JOY_RELEASE);
                        Log.d("MOT", "-");
                    }
                },
                new JoyTouchListener.PushListener() {
                    @Override
                    public void onPush() {
                        sendCommand(HttpRequestEngine.Command.JOY_RIGHT);
                        Log.d("MOT", "R");
                    }

                    @Override
                    public void onRelease() {
                        sendCommand(HttpRequestEngine.Command.JOY_RELEASE);
                        Log.d("MOT", "-");
                    }
                },
                new JoyTouchListener.PushListener() {
                    @Override
                    public void onPush() {
                        sendCommand(HttpRequestEngine.Command.JOY_BACK);
                        Log.d("MOT", "B");
                    }

                    @Override
                    public void onRelease() {
                        sendCommand(HttpRequestEngine.Command.JOY_RELEASE);
                        Log.d("MOT", "-");
                    }
                },
                new JoyTouchListener.PushListener() {
                    @Override
                    public void onPush() {
                        sendCommand(HttpRequestEngine.Command.JOY_LEFT);
                        Log.d("MOT", "L");
                    }

                    @Override
                    public void onRelease() {
                        sendCommand(HttpRequestEngine.Command.JOY_RELEASE);
                        Log.d("MOT", "-");
                    }
                },
                new JoyTouchListener.PushListener() {
                    @Override
                    public void onPush() {
                        sendCommand(HttpRequestEngine.Command.PAUSE);
                        Log.d("MOT", "P");
                    }

                    @Override
                    public void onRelease() { /* NO RELEASE FOR CENTER COMMAND */ }
                }
        }));

        // MAKE JOYPAD "SQUARE" ACCORDING TO THE SMALLER AVAILABLE DIMENSION FIXME BUGGY ON ORIENTATION CHANGE
        if (mViewHolder.joy.getViewTreeObserver().isAlive()) {
            mViewHolder.joy.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int finalSize = Math.min(mViewHolder.joy.getMeasuredWidth(), mViewHolder.joy.getMeasuredHeight());
                    mViewHolder.joy.setLayoutParams(new RelativeLayout.LayoutParams(finalSize, finalSize));
                    view.invalidate();
                }
            });
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Colorizer colorizer = getColorizer();
        int textColor = colorizer.getColorText();

        colorizer.colorizeButton(mViewHolder.commandHome, textColor);
        colorizer.colorizeButton(mViewHolder.commandSpiral, textColor);
        colorizer.colorizeButton(mViewHolder.commandMySpace, textColor);
        colorizer.colorizeButton(mViewHolder.commandTurbo, textColor);
        mViewHolder.joy.getBackground().setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);
        mViewHolder.joyLabel.setTextColor(textColor);
    }

}
