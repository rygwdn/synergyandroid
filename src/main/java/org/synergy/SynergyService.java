package org.synergy;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.view.Display;
import android.view.WindowManager;
import org.synergy.base.Event;
import org.synergy.base.EventQueue;
import org.synergy.base.EventType;
import org.synergy.base.Log;
import org.synergy.base.exceptions.InvalidMessageException;
import org.synergy.client.Client;
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

    private static final int DEFAULT_PORT = 24800;
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

    private boolean mRunning = false;
    private Client mClient;

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

    public class LocalBinder extends Binder {
        SynergyService getService() {
            return SynergyService.this;
        }
    }

    public void disconnect() {
        if (mRunning) {
            Log.info("SynergyService disconnecting");
            mRunning = false;
            mClient.disconnect("Closed");
        } else {
            Log.info("SynergyService not disconnecting (not running)");
        }
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
            }
        }
    }

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private Notification buildForegroundNotification(String host) {
        return new Notification.Builder(this)
                .setOngoing(true)
                .setContentTitle(getString(R.string.synergy_connected))
                .setContentText("Connected to " + host)
                .setSmallIcon(R.drawable.icon)
                .setTicker("Connected to " + host)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, Synergy.class), 0))
                .build();
    }

    private void runLoop() throws InvalidMessageException {
        try {
            mRunning = true;
            Event event = new Event();
            event = EventQueue.getInstance().getEvent(event, -1.0);
            Log.info("Start loop");

            while (event.getType() != EventType.QUIT && mRunning) {
                Log.debug("About to dispatch: " + event.toString());
                EventQueue.getInstance().dispatchEvent(event);
                if (!mRunning) return;

                Log.debug("About to wait");
                event = EventQueue.getInstance().getEvent(event, -1.0);
            }

        } finally {
            Log.info("End loop");
            mRunning = false;
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
        try {
            // TODO: message to UI here to indicate that we're connecting..
            NetworkAddress serverAddress = new NetworkAddress(host, port);
            Injection.startInjection(deviceName);
            BasicScreen basicScreen = new BasicScreen();

            Rect displayRect = getDisplayRect();
            basicScreen.setShape(displayRect.width(), displayRect.height());

            SocketFactoryInterface socketFactory = new TCPSocketFactory();
            mClient = new Client(getApplicationContext(), clientName, serverAddress, socketFactory, null, basicScreen);
            mClient.connect();

            Log.info("Connected to " + host);
            startForeground(FOREGROUND_ID, buildForegroundNotification(host));

            runLoop();

        } catch (Exception e) {
            e.printStackTrace();

            String message = "Connection Failed";
            if (e.getLocalizedMessage() != null) {
                message += ": " + e.getLocalizedMessage();
            }

            // TODO: send message back to client!
            // https://developer.android.com/training/run-background-service/report-status.html
            Log.error(message); // TODO -> toast?

        } finally {
            stopForeground(true);
        }
    }
}