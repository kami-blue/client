// 
// Decompiled by Procyon v0.5.36
// 

package javassist.compiler;

public class Lex implements TokenId
{
    private int lastChar;
    private StringBuffer textBuffer;
    private Token currentToken;
    private Token lookAheadTokens;
    private String input;
    private int position;
    private int maxlen;
    private int lineNumber;
    private static final int[] equalOps;
    private static final KeywordTable ktable;
    
    public Lex(final String s) {
        this.lastChar = -1;
        this.textBuffer = new StringBuffer();
        this.currentToken = new Token();
        this.lookAheadTokens = null;
        this.input = s;
        this.position = 0;
        this.maxlen = s.length();
        this.lineNumber = 0;
    }
    
    public int get() {
        if (this.lookAheadTokens == null) {
            return this.get(this.currentToken);
        }
        final Token t = this.currentToken = this.lookAheadTokens;
        this.lookAheadTokens = this.lookAheadTokens.next;
        return t.tokenId;
    }
    
    public int lookAhead() {
        return this.lookAhead(0);
    }
    
    public int lookAhead(int i) {
        Token tk = this.lookAheadTokens;
        if (tk == null) {
            tk = (this.lookAheadTokens = this.currentToken);
            tk.next = null;
            this.get(tk);
        }
        while (i-- > 0) {
            if (tk.next == null) {
                final Token tk2 = tk.next = new Token();
                this.get(tk2);
            }
            tk = tk.next;
        }
        this.currentToken = tk;
        return tk.tokenId;
    }
    
    public String getString() {
        return this.currentToken.textValue;
    }
    
    public long getLong() {
        return this.currentToken.longValue;
    }
    
    public double getDouble() {
        return this.currentToken.doubleValue;
    }
    
    private int get(final Token token) {
        int t;
        do {
            t = this.readLine(token);
        } while (t == 10);
        return token.tokenId = t;
    }
    
    private int readLine(final Token token) {
        int c = this.getNextNonWhiteChar();
        if (c < 0) {
            return c;
        }
        if (c == 10) {
            ++this.lineNumber;
            return 10;
        }
        if (c == 39) {
            return this.readCharConst(token);
        }
        if (c == 34) {
            return this.readStringL(token);
        }
        if (48 <= c && c <= 57) {
            return this.readNumber(c, token);
        }
        if (c == 46) {
            c = this.getc();
            if (48 <= c && c <= 57) {
                final StringBuffer tbuf = this.textBuffer;
                tbuf.setLength(0);
                tbuf.append('.');
                return this.readDouble(tbuf, c, token);
            }
            this.ungetc(c);
            return this.readSeparator(46);
        }
        else {
            if (Character.isJavaIdentifierStart((char)c)) {
                return this.readIdentifier(c, token);
            }
            return this.readSeparator(c);
        }
    }
    
    private int getNextNonWhiteChar() {
        int c;
        do {
            c = this.getc();
            if (c == 47) {
                c = this.getc();
                if (c == 47) {
                    do {
                        c = this.getc();
                        if (c != 10 && c != 13) {
                            continue;
                        }
                        break;
                    } while (c != -1);
                }
                else if (c == 42) {
                    while (true) {
                        c = this.getc();
                        if (c == -1) {
                            break;
                        }
                        if (c != 42) {
                            continue;
                        }
                        if ((c = this.getc()) == 47) {
                            c = 32;
                            break;
                        }
                        this.ungetc(c);
                    }
                }
                else {
                    this.ungetc(c);
                    c = 47;
                }
            }
        } while (isBlank(c));
        return c;
    }
    
    private int readCharConst(final Token token) {
        int value = 0;
        int c;
        while ((c = this.getc()) != 39) {
            if (c == 92) {
                value = this.readEscapeChar();
            }
            else {
                if (c < 32) {
                    if (c == 10) {
                        ++this.lineNumber;
                    }
                    return 500;
                }
                value = c;
            }
        }
        token.longValue = value;
        return 401;
    }
    
    private int readEscapeChar() {
        int c = this.getc();
        if (c == 110) {
            c = 10;
        }
        else if (c == 116) {
            c = 9;
        }
        else if (c == 114) {
            c = 13;
        }
        else if (c == 102) {
            c = 12;
        }
        else if (c == 10) {
            ++this.lineNumber;
        }
        return c;
    }
    
