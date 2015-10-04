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

    public static JoySection newInstance(int sectionNumber) {
        JoySection fragment = new JoySection();
        fragment.register(sectionNumber);
        return fragment;
    }

      @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_section_joy, container, false);

        ((Button)view.findViewById(R.id.cm_mode_myspace)).setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
              sendCommand(RequestEngine.Command.MODE_MYSPACE);
          }
        });

        ((Button)view.findViewById(R.id.cm_mode_spiral)).setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
              sendCommand(RequestEngine.Command.MODE_SPIRAL);
              sendCommand(RequestEngine.Command.START);
          }
        });

        ((Button)view.findViewById(R.id.cm_turbo)).setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
              sendCommand(RequestEngine.Command.TURBO);
          }
        });

        ((Button)view.findViewById(R.id.cm_home)).setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
              sendCommand(RequestEngine.Command.HOME);
          }
        });

/*
        view.findViewById(R.id.cm_joy_forward).setOnTouchListener(new RepeatListener(800, 800, new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              sendCommand(RequestEngine.Command.JOY_FORWARD);
          }
        }, new RepeatListener.ReleaseListener() {
          public void onRelease() {
              sendCommand(RequestEngine.Command.JOY_RELEASE);
          }
        }));

        view.findViewById(R.id.cm_joy_left).setOnTouchListener(new RepeatListener(800, 800, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCommand(RequestEngine.Command.JOY_LEFT);
            }
        }, new RepeatListener.ReleaseListener() {
            public void onRelease() {
                sendCommand(RequestEngine.Command.JOY_RELEASE);
            }
        }));

        view.findViewById(R.id.cm_joy_right).setOnTouchListener(new RepeatListener(800, 800, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCommand(RequestEngine.Command.JOY_RIGHT);
            }
        }, new RepeatListener.ReleaseListener() {
            public void onRelease() {
                sendCommand(RequestEngine.Command.JOY_RELEASE);
            }
        }));

        view.findViewById(R.id.cm_joy_back).setOnTouchListener(new RepeatListener(800, 800, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCommand(RequestEngine.Command.JOY_BACK);
            }
        }, new RepeatListener.ReleaseListener() {
            public void onRelease() {
                sendCommand(RequestEngine.Command.JOY_RELEASE);
            }
        }));
*/
        final View joy = view.findViewById(R.id.ct_joy);

        joy.getBackground().setColorFilter(0xffffffff, PorterDuff.Mode.MULTIPLY);

        joy.setOnTouchListener(new JoyTouchListener(800, 800, new JoyTouchListener.PushListener[] {
                new JoyTouchListener.PushListener() {
                    @Override
                    public void onPush() { sendCommand(RequestEngine.Command.JOY_FORWARD); Log.d("MOT", "F"); }
                    @Override
                    public void onRelease() { sendCommand(RequestEngine.Command.JOY_RELEASE); Log.d("MOT", "-"); }
                },
                new JoyTouchListener.PushListener() {
                    @Override
                    public void onPush() { sendCommand(RequestEngine.Command.JOY_RIGHT); Log.d("MOT", "R"); }
                    @Override
                    public void onRelease() { sendCommand(RequestEngine.Command.JOY_RELEASE); Log.d("MOT", "-"); }
                },
                new JoyTouchListener.PushListener() {
                    @Override
                    public void onPush() { sendCommand(RequestEngine.Command.JOY_BACK); Log.d("MOT", "B"); }
                    @Override
                    public void onRelease() { sendCommand(RequestEngine.Command.JOY_RELEASE); Log.d("MOT", "-"); }
                },
                new JoyTouchListener.PushListener() {
                    @Override
                    public void onPush() { sendCommand(RequestEngine.Command.JOY_LEFT); Log.d("MOT", "L"); }
                    @Override
                    public void onRelease() { sendCommand(RequestEngine.Command.JOY_RELEASE); Log.d("MOT", "-"); }
                },
                new JoyTouchListener.PushListener() {
                    @Override
                    public void onPush() { sendCommand(RequestEngine.Command.PAUSE); Log.d("MOT", "P"); }
                    @Override
                    public void onRelease() { /* NO RELEASE FOR CENTER COMMAND */ }
                }
        }));

        if(joy.getViewTreeObserver().isAlive()){
            joy.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout(){
                    int finalSize = Math.min(joy.getMeasuredWidth(), joy.getMeasuredHeight());
                    ((LinearLayout)joy).setLayoutParams(new FrameLayout.LayoutParams(finalSize, finalSize));
                }
            });
        }

        view.invalidate();

        return view;
    }

    private void handleAction(Runnable runner, MotionEvent event, Handler repeatedHandler) {
        switch(event.getAction()) {
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
