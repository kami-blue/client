// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.util;

import org.yaml.snakeyaml.external.com.google.gdata.util.common.base.PercentEscaper;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.Charset;
import java.io.UnsupportedEncodingException;
import org.yaml.snakeyaml.error.YAMLException;
import java.net.URLDecoder;
import java.nio.charset.CharacterCodingException;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import org.yaml.snakeyaml.external.com.google.gdata.util.common.base.Escaper;
import java.nio.charset.CharsetDecoder;

public abstract class UriEncoder
{
    private static final CharsetDecoder UTF8Decoder;
    private static final String SAFE_CHARS = "-_.!~*'()@:$&,;=[]/";
    private static final Escaper escaper;
    
    public static String encode(final String uri) {
        return UriEncoder.escaper.escape(uri);
    }
    
    public static String decode(final ByteBuffer buff) throws CharacterCodingException {
        final CharBuffer chars = UriEncoder.UTF8Decoder.decode(buff);
        return chars.toString();
    }
    
    public static String decode(final String buff) {
        try {
            return URLDecoder.decode(buff, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new YAMLException(e);
        }
    }
    
    static {
        UTF8Decoder = Charset.forName("UTF-8").newDecoder().onMalformedInput(CodingErrorAction.REPORT);
        escaper = new PercentEscaper("-_.!~*'()@:$&,;=[]/", false);
    }
}
