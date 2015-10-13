package de.jlab.android.hombot.common.core;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by frede_000 on 09.10.2015.
 */
public abstract class RequestEngine {

    public enum Command {
        TURBO, MODE, HOME, REPEAT, START, PAUSE,

        MODE_MYSPACE, MODE_SPIRAL, MODE_ZIGZAG, MODE_CELLBYCELL,

        JOY_FORWARD, JOY_LEFT, JOY_RIGHT, JOY_BACK, JOY_RELEASE
    }

    public interface RequestListener {
        void statusUpdate(HombotStatus status);
    }

    private RequestListener mListener = null;
    private Thread mThread = null;

    public abstract void requestStatus(RequestListener listener);

    public abstract HombotMap requestMap(String mapName);

    public abstract List<String> requestMapList();

    public abstract HombotSchedule requestSchedule();

    public abstract void updateSchedule(final HombotSchedule schedule);

    protected abstract void dispatchCommand(String commandString);

    public void sendCommand(final Command command) {
        String commandString = null;

        if (Command.TURBO.equals(command)) {
            commandString = "turbo";
        } else if (Command.REPEAT.equals(command)) {
            commandString = "repeat";
        } else if (Command.MODE.equals(command)) {
            commandString = "mode";
        } else if (Command.START.equals(command)) {
            commandString = "{\"COMMAND\":\"CLEAN_START\"}";
        } else if (Command.PAUSE.equals(command)) {
            commandString = "{\"COMMAND\":\"PAUSE\"}";
        } else if (Command.HOME.equals(command)) {
            commandString = "{\"COMMAND\":\"HOMING\"}";

        } else if (Command.MODE_MYSPACE.equals(command)) {
            commandString = "{\"COMMAND\":{\"CLEAN_MODE\":\"MACRO_SECTOR\"}}";
        } else if (Command.MODE_SPIRAL.equals(command)) {
            commandString = "{\"COMMAND\":{\"CLEAN_MODE\":\"CLEAN_SPOT\"}}";
        } else if (Command.MODE_ZIGZAG.equals(command)) {
            commandString = "{\"COMMAND\":{\"CLEAN_MODE\":\"CLEAN_ZZ\"}}";
        } else if (Command.MODE_CELLBYCELL.equals(command)) {
            commandString = "{\"COMMAND\":{\"CLEAN_MODE\":\"CLEAN_SB\"}}";

        } else if (Command.JOY_FORWARD.equals(command)) {
            commandString = "{\"JOY\":\"FORWARD\"}";
        } else if (Command.JOY_LEFT.equals(command)) {
            commandString = "{\"JOY\":\"LEFT\"}";
        } else if (Command.JOY_RIGHT.equals(command)) {
            commandString = "{\"JOY\":\"RIGHT\"}";
        } else if (Command.JOY_BACK.equals(command)) {
            commandString = "{\"JOY\":\"BACKWARD\"}";
        } else if (Command.JOY_RELEASE.equals(command)) {
            commandString = "{\"JOY\":\"RELEASE\"}";
        }

        dispatchCommand(commandString);
    }

    public final void start(RequestListener listener) {
        mListener = listener;
        mThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        requestStatus(mListener);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        mThread.start();
    }

    public final void stop() {
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
        mListener = null;
    }

    protected RequestListener getListener() {
        return this.mListener;
    }

}
