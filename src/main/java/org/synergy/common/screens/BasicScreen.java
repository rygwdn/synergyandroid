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
package org.synergy.common.screens;

import android.graphics.Point;
import android.graphics.Rect;
import org.synergy.base.Log;
import org.synergy.injection.Injection;

import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BasicScreen implements ScreenInterface {

    private final int[] buttonToKeyDownID;
    private final MouseUpdater mMouseUpdater;

    // Keep track of the mouse cursor since I cannot find a way of
    //  determining the current mouse position
    private int mouseX = -1;
    private int mouseY = -1;

    // Screen dimensions
    private int width;
    private int height;

    private boolean onScreen = false;

    public BasicScreen() {

        // the keyUp/Down/Repeat button parameter appears to be the low-level
        // keyboard scan code (*shouldn't be* more than 256 of these, but I speak
        // from anecdotal experience, not as an expert...
        buttonToKeyDownID = new int[256];
        Arrays.fill(buttonToKeyDownID, -1);
        mMouseUpdater = new MouseUpdater();
        mMouseUpdater.start();
    }

    /**
     * Set the shape of the screen -- set from the initializing activity
     *
     * @param width
     * @param height
     */
    public void setShape(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public Rect getShape() {
        return new Rect(0, 0, width, height);
    }

    @Override
    public void enable() {
    }

    @Override
    public void disable() {
    }

    @Override
    public void enter(int toggleMask) {
        onScreen = true;
        Log.info("Enter");
        clearMousePosition();
        allKeysUp();
    }

    @Override
    public boolean leave() {
        Log.info("Leave");
        clearMousePosition();
        allKeysUp();
        onScreen = false;
        return true;
    }

    private void allKeysUp() {
        // TODO Auto-generated method stub
    }


    @Override
    public void keyDown(int id, int mask, int button) {
        // 1) 'button - 1' appears to be the low-level keyboard scan code
        // 2) 'id' does not appear to be conserved between server keyDown
        // and keyUp event broadcasts as the 'id' on *most* keyUp events
        // appears to be set to 0.  'button' does appear to be conserved
        // so we store the keyDown 'id' using this event so that we can
        // pull out the 'id' used for keyDown for proper keyUp handling
        if (button < buttonToKeyDownID.length) {
            buttonToKeyDownID[button] = id;
        } else {
            Log.note("found keyDown button parameter > " + buttonToKeyDownID.length + ", may not be able to properly handle keyUp event.");
        }
        Injection.keydown(id, mask);
    }

    @Override
    public void keyUp(int id, int mask, int button) {
        if (button < buttonToKeyDownID.length) {
            int keyDownID = buttonToKeyDownID[button];
            if (keyDownID > -1) {
                id = keyDownID;
            }
        } else {
            Log.note("found keyUp button parameter > " + buttonToKeyDownID.length + ", may not be able to properly handle keyUp event.");
        }
        Injection.keyup(id, mask);
    }

    @Override
    public void keyRepeat(int keyEventID, int mask, int button) {
    }

    @Override
    public void mouseDown(int buttonID) {
        Injection.mousedown(buttonID);
    }

    @Override
    public void mouseUp(int buttonID) {
        Injection.mouseup(buttonID);
    }

    @Override
    public final void mouseMove(int x, int y) {
        if (!onScreen) {
            return;
        }

        if (mouseX == width && mouseY == height) {
            Log.debug("abs mouseMove: " + x + ", " + y);

            mMouseUpdater.movemouse(x - width, y - height);
            mouseX = Math.max(0, Math.min(width, x));
            mouseY = Math.max(0, Math.min(height, y));
        } else {
            mouseRelativeMove(x - mouseX, y - mouseY);
        }
    }

    @Override
    public void mouseRelativeMove(int x, int y) {
        Log.debug("rel mouseMove: " + x + ", " + y);
        mMouseUpdater.movemouse(x, y);
        mouseX = Math.max(0, Math.min(width, mouseX + x));
        mouseY = Math.max(0, Math.min(height, mouseY + y));
    }

    @Override
    public void mouseWheel(int x, int y) {
        Injection.mousewheel(x, y);
    }

    private void clearMousePosition() {
        Log.debug("clear mouse");

        // hide mouse pointer
        mMouseUpdater.hide();
        mouseX = width;
        mouseY = height;
    }

    @Override
    public Point getCursorPos() {
        return new Point(0, 0);
    }

    @Override
    public Object getEventTarget() {
        return this;
    }

    /**
     * Thread to ensure we don't flood android with movement events.
     */
    private class MouseUpdater extends  Thread {
        private static final int MOUSE_HIDE_DELAY = 50;
        private static final int MOUSE_UPDATE_FREQ_MILIS = 20;

        private int mDx = 0;
        private int mDy = 0;
        private boolean mHide = false;

        private Lock mLock = new ReentrantLock();
        private Condition mCondition = mLock.newCondition();

        /**
         * Perform relative mouse move.
         */
        public void movemouse(int dx, int dy) {
            mLock.lock();
            try {
                mDx += dx;
                mDy += dy;
                mCondition.signal();
            } finally {
                mLock.unlock();
            }
        }

        /**
         * Hide the mouse by moving to (width, height)
         */
        public void hide() {
            mLock.lock();
            try {
                mHide = true;
                mDx = 0;
                mDy = 0;
                mCondition.signal();
            } finally {
                mLock.unlock();
            }
        }

        public void run() {
            while (isAlive()) {
                int delay = MOUSE_UPDATE_FREQ_MILIS;
                mLock.lock();
                try {
                    while (!mHide && mDx == 0 && mDy == 0) {
                        // Wait for something to do (no busy loop)
                        mCondition.await();
                    }

                    if (mHide) {
                        doMouseMove(width, height);
                        mHide = false;
                        // Sleep a bit longer for a clear to avoid bug(reason unknown)
                        delay = MOUSE_HIDE_DELAY;
                    } else {
                        doMouseMove(mDx, mDy);
                        mDx = mDy = 0;
                    }
                } catch (InterruptedException e) {
                    Log.error("MouseUpdater interrupted while awaiting");
                    e.printStackTrace();
                } finally {
                    mLock.unlock();
                }

                // Don't need the lock while we sleep
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Log.error("MouseUpdater interrupted while sleeping");
                    e.printStackTrace();
                }
            }
        }

        private void doMouseMove(final int dx, final int dy) {
            Injection.movemouse(dx, dy);
        }
    }
}
