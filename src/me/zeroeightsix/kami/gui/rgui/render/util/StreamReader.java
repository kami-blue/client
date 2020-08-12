// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.rgui.render.util;

import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringJoiner;
import java.io.InputStream;

public final class StreamReader
{
    private final InputStream stream;
    
    public StreamReader(final InputStream stream) {
        this.stream = stream;
    }
    
    public final String read() {
        final StringJoiner joiner = new StringJoiner("\n");
        try {
            final BufferedReader br = new BufferedReader(new InputStreamReader(this.stream));
            String line;
            while ((line = br.readLine()) != null) {
                joiner.add(line);
            }
            br.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return joiner.toString();
    }
}
