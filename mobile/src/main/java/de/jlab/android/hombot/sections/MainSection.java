package de.jlab.android.hombot.sections;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.jlab.android.hombot.R;
import de.jlab.android.hombot.SectionFragment;
import de.jlab.android.hombot.common.core.HombotStatus;
import de.jlab.android.hombot.sections.common.StatusFragment;
import de.jlab.android.hombot.utils.Colorizer;

/**
 * Created by frede_000 on 31.12.2015.
 */
public class MainSection extends SectionFragment {

    static class ViewHolder {
        StatusFragment statusDisplay;
    }
    private ViewHolder mViewHolder;

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
        mViewHolder.statusDisplay = (StatusFragment) getFragmentManager().findFragmentById(R.id.ct_status);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        mViewHolder.statusDisplay.statusUpdate(status);
    }

}
