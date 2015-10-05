package de.jlab.android.hombot.sections;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import de.jlab.android.hombot.MainActivity;
import de.jlab.android.hombot.NavigationDrawerFragment;
import de.jlab.android.hombot.R;
import de.jlab.android.hombot.SectionFragment;
import de.jlab.android.hombot.sections.schedule.ScheduleItem;

/**
 * A {@link SectionFragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SectionFragment.SectionInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScheduleSection#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScheduleSection extends SectionFragment {

    private String[] days = new String[7];

    public static ScheduleSection newInstance(int sectionNumber) {
        ScheduleSection fragment = new ScheduleSection();
        fragment.register(sectionNumber);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_section_schedule, container, false);

        LinearLayout schedule = (LinearLayout) view.findViewById(R.id.schedule);

        for (int i = 0; i < 7; i++) {
            ScheduleItem item = ScheduleItem.newInstance(i);
            FragmentManager fragmentManager = ((MainActivity) getContext()).getSupportFragmentManager();
            fragmentManager.beginTransaction().add(R.id.schedule, item, null).commit();
        }

        //schedule.requestLayout();

        return view;
    }

    public void scheduleChanged(int dayNum, String time, String mode) {
        days[dayNum] = time + "," + mode;
    }

}