    private int readStringL(final Token token) {
        final StringBuffer tbuf = this.textBuffer;
        tbuf.setLength(0);
        while (true) {
            int c;
            if ((c = this.getc()) != 34) {
                if (c == 92) {
                    c = this.readEscapeChar();
                }
                else if (c == 10 || c < 0) {
                    ++this.lineNumber;
                    return 500;
                }
                tbuf.append((char)c);
            }
            else {
                while (true) {
                    c = this.getc();
                    if (c == 10) {
                        ++this.lineNumber;
                    }
                    else {
                        if (!isBlank(c)) {
                            break;
                        }
                        continue;
                    }
                }
                if (c != 34) {
                    this.ungetc(c);
                    token.textValue = tbuf.toString();
                    return 406;
                }
                continue;
            }
        }
    }
    
    private int readNumber(int c, final Token token) {
        long value = 0L;
        int c2 = this.getc();
        if (c == 48) {
            if (c2 == 88 || c2 == 120) {
                while (true) {
                    c = this.getc();
                    if (48 <= c && c <= 57) {
                        value = value * 16L + (c - 48);
                    }
                    else if (65 <= c && c <= 70) {
                        value = value * 16L + (c - 65 + 10);
                    }
                    else {
                        if (97 > c || c > 102) {
                            break;
                        }
                        value = value * 16L + (c - 97 + 10);
                    }
                }
                token.longValue = value;
                if (c == 76 || c == 108) {
                    return 403;
                }
                this.ungetc(c);
                return 402;
            }
            else if (48 <= c2 && c2 <= 55) {
                value = c2 - 48;
                while (true) {
                    c = this.getc();
                    if (48 > c || c > 55) {
                        break;
                    }
                    value = value * 8L + (c - 48);
                }
                token.longValue = value;
                if (c == 76 || c == 108) {
                    return 403;
                }
                this.ungetc(c);
                return 402;
            }
        }
        value = c - 48;
        while (48 <= c2 && c2 <= 57) {
            value = value * 10L + c2 - 48L;
            c2 = this.getc();
        }
        token.longValue = value;
        if (c2 == 70 || c2 == 102) {
            token.doubleValue = (double)value;
            return 404;
        }
        if (c2 == 69 || c2 == 101 || c2 == 68 || c2 == 100 || c2 == 46) {
            final StringBuffer tbuf = this.textBuffer;
            tbuf.setLength(0);
            tbuf.append(value);
            return this.readDouble(tbuf, c2, token);
        }
        if (c2 == 76 || c2 == 108) {
            return 403;
        }
        this.ungetc(c2);
        return 402;
    }
    
    private int readDouble(final StringBuffer sbuf, int c, final Token token) {
        if (c != 69 && c != 101 && c != 68 && c != 100) {
            sbuf.append((char)c);
            while (true) {
                c = this.getc();
                if (48 > c || c > 57) {
                    break;
                }
                sbuf.append((char)c);
            }
        }
        if (c == 69 || c == 101) {
            sbuf.append((char)c);
            c = this.getc();
            if (c == 43 || c == 45) {
                sbuf.append((char)c);
                c = this.getc();
            }
            while (48 <= c && c <= 57) {
                sbuf.append((char)c);
                c = this.getc();
            }
        }
        try {
            token.doubleValue = Double.parseDouble(sbuf.toString());
        }
        catch (NumberFormatException e) {
            return 500;
        }
        if (c == 70 || c == 102) {
            return 404;
        }
        if (c != 68 && c != 100) {
            this.ungetc(c);
        }
        return 405;
    }
    
    private int readSeparator(final int c) {
        int c2;
        if (33 <= c && c <= 63) {
            final int t = Lex.equalOps[c - 33];
            if (t == 0) {
                return c;
            }
            c2 = this.getc();
            if (c == c2) {
                switch (c) {
                    case 61: {
                        return 358;
                    }
                    case 43: {
                        return 362;
                    }
                    case 45: {
                        return 363;
                    }
                    case 38: {
                        return 369;
                    }
                    case 60: {
                        final int c3 = this.getc();
                        if (c3 == 61) {
                            return 365;
                        }
                        this.ungetc(c3);
                        return 364;
                    }
                    case 62: {
                        int c3 = this.getc();
                        if (c3 == 61) {
                            return 367;
                        }
                        if (c3 != 62) {
                            this.ungetc(c3);
                            return 366;
                        }
                        c3 = this.getc();
                        if (c3 == 61) {
                            return 371;
                        }
                        this.ungetc(c3);
                        return 370;
                    }
                }
            }
            else if (c2 == 61) {
                return t;
            }
        }
        else if (c == 94) {
            c2 = this.getc();
            if (c2 == 61) {
                return 360;
            }
        }
        else {
            if (c != 124) {
                return c;
            }
            c2 = this.getc();
            if (c2 == 61) {
                return 361;
            }
            if (c2 == 124) {
                return 368;
            }
        }
        this.ungetc(c2);
        return c;
    }
    
