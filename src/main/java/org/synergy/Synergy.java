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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.synergy.base.IToastListener;
import org.synergy.base.IUILogListener;
import org.synergy.base.Log;
import org.synergy.client.ConnectionParams;
import org.synergy.injection.Injection;

import java.sql.Connection;

public class Synergy extends Activity implements IToastListener, IUILogListener {

    private EditText outputText;

    private boolean mIsBound = false;
    private SynergyService mBoundService;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((SynergyService.LocalBinder)service).getService();
            if (mBoundService != null) {
                Log.info("Client says service connected!");
                mBoundService.setToastListener(Synergy.this);
                mBoundService.setUILogListener(Synergy.this);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
            Log.info("Client says service disconnected!");
        }
    };


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        ConnectionParams params = ConnectionParams.getDefaultConnectionParams(this);
        ((EditText) findViewById(R.id.clientNameEditText)).setText(params.clientName);
        ((EditText) findViewById(R.id.serverHostEditText)).setText(params.ipAddress);
        ((EditText) findViewById(R.id.serverPortEditText)).setText(Integer.toString(params.port));
        ((EditText) findViewById(R.id.inputDeviceEditText)).setText(params.deviceName);

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

        Log.setLogLevel(Log.Level.DEBUG);
        outputText = (EditText) findViewById(R.id.outputEditText);

        Log.debug("Client starting....");

        try {
            Injection.setPermissionsForInputDevice();
        } catch (Exception e) {
            // TODO handle exception
        }

        doBindService();
    }

    private void connect() {
        ConnectionParams params = new ConnectionParams();
        params.clientName = ((EditText) findViewById(R.id.clientNameEditText)).getText().toString();
        params.ipAddress = ((EditText) findViewById(R.id.serverHostEditText)).getText().toString();
        params.port = Integer.parseInt(((EditText) findViewById(R.id.serverPortEditText)).getText().toString());
        params.deviceName = ((EditText) findViewById(R.id.inputDeviceEditText)).getText().toString();
        params.save(this);

        Log.info("Connecting..");
        SynergyService.connect(getApplicationContext(), params.clientName, params.ipAddress, params.port, params.deviceName);
    }

    private void disconnect() {
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

    @Override
    public void onShowToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(Synergy.this, message, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    @Override
    public void onLogAdded(final String log) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                outputText.append(log + "\n");
            }
        });
    }
}
