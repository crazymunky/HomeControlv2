package com.investigacion.maxi.homecontrolv2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.investigacion.maxi.shared.Cuarto;
import com.investigacion.maxi.shared.Luz;
import com.investigacion.maxi.shared.MockDb;

import java.util.Date;

/**
 * Created by maxit on 7/27/2015.
 */
public class LightsActionsReceiver extends BroadcastReceiver implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    public final static String ACTION_TOGGLE = "com.investigacion.maxi.homecontrolv2.action_toggle";
    public final static String HANDHELD_DATA_PATH = "/handheld_data";

    private MockDb mockDb= new MockDb();
    GoogleApiClient googleClient;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null) {
            return;
        }

        final String action = intent.getAction();
        if(ACTION_TOGGLE.equals(action)) {
            googleClient = new GoogleApiClient.Builder(context)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            googleClient.connect();
            Bundle extras = intent.getExtras();
            if(extras != null){
                int idCuarto = extras.getInt("idCuarto");
                int idLuz = extras.getInt("idLuz");
                Cuarto cuarto = mockDb.getCuartos().get(idCuarto);
                Luz luz = cuarto.getLuces().get(idLuz);

                // Create a DataMap object and send it to the data layer
                DataMap notifyHandheld = new DataMap();
                notifyHandheld.putInt("idCuarto", idCuarto);
                notifyHandheld.putInt("idLuz", idLuz);
                notifyHandheld.putString("action", "change_status_light");
                notifyHandheld.putLong("time", new Date().getTime());
                // Send to data layer
                new SendToDataLayerThread(HANDHELD_DATA_PATH, notifyHandheld).start();
            }
        }
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        Log.v("myApp", "OnConnected entered");
    }


    // Placeholders for required connection callbacks
    @Override
    public void onConnectionSuspended(int cause) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { }

    class SendToDataLayerThread extends Thread {
        String path;
        DataMap dataMap;

        // Constructor for sending data objects to the data layer
        SendToDataLayerThread(String p, DataMap data) {
            path = p;
            dataMap = data;
        }

        public void run() {
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
            for (Node node : nodes.getNodes()) {

                // Construct a DataRequest and send over the data layer
                PutDataMapRequest putDMR = PutDataMapRequest.create(path);
                putDMR.getDataMap().putAll(dataMap);
                PutDataRequest request = putDMR.asPutDataRequest();
                DataApi.DataItemResult result = Wearable.DataApi.putDataItem(googleClient,request).await();
                if (result.getStatus().isSuccess()) {
                    Log.v("myTag", "DataMap: " + dataMap + " sent to: " + node.getDisplayName());
                } else {
                    // Log an error
                    Log.v("myTag", "ERROR: failed to send DataMap");
                }
            }
        }
    }
}