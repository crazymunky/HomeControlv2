package com.investigacion.maxi.homecontrolv2;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;
import com.investigacion.maxi.shared.Cuarto;
import com.investigacion.maxi.shared.Luz;

import java.util.Date;

import static com.investigacion.maxi.homecontrolv2.WearActivity.*;

/**
 * Created by michaelHahn on 1/16/15.
 * Listener service or data events on the data layer
 */
public class WearListenerService extends WearableListenerService {

    private static final String WEARABLE_DATA_PATH = "/wearable_data";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        DataMap dataMap;
        for (DataEvent event : dataEvents) {

            // Check the data type
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // Check the data path
                String path = event.getDataItem().getUri().getPath();
                if (path.equals(WEARABLE_DATA_PATH)) {
                    dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    Log.v("myTag", "DataMap received on watch: " + dataMap);
                    int idRoom = dataMap.getInt("idCuarto");
                    int idLuz = dataMap.getInt("idLuz");
                    Cuarto cuarto = mockDb.getCuartos().get(idRoom);
                    Luz luz = cuarto.getLuces().get(idLuz);
                    luz.setPrendida(dataMap.getBoolean("estado"));
                    int idIcono = !luz.isPrendida() ? R.drawable.ic_visibility_off_black_18dp : R.drawable.ic_visibility_black_18dp;
                    int idBg = !luz.isPrendida() ? R.drawable.backgroud_light_off : R.drawable.backgroud_light_on;
                    String textAction = !luz.isPrendida() ? "Turn on" : "Turn off";

                    int notificationId = Integer.parseInt(idRoom + "" + idLuz);
                    //Creamos el intent para realizar la accion de toggle
                    Intent toggleLightIntent = new Intent(this, LightsActionsReceiver.class)
                            .putExtra("idCuarto", idRoom)
                            .putExtra("idLuz", idLuz)
                            .putExtra("time", new Date().getTime());


                    toggleLightIntent.setAction(LightsActionsReceiver.ACTION_TOGGLE);


                    PendingIntent togglePendingIntent = PendingIntent.getBroadcast(this, notificationId, toggleLightIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    // Create a pending intent that starts this wearable app
                    Intent startIntent = new Intent(this, WearActivity.class).setAction(Intent.ACTION_MAIN)
                            .putExtra("idCuarto", idRoom)
                            .putExtra("idLuz", idLuz);
                    PendingIntent startPendingIntent =
                            PendingIntent.getActivity(this, 0, startIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                    //Creamos la accion
                    NotificationCompat.Action toggleLightAction = new NotificationCompat.Action.Builder(idIcono, textAction, togglePendingIntent).build();
                    NotificationCompat.Action openRoomLightsAction = new NotificationCompat.Action.Builder(R.drawable.ic_wb_sunny_black_18dp, "Ver mas luces", startPendingIntent).build();


                    //Utilizamos WearableExtender para agregar cosas especiales de Wear
                    NotificationCompat.WearableExtender wearableExtender =
                            new NotificationCompat.WearableExtender()
                                    .setBackground(BitmapFactory.decodeResource(this.getResources(), idBg))
                                    .setHintHideIcon(true)
                                    .addAction(toggleLightAction)
                                    .addAction(openRoomLightsAction)
                                    .setContentAction(0);

                    //Creamos la notificacion y la extendemos con las opciones wear
                    Notification notification =
                            new NotificationCompat.Builder(this)
                                    .setContentTitle(luz.getNombre())
                                    .setContentText(cuarto.getNombre())
                                    .setSmallIcon(idIcono)
                                    .extend(wearableExtender)
                                    .build();

                    NotificationManagerCompat notificationManager =
                            NotificationManagerCompat.from(this);


                    notificationManager.notify(notificationId, notification);
                }
            }
        }
    }
}
