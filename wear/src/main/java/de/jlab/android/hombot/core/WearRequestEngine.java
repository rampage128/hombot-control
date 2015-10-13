package de.jlab.android.hombot.core;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import de.jlab.android.hombot.common.core.HombotMap;
import de.jlab.android.hombot.common.core.HombotSchedule;
import de.jlab.android.hombot.common.core.HombotStatus;
import de.jlab.android.hombot.common.core.RequestEngine;
import de.jlab.android.hombot.common.wear.WearMessages;

/**
 * Created by frede_000 on 09.10.2015.
 */
public class WearRequestEngine extends RequestEngine implements MessageApi.MessageListener {



    private static final long CONNECTION_TIME_OUT_MS = 1000;

    private GoogleApiClient mGoogleApiClient;
    private String mNodeId;

    private void retrieveDeviceNode() {

        final CapabilityApi.GetCapabilityResult result =
                Wearable.CapabilityApi.getCapability(mGoogleApiClient, WearMessages.CAPABILITY_HOMBOT_HOST, Wearable.CapabilityApi.FILTER_REACHABLE).await();

        // NodeApi.GetConnectedNodesResult result = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

        Set<Node> nodes = result.getCapability().getNodes();
        if (nodes.size() > 0) {
            for (Node node : nodes) {
                if (node.isNearby()) {
                    mNodeId = node.getId();
                }
            }
        }
    }

    private void sendWearMessage(final String path, final byte[] data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (reconnect()) {
                    retrieveDeviceNode();
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, mNodeId, path, data);
                    Log.d(getClass().getSimpleName(), path);
                }
            }
        }).start();
    }

    public void connect(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                    }
                })
                        // Request access only to the Wearable API
                .addApi(Wearable.API)
                .build();
        Wearable.MessageApi.addListener(mGoogleApiClient, WearRequestEngine.this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                reconnect();
            }
        }).start();
    }

    public void disconnect() {
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    private boolean reconnect() {
        return mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS).isSuccess();
    }


    ///////////////////////////////
    /// REQUEST ENGINE ////////////
    ///////////////////////////////

    @Override
    public void requestStatus(RequestListener listener) {
        //mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
        sendWearMessage(WearMessages.MESSAGE_STATUS, null);
        //mGoogleApiClient.disconnect();
    }

    @Override
    public HombotMap requestMap(String mapName) {
        return null;
    }

    @Override
    public List<String> requestMapList() {
        return null;
    }

    @Override
    public HombotSchedule requestSchedule() {
        return null;
    }

    @Override
    public void updateSchedule(HombotSchedule schedule) {

    }

    @Override
    public void sendCommand(Command command) {
        dispatchCommand(command.name());
    }

    @Override
    protected void dispatchCommand(final String commandString) {
        //mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
        sendWearMessage(WearMessages.MESSAGE_COMMAND, commandString.getBytes());
        //mGoogleApiClient.disconnect();
    }

      ///////////////////////////////
     /// WEAR CONNECTION ///////////
    ///////////////////////////////

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (WearMessages.MESSAGE_STATUS.equalsIgnoreCase(messageEvent.getPath())) {
            HombotStatus status = HombotStatus.getInstance(new String(messageEvent.getData()), null);
            getListener().statusUpdate(status);
        }
    }
}
