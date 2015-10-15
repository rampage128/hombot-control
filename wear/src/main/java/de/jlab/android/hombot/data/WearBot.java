package de.jlab.android.hombot.data;

import com.google.android.gms.wearable.DataMap;

import de.jlab.android.hombot.common.wear.WearMessages;

/**
 * Created by frede_000 on 15.10.2015.
 */
public class WearBot {

    private long id;
    private String address;
    private String name;

    public WearBot(DataMap botMap) {
        this.id = botMap.getLong(WearMessages.MAPENTRY_BOT_ID);
        this.address = botMap.getString(WearMessages.MAPENTRY_BOT_ADDRESS);
        this.name = botMap.getString(WearMessages.MAPENTRY_BOT_NAME);
    }

    public long getId() {
        return this.id;
    }

    public String getAddress() {
        return this.address;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return this.name;
    }

}

