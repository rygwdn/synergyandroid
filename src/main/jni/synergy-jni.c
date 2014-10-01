/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <string.h>
#include <jni.h>
#include <android/log.h>

#include "uinput.h"
#include "input.h"
#include "key_map.h"
#include "key_types.h"

#define DEBUG_TAG "Synergy"


/*
 * File descriptor for /dev/uinput
 */
int uinput_fd = 0;

/*
 * Start event injection
 */
void Java_org_synergy_injection_Injection_start (JNIEnv *env, jobject thiz, jstring deviceName) {
  struct input_id id = {
    0x06, /*BUS_VIRTUAL, /* Bus type. */
    1, /* Vendor id. */
    1, /* Product id. */
    1 /* Version id. */
  };

  jboolean isCopy;
  const char * szDeviceName = (*env)->GetStringUTFChars(env, deviceName, &isCopy);
  (*env)->MonitorEnter(env, thiz);
  uinput_fd = suinput_open(szDeviceName, &id);
  (*env)->MonitorExit(env, thiz);
}
                                        


/*
 * Close down event injection
 */
void Java_org_synergy_injection_Injection_stop (JNIEnv *env, jobject thiz) {
  (*env)->MonitorEnter(env, thiz);
  suinput_close (uinput_fd);
  (*env)->MonitorExit(env, thiz);
}


static inline jint keycode(jint key) {
    if (key == kKeyEscape) {
        return KEY_BACK;
    } else if (key == kKeyF1) {
        return KEY_HOMEPAGE;
    } else if (key == kKeyF2) {
        return KEY_MENU;
    } else if (key == kKeyF3) {
        return KEY_BACK;
    } else if (key == kKeyF4) {
        return KEY_SEARCH;
    } else if (key == kKeyF5) {
        return KEY_POWER;
    } else {
        return synergyToUinput(key);
    }
}

void Java_org_synergy_injection_Injection_keydown(JNIEnv *env, jobject thiz, jint key, jint mask, jint button) {
    uint16_t translatedKey = keycode(key);
    if (translatedKey == 0) {
        // Unknown key...
        __android_log_print (ANDROID_LOG_WARN, DEBUG_TAG, "Unknown keycode on keydown");
        return;
    }
    __android_log_print (ANDROID_LOG_WARN, DEBUG_TAG, "keycode keydown: %d -> %d", key, translatedKey);

    (*env)->MonitorEnter(env, thiz);
    suinput_press (uinput_fd, translatedKey);
    (*env)->MonitorExit(env, thiz);
}

void Java_org_synergy_injection_Injection_keyup(JNIEnv *env, jobject thiz, jint key, jint mask, jint button) {
    uint16_t translatedKey = keycode(key);
    if (translatedKey == 0) {
        // Unknown key...
        __android_log_print (ANDROID_LOG_WARN, DEBUG_TAG, "Unknown keycode on keyup");
        return;
    }
    __android_log_print (ANDROID_LOG_WARN, DEBUG_TAG, "keycode keyup: %d -> %d", key, translatedKey);

    (*env)->MonitorEnter(env, thiz);
    suinput_release (uinput_fd, translatedKey);
    (*env)->MonitorExit(env, thiz);
}

void Java_org_synergy_injection_Injection_movemouse (JNIEnv *env, jobject thiz, const jint x, const jint y) {
    (*env)->MonitorEnter(env, thiz);
    suinput_move_pointer (uinput_fd, x, y);
    (*env)->MonitorExit(env, thiz);
}

void Java_org_synergy_injection_Injection_mousedown (JNIEnv *env, jobject thiz, jint buttonId) {
    (*env)->MonitorEnter(env, thiz);
    suinput_press (uinput_fd, BTN_LEFT);
    (*env)->MonitorExit(env, thiz);
}

void Java_org_synergy_injection_Injection_mouseup (JNIEnv *env, jobject thiz, jint buttonId) {
    (*env)->MonitorEnter(env, thiz);
    suinput_release (uinput_fd, BTN_LEFT);
    (*env)->MonitorExit(env, thiz);
}

void Java_org_synergy_injection_Injection_mousewheel (JNIEnv *env, jobject thiz, jint x, jint y) {
    (*env)->MonitorEnter(env, thiz);
    suinput_move_wheel (uinput_fd, x, y);
    (*env)->MonitorExit(env, thiz);
}
