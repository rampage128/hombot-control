package de.jlab.android.hombot.utils;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.ArrayRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by frede_000 on 19.12.2015.
 */
public class ColorableArrayAdapter<T> extends ArrayAdapter<T> {

    private Colorizer colorizer;
    private int referenceColor;

    private int mFieldId = 0;

    public static ColorableArrayAdapter<CharSequence> createFromResource(Context context,
                                                                @ArrayRes int textArrayResId, @LayoutRes int textViewResId, Colorizer colorizer, int referenceColor) {
        CharSequence[] strings = context.getResources().getTextArray(textArrayResId);
        ColorableArrayAdapter ad = new ColorableArrayAdapter<>(context, textViewResId, strings, colorizer, referenceColor);
        return ad;
    }

    public ColorableArrayAdapter(Context context, @LayoutRes int resource, @NonNull T[] objects, Colorizer colorizer, int referenceColor) {
        super(context, resource, objects);

        mFieldId = resource;
        this.referenceColor = referenceColor;
        this.colorizer = colorizer;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        ((TextView)view).setTextColor(this.colorizer.getContrastingTextColor(this.referenceColor));
        return view;
    }


}
