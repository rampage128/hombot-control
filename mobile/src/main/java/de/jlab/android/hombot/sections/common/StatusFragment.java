package de.jlab.android.hombot.sections.common;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import de.jlab.android.hombot.R;
import de.jlab.android.hombot.common.core.HombotSchedule;
import de.jlab.android.hombot.common.core.HombotStatus;
import de.jlab.android.hombot.utils.Colorizer;

/**
 * Created by frede_000 on 31.12.2015.
 */
public class StatusFragment extends Fragment {

    static class ViewHolder {
        View display;
        TextView statusText;
        ProgressBar batteryProgress;
        TextView batteryText;
        ImageView modeHome;
        ImageView modeZigZag;
        ImageView modeCellByCell;
        ImageView modeSpiral;
        ImageView modeMySpace;
        ImageView modeRepeat;
        ImageView turboLeft;
        ImageView turboRight;
    }

    private ViewHolder mViewHolder;

    public static StatusFragment newInstance() {
        StatusFragment fragment = new StatusFragment();
        return fragment;
    }

    public StatusFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.display, container, false);

        mViewHolder = new ViewHolder();
        mViewHolder.display = view.findViewById(R.id.ct_status);
        mViewHolder.statusText = (TextView) view.findViewById(R.id.lb_status);
        mViewHolder.batteryProgress = (ProgressBar) view.findViewById(R.id.battery_progress);
        mViewHolder.batteryText = (TextView) view.findViewById(R.id.battery_text);
        mViewHolder.modeHome = (ImageView) view.findViewById(R.id.ic_mode_home);
        mViewHolder.modeZigZag = (ImageView) view.findViewById(R.id.ic_mode_zigzag);
        mViewHolder.modeCellByCell = (ImageView) view.findViewById(R.id.ic_mode_cell);
        mViewHolder.modeSpiral = (ImageView) view.findViewById(R.id.ic_mode_spiral);
        mViewHolder.modeMySpace = (ImageView) view.findViewById(R.id.ic_mode_myspace);
        mViewHolder.modeRepeat = (ImageView) view.findViewById(R.id.ic_mode_repeat);
        mViewHolder.turboLeft = (ImageView) view.findViewById(R.id.ic_turbo_left);
        mViewHolder.turboRight = (ImageView) view.findViewById(R.id.ic_turbo_right);

        return view;
    }

    public void colorize(Colorizer colorizer) {
        mViewHolder.statusText.setTextColor(colorizer.getColorPrimary());
        mViewHolder.batteryText.setTextColor(colorizer.getColorPrimary());
        colorizer.colorizeDrawable(mViewHolder.modeCellByCell.getDrawable(), colorizer.getColorPrimary());
        colorizer.colorizeDrawable(mViewHolder.modeHome.getDrawable(), colorizer.getColorPrimary());
        colorizer.colorizeDrawable(mViewHolder.modeMySpace.getDrawable(), colorizer.getColorPrimary());
        colorizer.colorizeDrawable(mViewHolder.modeRepeat.getDrawable(), colorizer.getColorPrimary());
        colorizer.colorizeDrawable(mViewHolder.modeSpiral.getDrawable(), colorizer.getColorPrimary());
        colorizer.colorizeDrawable(mViewHolder.modeZigZag.getDrawable(), colorizer.getColorPrimary());
        colorizer.colorizeDrawable(mViewHolder.turboLeft.getDrawable(), colorizer.getColorPrimary());
        colorizer.colorizeDrawable(mViewHolder.turboRight.getDrawable(), colorizer.getColorPrimary());
        colorizer.colorizeDrawable(mViewHolder.batteryProgress.getProgressDrawable(), colorizer.getColorPrimary());
    }

    public void statusUpdate(HombotStatus status) {

        Resources res = getResources();
        String localized = res.getString(res.getIdentifier("status_" + status.getStatus().name().toLowerCase(), "string", getActivity().getPackageName()));
        mViewHolder.statusText.setText(localized);

        if (HombotStatus.Status.OFFLINE.equals(status.getStatus())) {
            mViewHolder.batteryProgress.setProgress(0);
            mViewHolder.batteryText.setText("0%");
            mViewHolder.batteryProgress.setVisibility(View.VISIBLE);
            mViewHolder.batteryText.setVisibility(View.VISIBLE);
            mViewHolder.modeHome.setVisibility(View.VISIBLE);
            mViewHolder.modeZigZag.setVisibility(View.VISIBLE);
            mViewHolder.modeCellByCell.setVisibility(View.VISIBLE);
            mViewHolder.modeSpiral.setVisibility(View.VISIBLE);
            mViewHolder.modeCellByCell.setVisibility(View.VISIBLE);
            mViewHolder.modeRepeat.setVisibility(View.VISIBLE);
            mViewHolder.turboLeft.setVisibility(View.VISIBLE);
            mViewHolder.turboRight.setVisibility(View.VISIBLE);
            if (mViewHolder.display.getVisibility() == View.VISIBLE) {
                mViewHolder.display.setVisibility(View.INVISIBLE);
            } else {
                mViewHolder.display.setVisibility(View.VISIBLE);
            }
            return;
        } else {
            mViewHolder.batteryProgress.setVisibility(View.VISIBLE);
            mViewHolder.batteryText.setVisibility(View.VISIBLE);
        }

        if (HombotStatus.Status.HOMING.equals(status.getStatus()) || HombotStatus.Status.DOCKING.equals(status.getStatus())) {
            mViewHolder.modeHome.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.modeHome.setVisibility(View.INVISIBLE);
        }

        mViewHolder.batteryProgress.setProgress(status.getBatteryPercent());
        mViewHolder.batteryText.setText(status.getBatteryPercent() + "%");

        if (!status.getTurbo()) {
            mViewHolder.turboLeft.setVisibility(View.INVISIBLE);
            mViewHolder.turboRight.setVisibility(View.INVISIBLE);
        } else {
            mViewHolder.turboLeft.setVisibility(View.VISIBLE);
            mViewHolder.turboRight.setVisibility(View.VISIBLE);
        }

        if (!status.getRepeat()) {
            mViewHolder.modeRepeat.setVisibility(View.INVISIBLE);
        } else {
            mViewHolder.modeRepeat.setVisibility(View.VISIBLE);
        }

        if (HombotStatus.Status.HOMING.equals(status.getStatus()) || HombotStatus.Status.DOCKING.equals(status.getStatus())) {
            mViewHolder.modeHome.setVisibility(View.VISIBLE);
            mViewHolder.modeZigZag.setVisibility(View.INVISIBLE);
            mViewHolder.modeCellByCell.setVisibility(View.INVISIBLE);
            mViewHolder.modeSpiral.setVisibility(View.INVISIBLE);
            mViewHolder.modeCellByCell.setVisibility(View.INVISIBLE);
        } else {
            mViewHolder.modeHome.setVisibility(View.INVISIBLE);
            if (HombotStatus.Mode.ZIGZAG.equals(status.getMode())) {
                mViewHolder.modeZigZag.setVisibility(View.VISIBLE);
                mViewHolder.modeCellByCell.setVisibility(View.INVISIBLE);
                mViewHolder.modeSpiral.setVisibility(View.INVISIBLE);
                mViewHolder.modeCellByCell.setVisibility(View.INVISIBLE);
            } else if (HombotStatus.Mode.CELLBYCELL.equals(status.getMode())) {
                mViewHolder.modeZigZag.setVisibility(View.INVISIBLE);
                mViewHolder.modeCellByCell.setVisibility(View.VISIBLE);
                mViewHolder.modeSpiral.setVisibility(View.INVISIBLE);
                mViewHolder.modeCellByCell.setVisibility(View.INVISIBLE);
            } else if (HombotStatus.Mode.SPIRAL.equals(status.getMode())) {
                mViewHolder.modeZigZag.setVisibility(View.INVISIBLE);
                mViewHolder.modeCellByCell.setVisibility(View.INVISIBLE);
                mViewHolder.modeSpiral.setVisibility(View.VISIBLE);
                mViewHolder.modeCellByCell.setVisibility(View.INVISIBLE);
            } else if (HombotStatus.Mode.MYSPACE.equals(status.getMode()) || HombotStatus.Mode.MYSPACE_RECORD.equals(status.getMode())) {
                mViewHolder.modeZigZag.setVisibility(View.INVISIBLE);
                mViewHolder.modeCellByCell.setVisibility(View.INVISIBLE);
                mViewHolder.modeSpiral.setVisibility(View.INVISIBLE);
                mViewHolder.modeCellByCell.setVisibility(View.VISIBLE);
            }
        }

        mViewHolder.display.setVisibility(View.VISIBLE);

    }

}
