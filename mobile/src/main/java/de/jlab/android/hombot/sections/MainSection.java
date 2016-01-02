package de.jlab.android.hombot.sections;

import android.content.SharedPreferences;
import android.os.Bundle;
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
import de.jlab.android.hombot.common.core.HombotStatus;
import de.jlab.android.hombot.common.settings.SharedSettings;
import de.jlab.android.hombot.common.utils.JoyTouchListener;
import de.jlab.android.hombot.core.HttpRequestEngine;
import de.jlab.android.hombot.sections.common.StatusFragment;
import de.jlab.android.hombot.utils.Colorizer;

/**
 * Created by frede_000 on 31.12.2015.
 */
public class MainSection extends SectionFragment {

    static class ViewHolder {
        StatusFragment statusDisplay;

        Button commandStartStop;
        Button commandTurbo;
        Button commandMode;
        Button commandHome;
        Button commandRepeat;

        View joy;
        TextView joyLabel;
    }
    private ViewHolder mViewHolder;

    private boolean isDocked = false;
    private boolean isWorking = false;
    private HombotStatus.Mode mode = null;

    public static MainSection newInstance(int sectionNumber) {
        MainSection fragment = new MainSection();
        fragment.register(sectionNumber);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_section_main, container, false);

        mViewHolder = new ViewHolder();


        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mViewHolder.statusDisplay = (StatusFragment) getChildFragmentManager().findFragmentById(R.id.ct_status);
        mViewHolder.commandStartStop = (Button) view.findViewById(R.id.cm_startstop);
        mViewHolder.commandTurbo = (Button) view.findViewById(R.id.cm_turbo);
        mViewHolder.commandMode = (Button) view.findViewById(R.id.cm_mode);
        mViewHolder.commandHome = (Button) view.findViewById(R.id.cm_home);
        mViewHolder.commandRepeat = (Button) view.findViewById(R.id.cm_repeat);
        mViewHolder.joy = view.findViewById(R.id.ct_joy);
        mViewHolder.joyLabel = (TextView)view.findViewById(R.id.ct_joy_label);

        // INIT COMMANDS
        mViewHolder.commandTurbo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand(HttpRequestEngine.Command.TURBO);
            }
        });

        mViewHolder.commandMode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isDocked) {
                    if (HombotStatus.Mode.ZIGZAG.equals(MainSection.this.mode)) {
                        sendCommand(HttpRequestEngine.Command.MODE_CELLBYCELL);
                    } else {
                        sendCommand(HttpRequestEngine.Command.MODE_ZIGZAG);
                    }
                } else {
                    sendCommand(HttpRequestEngine.Command.MODE);
                }
            }
        });

        mViewHolder.commandHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand(HttpRequestEngine.Command.HOME);
            }
        });

        mViewHolder.commandRepeat.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand(HttpRequestEngine.Command.REPEAT);
            }
        });

        mViewHolder.commandStartStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isWorking) {
                    sendCommand(HttpRequestEngine.Command.PAUSE);
                } else {
                    sendCommand(HttpRequestEngine.Command.START);
                }
            }
        });

        // INIT JOYSTICK
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
                    getView().invalidate();
                }
            });
        }

        Colorizer colorizer = getColorizer();

        mViewHolder.statusDisplay.colorize(colorizer);

        /*
        colorizer.colorizeButton(mViewHolder.commandHome, colorizer.getColorText());
        colorizer.colorizeButton(mViewHolder.commandMode, colorizer.getColorText());
        colorizer.colorizeButton(mViewHolder.commandRepeat, colorizer.getColorText());
        colorizer.colorizeButton(mViewHolder.commandTurbo, colorizer.getColorText());
        colorizer.colorizeButton(mViewHolder.commandStartStop, colorizer.getColorText());
        */
    }

    @Override
    public void statusUpdate(HombotStatus status) {

        this.isDocked = HombotStatus.Status.CHARGING.equals(status.getStatus());
        this.isWorking = HombotStatus.Status.WORKING.equals(status.getStatus()) || HombotStatus.Status.HOMING.equals(status.getStatus());
        this.mode = status.getMode();

        mViewHolder.statusDisplay.statusUpdate(status);
    }

}
