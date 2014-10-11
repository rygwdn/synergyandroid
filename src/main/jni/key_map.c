#include "key_map.h"

#include "input.h"
#include "key_types.h"

static uint16_t KEY_MAP[65535];
static int DID_FILL = 0;

static void fillMap() {
    if (DID_FILL) {
        return;
    }
    DID_FILL = 1;

    int i = 0;
    for (i = 0; i < 65535; i++) {
        KEY_MAP[i] = 0;
    }

    KEY_MAP['`'] = KEY_GRAVE;
    KEY_MAP['1'] = KEY_1;
    KEY_MAP['2'] = KEY_2;
    KEY_MAP['3'] = KEY_3;
    KEY_MAP['4'] = KEY_4;
    KEY_MAP['5'] = KEY_5;
    KEY_MAP['6'] = KEY_6;
    KEY_MAP['7'] = KEY_7;
    KEY_MAP['8'] = KEY_8;
    KEY_MAP['9'] = KEY_9;
    KEY_MAP['0'] = KEY_0;
    KEY_MAP['a'] = KEY_A;
    KEY_MAP['b'] = KEY_B;
    KEY_MAP['c'] = KEY_C;
    KEY_MAP['d'] = KEY_D;
    KEY_MAP['e'] = KEY_E;
    KEY_MAP['f'] = KEY_F;
    KEY_MAP['g'] = KEY_G;
    KEY_MAP['h'] = KEY_H;
    KEY_MAP['i'] = KEY_I;
    KEY_MAP['j'] = KEY_J;
    KEY_MAP['k'] = KEY_K;
    KEY_MAP['l'] = KEY_L;
    KEY_MAP['m'] = KEY_M;
    KEY_MAP['n'] = KEY_N;
    KEY_MAP['o'] = KEY_O;
    KEY_MAP['p'] = KEY_P;
    KEY_MAP['q'] = KEY_Q;
    KEY_MAP['r'] = KEY_R;
    KEY_MAP['s'] = KEY_S;
    KEY_MAP['t'] = KEY_T;
    KEY_MAP['u'] = KEY_U;
    KEY_MAP['v'] = KEY_V;
    KEY_MAP['w'] = KEY_W;
    KEY_MAP['x'] = KEY_X;
    KEY_MAP['y'] = KEY_Y;
    KEY_MAP['z'] = KEY_Z;

    KEY_MAP['-'] = KEY_MINUS;
    KEY_MAP['='] = KEY_EQUAL;
    KEY_MAP['['] = KEY_LEFTBRACE;
    KEY_MAP[']'] = KEY_RIGHTBRACE;
    KEY_MAP[';'] = KEY_SEMICOLON;
    KEY_MAP['\''] = KEY_APOSTROPHE;

    KEY_MAP['\\'] = KEY_BACKSLASH;
    KEY_MAP[','] = KEY_COMMA;
    KEY_MAP['.'] = KEY_DOT;
    KEY_MAP['/'] = KEY_SLASH;

    KEY_MAP['~'] = KEY_GRAVE;
    KEY_MAP['!'] = KEY_1;
    KEY_MAP['@'] = KEY_2;
    KEY_MAP['#'] = KEY_3;
    KEY_MAP['$'] = KEY_4;
    KEY_MAP['%'] = KEY_5;
    KEY_MAP['^'] = KEY_6;
    KEY_MAP['&'] = KEY_7;
    KEY_MAP['*'] = KEY_8;
    KEY_MAP['('] = KEY_9;
    KEY_MAP[')'] = KEY_0;
    KEY_MAP['A'] = KEY_A;
    KEY_MAP['B'] = KEY_B;
    KEY_MAP['C'] = KEY_C;
    KEY_MAP['D'] = KEY_D;
    KEY_MAP['E'] = KEY_E;
    KEY_MAP['F'] = KEY_F;
    KEY_MAP['G'] = KEY_G;
    KEY_MAP['H'] = KEY_H;
    KEY_MAP['I'] = KEY_I;
    KEY_MAP['J'] = KEY_J;
    KEY_MAP['K'] = KEY_K;
    KEY_MAP['L'] = KEY_L;
    KEY_MAP['M'] = KEY_M;
    KEY_MAP['N'] = KEY_N;
    KEY_MAP['O'] = KEY_O;
    KEY_MAP['P'] = KEY_P;
    KEY_MAP['Q'] = KEY_Q;
    KEY_MAP['R'] = KEY_R;
    KEY_MAP['S'] = KEY_S;
    KEY_MAP['T'] = KEY_T;
    KEY_MAP['U'] = KEY_U;
    KEY_MAP['V'] = KEY_V;
    KEY_MAP['W'] = KEY_W;
    KEY_MAP['X'] = KEY_X;
    KEY_MAP['Y'] = KEY_Y;
    KEY_MAP['Z'] = KEY_Z;

    KEY_MAP['_'] = KEY_MINUS;
    KEY_MAP['+'] = KEY_EQUAL;
    KEY_MAP['{'] = KEY_LEFTBRACE;
    KEY_MAP['}'] = KEY_RIGHTBRACE;
    KEY_MAP[':'] = KEY_SEMICOLON;
    KEY_MAP['"'] = KEY_APOSTROPHE;

    KEY_MAP['|'] = KEY_BACKSLASH;
    KEY_MAP['<'] = KEY_COMMA;
    KEY_MAP['>'] = KEY_DOT;
    KEY_MAP['?'] = KEY_SLASH;

    KEY_MAP[' '] = KEY_SPACE;

    // TTY functions
    KEY_MAP[kKeyBackSpace] = KEY_BACKSPACE;
    KEY_MAP[kKeyTab] = KEY_TAB;
    KEY_MAP[kKeyLinefeed] = KEY_LINEFEED;
    KEY_MAP[kKeyClear] = KEY_CLEAR;
    KEY_MAP[kKeyReturn] = KEY_ENTER;
    KEY_MAP[kKeyPause] = KEY_PAUSE;
    KEY_MAP[kKeyScrollLock] = KEY_SCROLLLOCK;
    KEY_MAP[kKeySysReq] = KEY_SYSRQ;
    KEY_MAP[kKeyEscape] = KEY_ESC;
    KEY_MAP[kKeyZenkaku] = KEY_ZENKAKUHANKAKU;
    KEY_MAP[kKeyHanjaKanzi] = KEY_HANJA;
    KEY_MAP[kKeyDelete] = KEY_DELETE;

    // cursor control
    KEY_MAP[kKeyHome] = KEY_HOME;
    KEY_MAP[kKeyLeft] = KEY_LEFT;
    KEY_MAP[kKeyUp] = KEY_UP;
    KEY_MAP[kKeyRight] = KEY_RIGHT;
    KEY_MAP[kKeyDown] = KEY_DOWN;
    KEY_MAP[kKeyPageUp] = KEY_PAGEUP;
    KEY_MAP[kKeyPageDown] = KEY_PAGEDOWN;
    KEY_MAP[kKeyEnd] = KEY_END;
    KEY_MAP[kKeyBegin] = KEY_HOME;

    // misc functions
    KEY_MAP[kKeySelect] = KEY_SELECT;
    KEY_MAP[kKeyPrint] = KEY_PRINT;
    KEY_MAP[kKeyInsert] = KEY_INSERT;
    KEY_MAP[kKeyUndo] = KEY_UNDO;
    KEY_MAP[kKeyRedo] = KEY_REDO;
    KEY_MAP[kKeyMenu] = KEY_MENU;
    KEY_MAP[kKeyFind] = KEY_FIND;
    KEY_MAP[kKeyCancel] = KEY_CANCEL;
    KEY_MAP[kKeyHelp] = KEY_HELP;
    KEY_MAP[kKeyBreak] = KEY_BREAK;
    KEY_MAP[kKeyNumLock] = KEY_NUMLOCK;

    // keypad
    KEY_MAP[kKeyKP_Space] = KEY_SPACE;
    KEY_MAP[kKeyKP_Tab] = KEY_TAB;
    KEY_MAP[kKeyKP_Enter] = KEY_KPENTER;
    KEY_MAP[kKeyKP_F1] = KEY_F1;
    KEY_MAP[kKeyKP_F2] = KEY_F2;
    KEY_MAP[kKeyKP_F3] = KEY_F3;
    KEY_MAP[kKeyKP_F4] = KEY_F4;
    KEY_MAP[kKeyKP_Home] = KEY_HOME;
    KEY_MAP[kKeyKP_Left] = KEY_LEFT;
    KEY_MAP[kKeyKP_Up] = KEY_UP;
    KEY_MAP[kKeyKP_Right] = KEY_RIGHT;
    KEY_MAP[kKeyKP_Down] = KEY_DOWN;
    KEY_MAP[kKeyKP_PageUp] = KEY_PAGEUP;
    KEY_MAP[kKeyKP_PageDown] = KEY_PAGEDOWN;
    KEY_MAP[kKeyKP_End] = KEY_END;
    KEY_MAP[kKeyKP_Begin] = KEY_HOME;
    KEY_MAP[kKeyKP_Insert] = KEY_INSERT;
    KEY_MAP[kKeyKP_Delete] = KEY_DELETE;
    KEY_MAP[kKeyKP_Equal] = KEY_KPEQUAL;
    KEY_MAP[kKeyKP_Multiply] = KEY_KPASTERISK;
    KEY_MAP[kKeyKP_Add] = KEY_KPPLUS;
    KEY_MAP[kKeyKP_Separator] = KEY_KPCOMMA;
    KEY_MAP[kKeyKP_Subtract] = KEY_KPMINUS;
    KEY_MAP[kKeyKP_Decimal] = KEY_KPDOT;
    KEY_MAP[kKeyKP_Divide] = KEY_KPSLASH;

    KEY_MAP[kKeyKP_0] = KEY_KP0;
    KEY_MAP[kKeyKP_1] = KEY_KP1;
    KEY_MAP[kKeyKP_2] = KEY_KP2;
    KEY_MAP[kKeyKP_3] = KEY_KP3;
    KEY_MAP[kKeyKP_4] = KEY_KP4;
    KEY_MAP[kKeyKP_5] = KEY_KP5;
    KEY_MAP[kKeyKP_6] = KEY_KP6;
    KEY_MAP[kKeyKP_7] = KEY_KP7;
    KEY_MAP[kKeyKP_8] = KEY_KP8;
    KEY_MAP[kKeyKP_9] = KEY_KP9;

    // function keys
    KEY_MAP[kKeyF1] = KEY_F1;
    KEY_MAP[kKeyF2] = KEY_F2;
    KEY_MAP[kKeyF3] = KEY_F3;
    KEY_MAP[kKeyF4] = KEY_F4;
    KEY_MAP[kKeyF5] = KEY_F5;
    KEY_MAP[kKeyF6] = KEY_F6;
    KEY_MAP[kKeyF7] = KEY_F7;
    KEY_MAP[kKeyF8] = KEY_F8;
    KEY_MAP[kKeyF9] = KEY_F9;
    KEY_MAP[kKeyF10] = KEY_F10;
    KEY_MAP[kKeyF11] = KEY_F11;
    KEY_MAP[kKeyF12] = KEY_F12;
    KEY_MAP[kKeyF13] = KEY_F13;
    KEY_MAP[kKeyF14] = KEY_F14;
    KEY_MAP[kKeyF15] = KEY_F15;
    KEY_MAP[kKeyF16] = KEY_F16;
    KEY_MAP[kKeyF17] = KEY_F17;
    KEY_MAP[kKeyF18] = KEY_F18;
    KEY_MAP[kKeyF19] = KEY_F19;
    KEY_MAP[kKeyF20] = KEY_F20;
    KEY_MAP[kKeyF21] = KEY_F21;
    KEY_MAP[kKeyF22] = KEY_F22;
    KEY_MAP[kKeyF23] = KEY_F23;
    KEY_MAP[kKeyF24] = KEY_F24;

    // modifiers
    KEY_MAP[kKeyShift_L] = KEY_LEFTSHIFT;
    KEY_MAP[kKeyShift_R] = KEY_RIGHTSHIFT;
    KEY_MAP[kKeyControl_L] = KEY_LEFTCTRL;
    KEY_MAP[kKeyControl_R] = KEY_RIGHTCTRL;
    KEY_MAP[kKeyCapsLock] = KEY_CAPSLOCK;
    KEY_MAP[kKeyMeta_L] = KEY_LEFTMETA;
    KEY_MAP[kKeyMeta_R] = KEY_RIGHTMETA;
    KEY_MAP[kKeyAlt_L] = KEY_LEFTALT;
    KEY_MAP[kKeyAlt_R] = KEY_RIGHTALT;

    // Map super/command key -> ctrl
    KEY_MAP[kKeySuper_L] = KEY_LEFTCTRL;
    KEY_MAP[kKeySuper_R] = KEY_RIGHTCTRL;

    // multi-key character composition
    KEY_MAP[kKeyCompose] = KEY_COMPOSE;

    // more function and modifier keys
    KEY_MAP[kKeyLeftTab] = KEY_TAB;

    // extended keys
    KEY_MAP[kKeyEject] = KEY_EJECTCD;
    KEY_MAP[kKeySleep] = KEY_SLEEP;
    KEY_MAP[kKeyWWWBack] = KEY_BACK;
    KEY_MAP[kKeyWWWForward] = KEY_FORWARD;
    KEY_MAP[kKeyWWWRefresh] = KEY_REFRESH;
    KEY_MAP[kKeyWWWStop] = KEY_STOP;
    KEY_MAP[kKeyWWWSearch] = KEY_SEARCH;
    KEY_MAP[kKeyWWWFavorites] = KEY_FAVORITES;
    KEY_MAP[kKeyWWWHome] = KEY_HOMEPAGE;
    KEY_MAP[kKeyAudioMute] = KEY_MUTE;
    KEY_MAP[kKeyAudioDown] = KEY_VOLUMEDOWN;
    KEY_MAP[kKeyAudioUp] = KEY_VOLUMEUP;
    KEY_MAP[kKeyAudioNext] = KEY_NEXTSONG;
    KEY_MAP[kKeyAudioPrev] = KEY_PREVIOUSSONG;
    KEY_MAP[kKeyAudioStop] = KEY_STOPCD;
    KEY_MAP[kKeyAudioPlay] = KEY_PLAYPAUSE;
    KEY_MAP[kKeyAppMail] = KEY_EMAIL;
    KEY_MAP[kKeyAppMedia] = KEY_MEDIA;
}

uint16_t synergyToUinput(uint16_t synergyKey) {
    fillMap();

    if (synergyKey < 0 || synergyKey > 65535) {
        return 0;
    }
    return KEY_MAP[synergyKey];
}
