package de.jlab.android.hombot.sections;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.jlab.android.hombot.R;
import de.jlab.android.hombot.SectionFragment;
import de.jlab.android.hombot.core.HombotMap;
import de.jlab.android.hombot.sections.map.MapView;

/**
 * A {@link SectionFragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SectionFragment.SectionInteractionListener} interface
 * to handle interaction events.
 * Use the {@link JoySection#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapSection extends SectionFragment {

    private static class ViewHolder {
        MapView map;
    }
    private ViewHolder mViewHolder;

    private HombotMap mSelectedMap;

    public static MapSection newInstance(int sectionNumber) {
        MapSection fragment = new MapSection();
        fragment.register(sectionNumber);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_section_map, container, false);

        mViewHolder = new ViewHolder();
        mViewHolder.map = (MapView) view.findViewById(R.id.map);

        readMap(null);

        return view;
    }

    private void readMap(final String mapName) {
        new Thread(new Runnable() {
            public void run() {
                mSelectedMap = ((SectionInteractionListener) getActivity()).requestMap(mapName);
            }
        }).start();
    }
}
