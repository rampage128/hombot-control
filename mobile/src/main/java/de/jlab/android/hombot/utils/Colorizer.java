package de.jlab.android.hombot.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import de.jlab.android.hombot.R;
import de.jlab.android.hombot.SettingsActivity;

/**
 * Created by frede_000 on 06.10.2015.
 */
public final class Colorizer {

    private int mColorPrimary;
    private int mColorPrimaryDark;
    private int mPrimaryTextColor;
    private int mTextColor;
    private int mTextColorInverse;
    private int mBgColor;

    public Colorizer(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        mColorPrimary = Color.parseColor(sp.getString(SettingsActivity.PREF_COLOR_PRIMARY, context.getResources().getString(R.string.pref_default_color_primary)));
        mColorPrimaryDark = getDarkerColor(mColorPrimary);
        if ("light".equalsIgnoreCase(sp.getString(SettingsActivity.PREF_COLOR_THEME, context.getResources().getString(R.string.pref_default_color_theme)))) {
            mTextColor = Color.BLACK;
            mTextColorInverse = Color.WHITE;
            mBgColor = Color.LTGRAY;
        } else {
            mTextColor = Color.LTGRAY;
            mTextColorInverse = Color.BLACK;
            mBgColor = Color.DKGRAY;
        }

        mPrimaryTextColor = getContrastingTextColor(mColorPrimary);
    }

    public void colorize(Activity activity) {
        Window window = activity.getWindow();

/*
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(mColorPrimaryDark);
*/
        window.getDecorView().setBackgroundColor(mBgColor);


        colorizeToolbar((Toolbar) activity.findViewById(R.id.toolbar), activity);
    }

    public void colorizeToolbar(Toolbar toolbar, Activity activity) {
        toolbar.setBackgroundColor(mColorPrimary);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(mColorPrimaryDark);
        }

        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View toolbarItem = toolbar.getChildAt(i);

            if (toolbarItem instanceof TextView) {
                ((TextView) toolbarItem).setTextColor(mPrimaryTextColor);
            } else if (toolbarItem instanceof ImageView) {
                ((ImageView) toolbarItem).getDrawable().setColorFilter(mPrimaryTextColor, PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    public void colorizeIcon(ImageView icon, int color) {
        icon.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    @Deprecated
    public void colorize(View view, boolean recursive, boolean inverse) {

        int textColor = mTextColor;
        if (inverse) {
            textColor = mTextColorInverse;
        }

        if (view instanceof TextView) {
            ((TextView) view).setTextColor(textColor);
            if (view instanceof Button) {
                tintBackground(view, textColor);
            }
        } else if (view instanceof ImageView) {
            ((ImageView) view).getDrawable().setColorFilter(mColorPrimary, PorterDuff.Mode.SRC_ATOP);
        } else if (view instanceof ProgressBar) {
            ((ProgressBar) view).getProgressDrawable().setColorFilter(mColorPrimary, PorterDuff.Mode.SRC_ATOP);
            tintBackground(view, mColorPrimary);
        }

        if (recursive && view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)view;
            for (int i = 0; i < group.getChildCount(); i++) {
                colorize(group.getChildAt(i), true, inverse);
            }
        }
    }

    public int getColorPrimary() {
        return this.mColorPrimary;
    }

    public int getColorPrimaryText() {
        return this.mPrimaryTextColor;
    }

    public int getColorPrimaryDark() {
        return this.mColorPrimaryDark;
    }

    public int getColorText() {
        return this.mTextColor;
    }

    public int getColorTextInverse() {
        return this.mTextColorInverse;
    }

    public void colorize(Fragment fragment, boolean inverse) {
        colorize(fragment.getView(), true, inverse);
    }

    public void tintBackground(View view, int color) {
        Drawable bg = view.getBackground();
        if (bg == null)
            return;

        bg.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        /*
        bg = DrawableCompat.wrap(bg);

        DrawableCompat.setTint(bg, color);
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.MULTIPLY);
        */
    }

    /**
     * Gets a darker color form any input-color
     * Credits to http://stackoverflow.com/a/4928826
     * @param color input color to darken
     * @return a darker version of the given color
     */
    private int getDarkerColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }

    /**
     * Primitive function to get a contrasting text-color for a given color
     * @param color Color to get contrasting color for
     * @return {@Link Color.White} if color is darker than 0.5 or {@Link Color.Black}
     */
    public int getContrastingTextColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return hsv[2] < 0.9f ? Color.WHITE : Color.BLACK;
    }

}