    private int readIdentifier(int c, final Token token) {
        final StringBuffer tbuf = this.textBuffer;
        tbuf.setLength(0);
        do {
            tbuf.append((char)c);
            c = this.getc();
        } while (Character.isJavaIdentifierPart((char)c));
        this.ungetc(c);
        final String name = tbuf.toString();
        final int t = Lex.ktable.lookup(name);
        if (t >= 0) {
            return t;
        }
        token.textValue = name;
        return 400;
    }
    
    private static boolean isBlank(final int c) {
        return c == 32 || c == 9 || c == 12 || c == 13 || c == 10;
    }
    
    private static boolean isDigit(final int c) {
        return 48 <= c && c <= 57;
    }
    
    private void ungetc(final int c) {
        this.lastChar = c;
    }
    
    public String getTextAround() {
        int begin = this.position - 10;
        if (begin < 0) {
            begin = 0;
        }
        int end = this.position + 10;
        if (end > this.maxlen) {
            end = this.maxlen;
        }
        return this.input.substring(begin, end);
    }
    
    private int getc() {
        if (this.lastChar >= 0) {
            final int c = this.lastChar;
            this.lastChar = -1;
            return c;
        }
        if (this.position < this.maxlen) {
            return this.input.charAt(this.position++);
        }
        return -1;
    }
    
    static {
        equalOps = new int[] { 350, 0, 0, 0, 351, 352, 0, 0, 0, 353, 354, 0, 355, 0, 356, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 357, 358, 359, 0 };
        (ktable = new KeywordTable()).append("abstract", 300);
        Lex.ktable.append("boolean", 301);
        Lex.ktable.append("break", 302);
        Lex.ktable.append("byte", 303);
        Lex.ktable.append("case", 304);
        Lex.ktable.append("catch", 305);
        Lex.ktable.append("char", 306);
        Lex.ktable.append("class", 307);
        Lex.ktable.append("const", 308);
        Lex.ktable.append("continue", 309);
        Lex.ktable.append("default", 310);
        Lex.ktable.append("do", 311);
        Lex.ktable.append("double", 312);
        Lex.ktable.append("else", 313);
        Lex.ktable.append("extends", 314);
        Lex.ktable.append("false", 411);
        Lex.ktable.append("final", 315);
        Lex.ktable.append("finally", 316);
        Lex.ktable.append("float", 317);
        Lex.ktable.append("for", 318);
        Lex.ktable.append("goto", 319);
        Lex.ktable.append("if", 320);
        Lex.ktable.append("implements", 321);
        Lex.ktable.append("import", 322);
        Lex.ktable.append("instanceof", 323);
        Lex.ktable.append("int", 324);
        Lex.ktable.append("interface", 325);
        Lex.ktable.append("long", 326);
        Lex.ktable.append("native", 327);
        Lex.ktable.append("new", 328);
        Lex.ktable.append("null", 412);
        Lex.ktable.append("package", 329);
        Lex.ktable.append("private", 330);
        Lex.ktable.append("protected", 331);
        Lex.ktable.append("public", 332);
        Lex.ktable.append("return", 333);
        Lex.ktable.append("short", 334);
        Lex.ktable.append("static", 335);
        Lex.ktable.append("strictfp", 347);
        Lex.ktable.append("super", 336);
        Lex.ktable.append("switch", 337);
        Lex.ktable.append("synchronized", 338);
        Lex.ktable.append("this", 339);
        Lex.ktable.append("throw", 340);
        Lex.ktable.append("throws", 341);
        Lex.ktable.append("transient", 342);
        Lex.ktable.append("true", 410);
        Lex.ktable.append("try", 343);
        Lex.ktable.append("void", 344);
        Lex.ktable.append("volatile", 345);
        Lex.ktable.append("while", 346);
    }
}
