package com.googlecode.xmppremote.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.googlecode.xmppremote.MainService;
import com.googlecode.xmppremote.tools.Tools;

public class NetworkConnectivityReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(Tools.APP_NAME, 0);
        boolean debugLog = prefs.getBoolean("debugLog", false);
        boolean failover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);
        boolean connected;
        String networkType;
        
        NetworkInfo network = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
        if (network != null) {
            connected = network.getState().equals(NetworkInfo.State.CONNECTED);
            networkType = network.getTypeName();
        } else {
            Log.e(Tools.LOG_TAG, "NetworkConnectivityReceiver: could not get EXTRA_NETWORK_INFO)");
            return;
        }

        if (prefs.getBoolean("startOnWifiConnected", false) && connected && networkType.equals("WIFI")) {
            // Start xmppremote
            if (debugLog) Log.d(Tools.LOG_TAG, "NetworkConnectivityReceiver: startOnWifiConnected enabled, wifi connected, sending intent");
            Intent net_changed = new Intent(MainService.ACTION_NETWORK_CHANGED);
            net_changed.putExtra("available", true);
            context.startService(net_changed);
            context.startService(new Intent(MainService.ACTION_CONNECT));
        } else if (prefs.getBoolean("stopOnWifiDisconnected", false) && !connected && networkType.equals("WIFI")) {            
            // Stop xmppremote
            if (debugLog) Log.d(Tools.LOG_TAG, "NetworkConnectivityReceiver: stopOnWifiDisconnected enabled, wifi disconnected, sending intent");
            Intent serviceIntent = new Intent(MainService.ACTION_CONNECT);
            serviceIntent.putExtra("disconnect", true);
            context.startService(serviceIntent);
        } else if (MainService.IsRunning) {
            if (debugLog)
                Log.d(Tools.LOG_TAG, "NetworkConnectivityReceiver: Broadcasting intent " + MainService.ACTION_NETWORK_CHANGED 
                        + " with available=" + connected + ", failover=" + failover + ", networkName=" + network.getTypeName());
            Intent svcintent = new Intent(MainService.ACTION_NETWORK_CHANGED);
            svcintent.putExtra("available", connected);
            svcintent.putExtra("failover", failover);
            context.startService(svcintent);

        }
    }
}
