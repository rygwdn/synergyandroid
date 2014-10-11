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
package org.synergy.io.msgs;

import android.content.ClipData;
import org.synergy.base.Log;
import org.synergy.io.MessageDataInputStream;

import java.io.DataInputStream;
import java.io.IOException;

public class ClipboardDataMessage extends Message {
    public static final MessageType MESSAGE_TYPE = MessageType.DCLIPBOARD;

    private byte id;
    private int sequenceNumber;
    private String htmlData;
    private String textData;
    private byte[] bitmapData;

    // Text format, UTF-8, newline is LF
    public static final int FORMAT_TEXT = 0;
    // Bitmap format, BMP 24/32bpp, BI_RGB
    public static final int FORMAT_BITMAP = 1;
    // HTML format, HTML fragment, UTF-8, newline is LF
    public static final int FORMAT_HTML = 2;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ClipboardDataMessage(MessageHeader header, DataInputStream din) throws IOException {
        super(header);

        MessageDataInputStream mdin = new MessageDataInputStream(din);

        id = mdin.readByte();
        sequenceNumber = mdin.readInt();
        mdin.readInt(); // total size of remaining message: don't care

        int numFormats = mdin.readInt();
        for (int i = 0; i < numFormats; i++) {
            int format = mdin.readInt();
            switch (format) {
                case FORMAT_TEXT:
                    textData = mdin.readString();
                    break;
                case FORMAT_HTML:
                    htmlData = mdin.readString();
                    break;
                case FORMAT_BITMAP:
                    int dataLength = mdin.readInt();
                    bitmapData = new byte[dataLength];
                    mdin.read(bitmapData, 0, dataLength);
                    break;
                default:
                    int skipLength = mdin.readInt();
                    mdin.skip(skipLength);
            }
        }
    }

    public ClipData getClipData() {
        if (textData != null && !textData.isEmpty()) {
            if (htmlData != null && !htmlData.isEmpty()) {
                return ClipData.newHtmlText("Synergy HTML", textData, htmlData);
            }
            return ClipData.newPlainText("Synergy Plain Text", textData);
        } else if (bitmapData != null && bitmapData.length > 0) {
            // TODO: handle this
            Log.debug("Skipping bitmap data");
        }
        return null;
    }

    public String toString() {
        return "ClipboardDataMessage:" + id + ":" + sequenceNumber;
    }
}
