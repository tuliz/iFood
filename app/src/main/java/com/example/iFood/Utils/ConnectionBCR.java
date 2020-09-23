package com.example.iFood.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.example.iFood.Activities.connectionActivity;

/**
 * Responsible for checking the internet state of the device.
 */
public class ConnectionBCR extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent){
        if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
            boolean noConnectivity = intent.getBooleanExtra(
                    ConnectivityManager.EXTRA_NO_CONNECTIVITY,false
            );
            if(noConnectivity){
                Intent noConnection = new Intent(context, connectionActivity.class);
                context.startActivity(noConnection);
            }
        }
    }
}
