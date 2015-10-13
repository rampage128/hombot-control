package de.jlab.android.hombot.wear;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.concurrent.TimeUnit;

import de.jlab.android.hombot.SettingsActivity;
import de.jlab.android.hombot.common.core.HombotStatus;
import de.jlab.android.hombot.common.core.RequestEngine;
import de.jlab.android.hombot.common.wear.WearMessages;
import de.jlab.android.hombot.core.HttpRequestEngine;
import de.jlab.android.hombot.data.HombotDataContract;
import de.jlab.android.hombot.data.HombotDataOpenHelper;

/**
 * Created by frede_000 on 10.10.2015.
 */
public class WearListenerService extends WearableListenerService implements RequestEngine.RequestListener {

    private HttpRequestEngine mRequestEngine;
    private GoogleApiClient mGoogleApiClient;

    private String mRecentNodeId;

    private Cursor mBotCursor;

    @Override
    public void onCreate() {
        super.onCreate();

        mRequestEngine = new HttpRequestEngine();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        HombotDataOpenHelper dataHelper = new HombotDataOpenHelper(this);
        final SQLiteDatabase db = dataHelper.getReadableDatabase();
        mBotCursor = db.query(HombotDataContract.BotEntry.TABLE_NAME, new String[]{HombotDataContract.BotEntry._ID, HombotDataContract.BotEntry.COLUMN_NAME_NAME, HombotDataContract.BotEntry.COLUMN_NAME_ADDRESS}, null, new String[0], null, null, HombotDataContract.BotEntry.COLUMN_NAME_NAME);

        selectBot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBotCursor.close();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(getClass().getSimpleName(), messageEvent.getPath());

        mRecentNodeId = messageEvent.getSourceNodeId();

        if (WearMessages.MESSAGE_COMMAND.equalsIgnoreCase(messageEvent.getPath())) {
            mRequestEngine.sendCommand(RequestEngine.Command.valueOf(new String(messageEvent.getData())));
        } else if (WearMessages.MESSAGE_STATUS.equalsIgnoreCase(messageEvent.getPath())) {
            selectBot();
            mRequestEngine.requestStatus(this);
        }
    }

    private void reply(String message, byte[] data, String nodeId) {
        if (mGoogleApiClient.blockingConnect(1000, TimeUnit.MILLISECONDS).isSuccess()) {
            Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, message, data);
            mGoogleApiClient.disconnect();
        }
    }

    private void selectBot() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        long recentBotId = sp.getLong(SettingsActivity.PREF_RECENT_BOT, -1);
        if (mBotCursor.getCount() > 0) {
            for (int i = 0; i < mBotCursor.getCount(); i++) {
                mBotCursor.moveToPosition(i);
                long checkId = mBotCursor.getLong(mBotCursor.getColumnIndexOrThrow(HombotDataContract.BotEntry._ID));
                if (recentBotId == checkId) {
                    String botAddress = mBotCursor.getString(mBotCursor.getColumnIndexOrThrow(HombotDataContract.BotEntry.COLUMN_NAME_ADDRESS));
                    mRequestEngine.setBotAddress(botAddress);
                }
            }
        }
    }

    @Override
    public void statusUpdate(HombotStatus status) {
        byte[] data = null;
        if (status.getSourceString() != null) {
            data = status.getSourceString().getBytes();
        }
        reply(WearMessages.MESSAGE_STATUS, data, mRecentNodeId);
    }
}
