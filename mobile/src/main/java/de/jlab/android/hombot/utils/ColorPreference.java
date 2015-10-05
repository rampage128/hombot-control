package de.jlab.android.hombot.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import de.jlab.android.hombot.R;
import de.jlab.android.hombot.SettingsActivity;

/**
 * Created by frede_000 on 04.10.2015.
 */
public class ColorPreference extends Preference {

    private static class ViewHolder {
        View colorPreview;
    }

    private ViewHolder mViewHolder;
    private String mValue;

    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        LayoutInflater li = (LayoutInflater)getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view = li.inflate( R.layout.preference_color, parent, false);

        mViewHolder = new ViewHolder();
        mViewHolder.colorPreview = view.findViewById(R.id.color_preview);

        return view;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        View colorPreview = view.findViewById(R.id.color_preview);
        colorPreview.setBackgroundColor(Color.parseColor(mValue));
    }

    public void setValue(String value) {
        if (callChangeListener(value)) {
            mValue = value;
            persistString(value);
            notifyChanged();
        }
    }

    public void updateColor() {
        if (mViewHolder != null) {
            View colorPreview = mViewHolder.colorPreview;
            colorPreview.setBackgroundColor(Color.parseColor(mValue));
        }
    }

    @Override
    protected void onClick() {
        super.onClick();

        int color = Color.parseColor(mValue);

        final ColorPicker cp = new ColorPicker((SettingsActivity)getContext(), Color.red(color), Color.green(color), Color.blue(color));
        cp.show();

        Button okColor = (Button)cp.findViewById(R.id.okColorButton);
        okColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* Or the android RGB Color (see the android Color class reference) */
                mValue = String.format("#%06X", (0xFFFFFF & cp.getColor()));
                updateColor();

                cp.dismiss();
            }
        });
    }

    @Override
    protected void onAttachedToActivity() {
        super.onAttachedToActivity();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedString(mValue) : (String) defaultValue);
    }

    public String getValue() {
        return mValue;
    }

}
