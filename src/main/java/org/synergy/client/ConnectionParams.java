package org.synergy.client;

import android.content.Context;
import android.content.SharedPreferences;
import org.synergy.R;
import org.synergy.Synergy;

/**
* Created by rwooden on 2014-09-24.
*/
public class ConnectionParams {
    public String clientName;
    public String deviceName;
    public String ipAddress;
    public int port;

    private final static String PROP_clientName = "clientName";
    private final static String PROP_serverHost = "serverHost";
    private final static String PROP_deviceName = "deviceName";
    private final static String PROP_port = "serverPort";


    public static ConnectionParams getDefaultConnectionParams(Context context) {
        ConnectionParams params = new ConnectionParams();
        SharedPreferences preferences = context.getSharedPreferences("org.synergy", Context.MODE_PRIVATE);
        params.clientName = preferences.getString(PROP_clientName, context.getString(R.string.client));
        params.deviceName = preferences.getString(PROP_deviceName, context.getString(R.string.device_name_default));
        params.ipAddress = preferences.getString(PROP_serverHost, "");
        params.port = preferences.getInt(PROP_port, Integer.parseInt(context.getString(R.string.server_port_default)));
        return params;
    }

    public void save(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("org.synergy", Context.MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = preferences.edit();

        preferencesEditor.putString(PROP_clientName, clientName);
        preferencesEditor.putString(PROP_serverHost, ipAddress);
        preferencesEditor.putString(PROP_deviceName, deviceName);
        preferencesEditor.putInt(PROP_port, port);

        preferencesEditor.apply();
    }
}
