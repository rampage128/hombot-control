package de.jlab.android.hombot.sections.schedule;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.jlab.android.hombot.MainActivity;
import de.jlab.android.hombot.R;
import de.jlab.android.hombot.core.HombotSchedule;

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
    private static final String ARG_PARAM1 = "day";
    private static final String ARG_PARAM2 = "time";
    private static final String ARG_PARAM3 = "mode";

    private HombotSchedule.Weekday mDay;
    private String mTime;
    private HombotSchedule.Mode mMode;

    private DayChangedListener mListener;

    private static class ViewHolder {
        Spinner modeSpinner;
        EditText timeEdit;
        CheckBox check;
    }

    private ViewHolder mViewHolder;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param day  Parameter 1.
     * @param time Parameter 2.
     * @param mode Parameter 3.
     * @return A new instance of fragment ScheduleItem.
     */
    // TODO: Rename and change types and number of parameters
    public static ScheduleItem newInstance(HombotSchedule.Weekday day, String time, HombotSchedule.Mode mode) {
        ScheduleItem fragment = new ScheduleItem();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, day);
        args.putString(ARG_PARAM2, time);
        args.putSerializable(ARG_PARAM3, mode);
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
            mDay = (HombotSchedule.Weekday) getArguments().getSerializable(ARG_PARAM1);
            mTime = getArguments().getString(ARG_PARAM2);

            // CONVERT 12 HOUR TO 24 HOUR FORMAT IF NECESSARY
            if (DateFormat.is24HourFormat(getActivity()) && mTime.matches(".*(AM|PM)")) {
                try {
                    final SimpleDateFormat sdf = new SimpleDateFormat("hh:mmaa", Locale.US);
                    final Date dateObj = sdf.parse(mTime);
                    mTime = new SimpleDateFormat("HH:mm").format(dateObj);
                } catch (final ParseException e) {
                    throw new IllegalArgumentException("Wrong time format given for DayData: " + mTime);
                }
            }

            mMode = (HombotSchedule.Mode) getArguments().getSerializable(ARG_PARAM3);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_schedule_item, container, false);

        TextView header = (TextView) view.findViewById(R.id.header_label);
        header.setText(getResources().getTextArray(R.array.weekdays)[mDay.ordinal()]);

        mViewHolder = new ViewHolder();
        mViewHolder.modeSpinner = (Spinner) view.findViewById(R.id.mode_spinner);
        mViewHolder.timeEdit = (EditText) view.findViewById(R.id.time_field);
        mViewHolder.check = (CheckBox) view.findViewById(R.id.time_check);


        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.schedule_modes, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mViewHolder.modeSpinner.setAdapter(adapter);

        if (null != mMode) {
            int pos = adapter.getPosition(mMode.toString());
            mViewHolder.modeSpinner.setSelection(pos);
        }

        mViewHolder.modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                changeDay();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mViewHolder.timeEdit.setText(mTime);

        mViewHolder.timeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment newFragment = new TimePickerFragment();
                newFragment.setEditText(mViewHolder.timeEdit);
                newFragment.show(((MainActivity) getActivity()).getSupportFragmentManager(), "timePicker");
            }
        });

        mViewHolder.timeEdit.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                changeDay();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        mViewHolder.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mViewHolder.timeEdit.setEnabled(isChecked);
                mViewHolder.modeSpinner.setEnabled(isChecked);
                if (!isChecked) {
                    mViewHolder.timeEdit.setText("");
                }
            }
        });

        boolean enabled = mViewHolder.timeEdit.getText().length() > 0;
        mViewHolder.check.setChecked(enabled);
        mViewHolder.timeEdit.setEnabled(enabled);
        mViewHolder.modeSpinner.setEnabled(enabled);

        return view;
    }

    private void changeDay() {

        String time = mViewHolder.timeEdit.getText().toString();
        HombotSchedule.Mode mode = HombotSchedule.Mode.valueOf(mViewHolder.modeSpinner.getSelectedItem().toString());

        if (mListener != null) {
            if (time == null || time.isEmpty()) {
                mListener.clearScheduleDay(mDay);
            } else {
                mListener.setScheduleDay(mDay, time, mode);
            }
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
        public void setScheduleDay(HombotSchedule.Weekday day, String time, HombotSchedule.Mode mode);

        public void clearScheduleDay(HombotSchedule.Weekday day);
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        private EditText mField;

        public TimePickerFragment() {

        }

        public void setEditText(EditText field) {
            mField = field;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // CONVERT EXISTING TIME FROM 12 HOUR TO 24 HOUR IF USER USES 12 HOUR FORMAT! TIMEPICKER ALWAYS WANTS 24 HOUR FORMAT HERE!
            String timeString = mField.getText().toString();
            if (timeString != null && !timeString.isEmpty()) {
                if (!DateFormat.is24HourFormat(getActivity()) && timeString.matches(".*(AM|PM)")) {
                    try {
                        final SimpleDateFormat sdf = new SimpleDateFormat("hh:mmaa", Locale.US);
                        final Date dateObj = sdf.parse(timeString);
                        timeString = new SimpleDateFormat("HH:mm").format(dateObj);
                    } catch (final ParseException e) {
                        throw new IllegalArgumentException("Wrong time format given for DayData: " + timeString);
                    }
                }
                String[] timeParts = timeString.split(":");
                if (timeParts.length < 2) {
                    throw new IllegalArgumentException("Wrong time format given for DayData: " + timeString);
                }
                hour = Integer.parseInt(timeParts[0]);
                minute = Integer.parseInt(timeParts[1]);
            }

/*
            String timeString = mField.getText().toString();
            String[] times = timeString.split(":");
            if (times.length == 2) {
                times[1] = times[1].replaceAll("(AM|PM)", "");
                hour = Integer.parseInt(times[0]);
                minute = Integer.parseInt(times[1]);
                if (timeString.endsWith("PM")) {
                    hour += 12;
                }
            }
*/

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            String time = hourOfDay + ":" + minute;

            // CONVERT 24 HOUR TO 12 HOUR IF USER USES 12 HOUR FORMAT! TIMEPICKER ALWAYS RETURNS 24 HOUR FORMAT HERE!
            if (!DateFormat.is24HourFormat(getActivity()) && !time.matches(".*(AM|PM)")) {
                try {
                    final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    final Date dateObj = sdf.parse(time);
                    time = new SimpleDateFormat("hh:mmaa", Locale.US).format(dateObj);
                } catch (final ParseException e) {
                    throw new IllegalArgumentException("Wrong time format given for DayData: " + time);
                }
            } else if (DateFormat.is24HourFormat(getActivity())) {
                try {
                    final SimpleDateFormat sdf = new SimpleDateFormat("H:m");
                    final Date dateObj = sdf.parse(time);
                    time = new SimpleDateFormat("HH:mm").format(dateObj);
                } catch (final ParseException e) {
                    throw new IllegalArgumentException("Wrong time format given for DayData: " + time);
                }
            }

            mField.setText(time);
        }
    }

}
