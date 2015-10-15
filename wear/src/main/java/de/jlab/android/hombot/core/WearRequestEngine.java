package de.jlab.android.hombot.core;

import android.app.DownloadManager;
import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import de.jlab.android.hombot.common.core.HombotMap;
import de.jlab.android.hombot.common.core.HombotSchedule;
import de.jlab.android.hombot.common.core.HombotStatus;
import de.jlab.android.hombot.common.core.RequestEngine;
import de.jlab.android.hombot.common.wear.WearMessages;
import de.jlab.android.hombot.data.WearBot;

/**
 * Created by frede_000 on 09.10.2015.
 */
public class WearRequestEngine extends RequestEngine implements MessageApi.MessageListener, DataApi.DataListener {



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
                if (internalConnect()) {
                    retrieveDeviceNode();
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, mNodeId, path, data);
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
        Wearable.DataApi.addListener(mGoogleApiClient, WearRequestEngine.this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                internalConnect();
            }
        }).start();
    }

    public void disconnect() {
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    private boolean internalConnect() {
        return mGoogleApiClient.isConnected() || mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS).isSuccess();
    }

    public void selectBot(String address) {
        if (address == null) {
            return;
        }
        sendWearMessage(WearMessages.MESSAGE_BOT_SELECT, address.getBytes());
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
    public void requestBotList() {
        sendWearMessage(WearMessages.MESSAGE_BOT_LIST, null);
    }


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (WearMessages.MESSAGE_STATUS.equalsIgnoreCase(messageEvent.getPath())) {
            HombotStatus status = HombotStatus.getInstance(new String(messageEvent.getData()), null);
            getListener().statusUpdate(status);
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEventBuffer);
        for(DataEvent event : events) {
            if (WearMessages.MESSAGE_BOT_LIST.equalsIgnoreCase(event.getDataItem().getUri().getPath())) {
                final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();

                ArrayList<DataMap> botList = map.getDataMapArrayList(WearMessages.MAPENTRY_BOT_LIST);

                WearBot[] bots = new WearBot[botList.size()];
                for (int i = 0; i < botList.size(); i++) {
                    bots[i] = new WearBot(botList.get(i));
                }

                getListener().updateBotList(bots);
            }
        }
    }

    @Override
    protected WearRequestListener getListener() {
        return (WearRequestListener)super.getListener();
    }

    public interface WearRequestListener extends RequestListener {
        void updateBotList(WearBot[] bots);
    }



}
