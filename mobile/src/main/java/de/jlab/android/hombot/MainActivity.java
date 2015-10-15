package de.jlab.android.hombot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import de.jlab.android.hombot.common.core.HombotMap;
import de.jlab.android.hombot.common.core.HombotSchedule;
import de.jlab.android.hombot.common.core.HombotStatus;
import de.jlab.android.hombot.common.settings.SharedSettings;
import de.jlab.android.hombot.core.HttpRequestEngine;
import de.jlab.android.hombot.common.data.HombotDataContract;
import de.jlab.android.hombot.common.data.HombotDataOpenHelper;
import de.jlab.android.hombot.sections.JoySection;
import de.jlab.android.hombot.sections.MapSection;
import de.jlab.android.hombot.sections.ScheduleSection;
import de.jlab.android.hombot.sections.StatusSection;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SectionFragment.SectionInteractionListener, HttpRequestEngine.RequestListener {

    public static final String PREF_BOT_IP = "bot_ip";

    private static class ViewHolder {
        View botPanel;
        TextView botName;
        TextView botVersion;
        Spinner botSelect;
        Toolbar windowToolbar;
    }
    private ViewHolder mViewHolder;

    HttpRequestEngine mRequestEngine;

    @Override
    public void onWindowFocusChanged(boolean focused) {
        if (focused) {
            //SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            //String botAddress = sp.getString(PREF_BOT_IP, null);
            //mRequestEngine.setBotAddress(botAddress);
            mRequestEngine.start(this);
        } else {
            mRequestEngine.stop();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        mRequestEngine = new HttpRequestEngine();

        setContentView(R.layout.activity_main);

        mViewHolder = new ViewHolder();
        mViewHolder.botPanel    = findViewById(R.id.bot_panel);
        mViewHolder.botSelect  = (Spinner)findViewById(R.id.bot_select);
        mViewHolder.botName     = (TextView)findViewById(R.id.bot_name);
        mViewHolder.botVersion  = (TextView)findViewById(R.id.bot_version);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* TODO IMPLEMENT EVENTS FOR CONTEXT SENSITIVE EXPANDABLE FAB WITH MAIN ACTIONS (CLEAN_START, HOME, CLEAN_SPOT ...)
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // FETCH BOTS
        HombotDataOpenHelper dataHelper = new HombotDataOpenHelper(this);
        final SQLiteDatabase db = dataHelper.getReadableDatabase();
        Cursor botCursor = db.query(HombotDataContract.BotEntry.TABLE_NAME, new String[]{HombotDataContract.BotEntry._ID, HombotDataContract.BotEntry.COLUMN_NAME_NAME, HombotDataContract.BotEntry.COLUMN_NAME_ADDRESS}, null, new String[0], null, null, HombotDataContract.BotEntry.COLUMN_NAME_NAME);
        String[] adapterCols=new String[]{ HombotDataContract.BotEntry.COLUMN_NAME_NAME };
        int[] adapterRowViews=new int[]{ android.R.id.text1 };
        SimpleCursorAdapter sca = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, botCursor, adapterCols, adapterRowViews, 0);
        sca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mViewHolder.botSelect.setAdapter(sca);

        mViewHolder.botSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor)parent.getAdapter().getItem(position);
                sp.edit().putLong(SharedSettings.PREF_RECENT_BOT, id).apply();
                mRequestEngine.setBotAddress(cursor.getString(cursor.getColumnIndexOrThrow(HombotDataContract.BotEntry.COLUMN_NAME_ADDRESS)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        long recentBotId = sp.getLong(SharedSettings.PREF_RECENT_BOT, -1);
        if (sca.getCount() > 0) {
            for (int i = 0; i < sca.getCount(); i++) {
                if (recentBotId == sca.getItemId(i)) {
                    mViewHolder.botSelect.setSelection(i);
                }
            }

        } else {
            startActivity(new Intent(this, BotManagerActivity.class));
        }

        if (getCurrentSection() == null) {
            int lastSection = R.id.nav_status;
            if (sp.getBoolean(SettingsActivity.PREF_REMEMBER_SECTION, false)) {
                lastSection = sp.getInt(SettingsActivity.PREF_RECENT_SECTION, R.id.nav_status);
            }
            //navigationView.getMenu().performIdentifierAction(lastSection, 0);
            navigationView.getMenu().findItem(lastSection).setChecked(true);
            switchSection(lastSection, false);
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_bots) {
            startActivity(new Intent(this, BotManagerActivity.class));
        } else {
            switchSection(id, true);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void switchSection(int sectionId, boolean track) {
        if (sectionId == R.id.nav_joy) {
            switchSection(JoySection.newInstance(R.id.nav_joy), track);
        } else if (sectionId == R.id.nav_schedule) {
            switchSection(ScheduleSection.newInstance(R.id.nav_schedule), track);
        } else if (sectionId == R.id.nav_map) {
            switchSection(MapSection.newInstance(R.id.nav_map), track);
        } else {
            // SWITCH TO DEFAULT IF NOT SURE: R.id.nav_status
            switchSection(StatusSection.newInstance(R.id.nav_status), track);
        }
    }

    private void switchSection(SectionFragment section, boolean track) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.section_container, section, "main_content");
        if (track) {
            transaction.addToBackStack(null);
        }
        transaction.commit();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putInt(SettingsActivity.PREF_RECENT_SECTION, section.getSectionId()).apply();
    }

    private Fragment getCurrentSection() {
        return getSupportFragmentManager().findFragmentByTag("main_content");
    }

      ///////////////////////////
     /// SECTION INTERACTION ///
    ///////////////////////////

    @Override
    public void onSectionAttached(int section) {
        // DEPRECATED
    }

    @Override
    public void sendCommand(HttpRequestEngine.Command command) {
        mRequestEngine.sendCommand(command);
    }

    @Override
    public HombotSchedule requestSchedule() {
        return mRequestEngine.requestSchedule();
    }

    @Override
    public HombotMap requestMap(String mapName) {
        return mRequestEngine.requestMap(mapName);
    }

    @Override
    public List<String> requestMapList() {
        return mRequestEngine.requestMapList();
    }

    @Override
    public void setSchedule(HombotSchedule schedule) {
        mRequestEngine.updateSchedule(schedule);
        Snackbar.make(findViewById(R.id.section_container), R.string.schedule_saved, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    //////////////////////////////
    /// REQUEST ENGINE EVENTS ///
    ////////////////////////////

    @Override
    public void statusUpdate(final HombotStatus status) {
        runOnUiThread(new Runnable() {
            public void run() {
                mViewHolder.botName.setText(status.getNickname());
                mViewHolder.botVersion.setText(status.getVersion());

                Fragment fragment = getCurrentSection();
                if (fragment instanceof SectionFragment) {
                    ((SectionFragment) fragment).statusUpdate(status);
                }
            }
        });
    }
}
