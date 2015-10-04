package de.jlab.android.hombot.core;

/**
 * Created by frede_000 on 02.10.2015.
 */
public class HombotStatus {

    public static enum Status {
        OFFLINE, CHARGING, STANDBY, WORKING, UNDOCKING, DOCKING, PAUSE, HOMING
    }

    public static enum Mode {
        ZIGZAG, CELLBYCELL, SPIRAL, MYSPACE, MYSPACE_RECORD
    }

    private Status status = Status.OFFLINE;
    private int battery = 0;
    private float cpuIdle = 0;
    private float cpuUser = 0;
    private float cpuSys = 0;
    private float cpuNice = 0;
    private boolean turbo = false;
    private boolean repeat = false;
    private Mode mode = Mode.ZIGZAG;
    private String nickname = null;
    private String version = null;

    private HombotStatus() {}

    private void parseStatus(String response) {
        String[] lines = response.split("\n");

        for (String line : lines) {
            String[] keyval = line.split("=");
            if (keyval.length < 2)
                continue;

            String value = keyval[1].replaceAll("\"", "");

            if (keyval[0].equalsIgnoreCase("JSON_ROBOT_STATE")) {
                if (value.equalsIgnoreCase("CHARGING")) {
                    status = Status.CHARGING;
                } else if (value.equalsIgnoreCase("STANDBY")) {
                    status = Status.STANDBY;
                } else if (value.equalsIgnoreCase("BACKMOVING_INIT")) {
                    status = Status.UNDOCKING;
                } else if (value.equalsIgnoreCase("WORKING")) {
                    status = Status.WORKING;
                } else if (value.equalsIgnoreCase("HOMING")) {
                    status = Status.HOMING;
                } else if (value.equalsIgnoreCase("DOCKING")) {
                    status = Status.DOCKING;
                } else if (value.equalsIgnoreCase("PAUSE")) {
                    status = Status.PAUSE;
                }

            } else if (keyval[0].equalsIgnoreCase("JSON_BATTPERC")) {
                battery = Integer.parseInt(value);

            } else if (keyval[0].equalsIgnoreCase("CPU_IDLE")) {
                cpuIdle = Float.parseFloat(value);
            } else if (keyval[0].equalsIgnoreCase("CPU_USER")) {
                cpuUser = Float.parseFloat(value);
            } else if (keyval[0].equalsIgnoreCase("CPU_SYS")) {
                cpuSys = Float.parseFloat(value);
            } else if (keyval[0].equalsIgnoreCase("CPU_NICE")) {
                cpuNice = Float.parseFloat(value);

            } else if (keyval[0].equalsIgnoreCase("JSON_TURBO")) {
                turbo = Boolean.parseBoolean(value);
            } else if (keyval[0].equalsIgnoreCase("JSON_REPEAT")) {
                repeat = Boolean.parseBoolean(value);

            } else if (keyval[0].equalsIgnoreCase("JSON_MODE")) {
                if (value.equalsIgnoreCase("ZZ")) {
                    mode = Mode.ZIGZAG;
                } else if (value.equalsIgnoreCase("SB")) {
                    mode = Mode.CELLBYCELL;
                } else if (value.equalsIgnoreCase("SPOT")) {
                    mode = Mode.SPIRAL;
                } else if (value.equalsIgnoreCase("MACRO")) {
                    mode = Mode.MYSPACE;
                } else if (value.equalsIgnoreCase("MACRO_SECTOR")) {
                    mode = Mode.MYSPACE_RECORD;
                }
            } else if (keyval[0].equalsIgnoreCase("JSON_NICKNAME")) {
                this.nickname = value;
            } else if (keyval[0].equalsIgnoreCase("JSON_VERSION")) {
                this.version = value;
            }
        }
    }

    public static HombotStatus getInstance(String response) {
        HombotStatus status = new HombotStatus();
        if (response != null) {
            status.parseStatus(response);
        }
        return status;
    }

    public boolean getTurbo() {
        return this.turbo;
    }

    public boolean getRepeat() {
        return this.repeat;
    }

    public Mode getMode() {
        return this.mode;
    }

    public Status getStatus() {
        return this.status;
    }

    public int getBatteryPercent() {
        return this.battery;
    }

    public String getNickname() { return this.nickname; }

    public String getVersion() { return this.version; }

}
