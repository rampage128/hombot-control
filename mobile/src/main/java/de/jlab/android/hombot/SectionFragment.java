package de.jlab.android.hombot;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.jlab.android.hombot.core.HombotStatus;
import de.jlab.android.hombot.R;
import de.jlab.android.hombot.core.RequestEngine;
import de.jlab.android.hombot.sections.StatusSection;

/**
 * An extensible {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StatusSection.SectionInteractionListener} interface
 * to handle interaction events.
 */
public class SectionFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    protected static final String ARG_SECTION_NUMBER = "section_number";

    private SectionInteractionListener mListener;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public SectionFragment() {
    }

    protected void register(int sectionNumber) {
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        this.setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_placeholder, container, false);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (SectionInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        mListener.onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    protected void sendCommand(RequestEngine.Command command) {
        mListener.sendCommand(command);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface SectionInteractionListener {
        public void onSectionAttached(int section);

        public void sendCommand(RequestEngine.Command command);
    }

    public void statusUpdate(HombotStatus status) {

    }

    public void scheduleChanged(int dayNum, String time, String mode) {

    }

}
