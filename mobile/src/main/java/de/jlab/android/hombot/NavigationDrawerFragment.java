package de.jlab.android.hombot;


import android.app.Activity;
import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import de.jlab.android.hombot.core.HombotStatus;
import de.jlab.android.hombot.sections.JoySection;
import de.jlab.android.hombot.sections.MapSection;
import de.jlab.android.hombot.sections.PlaceholderSection;
import de.jlab.android.hombot.sections.ScheduleSection;
import de.jlab.android.hombot.sections.StatusSection;
import de.jlab.android.hombot.utils.Colorizer;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    public static final String PREF_BOT_IP = "bot_ip";

    private static class ViewHolder {
        View botPanel;
        TextView botName;
        TextView botVersion;
        EditText botAddress;
        ListView drawerListView;
        DrawerLayout drawerLayout;
        View container;
        Toolbar windowToolbar;
    }

    private ViewHolder mViewHolder;

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    private String mBotAddress = null;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);
        mBotAddress = sp.getString(PREF_BOT_IP, null);
    }

    public void statusUpdate(HombotStatus status) {
        mViewHolder.botName.setText(status.getNickname());
        mViewHolder.botVersion.setText(status.getVersion());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);

        mViewHolder                 = new ViewHolder();
        mViewHolder.botPanel        = view.findViewById(R.id.bot_panel);
        mViewHolder.botName         = (TextView) view.findViewById(R.id.bot_name);
        mViewHolder.botVersion      = (TextView) view.findViewById(R.id.bot_version);
        mViewHolder.botAddress      = (EditText) view.findViewById(R.id.bot_address);
        mViewHolder.drawerListView  = (ListView) view.findViewById(R.id.drawer_list);
        mViewHolder.windowToolbar   = (Toolbar) getActivity().findViewById(R.id.toolbar);

        final SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        } else {
            if (sp.getBoolean(SettingsActivity.PREF_REMEMBER_SECTION, false)) {
                mCurrentSelectedPosition = sp.getInt(SettingsActivity.PREF_RECENT_SECTION, 0);
            }
            // Select either the default item (0) or the last selected item.
            selectItem(mCurrentSelectedPosition);
        }


        mViewHolder.botAddress.setText(sp.getString(PREF_BOT_IP, null));
        mViewHolder.botAddress.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                mBotAddress = s.toString();
                sp.edit().putString(PREF_BOT_IP, mBotAddress).apply();
                mCallbacks.onBotAddressChanged(mBotAddress);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });


        mViewHolder.drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                selectItem(position);
            }
        });

        // TODO CREATE CUSTOM ADAPTER TO ENHANCE MENU CONTROL! (ESPECIALLY TO REPLACE STATIC SETTINGS ITEM)
        mViewHolder.drawerListView.setAdapter(new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                getSections(getResources())));
        mViewHolder.drawerListView.setItemChecked(mCurrentSelectedPosition, true);

        Colorizer colorizer = new Colorizer(getActivity());
        mViewHolder.botPanel.setBackgroundColor(colorizer.getColorPrimary());
        mViewHolder.botAddress.setTextColor(colorizer.getColorPrimaryText());
        mViewHolder.botVersion.setTextColor(colorizer.getColorPrimaryText());
        mViewHolder.botAddress.setTextColor(colorizer.getColorPrimaryText());

        return view;
    }

    public boolean isDrawerOpen() {
        return mViewHolder.drawerLayout != null && mViewHolder.drawerLayout.isDrawerOpen(mViewHolder.container);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mViewHolder.container = getActivity().findViewById(fragmentId);
        mViewHolder.drawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mViewHolder.drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        /*
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
         */

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mViewHolder.drawerLayout,         /* DrawerLayout object */
                mViewHolder.windowToolbar,
                //R.drawable.ic_menu_white_24dp,    /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_36dp);

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if ((!mUserLearnedDrawer && !mFromSavedInstanceState) || mBotAddress == null) {
            mViewHolder.drawerLayout.openDrawer(mViewHolder.container);
        }

        // Defer code dependent on restoration of previous instance state.
        mViewHolder.drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mViewHolder.drawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        // FIXME HACKY AND NEEDS TO BE REMOVED WHEN CUSTOM ADAPTER FOR DRAWERLIST IS IMPLEMENTED
        if (getResources().getString(R.string.settings).equalsIgnoreCase(getSections(getResources())[position])) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return;
        }

        mCurrentSelectedPosition = position;
        if (mViewHolder.drawerListView != null) {
            mViewHolder.drawerListView.setItemChecked(position, true);
        }
        if (mViewHolder.drawerLayout != null) {
            mViewHolder.drawerLayout.closeDrawer(mViewHolder.container);
        }

        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            sp.edit().putInt(SettingsActivity.PREF_RECENT_SECTION, position).commit();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
            mCallbacks.onBotAddressChanged(mBotAddress);
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (true)
            return;

        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mViewHolder.drawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return getActivity().getActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);

        void onBotAddressChanged(String address);
    }

    public static String[] getSections(Resources res) {
        return new String[]{
            res.getString(R.string.section_status),
            res.getString(R.string.section_joy),
            res.getString(R.string.section_schedule),
            res.getString(R.string.section_map),
            res.getString(R.string.settings)
        };
    }

    public static SectionFragment getSectionFragment(int position) {
        switch(position) {
            case 0:
                return StatusSection.newInstance(position);
            case 1:
                return JoySection.newInstance(position);
            case 2:
                return ScheduleSection.newInstance(position);
            case 3:
                return MapSection.newInstance(position);
        }

        return PlaceholderSection.newInstance(position);
    }

}
