package org.synergy.injection;

import org.synergy.base.Log;

import java.io.DataOutputStream;

public final class Injection {

    static {
        System.loadLibrary("synergy-jni");
    }

    private Injection() {
    }

    /**
     * Functions imported from synergy-jni library
     */
    public static final native void start(String deviceName);

    public static native void stop();

    public static native void keydown(int key, int mask, int button);

    public static native void keyup(int key, int mask, int button);

    public static final native void movemouse(final int x, final int y);

    public static native void mousedown(int buttonId);

    public static native void mouseup(int buttonId);

    public static native void mousewheel(int x, int y);

    public static void startInjection(String deviceName) {
        start(deviceName);
    }

    public static void stopInjection() {
        stop();
    }
}
