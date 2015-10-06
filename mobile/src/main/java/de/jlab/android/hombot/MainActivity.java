package de.jlab.android.hombot;

import android.app.Activity;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import de.jlab.android.hombot.core.HombotSchedule;
import de.jlab.android.hombot.core.HombotStatus;
import de.jlab.android.hombot.core.RequestEngine;
import de.jlab.android.hombot.sections.schedule.ScheduleItem;


public class MainActivity extends AppCompatActivity
implements NavigationDrawerFragment.NavigationDrawerCallbacks, SectionFragment.SectionInteractionListener, RequestEngine.RequestListener, ScheduleItem.DayChangedListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private SectionFragment mSectionFragment;

    private RequestEngine mRequestEngine = null;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestEngine = new RequestEngine();
        setContentView(R.layout.activity_main);

        // Set up Toolbar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.ColorPrimaryDark));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        mSectionFragment = NavigationDrawerFragment.getSectionFragment(position);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, mSectionFragment)
                .commit();
    }

    @Override
    public void onBotAddressChanged(String address) {
        mRequestEngine.setBotAddress(address);
    }

    public void onSectionAttached(int number) {
        setTitle(NavigationDrawerFragment.getSections(getResources())[number]);
        // getSupportActionBar().setTitle(NavigationDrawerFragment.getSections(getResources())[number]);
    }

    @Override
    public void sendCommand(RequestEngine.Command command) {
        mRequestEngine.sendCommand(command);
    }

    @Override
    public HombotSchedule requestSchedule() {
        return mRequestEngine.requestSchedule();
    }

    @Override
    public void setSchedule(HombotSchedule schedule) {
        mRequestEngine.updateSchedule(schedule);
        Toast.makeText(this, R.string.schedule_saved, Toast.LENGTH_LONG).show();
    }

    public void restoreActionBar() {
        //Toolbar toolbar = getSupportActionBar();
        //getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        //toolbar.setDisplayShowTitleEnabled(true);
        //getSupportActionBar().setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWindowFocusChanged(boolean focused) {
        if (focused) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            String botAddress = sp.getString(NavigationDrawerFragment.PREF_BOT_IP, null);
            onBotAddressChanged(botAddress);
            mRequestEngine.start(this);
        } else {
            mRequestEngine.stop();
        }
    }

    @Override
    public void statusUpdate(HombotStatus status) {
        if (mSectionFragment != null) {
            mSectionFragment.statusUpdate(status);
        }

        mNavigationDrawerFragment.statusUpdate(status);
    }

    @Override
    public void clearScheduleDay(HombotSchedule.Weekday day) {
        mSectionFragment.clearScheduleDay(day);
    }

    @Override
    public void setScheduleDay(HombotSchedule.Weekday day, String time, HombotSchedule.Mode mode) {
        mSectionFragment.setScheduleDay(day, time, mode);
    }
}
