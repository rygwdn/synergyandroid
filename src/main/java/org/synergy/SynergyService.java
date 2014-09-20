package org.synergy;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

/**
 * Background service implementing a synergy client.
 */
public class SynergyService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_CONNECT = "org.synergy.action.CONNECT";
    private static final String ACTION_DISCONNECT = "org.synergy.action.DISCONNECT";

    private static final int DEFAULT_PORT = 24800;
    private static final String EXTRA_CLIENT_NAME = "org.synergy.extra.CLIENT_NAME";
    private static final String EXTRA_IP_ADDRESS = "org.synergy.extra.IP_ADDRESS";
    private static final String EXTRA_PORT = "org.synergy.extra.PORT";
    private static final String EXTRA_DEVICE_NAME = "org.synergy.extra.DEVICE_NAME";

    /**
     * Starts this service to connect with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void connect(Context context, String clientName, String ipAddress, int port, String deviceName) {
        Intent intent = new Intent(context, SynergyService.class);
        intent.setAction(ACTION_CONNECT);
        intent.putExtra(EXTRA_CLIENT_NAME, clientName);
        intent.putExtra(EXTRA_DEVICE_NAME, deviceName);
        intent.putExtra(EXTRA_IP_ADDRESS, ipAddress);
        intent.putExtra(EXTRA_PORT, port);
        context.startService(intent);
    }

    /**
     * Starts this service to disconnect. If the service is already performing a
     task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void disconnect(Context context) {
        Intent intent = new Intent(context, SynergyService.class);
        intent.setAction(ACTION_DISCONNECT);
        context.startService(intent);
    }

    public SynergyService() {
        super("SynergyService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CONNECT.equals(action)) {
                final String clientName = intent.getStringExtra(EXTRA_CLIENT_NAME);
                final String deviceName = intent.getStringExtra(EXTRA_DEVICE_NAME);
                final String ipAddress = intent.getStringExtra(EXTRA_IP_ADDRESS);
                final int port = intent.getIntExtra(EXTRA_PORT, DEFAULT_PORT);
                handleActionConnect(clientName, deviceName, ipAddress, port);
            } else if (ACTION_DISCONNECT.equals(action)) {
                handleActionDisconnect();
            }
        }
    }

    private void handleActionDisconnect() {
        // TODO
    }

    private void handleActionConnect(String clientName, String deviceName, String ipAddress, int port) {
        // TODO
    }
}
