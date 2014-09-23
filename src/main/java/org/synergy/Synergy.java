/*
 * synergy -- mouse and keyboard sharing utility
 * Copyright (C) 2010 Shaun Patterson
 * Copyright (C) 2010 The Synergy Project
 * Copyright (C) 2009 The Synergy+ Project
 * Copyright (C) 2002 Chris Schoeneman
 * 
 * This package is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * found in the file COPYING that should have accompanied this file.
 * 
 * This package is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.synergy;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.synergy.base.Log;
import org.synergy.injection.Injection;

public class Synergy extends Activity {

    private final static String PROP_clientName = "clientName";
    private final static String PROP_serverHost = "serverHost";
    private final static String PROP_deviceName = "deviceName";

    private boolean mIsBound = false;
    private SynergyService mBoundService;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((SynergyService.LocalBinder)service).getService();
            if (mBoundService != null) {
                Log.info("Client says service connected!");
                Toast.makeText(Synergy.this, "Client says service connected!", Toast.LENGTH_SHORT).show();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
            Log.info("Client says service disconnected!");
            Toast.makeText(Synergy.this, "Client says service disconnected!", Toast.LENGTH_SHORT).show();
        }
    };


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        String clientName = preferences.getString(PROP_clientName, null);
        if (clientName != null) {
            ((EditText) findViewById(R.id.clientNameEditText)).setText(clientName);
        }
        String serverHost = preferences.getString(PROP_serverHost, null);
        if (serverHost != null) {
            ((EditText) findViewById(R.id.serverHostEditText)).setText(serverHost);
        }

        final Button connectButton = (Button) findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg) {
                connect();
            }
        });

        final Button disconnectButton = (Button) findViewById(R.id.disconnectButton);
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg) {
                disconnect();
            }
        });

        Log.setLogLevel(Log.Level.INFO);

        Log.debug("Client starting....");

        try {
            Injection.setPermissionsForInputDevice();
        } catch (Exception e) {
            // TODO handle exception
        }

        doBindService();
    }

    private void connect() {
        String clientName = ((EditText) findViewById(R.id.clientNameEditText)).getText().toString();
        String ipAddress = ((EditText) findViewById(R.id.serverHostEditText)).getText().toString();
        String portStr = ((EditText) findViewById(R.id.serverPortEditText)).getText().toString();
        int port = Integer.parseInt(portStr);
        String deviceName = ((EditText) findViewById(R.id.inputDeviceEditText)).getText().toString();

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putString(PROP_clientName, clientName);
        preferencesEditor.putString(PROP_serverHost, ipAddress);
        preferencesEditor.putString(PROP_deviceName, deviceName);
        preferencesEditor.apply();

        Log.info("Connecting..");
        SynergyService.connect(getApplicationContext(), clientName, ipAddress, port, deviceName);
    }

    private void disconnect() {
        doBindService();
        if (mBoundService != null) {
            mBoundService.disconnect();
        } else {
            Log.error("Unable to connect to service");
        }
    }

    void doBindService() {
        bindService(new Intent(this, SynergyService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
}
