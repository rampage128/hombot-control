package de.jlab.android.hombot.core;

import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import de.jlab.android.hombot.common.core.HombotMap;
import de.jlab.android.hombot.common.core.HombotSchedule;
import de.jlab.android.hombot.common.core.HombotStatus;
import de.jlab.android.hombot.common.core.RequestEngine;

/**
 * Created by frede_000 on 02.10.2015.
 */
public class HttpRequestEngine extends RequestEngine {

    private String mBotAddress = null;
    private HombotStatus mStatus;

    public HttpRequestEngine() {
        // HTTP connection reuse which was buggy pre-froyo
        if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    @Override
    public void requestStatus(RequestListener listener) {
        String statusString = null;
        try {
            URL statusUrl = new URL("http://" + mBotAddress + "/status.txt");
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

        if (listener != null) {
            mStatus = HombotStatus.getInstance(statusString, mStatus);
            listener.statusUpdate(mStatus);
        }
    }

    @Override
    public HombotMap requestMap(String mapName) {
        if (mapName == null) {
            mapName = HombotMap.MAP_GLOBAL;
        }
        String mapLocation = ".../usr/data/blackbox/"+mapName;
        if (HombotMap.MAP_GLOBAL.equalsIgnoreCase(mapName)) {
            mapLocation = ".../usr/data/"+mapName;
        }

        try {
            URL statusUrl = new URL("http://" + mBotAddress + "/" + mapLocation);
            HttpURLConnection urlConnection = (HttpURLConnection) statusUrl.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = in.read(buffer, 0, buffer.length)) != -1) {
                baos.write(buffer, 0, read);
            }
            baos.flush();
            ByteBuffer buf = ByteBuffer.wrap(baos.toByteArray());
            urlConnection.disconnect();
            buf.order(ByteOrder.LITTLE_ENDIAN);
            HombotMap map = HombotMap.getInstance(buf);
            return map;
            /*
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append("\n");
            }
            urlConnection.disconnect();
            return HombotMap.getInstance(total.toString());
            */
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<String> requestMapList() {
        List<String> resultList = new ArrayList<String>();
        if (mBotAddress == null) {
            return resultList;
        }
        String statusString = null;
        try {
            URL statusUrl = new URL("http://" + mBotAddress + "/cleandata.html");
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


        try {
            JSONObject resultObject = new JSONObject(statusString);
            JSONArray mapList = resultObject.getJSONArray("maps");

            for (int i = 0; i < mapList.length(); i++) {
                resultList.add(mapList.getString(i));
            }
        } catch (JSONException e) {
            Log.e(getClass().getSimpleName(), "Error parsing JSON from maplist!", e);
        }

        return resultList;
    }

    @Override
    public HombotSchedule requestSchedule() {
        try {
            URL statusUrl = new URL("http://" + mBotAddress + "/timer.txt");
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

    @Override
    public void updateSchedule(final HombotSchedule schedule) {
        new Thread(new Runnable() {

            public void run() {
                try {
                    URL statusUrl = new URL("http://" + mBotAddress + "/sites/schedule.html?" + schedule.getCommandString());
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

    @Override
    protected void dispatchCommand(final String commandString) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    URL statusUrl = new URL("http://" + mBotAddress + "/json.cgi?" + commandString);
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

    public void setBotAddress(String address) {
        this.mBotAddress = address;
    }

}
