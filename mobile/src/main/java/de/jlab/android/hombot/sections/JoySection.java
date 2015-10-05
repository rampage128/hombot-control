package de.jlab.android.hombot.sections;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import de.jlab.android.hombot.R;
import de.jlab.android.hombot.SectionFragment;
import de.jlab.android.hombot.core.RequestEngine;
import de.jlab.android.hombot.sections.joy.JoyTouchListener;
import de.jlab.android.hombot.utils.RepeatListener;

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
    }

    private ViewHolder mViewHolder;

    public static JoySection newInstance(int sectionNumber) {
        JoySection fragment = new JoySection();
        fragment.register(sectionNumber);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_section_joy, container, false);

        mViewHolder = new ViewHolder();
        mViewHolder.commandMySpace = (Button) view.findViewById(R.id.cm_mode_myspace);
        mViewHolder.commandSpiral = (Button) view.findViewById(R.id.cm_mode_spiral);
        mViewHolder.commandTurbo = (Button) view.findViewById(R.id.cm_turbo);
        mViewHolder.commandHome = (Button) view.findViewById(R.id.cm_home);
        mViewHolder.joy = view.findViewById(R.id.ct_joy);


        mViewHolder.commandMySpace.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand(RequestEngine.Command.MODE_MYSPACE);
            }
        });

        mViewHolder.commandSpiral.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand(RequestEngine.Command.MODE_SPIRAL);
                sendCommand(RequestEngine.Command.START);
            }
        });

        mViewHolder.commandTurbo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand(RequestEngine.Command.TURBO);
            }
        });

        mViewHolder.commandHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand(RequestEngine.Command.HOME);
            }
        });


        // TODO FOR TESTING!
        mViewHolder.joy.getBackground().setColorFilter(0xffffffff, PorterDuff.Mode.MULTIPLY);

        mViewHolder.joy.setOnTouchListener(new JoyTouchListener(800, 800, new JoyTouchListener.PushListener[]{
                new JoyTouchListener.PushListener() {
                    @Override
                    public void onPush() {
                        sendCommand(RequestEngine.Command.JOY_FORWARD);
                        Log.d("MOT", "F");
                    }

                    @Override
                    public void onRelease() {
                        sendCommand(RequestEngine.Command.JOY_RELEASE);
                        Log.d("MOT", "-");
                    }
                },
                new JoyTouchListener.PushListener() {
                    @Override
                    public void onPush() {
                        sendCommand(RequestEngine.Command.JOY_RIGHT);
                        Log.d("MOT", "R");
                    }

                    @Override
                    public void onRelease() {
                        sendCommand(RequestEngine.Command.JOY_RELEASE);
                        Log.d("MOT", "-");
                    }
                },
                new JoyTouchListener.PushListener() {
                    @Override
                    public void onPush() {
                        sendCommand(RequestEngine.Command.JOY_BACK);
                        Log.d("MOT", "B");
                    }

                    @Override
                    public void onRelease() {
                        sendCommand(RequestEngine.Command.JOY_RELEASE);
                        Log.d("MOT", "-");
                    }
                },
                new JoyTouchListener.PushListener() {
                    @Override
                    public void onPush() {
                        sendCommand(RequestEngine.Command.JOY_LEFT);
                        Log.d("MOT", "L");
                    }

                    @Override
                    public void onRelease() {
                        sendCommand(RequestEngine.Command.JOY_RELEASE);
                        Log.d("MOT", "-");
                    }
                },
                new JoyTouchListener.PushListener() {
                    @Override
                    public void onPush() {
                        sendCommand(RequestEngine.Command.PAUSE);
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
                    ((LinearLayout) mViewHolder.joy).setLayoutParams(new FrameLayout.LayoutParams(finalSize, finalSize));
                    view.invalidate();
                }
            });
        }

        return view;
    }

    private void handleAction(Runnable runner, MotionEvent event, Handler repeatedHandler) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                repeatedHandler.removeCallbacks(runner);
                repeatedHandler.postDelayed(runner, 0);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                repeatedHandler.removeCallbacks(runner);
                break;
        }
    }

}
