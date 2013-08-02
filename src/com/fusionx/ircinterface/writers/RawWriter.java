package com.fusionx.ircinterface.writers;

import java.io.IOException;
import java.io.OutputStreamWriter;

abstract class RawWriter {
    protected final OutputStreamWriter streamWriter;

    protected RawWriter(OutputStreamWriter writer) {
        streamWriter = writer;
    }

    protected void writeLineToServer(final String line) {
        try {
            streamWriter.write(line + "\r\n");
            streamWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}