package de.jlab.android.hombot.sections;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.EnumMap;

import de.jlab.android.hombot.R;
import de.jlab.android.hombot.SectionFragment;
import de.jlab.android.hombot.core.HombotSchedule;
import de.jlab.android.hombot.sections.schedule.ScheduleItem;

/**
 * A {@link SectionFragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SectionFragment.SectionInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScheduleSection#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScheduleSection extends SectionFragment implements ScheduleItem.DayChangedListener {

    private static class ViewHolder {
        Button saveButton;
    }
    private ViewHolder mViewHolder;

    private HombotSchedule mSchedule;

    private EnumMap<HombotSchedule.Weekday, ScheduleItem> mScheduleItemMap = new EnumMap<>(HombotSchedule.Weekday.class);


    public static ScheduleSection newInstance(int sectionNumber) {
        ScheduleSection fragment = new ScheduleSection();
        fragment.register(sectionNumber);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_section_schedule, container, false);

        //LinearLayout schedule = (LinearLayout) view.findViewById(R.id.schedule);

        mViewHolder = new ViewHolder();
        mViewHolder.saveButton = (Button)view.findViewById(R.id.savebutton);

        mViewHolder.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeSchedule();
            }
        });

        FragmentManager fragmentManager = getChildFragmentManager();
        if (fragmentManager.findFragmentByTag("schedule_item") == null) {
            for (HombotSchedule.Weekday day : HombotSchedule.Weekday.values()) {
                ScheduleItem item = ScheduleItem.newInstance(day);
                item.setDayChangeListener(this);
                mScheduleItemMap.put(day, item);
                fragmentManager.beginTransaction().add(R.id.schedule, item, "schedule_item").commit();
            }
            readSchedule();
        }
        /*
        else {
            mScheduleItemMap = (EnumMap<HombotSchedule.Weekday, ScheduleItem>)savedInstanceState.getSerializable("item_map");
            for (HombotSchedule.Weekday day : HombotSchedule.Weekday.values()) {
                ScheduleItem item = mScheduleItemMap.get(day);
                fragmentManager.beginTransaction().add(R.id.schedule, item, "schedule_item").commit();
            }
        }
        */


        return view;
    }

    private void writeSchedule() {
        ((SectionInteractionListener) getActivity()).setSchedule(mSchedule);
    }

    private void readSchedule() {
        new Thread(new Runnable() {
            public void run() {
                mSchedule = ((SectionInteractionListener) getActivity()).requestSchedule();
                for (HombotSchedule.Weekday day : HombotSchedule.Weekday.values()) {
                    mScheduleItemMap.get(day).update(mSchedule.getDayTime(day), mSchedule.getDayMode(day), getActivity());
                }
/*
                FragmentManager fragmentManager = ((MainActivity) getContext()).getSupportFragmentManager();
                List<Fragment> all = fragmentManager.getFragments();
                if (all != null) {
                    for (Fragment frag : all) {
                        fragmentManager.beginTransaction().remove(frag).commit();
                    }
                }
                for (HombotSchedule.Weekday day : HombotSchedule.Weekday.values()) {
                    ScheduleItem item = ScheduleItem.newInstance(day, mSchedule.getDayTime(day), mSchedule.getDayMode(day));
                    fragmentManager.beginTransaction().add(R.id.schedule, item, null).commit();
                }
*/
            }
        }).start();
    }

    public void clearScheduleDay(HombotSchedule.Weekday day) {
        mSchedule.clearDay(day);
    }

    public void setScheduleDay(HombotSchedule.Weekday day, String time, HombotSchedule.Mode mode) {
        mSchedule.setDay(day, time, mode);
    }

}
