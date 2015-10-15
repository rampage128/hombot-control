package de.jlab.android.hombot;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.wearable.DataMap;

import java.lang.reflect.Array;
import java.util.ArrayList;

import de.jlab.android.hombot.common.core.HombotStatus;
import de.jlab.android.hombot.common.core.RequestEngine;
import de.jlab.android.hombot.common.data.HombotDataContract;
import de.jlab.android.hombot.common.data.HombotDataOpenHelper;
import de.jlab.android.hombot.common.settings.SharedSettings;
import de.jlab.android.hombot.common.wear.WearMessages;
import de.jlab.android.hombot.data.WearBot;

/**
 * Created by frede_000 on 14.10.2015.
 */
public class OverlayFragment extends Fragment {

    private HombotStatus mBotStatus;
    private OverlayListener mListener;

    private static class ViewHolder {
        FrameLayout button1;
        FrameLayout button2;
        FrameLayout button3;
        FrameLayout button4;
        ImageView icon1;
        ImageView icon2;
        ImageView icon3;
        ImageView icon4;
        View closeButton;
        Spinner botSelect;
        TextView statusText;
    }
    private ViewHolder mViewHolder;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OverlayListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OverlayListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_overlay, container, false);

        mViewHolder = new ViewHolder();
        mViewHolder.button1 = (FrameLayout)view.findViewById(R.id.overlay_button1);
        mViewHolder.button2 = (FrameLayout)view.findViewById(R.id.overlay_button2);
        mViewHolder.button3 = (FrameLayout)view.findViewById(R.id.overlay_button3);
        mViewHolder.button4 = (FrameLayout)view.findViewById(R.id.overlay_button4);
        mViewHolder.icon1 = (ImageView)view.findViewById(R.id.overlay_icon1);
        mViewHolder.icon2 = (ImageView)view.findViewById(R.id.overlay_icon2);
        mViewHolder.icon3 = (ImageView)view.findViewById(R.id.overlay_icon3);
        mViewHolder.icon4 = (ImageView)view.findViewById(R.id.overlay_icon4);
        mViewHolder.botSelect = (Spinner)view.findViewById(R.id.bot_select);
        mViewHolder.statusText = (TextView)view.findViewById(R.id.overlay_status);
        mViewHolder.closeButton = view.findViewById(R.id.overlay_close);

        mViewHolder.icon2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mListener.sendCommand(RequestEngine.Command.TURBO);
                return false;
            }
        });

        mViewHolder.icon3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (HombotStatus.Status.CHARGING.equals(mBotStatus.getStatus())) {
                    mListener.sendCommand(RequestEngine.Command.REPEAT);
                } else {
                    mListener.sendCommand(RequestEngine.Command.HOME);
                }
                return false;
            }
        });

        mViewHolder.icon4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (HombotStatus.Status.WORKING.equals(mBotStatus.getStatus())) {
                    mListener.sendCommand(RequestEngine.Command.PAUSE);
                } else {
                    mListener.sendCommand(RequestEngine.Command.START);
                }
                return false;
            }
        });

        mViewHolder.closeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hide();
                return false;
            }
        });

        mViewHolder.botSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                WearBot bot = (WearBot) parent.getAdapter().getItem(position);
                mListener.selectBot(bot);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    public void updateBotList(WearBot[] bots, Context context) {



        ArrayAdapter<WearBot> botArrayAdapter = new ArrayAdapter<WearBot>(context, android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, bots);
        mViewHolder.botSelect.setAdapter(botArrayAdapter);

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String recentBotAddress = sp.getString(SharedSettings.PREF_RECENT_BOT, null);
        if (bots.length > 0) {
            if (recentBotAddress == null) {
                mViewHolder.botSelect.setSelection(0);
            } else {
                for (int i = 0; i < bots.length; i++) {
                    WearBot bot = bots[i];
                    if (recentBotAddress.equalsIgnoreCase(bot.getAddress())) {
                        mViewHolder.botSelect.setSelection(i);
                    }
                }
            }
        }
    }

    public void requestBotList() {
        mListener.requestBotList();
    }

    public void show() {
        getActivity().findViewById(R.id.overlay_fragment).setVisibility(View.VISIBLE);
        getActivity().getFragmentManager().beginTransaction().show(this).commit(); // .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
        requestBotList();
    }

    public void hide() {
        getActivity().getFragmentManager().beginTransaction().hide(this).commit(); // .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
        getActivity().findViewById(R.id.overlay_fragment).setVisibility(View.GONE);
    }

    public void statusUpdate(final HombotStatus status) {

        if (mBotStatus == null || mBotStatus.getStatus() != status.getStatus()) {

            Resources res = getResources();
            String localized = res.getString(res.getIdentifier("status_" + status.getStatus().name().toLowerCase(), "string", getActivity().getPackageName()));
            mViewHolder.statusText.setText(localized); // status.getStatus().toString()

            if (HombotStatus.Status.OFFLINE.equals(status.getStatus())) {
                mViewHolder.button1.setVisibility(View.GONE);
                mViewHolder.button2.setVisibility(View.GONE);
                mViewHolder.button3.setVisibility(View.GONE);
                mViewHolder.button4.setVisibility(View.GONE);
            } else {
                mViewHolder.button1.setVisibility(View.VISIBLE);
                mViewHolder.button2.setVisibility(View.VISIBLE);
                mViewHolder.button3.setVisibility(View.VISIBLE);
                mViewHolder.button4.setVisibility(View.VISIBLE);
            }

            if (!HombotStatus.Status.OFFLINE.equals(status.getStatus()) && !HombotStatus.Status.WORKING.equals(status.getStatus())) {
                mViewHolder.icon1.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (HombotStatus.Status.CHARGING.equals(mBotStatus.getStatus())) {
                            if (HombotStatus.Mode.CELLBYCELL.equals(mBotStatus.getMode())) {
                                mListener.sendCommand(RequestEngine.Command.MODE_ZIGZAG);
                            } else {
                                mListener.sendCommand(RequestEngine.Command.MODE_CELLBYCELL);
                            }
                        } else {
                            mListener.sendCommand(RequestEngine.Command.MODE);
                        }
                        return false;
                    }
                });
            }

            if (HombotStatus.Status.WORKING.equals(status.getStatus())) {
                mViewHolder.icon4.setImageResource(R.drawable.ic_action_pause);
            } else {
                mViewHolder.icon4.setImageResource(R.drawable.ic_action_start);
            }

            if (HombotStatus.Status.STANDBY.equals(status.getStatus())) {
                mViewHolder.icon3.setImageResource(R.drawable.ic_action_home);
            } else {
                mViewHolder.icon3.setImageResource(R.drawable.ic_action_repeat);
            }

        }
        mBotStatus = status;

        setModeIcon();

    }

    private void setModeIcon() {
        if (HombotStatus.Mode.CELLBYCELL.equals(mBotStatus.getMode())) {
            mViewHolder.icon1.setImageResource(R.drawable.ic_action_mode_cell);
        } else if (HombotStatus.Mode.MYSPACE.equals(mBotStatus.getMode())) {
            mViewHolder.icon1.setImageResource(R.drawable.ic_action_mode_myspace);
        } else if (HombotStatus.Mode.SPIRAL.equals(mBotStatus.getMode())) {
            mViewHolder.icon1.setImageResource(R.drawable.ic_action_mode_spiral);
        } else {
            mViewHolder.icon1.setImageResource(R.drawable.ic_action_mode_zigzag);
        }
    }

    public interface OverlayListener {
        void sendCommand(RequestEngine.Command command);
        void selectBot(WearBot bot);
        void requestBotList();
    }

}
