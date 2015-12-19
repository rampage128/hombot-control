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
        View display;
        TextView displayStatusText;
        ProgressBar displayBatteryProgress;
        TextView displayBatteryText;
        ImageView displayModeHome;
        ImageView displayModeZigZag;
        ImageView displayModeCellByCell;
        ImageView displayModeSpiral;
        ImageView displayModeMySpace;
        ImageView displayModeRepeat;
        ImageView displayTurboLeft;
        ImageView displayTurboRight;

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

        mViewHolder.display = view.findViewById(R.id.ct_status);
        mViewHolder.displayStatusText = (TextView) view.findViewById(R.id.lb_status);
        mViewHolder.displayBatteryProgress = (ProgressBar) view.findViewById(R.id.battery_progress);
        mViewHolder.displayBatteryText = (TextView) view.findViewById(R.id.battery_text);
        mViewHolder.displayModeHome = (ImageView) view.findViewById(R.id.ic_mode_home);
        mViewHolder.displayModeZigZag = (ImageView) view.findViewById(R.id.ic_mode_zigzag);
        mViewHolder.displayModeCellByCell = (ImageView) view.findViewById(R.id.ic_mode_cell);
        mViewHolder.displayModeSpiral = (ImageView) view.findViewById(R.id.ic_mode_spiral);
        mViewHolder.displayModeMySpace = (ImageView) view.findViewById(R.id.ic_mode_myspace);
        mViewHolder.displayModeRepeat = (ImageView) view.findViewById(R.id.ic_mode_repeat);
        mViewHolder.displayTurboLeft = (ImageView) view.findViewById(R.id.ic_turbo_left);
        mViewHolder.displayTurboRight = (ImageView) view.findViewById(R.id.ic_turbo_right);

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

        mViewHolder.display.setVisibility(View.INVISIBLE);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Colorizer colorizer = getColorizer();

        mViewHolder.displayStatusText.setTextColor(colorizer.getColorPrimary());
        mViewHolder.displayBatteryText.setTextColor(colorizer.getColorPrimary());

        colorizer.colorizeDrawable(mViewHolder.displayModeCellByCell.getDrawable(), colorizer.getColorPrimary());
        colorizer.colorizeDrawable(mViewHolder.displayModeHome.getDrawable(), colorizer.getColorPrimary());
        colorizer.colorizeDrawable(mViewHolder.displayModeMySpace.getDrawable(), colorizer.getColorPrimary());
        colorizer.colorizeDrawable(mViewHolder.displayModeRepeat.getDrawable(), colorizer.getColorPrimary());
        colorizer.colorizeDrawable(mViewHolder.displayModeSpiral.getDrawable(), colorizer.getColorPrimary());
        colorizer.colorizeDrawable(mViewHolder.displayModeZigZag.getDrawable(), colorizer.getColorPrimary());
        colorizer.colorizeDrawable(mViewHolder.displayTurboLeft.getDrawable(), colorizer.getColorPrimary());
        colorizer.colorizeDrawable(mViewHolder.displayTurboRight.getDrawable(), colorizer.getColorPrimary());
        colorizer.colorizeDrawable(mViewHolder.displayBatteryProgress.getProgressDrawable(), colorizer.getColorPrimary());
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

        Resources res = getResources();
        String localized = res.getString(res.getIdentifier("status_" + status.getStatus().name().toLowerCase(), "string", getActivity().getPackageName()));
        mViewHolder.displayStatusText.setText(localized); // status.getStatus().toString()

        if (HombotStatus.Status.OFFLINE.equals(status.getStatus())) {
            mViewHolder.displayBatteryProgress.setProgress(0);
            mViewHolder.displayBatteryText.setText("0%");
            mViewHolder.displayBatteryProgress.setVisibility(View.VISIBLE);
            mViewHolder.displayBatteryText.setVisibility(View.VISIBLE);
            mViewHolder.displayModeHome.setVisibility(View.VISIBLE);
            mViewHolder.displayModeZigZag.setVisibility(View.VISIBLE);
            mViewHolder.displayModeCellByCell.setVisibility(View.VISIBLE);
            mViewHolder.displayModeSpiral.setVisibility(View.VISIBLE);
            mViewHolder.displayModeMySpace.setVisibility(View.VISIBLE);
            mViewHolder.displayModeRepeat.setVisibility(View.VISIBLE);
            mViewHolder.displayTurboLeft.setVisibility(View.VISIBLE);
            mViewHolder.displayTurboRight.setVisibility(View.VISIBLE);
            if (mViewHolder.display.getVisibility() == View.VISIBLE) {
                mViewHolder.display.setVisibility(View.INVISIBLE);
            } else {
                mViewHolder.display.setVisibility(View.VISIBLE);
            }
            return;
        } else {
            mViewHolder.displayBatteryProgress.setVisibility(View.VISIBLE);
            mViewHolder.displayBatteryText.setVisibility(View.VISIBLE);
        }

        if (HombotStatus.Status.HOMING.equals(status.getStatus()) || HombotStatus.Status.DOCKING.equals(status.getStatus())) {
            mViewHolder.displayModeHome.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.displayModeHome.setVisibility(View.INVISIBLE);
        }

        mViewHolder.displayBatteryProgress.setProgress(status.getBatteryPercent());
        mViewHolder.displayBatteryText.setText(status.getBatteryPercent() + "%");

        if (this.isWorking) {
            mViewHolder.commandStartStop.setText(getResources().getText(R.string.command_pause));
        } else {
            mViewHolder.commandStartStop.setText(getResources().getText(R.string.command_start));
        }

        if (!status.getTurbo()) {
            mViewHolder.displayTurboLeft.setVisibility(View.INVISIBLE);
            mViewHolder.displayTurboRight.setVisibility(View.INVISIBLE);
        } else {
            mViewHolder.displayTurboLeft.setVisibility(View.VISIBLE);
            mViewHolder.displayTurboRight.setVisibility(View.VISIBLE);
        }

        if (!status.getRepeat()) {
            mViewHolder.displayModeRepeat.setVisibility(View.INVISIBLE);
        } else {
            mViewHolder.displayModeRepeat.setVisibility(View.VISIBLE);
        }

        if (HombotStatus.Status.HOMING.equals(status.getStatus()) || HombotStatus.Status.DOCKING.equals(status.getStatus())) {
            mViewHolder.displayModeHome.setVisibility(View.VISIBLE);
            mViewHolder.displayModeZigZag.setVisibility(View.INVISIBLE);
            mViewHolder.displayModeCellByCell.setVisibility(View.INVISIBLE);
            mViewHolder.displayModeSpiral.setVisibility(View.INVISIBLE);
            mViewHolder.displayModeMySpace.setVisibility(View.INVISIBLE);
        } else {
            mViewHolder.displayModeHome.setVisibility(View.INVISIBLE);
            if (HombotStatus.Mode.ZIGZAG.equals(status.getMode())) {
                mViewHolder.displayModeZigZag.setVisibility(View.VISIBLE);
                mViewHolder.displayModeCellByCell.setVisibility(View.INVISIBLE);
                mViewHolder.displayModeSpiral.setVisibility(View.INVISIBLE);
                mViewHolder.displayModeMySpace.setVisibility(View.INVISIBLE);
            } else if (HombotStatus.Mode.CELLBYCELL.equals(status.getMode())) {
                mViewHolder.displayModeZigZag.setVisibility(View.INVISIBLE);
                mViewHolder.displayModeCellByCell.setVisibility(View.VISIBLE);
                mViewHolder.displayModeSpiral.setVisibility(View.INVISIBLE);
                mViewHolder.displayModeMySpace.setVisibility(View.INVISIBLE);
            } else if (HombotStatus.Mode.SPIRAL.equals(status.getMode())) {
                mViewHolder.displayModeZigZag.setVisibility(View.INVISIBLE);
                mViewHolder.displayModeCellByCell.setVisibility(View.INVISIBLE);
                mViewHolder.displayModeSpiral.setVisibility(View.VISIBLE);
                mViewHolder.displayModeMySpace.setVisibility(View.INVISIBLE);
            } else if (HombotStatus.Mode.MYSPACE.equals(status.getMode()) || HombotStatus.Mode.MYSPACE_RECORD.equals(status.getMode())) {
                mViewHolder.displayModeZigZag.setVisibility(View.INVISIBLE);
                mViewHolder.displayModeCellByCell.setVisibility(View.INVISIBLE);
                mViewHolder.displayModeSpiral.setVisibility(View.INVISIBLE);
                mViewHolder.displayModeMySpace.setVisibility(View.VISIBLE);
            }
        }

        mViewHolder.display.setVisibility(View.VISIBLE);

    }

}
