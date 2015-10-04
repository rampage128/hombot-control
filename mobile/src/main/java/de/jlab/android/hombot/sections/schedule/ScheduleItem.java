package de.jlab.android.hombot.sections.schedule;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import de.jlab.android.hombot.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScheduleItem.DayChangedListener} interface
 * to handle interaction events.
 * Use the {@link ScheduleItem#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScheduleItem extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "dayNum";

    private int mDayNum;

    private DayChangedListener mListener;

    private Spinner mModeSpinner;
    private EditText mTimeEdit;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param dayNum Parameter 1.
     * @return A new instance of fragment ScheduleItem.
     */
    // TODO: Rename and change types and number of parameters
    public static ScheduleItem newInstance(int dayNum) {
        ScheduleItem fragment = new ScheduleItem();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, dayNum);
        fragment.setArguments(args);
        return fragment;
    }

    public ScheduleItem() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDayNum = getArguments().getInt(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_schedule_item, container, false);

        TextView header = (TextView) view.findViewById(R.id.header_label);
        header.setText(getResources().getTextArray(R.array.weekdays)[mDayNum]);

        mModeSpinner = (Spinner) view.findViewById(R.id.mode_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.schedule_modes, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mModeSpinner.setAdapter(adapter);

        mModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                changeDay();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mTimeEdit = (EditText) view.findViewById(R.id.time_field);

        return view;
    }

    private void changeDay() {

        String time = mTimeEdit.getText().toString();
        String mode = mModeSpinner.getSelectedItem().toString();

        if (mListener != null) {
            mListener.onDayChanged(mDayNum, time, mode);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (DayChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DayChangedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface DayChangedListener {
        public void onDayChanged(int dayNum, String time, String mode);
    }

}
