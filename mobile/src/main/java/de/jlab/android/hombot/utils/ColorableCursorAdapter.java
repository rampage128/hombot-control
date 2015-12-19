package de.jlab.android.hombot.utils;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by frede_000 on 19.12.2015.
 */
public class ColorableCursorAdapter extends SimpleCursorAdapter {

    private Colorizer colorizer;
    private int referenceColor;

    public ColorableCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags, Colorizer colorizer, int referenceColor) {
        super(context, layout, c, from,to, flags);
        this.colorizer = colorizer;
        this.referenceColor = referenceColor;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = super.newView(context, cursor, parent);
        for (int i = 0; i < mTo.length; i++) {
            ((TextView)view.findViewById(mTo[i])).setTextColor(this.colorizer.getContrastingTextColor(this.referenceColor));
        }
        return view;
    }

}
