// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.reader;

import java.nio.charset.Charset;
import java.io.IOException;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.scanner.Constant;
import org.yaml.snakeyaml.error.Mark;
import java.io.Reader;

public class StreamReader
{
    private String name;
    private final Reader stream;
    private int pointer;
    private boolean eof;
    private String buffer;
    private int index;
    private int line;
    private int column;
    private char[] data;
    private static final int BUFFER_SIZE = 1025;
    
    public StreamReader(final String stream) {
        this.pointer = 0;
        this.eof = true;
        this.index = 0;
        this.line = 0;
        this.column = 0;
        this.name = "'string'";
        this.buffer = "";
        this.checkPrintable(stream);
        this.buffer = stream + "\u0000";
        this.stream = null;
        this.eof = true;
        this.data = null;
    }
    
    public StreamReader(final Reader reader) {
        this.pointer = 0;
        this.eof = true;
        this.index = 0;
        this.line = 0;
        this.column = 0;
        this.name = "'reader'";
        this.buffer = "";
        this.stream = reader;
        this.eof = false;
        this.data = new char[1025];
        this.update();
    }
    
    void checkPrintable(final String data) {
        int codePoint;
        for (int length = data.length(), offset = 0; offset < length; offset += Character.charCount(codePoint)) {
            codePoint = data.codePointAt(offset);
            if (!isPrintable(codePoint)) {
                throw new ReaderException(this.name, offset, codePoint, "special characters are not allowed");
            }
        }
    }
    
    public static boolean isPrintable(final String data) {
        int codePoint;
        for (int length = data.length(), offset = 0; offset < length; offset += Character.charCount(codePoint)) {
            codePoint = data.codePointAt(offset);
            if (!isPrintable(codePoint)) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isPrintable(final int c) {
        return (c >= 32 && c <= 126) || c == 9 || c == 10 || c == 13 || c == 133 || (c >= 160 && c <= 55295) || (c >= 57344 && c <= 65533) || (c >= 65536 && c <= 1114111);
    }
    
    public Mark getMark() {
        return new Mark(this.name, this.index, this.line, this.column, this.buffer, this.pointer);
    }
    
    public void forward() {
        this.forward(1);
    }
    
    public void forward(final int length) {
        for (int i = 0; i < length; ++i) {
            if (this.pointer == this.buffer.length()) {
                this.update();
            }
            if (this.pointer == this.buffer.length()) {
                break;
            }
            final int c = this.buffer.codePointAt(this.pointer);
            this.pointer += Character.charCount(c);
            this.index += Character.charCount(c);
            if (Constant.LINEBR.has(c) || (c == 13 && this.buffer.charAt(this.pointer) != '\n')) {
                ++this.line;
                this.column = 0;
            }
            else if (c != 65279) {
                ++this.column;
            }
        }
        if (this.pointer == this.buffer.length()) {
            this.update();
        }
    }
    
    public int peek() {
        if (this.pointer == this.buffer.length()) {
            this.update();
        }
        if (this.pointer == this.buffer.length()) {
            return -1;
        }
        return this.buffer.codePointAt(this.pointer);
    }
    
    public int peek(final int index) {
        int offset = 0;
        int nextIndex = 0;
        int codePoint;
        do {
            if (this.pointer + offset == this.buffer.length()) {
                this.update();
            }
            if (this.pointer + offset == this.buffer.length()) {
                return -1;
            }
            codePoint = this.buffer.codePointAt(this.pointer + offset);
            offset += Character.charCount(codePoint);
        } while (++nextIndex <= index);
        return codePoint;
    }
    
    public String prefix(final int length) {
        final StringBuilder builder = new StringBuilder();
        int offset = 0;
        for (int resultLength = 0; resultLength < length; ++resultLength) {
            if (this.pointer + offset == this.buffer.length()) {
                this.update();
            }
            if (this.pointer + offset == this.buffer.length()) {
                break;
            }
            final int c = this.buffer.codePointAt(this.pointer + offset);
            builder.appendCodePoint(c);
            offset += Character.charCount(c);
        }
        return builder.toString();
    }
    
    public String prefixForward(final int length) {
        final String prefix = this.prefix(length);
        this.pointer += prefix.length();
        this.index += prefix.length();
        this.column += length;
        return prefix;
    }
    
    private void update() {
        if (!this.eof) {
            this.buffer = this.buffer.substring(this.pointer);
            this.pointer = 0;
            try {
                boolean eofDetected = false;
                int converted = this.stream.read(this.data, 0, 1024);
                if (converted > 0) {
                    if (Character.isHighSurrogate(this.data[converted - 1])) {
                        final int oneMore = this.stream.read(this.data, converted, 1);
                        if (oneMore != -1) {
                            converted += oneMore;
                        }
                        else {
                            eofDetected = true;
                        }
                    }
                    final StringBuilder builder = new StringBuilder(this.buffer.length() + converted).append(this.buffer).append(this.data, 0, converted);
                    if (eofDetected) {
                        this.eof = true;
                        builder.append('\0');
                    }
                    this.checkPrintable(this.buffer = builder.toString());
                }
                else {
                    this.eof = true;
                    this.buffer += "\u0000";
                }
            }
            catch (IOException ioe) {
                throw new YAMLException(ioe);
            }
        }
    }
    
    public int getColumn() {
        return this.column;
    }
    
    public Charset getEncoding() {
        return Charset.forName(((UnicodeReader)this.stream).getEncoding());
    }
    
    public int getIndex() {
        return this.index;
    }
    
    public int getLine() {
        return this.line;
    }
}
