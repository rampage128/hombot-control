package de.jlab.android.hombot.common.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.Locale;

/**
 * Created by frede_000 on 05.10.2015.
 */
public class HombotSchedule {

    public enum Weekday {
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY,
        SUNDAY
    }

    public enum Mode {
        ZZ,
        SB
    }

    private EnumMap<Weekday, DayData> mScheduleMap = new EnumMap<>(Weekday.class);

    private HombotSchedule() {}
    public static HombotSchedule getInstance(String response) {
        HombotSchedule schedule = new HombotSchedule();
        if (response != null) {
            schedule.parseSchedule(response);
        }
        return schedule;
    }

    private void parseSchedule(String response) {
        String[] lines = response.split("\n");

        for (String line : lines) {
            String[] keyVal = line.split("=");
            if (keyVal.length < 2)
                continue;

            String[] dayString = keyVal[1].split(",");

            DayData data = new DayData(dayString[0], dayString.length > 1 ? Mode.valueOf(dayString[1]) : Mode.ZZ);
            mScheduleMap.put(Weekday.valueOf(keyVal[0]), data);
        }
    }

    public void setDay(Weekday day, String time, Mode mode) {
        mScheduleMap.put(day, new DayData(time, mode));
    }

    public void clearDay(Weekday day) {
        mScheduleMap.remove(day);
    }

    public String getDayTime(Weekday day) {
        DayData data = mScheduleMap.get(day);
        return data == null ? "" : data.getTime();
    }

    public Mode getDayMode(Weekday day) {
        DayData data = mScheduleMap.get(day);
        return data == null ? Mode.ZZ : data.getMode();
    }

    public String getCommandString() {
        StringBuilder commandStringBuilder = new StringBuilder("");
        for (Weekday day : Weekday.values()) {
            DayData data = mScheduleMap.get(day);
            commandStringBuilder.append(day.toString()).append("=").append(data == null ? "" : data.toString()).append("&");
        }
        commandStringBuilder.append("SEND=Save");
        return commandStringBuilder.toString();
    }

    public class DayData {

        private String mTime;
        private Mode mMode;

        public DayData(String time, Mode mode) {

            // CONVERT 24 HOUR TO 12 HOUR!
            if (!time.matches(".*(AM|PM)")) {
                try {
                    final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
                    final Date dateObj = sdf.parse(time);
                    time = new SimpleDateFormat("hh:mmaa", Locale.US).format(dateObj);
                } catch (final ParseException e) {
                    throw new IllegalArgumentException("Wrong time format given for DayData: " + time);
                }

/*
                String period = "AM";
                String[] timeparts = time.split(":");
                if (timeparts.length != 2) {
                    throw new IllegalArgumentException("Wrong time format given for DayData: " + time);
                }
                int hours = Integer.parseInt(timeparts[0]);
                if (hours > 11) {
                    hours -= 12;
                    period = "PM";
                } if (hours == 0) {
                    hours = 12;
                }
                time = String.format(Locale.US, "%02d", hours) + ":" + timeparts[1] + period;
*/
            }

            mTime = time;
            mMode = mode;
        }

        public String getTime() {
            return mTime;
        }

        public Mode getMode() {
            return mMode;
        }

        @Override
        public String toString() {
            return mTime + "," + mMode;
        }

    }

}
