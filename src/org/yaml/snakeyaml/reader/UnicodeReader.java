// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.reader;

import java.io.IOException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;
import java.io.Reader;

public class UnicodeReader extends Reader
{
    private static final Charset UTF8;
    private static final Charset UTF16BE;
    private static final Charset UTF16LE;
    PushbackInputStream internalIn;
    InputStreamReader internalIn2;
    private static final int BOM_SIZE = 3;
    
    public UnicodeReader(final InputStream in) {
        this.internalIn2 = null;
        this.internalIn = new PushbackInputStream(in, 3);
    }
    
    public String getEncoding() {
        return this.internalIn2.getEncoding();
    }
    
    protected void init() throws IOException {
        if (this.internalIn2 != null) {
            return;
        }
        final byte[] bom = new byte[3];
        final int n = this.internalIn.read(bom, 0, bom.length);
        Charset encoding;
        int unread;
        if (bom[0] == -17 && bom[1] == -69 && bom[2] == -65) {
            encoding = UnicodeReader.UTF8;
            unread = n - 3;
        }
        else if (bom[0] == -2 && bom[1] == -1) {
            encoding = UnicodeReader.UTF16BE;
            unread = n - 2;
        }
        else if (bom[0] == -1 && bom[1] == -2) {
            encoding = UnicodeReader.UTF16LE;
            unread = n - 2;
        }
        else {
            encoding = UnicodeReader.UTF8;
            unread = n;
        }
        if (unread > 0) {
            this.internalIn.unread(bom, n - unread, unread);
        }
        final CharsetDecoder decoder = encoding.newDecoder().onUnmappableCharacter(CodingErrorAction.REPORT);
        this.internalIn2 = new InputStreamReader(this.internalIn, decoder);
    }
    
    @Override
    public void close() throws IOException {
        this.init();
        this.internalIn2.close();
    }
    
    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        this.init();
        return this.internalIn2.read(cbuf, off, len);
    }
    
    static {
        UTF8 = Charset.forName("UTF-8");
        UTF16BE = Charset.forName("UTF-16BE");
        UTF16LE = Charset.forName("UTF-16LE");
    }
}
