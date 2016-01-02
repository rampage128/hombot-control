package de.jlab.android.hombot.sections;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import de.jlab.android.hombot.SectionFragment;
import de.jlab.android.hombot.common.core.HombotStatus;
import de.jlab.android.hombot.R;
import de.jlab.android.hombot.core.HttpRequestEngine;
import de.jlab.android.hombot.sections.common.StatusFragment;
import de.jlab.android.hombot.utils.Colorizer;

/**
 * A {@link SectionFragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SectionFragment.SectionInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StatusSection#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatusSection extends SectionFragment {

    static class ViewHolder {
        StatusFragment statusDisplay;

        Button commandStartStop;
        Button commandTurbo;
        Button commandMode;
        Button commandHome;
        Button commandRepeat;
    }

    private ViewHolder mViewHolder;

    private boolean isWorking = false;
    private boolean isDocked = false;

    private HombotStatus.Mode mode;

    public static StatusSection newInstance(int sectionNumber) {
        StatusSection fragment = new StatusSection();
        fragment.register(sectionNumber);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_section_status, container, false);

        mViewHolder = new ViewHolder();
        mViewHolder.commandStartStop = (Button) view.findViewById(R.id.cm_startstop);
        mViewHolder.commandTurbo = (Button) view.findViewById(R.id.cm_turbo);
        mViewHolder.commandMode = (Button) view.findViewById(R.id.cm_mode);
        mViewHolder.commandHome = (Button) view.findViewById(R.id.cm_home);
        mViewHolder.commandRepeat = (Button) view.findViewById(R.id.cm_repeat);

        mViewHolder.commandTurbo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand(HttpRequestEngine.Command.TURBO);
            }
        });

        mViewHolder.commandMode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isDocked) {
                    if (HombotStatus.Mode.ZIGZAG.equals(StatusSection.this.mode)) {
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

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Colorizer colorizer = getColorizer();

        mViewHolder.statusDisplay = (StatusFragment) getChildFragmentManager().findFragmentById(R.id.ct_status);

        mViewHolder.statusDisplay.getView().setVisibility(View.INVISIBLE);

        mViewHolder.statusDisplay.colorize(colorizer);
        colorizer.colorizeButton(mViewHolder.commandHome, colorizer.getColorText());
        colorizer.colorizeButton(mViewHolder.commandMode, colorizer.getColorText());
        colorizer.colorizeButton(mViewHolder.commandRepeat, colorizer.getColorText());
        colorizer.colorizeButton(mViewHolder.commandTurbo, colorizer.getColorText());
        colorizer.colorizeButton(mViewHolder.commandStartStop, colorizer.getColorText());
    }

    @Override
    public void statusUpdate(HombotStatus status) {

        this.isDocked = HombotStatus.Status.CHARGING.equals(status.getStatus());
        this.isWorking = HombotStatus.Status.WORKING.equals(status.getStatus()) || HombotStatus.Status.HOMING.equals(status.getStatus());
        this.mode = status.getMode();

        if (!this.isAdded()) {
            return;
        }

        mViewHolder.statusDisplay.statusUpdate(status);

        if (this.isWorking) {
            mViewHolder.commandStartStop.setText(getResources().getText(R.string.command_pause));
        } else {
            mViewHolder.commandStartStop.setText(getResources().getText(R.string.command_start));
        }

    }

}
