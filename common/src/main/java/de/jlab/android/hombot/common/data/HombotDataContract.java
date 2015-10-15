package de.jlab.android.hombot.common.data;

import android.provider.BaseColumns;

/**
 * Created by frede_000 on 08.10.2015.
 */
public class HombotDataContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public HombotDataContract() {}

    /* Inner class that defines the table contents */
    public static abstract class BotEntry implements BaseColumns {
        public static final String TABLE_NAME = "bot";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_ADDRESS = "address";
    }
}
