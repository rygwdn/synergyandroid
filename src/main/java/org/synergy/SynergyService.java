package org.synergy;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Display;
import android.view.WindowManager;
import org.synergy.base.*;
import org.synergy.base.exceptions.InvalidMessageException;
import org.synergy.client.Client;
import org.synergy.client.ConnectionParams;
import org.synergy.common.screens.BasicScreen;
import org.synergy.injection.Injection;
import org.synergy.net.NetworkAddress;
import org.synergy.net.SocketFactoryInterface;
import org.synergy.net.TCPSocketFactory;

/**
 * Background service implementing a synergy client.
 */
public class SynergyService extends IntentService {
    private static final String ACTION_CONNECT = "org.synergy.action.CONNECT";

    private static final int FOREGROUND_ID = 1234;

    private static final String EXTRA_CLIENT_NAME = "org.synergy.extra.CLIENT_NAME";
    private static final String EXTRA_IP_ADDRESS = "org.synergy.extra.IP_ADDRESS";
    private static final String EXTRA_PORT = "org.synergy.extra.PORT";
    private static final String EXTRA_DEVICE_NAME = "org.synergy.extra.DEVICE_NAME";

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();


    static {
        System.loadLibrary("synergy-jni");
    }

    private Client mClient;
    private IToastListener toastListener;
    private IUILogListener uiLogListener;

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

    public void setToastListener(IToastListener toastListener) {
        this.toastListener = toastListener;
    }

    private void showToast(String message) {
        if (toastListener != null) {
            toastListener.onShowToast(message);
        }
    }

    public void setUILogListener(IUILogListener UILogListener) {
        this.uiLogListener = UILogListener;
    }

    private void addUILog(String message) {
        if (uiLogListener != null) {
            uiLogListener.onLogAdded(message);
        }
    }

    public class LocalBinder extends Binder {
        SynergyService getService() {
            return SynergyService.this;
        }
    }

    public void disconnect() {
        String disconnectingMessage = getString(R.string.ui_disconnecting);
        showToast(disconnectingMessage);
        addUILog(disconnectingMessage);

        Log.info("SynergyService disconnecting");
        mClient.disconnect("Closed");
    }

    public SynergyService() {
        super("SynergyService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CONNECT.equals(action)) {
                ConnectionParams params = ConnectionParams.getDefaultConnectionParams(this);

                Bundle extras = intent.getExtras();
                if (extras != null) {
                    params.ipAddress = extras.getString(EXTRA_IP_ADDRESS, params.ipAddress);
                    params.clientName = extras.getString(EXTRA_CLIENT_NAME, params.clientName);
                    params.deviceName = extras.getString(EXTRA_DEVICE_NAME, params.deviceName);
                    params.port = extras.getInt(EXTRA_PORT, params.port);
                }

                handleActionConnect(params.clientName.trim(), params.deviceName.trim(), params.ipAddress.trim(), params.port);
            }
        }
    }

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private Notification buildForegroundNotification(String message) {
        return new Notification.Builder(this)
                .setOngoing(true)
                .setContentTitle(getString(R.string.synergy_connected))
                .setContentText(message)
                .setSmallIcon(R.drawable.icon)
                .setTicker(message)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, Synergy.class), 0))
                .build();
    }

    private void runLoop() throws InvalidMessageException {
        try {
            Event event = new Event();
            event = EventQueue.getInstance().getEvent(event, -1.0);
            Log.info("Start loop with Got event: " + event.toString());

            while (event.getType() != EventType.QUIT
                    && event.getType() != EventType.CLIENT_DISCONNECTED) {
                Log.debug("About to dispatch: " + event.toString());
                EventQueue.getInstance().dispatchEvent(event);

                Log.debug("About to get event");
                event = EventQueue.getInstance().getEvent(event, -1.0);
            }

        } finally {
            Log.info("End loop");
            Injection.stop();
        }
    }

    private Rect getDisplayRect() {
        Point shape = new Point();
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        display.getSize(shape);
        return new Rect(0, 0, shape.x, shape.y);
    }

    private void handleActionConnect(String clientName, String deviceName, String host, int port) {
        if (clientName == null || clientName.isEmpty()) {
            throw new IllegalArgumentException("clientName can not be empty");
        }
        if (deviceName == null || deviceName.isEmpty()) {
            throw new IllegalArgumentException("deviceName can not be empty");
        }
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("host can not be empty");
        }

        try {
            String uiMessage = String.format(getString(R.string.ui_connecting), host);
            showToast(uiMessage);
            addUILog(uiMessage);

            NetworkAddress serverAddress = new NetworkAddress(host, port);
            Injection.startInjection(deviceName);
            BasicScreen basicScreen = new BasicScreen();

            Rect displayRect = getDisplayRect();
            basicScreen.setShape(displayRect.width(), displayRect.height());

            SocketFactoryInterface socketFactory = new TCPSocketFactory();
            mClient = new Client(getApplicationContext(), clientName, serverAddress, socketFactory, null, basicScreen);
            mClient.connect();

            Log.info("Connected to " + host);

            String connectedMessage = String.format(getString(R.string.ui_connected), host);
            addUILog(connectedMessage);
            startForeground(FOREGROUND_ID, buildForegroundNotification(connectedMessage));

            runLoop();

        } catch (Exception e) {
            e.printStackTrace();

            String message = getString(R.string.ui_connection_failed);

            String detailMessage = null;
            if (e.getLocalizedMessage() != null) {
                detailMessage = message + ": " + e.getLocalizedMessage();
            }

            Log.error(detailMessage);
            showToast(message);
            addUILog(detailMessage);

        } finally {
            stopForeground(true);

            String disconnectedMessage = String.format(getString(R.string.ui_disconnected), host);
            showToast(disconnectedMessage);
            addUILog(disconnectedMessage);
        }
    }
}
