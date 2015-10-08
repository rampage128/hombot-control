package de.jlab.android.hombot.core;

import android.os.Build;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by frede_000 on 02.10.2015.
 */
public class RequestEngine {

    private HombotStatus mStatus;

    public enum Command {
        TURBO, MODE, HOME, REPEAT, START, PAUSE,

        MODE_MYSPACE, MODE_SPIRAL, MODE_ZIGZAG, MODE_CELLBYCELL,

        JOY_FORWARD, JOY_LEFT, JOY_RIGHT, JOY_BACK, JOY_RELEASE
    }

    public interface RequestListener {
        void statusUpdate(HombotStatus status);

        void runOnUiThread(Runnable runnable);
    }

    private String botAddress = null;

    private RequestListener mListener = null;
    private Thread mThread = null;

    public RequestEngine() {
        // HTTP connection reuse which was buggy pre-froyo
        if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    public HombotStatus requestStatus() {
        String statusString = null;
        try {
            URL statusUrl = new URL("http://" + botAddress + "/status.txt");
            HttpURLConnection urlConnection = (HttpURLConnection) statusUrl.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append("\n");
            }
            urlConnection.disconnect();
            statusString = total.toString();
        } catch (Exception ignored) {}

        return HombotStatus.getInstance(statusString, mStatus);
    }

    public HombotMap requestMap(String mapName) {
        if (mapName == null) {
            mapName = HombotMap.MAP_GLOBAL;
        }
        String mapLocation = ".../usr/data/blackbox/"+mapName;
        if (HombotMap.MAP_GLOBAL.equalsIgnoreCase(mapName)) {
            mapLocation = ".../usr/data/"+mapName;
        }

        try {
            URL statusUrl = new URL("http://" + botAddress + "/" + mapLocation);
            HttpURLConnection urlConnection = (HttpURLConnection) statusUrl.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append("\n");
            }
            urlConnection.disconnect();
            return HombotMap.getInstance(total.toString());
        } catch (Exception e) {
            return HombotMap.getInstance(null);
        }
    }

    public HombotSchedule requestSchedule() {
        try {
            URL statusUrl = new URL("http://" + botAddress + "/timer.txt");
            HttpURLConnection urlConnection = (HttpURLConnection) statusUrl.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append("\n");
            }
            urlConnection.disconnect();
            return HombotSchedule.getInstance(total.toString());
        } catch (Exception e) {
            return HombotSchedule.getInstance(null);
        }
    }

    public void updateSchedule(final HombotSchedule schedule) {
        new Thread(new Runnable() {

            public void run() {
                try {
                    URL statusUrl = new URL("http://" + botAddress + "/sites/schedule.html?" + schedule.getCommandString());
                    HttpURLConnection urlConnection = (HttpURLConnection) statusUrl.openConnection();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                            /*
                            BufferedReader r = new BufferedReader(new InputStreamReader(in));
                            StringBuilder total = new StringBuilder();
                            String line;
                            while ((line = r.readLine()) != null) {
                                total.append(line).append("\n");
                            }
                            */
                    urlConnection.disconnect();
                } catch (Exception ignored) {
                }

            }
        }).start();
    }

    public void sendCommand(final Command command) {
        new Thread(new Runnable() {
            public void run() {

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

                try {
                    URL statusUrl = new URL("http://" + botAddress + "/json.cgi?" + commandString);
                    HttpURLConnection urlConnection = (HttpURLConnection) statusUrl.openConnection();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    /*
                    BufferedReader r = new BufferedReader(new InputStreamReader(in));
                    StringBuilder total = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        total.append(line).append("\n");
                    }
                    */
                    urlConnection.disconnect();
                } catch (Exception ignored) {}
            }
        }).start();
    }

    public void start(RequestListener listener) {
        mListener = listener;
        mThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        final HombotStatus status = requestStatus();
                        if (mListener != null) {
                            mListener.runOnUiThread(new Runnable() {
                                public void run() {
                                    if (mListener != null) {
                                        mListener.statusUpdate(status);
                                    }
                                }
                            });
                        }
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        mThread.start();
    }

    public void stop() {
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
        mListener = null;
    }

    public void setBotAddress(String address) {
        this.botAddress = address;
    }

}
