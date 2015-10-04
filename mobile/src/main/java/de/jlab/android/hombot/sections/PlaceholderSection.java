package de.jlab.android.hombot.sections;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.jlab.android.hombot.R;
import de.jlab.android.hombot.SectionFragment;

/**
 * A {@link SectionFragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SectionFragment.SectionInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlaceholderSection#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlaceholderSection extends SectionFragment {

    public static PlaceholderSection newInstance(int sectionNumber) {
        PlaceholderSection fragment = new PlaceholderSection();
        fragment.register(sectionNumber);
        return fragment;
    }

      @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_placeholder, container, false);

          

        return view;
    }

}
