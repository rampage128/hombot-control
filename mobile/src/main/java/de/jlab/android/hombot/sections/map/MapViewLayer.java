package de.jlab.android.hombot.sections.map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.jlab.android.hombot.common.core.HombotMap;

/**
 * Created by frede_000 on 12.10.2015.
 */
public class MapViewLayer {

    private List<? extends MapDrawable.MapDrawableItem> mDrawables;

    private int mPrimaryColor = Color.WHITE;
    private boolean mEnabled = true;

    public MapViewLayer(List<? extends MapDrawable.MapDrawableItem> drawables, int primaryColor) {
        mDrawables = drawables;
        mPrimaryColor = primaryColor;
    }

    public void draw(Canvas c, Paint p, float zoom) {
        if (!mEnabled || mDrawables == null) {
            return;
        }

        p.setColor(mPrimaryColor);
        for (MapDrawable.MapDrawableItem drawable : mDrawables) {
            drawable.draw(c, p, zoom);
        }
    }

    public void setItems(List<? extends MapDrawable.MapDrawableItem> drawables) {
        this.mDrawables = drawables;
    }

    public void setPrimaryColor(int color) {
        this.mPrimaryColor = color;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

}
