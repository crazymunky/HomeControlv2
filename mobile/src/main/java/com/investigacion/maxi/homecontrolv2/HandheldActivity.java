package com.investigacion.maxi.homecontrolv2;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.investigacion.maxi.shared.Cuarto;
import com.investigacion.maxi.shared.Luz;
import com.investigacion.maxi.shared.MockDb;

import java.util.Date;


public class HandheldActivity extends ActionBarActivity implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private MockDb mockDb;
    private Spinner mSpinnerRooms;
    private Spinner mSpinnerLights;
    private Button mButtonRoom;
    GoogleApiClient googleClient;
    final String WEARABLE_DATA_PATH = "/wearable_data";
    final String HANDHELD_DATA_PATH = "/handheld_data";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handheld);
        mockDb = new MockDb();
        mSpinnerRooms = (Spinner) findViewById(R.id.spinner);
        mSpinnerLights = (Spinner) findViewById(R.id.spinner2);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.rooms_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerRooms.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.lights_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerLights.setAdapter(adapter2);

        mButtonRoom = (Button) findViewById(R.id.btn_room);
        mButtonRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int idRoom = mSpinnerRooms.getSelectedItemPosition();
                int idLuz = mSpinnerLights.getSelectedItemPosition();
                Cuarto cuarto = mockDb.getCuartos().get(idRoom);
                Luz luz = cuarto.getLuces().get(idLuz);

                // Create a DataMap object and send it to the data layer
                DataMap notifyWearable = new DataMap();
                notifyWearable.putInt("idCuarto", idRoom);
                notifyWearable.putInt("idLuz", idLuz);
                notifyWearable.putBoolean("estado", luz.isPrendida());
                notifyWearable.putLong("time", new Date().getTime());
                // Send to data layer
                new SendToDataLayerThread(WEARABLE_DATA_PATH, notifyWearable).start();
            }
        });

        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_handheld, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.v("myApp", "OnConnected entered");
        Wearable.DataApi.addListener(googleClient, this);
    }

    // Disconnect from the data layer when the Activity stops
    @Override
    protected void onStop() {
        if (null != googleClient && googleClient.isConnected()) {
            Wearable.DataApi.removeListener(googleClient, this);
            googleClient.disconnect();
        }
        super.onStop();
    }

    // Placeholders for required connection callbacks
    @Override
    public void onConnectionSuspended(int cause) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        DataMap dataMap;
        for (DataEvent event : dataEventBuffer) {

            // Check the data type
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // Check the data path
                String path = event.getDataItem().getUri().getPath();
                if (path.equals(HANDHELD_DATA_PATH)) {
                    dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    Log.v("myTag", "DataMap received on handheld: " + dataMap);
                    switch(dataMap.getString("action")){
                        case "change_status_light":
                            int idCuarto = dataMap.getInt("idCuarto");
                            int idLuz = dataMap.getInt("idLuz");
                            Cuarto selectedRoom = mockDb.getCuartos().get(idCuarto);
                            Luz selectedLuz = selectedRoom.getLuces().get(idLuz);
                            selectedLuz.setPrendida(!selectedLuz.isPrendida());

                            // Create a DataMap object and send it to the data layer
                            DataMap notifyWearable = new DataMap();
                            notifyWearable.putInt("idCuarto", idCuarto);
                            notifyWearable.putInt("idLuz", idLuz);
                            notifyWearable.putBoolean("estado", selectedLuz.isPrendida());
                            notifyWearable.putLong("time", new Date().getTime());
                            // Send to data layer
                            new SendToDataLayerThread(WEARABLE_DATA_PATH, notifyWearable).start();
                            break;
                        case "change_status_door":
                            int idRoom = dataMap.getInt("idCuarto");;
                            int idPuerta = dataMap.getInt("idPuerta");
                            boolean newState = dataMap.getBoolean("estado");;
                            break;
                    }
                }
            }
        }
    }

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
