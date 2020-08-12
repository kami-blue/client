// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.scanner;

import java.util.HashMap;
import java.nio.charset.CharacterCodingException;
import org.yaml.snakeyaml.util.UriEncoder;
import java.nio.ByteBuffer;
import org.yaml.snakeyaml.tokens.ScalarToken;
import org.yaml.snakeyaml.tokens.TagToken;
import org.yaml.snakeyaml.tokens.TagTuple;
import org.yaml.snakeyaml.tokens.AliasToken;
import org.yaml.snakeyaml.tokens.AnchorToken;
import org.yaml.snakeyaml.tokens.DirectiveToken;
import org.yaml.snakeyaml.tokens.ValueToken;
import org.yaml.snakeyaml.tokens.KeyToken;
import org.yaml.snakeyaml.tokens.BlockMappingStartToken;
import org.yaml.snakeyaml.tokens.BlockEntryToken;
import org.yaml.snakeyaml.tokens.BlockSequenceStartToken;
import org.yaml.snakeyaml.tokens.FlowEntryToken;
import org.yaml.snakeyaml.tokens.FlowSequenceEndToken;
import org.yaml.snakeyaml.tokens.FlowMappingEndToken;
import org.yaml.snakeyaml.tokens.FlowSequenceStartToken;
import org.yaml.snakeyaml.tokens.FlowMappingStartToken;
import org.yaml.snakeyaml.tokens.DocumentEndToken;
import org.yaml.snakeyaml.tokens.DocumentStartToken;
import org.yaml.snakeyaml.tokens.StreamEndToken;
import org.yaml.snakeyaml.tokens.StreamStartToken;
import org.yaml.snakeyaml.tokens.BlockEndToken;
import org.yaml.snakeyaml.error.YAMLException;
import java.util.Iterator;
import org.yaml.snakeyaml.error.Mark;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import org.yaml.snakeyaml.util.ArrayStack;
import org.yaml.snakeyaml.tokens.Token;
import java.util.List;
import org.yaml.snakeyaml.reader.StreamReader;
import java.util.Map;
import java.util.regex.Pattern;

public final class ScannerImpl implements Scanner
{
    private static final Pattern NOT_HEXA;
    public static final Map<Character, String> ESCAPE_REPLACEMENTS;
    public static final Map<Character, Integer> ESCAPE_CODES;
    private final StreamReader reader;
    private boolean done;
    private int flowLevel;
    private List<Token> tokens;
    private int tokensTaken;
    private int indent;
    private ArrayStack<Integer> indents;
    private boolean allowSimpleKey;
    private Map<Integer, SimpleKey> possibleSimpleKeys;
    
    public ScannerImpl(final StreamReader reader) {
        this.done = false;
        this.flowLevel = 0;
        this.tokensTaken = 0;
        this.indent = -1;
        this.allowSimpleKey = true;
        this.reader = reader;
        this.tokens = new ArrayList<Token>(100);
        this.indents = new ArrayStack<Integer>(10);
        this.possibleSimpleKeys = new LinkedHashMap<Integer, SimpleKey>();
        this.fetchStreamStart();
    }
    
    @Override
    public boolean checkToken(final Token.ID... choices) {
        while (this.needMoreTokens()) {
            this.fetchMoreTokens();
        }
        if (!this.tokens.isEmpty()) {
            if (choices.length == 0) {
                return true;
            }
            final Token.ID first = this.tokens.get(0).getTokenId();
            for (int i = 0; i < choices.length; ++i) {
                if (first == choices[i]) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public Token peekToken() {
        while (this.needMoreTokens()) {
            this.fetchMoreTokens();
        }
        return this.tokens.get(0);
    }
    
    @Override
    public Token getToken() {
        if (!this.tokens.isEmpty()) {
            ++this.tokensTaken;
            return this.tokens.remove(0);
        }
        return null;
    }
    
    private boolean needMoreTokens() {
        if (this.done) {
            return false;
        }
        if (this.tokens.isEmpty()) {
            return true;
        }
        this.stalePossibleSimpleKeys();
        return this.nextPossibleSimpleKey() == this.tokensTaken;
    }
    
    private void fetchMoreTokens() {
        this.scanToNextToken();
        this.stalePossibleSimpleKeys();
        this.unwindIndent(this.reader.getColumn());
        final int c = this.reader.peek();
        switch (c) {
            case 0: {
                this.fetchStreamEnd();
                return;
            }
            case 37: {
                if (this.checkDirective()) {
                    this.fetchDirective();
                    return;
                }
                break;
            }
            case 45: {
                if (this.checkDocumentStart()) {
                    this.fetchDocumentStart();
                    return;
                }
                if (this.checkBlockEntry()) {
                    this.fetchBlockEntry();
                    return;
                }
                break;
            }
            case 46: {
                if (this.checkDocumentEnd()) {
                    this.fetchDocumentEnd();
                    return;
                }
                break;
            }
            case 91: {
                this.fetchFlowSequenceStart();
                return;
            }
            case 123: {
                this.fetchFlowMappingStart();
                return;
            }
            case 93: {
                this.fetchFlowSequenceEnd();
                return;
            }
            case 125: {
                this.fetchFlowMappingEnd();
                return;
            }
            case 44: {
                this.fetchFlowEntry();
                return;
            }
            case 63: {
                if (this.checkKey()) {
                    this.fetchKey();
                    return;
                }
                break;
            }
            case 58: {
                if (this.checkValue()) {
                    this.fetchValue();
                    return;
                }
                break;
            }
            case 42: {
                this.fetchAlias();
                return;
            }
            case 38: {
                this.fetchAnchor();
                return;
            }
            case 33: {
                this.fetchTag();
                return;
            }
            case 124: {
                if (this.flowLevel == 0) {
                    this.fetchLiteral();
                    return;
                }
                break;
            }
            case 62: {
                if (this.flowLevel == 0) {
                    this.fetchFolded();
                    return;
                }
                break;
            }
            case 39: {
                this.fetchSingle();
                return;
            }
            case 34: {
                this.fetchDouble();
                return;
            }
        }
        if (this.checkPlain()) {
            this.fetchPlain();
            return;
        }
        String chRepresentation = String.valueOf(Character.toChars(c));
        for (final Character s : ScannerImpl.ESCAPE_REPLACEMENTS.keySet()) {
            final String v = ScannerImpl.ESCAPE_REPLACEMENTS.get(s);
            if (v.equals(chRepresentation)) {
                chRepresentation = "\\" + s;
                break;
            }
        }
        if (c == 9) {
            chRepresentation += "(TAB)";
        }
        final String text = String.format("found character '%s' that cannot start any token. (Do not use %s for indentation)", chRepresentation, chRepresentation);
        throw new ScannerException("while scanning for the next token", null, text, this.reader.getMark());
    }
    
    private int nextPossibleSimpleKey() {
        if (!this.possibleSimpleKeys.isEmpty()) {
            return this.possibleSimpleKeys.values().iterator().next().getTokenNumber();
        }
        return -1;
    }
    
    private void stalePossibleSimpleKeys() {
        if (!this.possibleSimpleKeys.isEmpty()) {
            final Iterator<SimpleKey> iterator = this.possibleSimpleKeys.values().iterator();
            while (iterator.hasNext()) {
                final SimpleKey key = iterator.next();
                if (key.getLine() != this.reader.getLine() || this.reader.getIndex() - key.getIndex() > 1024) {
                    if (key.isRequired()) {
                        throw new ScannerException("while scanning a simple key", key.getMark(), "could not find expected ':'", this.reader.getMark());
                    }
                    iterator.remove();
                }
            }
        }
    }
    
    private void savePossibleSimpleKey() {
        final boolean required = this.flowLevel == 0 && this.indent == this.reader.getColumn();
        if (!this.allowSimpleKey && required) {
            throw new YAMLException("A simple key is required only if it is the first token in the current line");
        }
        if (this.allowSimpleKey) {
            this.removePossibleSimpleKey();
            final int tokenNumber = this.tokensTaken + this.tokens.size();
            final SimpleKey key = new SimpleKey(tokenNumber, required, this.reader.getIndex(), this.reader.getLine(), this.reader.getColumn(), this.reader.getMark());
            this.possibleSimpleKeys.put(this.flowLevel, key);
        }
    }
    
    private void removePossibleSimpleKey() {
        final SimpleKey key = this.possibleSimpleKeys.remove(this.flowLevel);
        if (key != null && key.isRequired()) {
            throw new ScannerException("while scanning a simple key", key.getMark(), "could not find expected ':'", this.reader.getMark());
        }
    }
    
    private void unwindIndent(final int col) {
        if (this.flowLevel != 0) {
            return;
        }
        while (this.indent > col) {
            final Mark mark = this.reader.getMark();
            this.indent = this.indents.pop();
            this.tokens.add(new BlockEndToken(mark, mark));
        }
    }
    
    private boolean addIndent(final int column) {
        if (this.indent < column) {
            this.indents.push(this.indent);
            this.indent = column;
            return true;
        }
        return false;
    }
    
    private void fetchStreamStart() {
        final Mark mark = this.reader.getMark();
        final Token token = new StreamStartToken(mark, mark);
        this.tokens.add(token);
    }
    
    private void fetchStreamEnd() {
        this.unwindIndent(-1);
        this.removePossibleSimpleKey();
        this.allowSimpleKey = false;
        this.possibleSimpleKeys.clear();
        final Mark mark = this.reader.getMark();
        final Token token = new StreamEndToken(mark, mark);
        this.tokens.add(token);
        this.done = true;
    }
    
    private void fetchDirective() {
        this.unwindIndent(-1);
        this.removePossibleSimpleKey();
        this.allowSimpleKey = false;
        final Token tok = this.scanDirective();
        this.tokens.add(tok);
    }
    
    private void fetchDocumentStart() {
        this.fetchDocumentIndicator(true);
    }
    
    private void fetchDocumentEnd() {
        this.fetchDocumentIndicator(false);
    }
    
    private void fetchDocumentIndicator(final boolean isDocumentStart) {
        this.unwindIndent(-1);
        this.removePossibleSimpleKey();
        this.allowSimpleKey = false;
        final Mark startMark = this.reader.getMark();
        this.reader.forward(3);
        final Mark endMark = this.reader.getMark();
        Token token;
        if (isDocumentStart) {
            token = new DocumentStartToken(startMark, endMark);
        }
        else {
            token = new DocumentEndToken(startMark, endMark);
        }
        this.tokens.add(token);
    }
    
    private void fetchFlowSequenceStart() {
        this.fetchFlowCollectionStart(false);
    }
    
    private void fetchFlowMappingStart() {
        this.fetchFlowCollectionStart(true);
    }
    
    private void fetchFlowCollectionStart(final boolean isMappingStart) {
        this.savePossibleSimpleKey();
        ++this.flowLevel;
        this.allowSimpleKey = true;
        final Mark startMark = this.reader.getMark();
        this.reader.forward(1);
        final Mark endMark = this.reader.getMark();
        Token token;
        if (isMappingStart) {
            token = new FlowMappingStartToken(startMark, endMark);
        }
        else {
            token = new FlowSequenceStartToken(startMark, endMark);
        }
        this.tokens.add(token);
    }
    
    private void fetchFlowSequenceEnd() {
        this.fetchFlowCollectionEnd(false);
    }
    
    private void fetchFlowMappingEnd() {
        this.fetchFlowCollectionEnd(true);
    }
    
    private void fetchFlowCollectionEnd(final boolean isMappingEnd) {
        this.removePossibleSimpleKey();
        --this.flowLevel;
        this.allowSimpleKey = false;
        final Mark startMark = this.reader.getMark();
        this.reader.forward();
        final Mark endMark = this.reader.getMark();
        Token token;
        if (isMappingEnd) {
            token = new FlowMappingEndToken(startMark, endMark);
        }
        else {
            token = new FlowSequenceEndToken(startMark, endMark);
        }
        this.tokens.add(token);
    }
    
    private void fetchFlowEntry() {
        this.allowSimpleKey = true;
        this.removePossibleSimpleKey();
        final Mark startMark = this.reader.getMark();
        this.reader.forward();
        final Mark endMark = this.reader.getMark();
        final Token token = new FlowEntryToken(startMark, endMark);
        this.tokens.add(token);
    }
    
    private void fetchBlockEntry() {
        if (this.flowLevel == 0) {
            if (!this.allowSimpleKey) {
                throw new ScannerException(null, null, "sequence entries are not allowed here", this.reader.getMark());
            }
            if (this.addIndent(this.reader.getColumn())) {
                final Mark mark = this.reader.getMark();
                this.tokens.add(new BlockSequenceStartToken(mark, mark));
            }
        }
        this.allowSimpleKey = true;
        this.removePossibleSimpleKey();
        final Mark startMark = this.reader.getMark();
        this.reader.forward();
        final Mark endMark = this.reader.getMark();
        final Token token = new BlockEntryToken(startMark, endMark);
        this.tokens.add(token);
    }
    
    private void fetchKey() {
        if (this.flowLevel == 0) {
            if (!this.allowSimpleKey) {
                throw new ScannerException(null, null, "mapping keys are not allowed here", this.reader.getMark());
            }
            if (this.addIndent(this.reader.getColumn())) {
                final Mark mark = this.reader.getMark();
                this.tokens.add(new BlockMappingStartToken(mark, mark));
            }
        }
        this.allowSimpleKey = (this.flowLevel == 0);
        this.removePossibleSimpleKey();
        final Mark startMark = this.reader.getMark();
        this.reader.forward();
        final Mark endMark = this.reader.getMark();
        final Token token = new KeyToken(startMark, endMark);
        this.tokens.add(token);
    }
    
    private void fetchValue() {
        final SimpleKey key = this.possibleSimpleKeys.remove(this.flowLevel);
        if (key != null) {
            this.tokens.add(key.getTokenNumber() - this.tokensTaken, new KeyToken(key.getMark(), key.getMark()));
            if (this.flowLevel == 0 && this.addIndent(key.getColumn())) {
                this.tokens.add(key.getTokenNumber() - this.tokensTaken, new BlockMappingStartToken(key.getMark(), key.getMark()));
            }
            this.allowSimpleKey = false;
        }
        else {
            if (this.flowLevel == 0 && !this.allowSimpleKey) {
                throw new ScannerException(null, null, "mapping values are not allowed here", this.reader.getMark());
            }
            if (this.flowLevel == 0 && this.addIndent(this.reader.getColumn())) {
                final Mark mark = this.reader.getMark();
                this.tokens.add(new BlockMappingStartToken(mark, mark));
            }
            this.allowSimpleKey = (this.flowLevel == 0);
            this.removePossibleSimpleKey();
        }
        final Mark startMark = this.reader.getMark();
        this.reader.forward();
        final Mark endMark = this.reader.getMark();
        final Token token = new ValueToken(startMark, endMark);
        this.tokens.add(token);
    }
    
    private void fetchAlias() {
        this.savePossibleSimpleKey();
        this.allowSimpleKey = false;
        final Token tok = this.scanAnchor(false);
        this.tokens.add(tok);
    }
    
    private void fetchAnchor() {
        this.savePossibleSimpleKey();
        this.allowSimpleKey = false;
        final Token tok = this.scanAnchor(true);
        this.tokens.add(tok);
    }
    
    private void fetchTag() {
        this.savePossibleSimpleKey();
        this.allowSimpleKey = false;
        final Token tok = this.scanTag();
        this.tokens.add(tok);
    }
    
    private void fetchLiteral() {
        this.fetchBlockScalar('|');
    }
    
    private void fetchFolded() {
        this.fetchBlockScalar('>');
    }
    
    private void fetchBlockScalar(final char style) {
        this.allowSimpleKey = true;
        this.removePossibleSimpleKey();
        final Token tok = this.scanBlockScalar(style);
        this.tokens.add(tok);
    }
    
    private void fetchSingle() {
        this.fetchFlowScalar('\'');
    }
    
    private void fetchDouble() {
        this.fetchFlowScalar('\"');
    }
    
    private void fetchFlowScalar(final char style) {
        this.savePossibleSimpleKey();
        this.allowSimpleKey = false;
        final Token tok = this.scanFlowScalar(style);
        this.tokens.add(tok);
    }
    
    private void fetchPlain() {
        this.savePossibleSimpleKey();
        this.allowSimpleKey = false;
        final Token tok = this.scanPlain();
        this.tokens.add(tok);
    }
    
    private boolean checkDirective() {
        return this.reader.getColumn() == 0;
    }
    
    private boolean checkDocumentStart() {
        return this.reader.getColumn() == 0 && "---".equals(this.reader.prefix(3)) && Constant.NULL_BL_T_LINEBR.has(this.reader.peek(3));
    }
    
    private boolean checkDocumentEnd() {
        return this.reader.getColumn() == 0 && "...".equals(this.reader.prefix(3)) && Constant.NULL_BL_T_LINEBR.has(this.reader.peek(3));
    }
    
    private boolean checkBlockEntry() {
        return Constant.NULL_BL_T_LINEBR.has(this.reader.peek(1));
    }
    
    private boolean checkKey() {
        return this.flowLevel != 0 || Constant.NULL_BL_T_LINEBR.has(this.reader.peek(1));
    }
    
    private boolean checkValue() {
        return this.flowLevel != 0 || Constant.NULL_BL_T_LINEBR.has(this.reader.peek(1));
    }
    
    private boolean checkPlain() {
        final int c = this.reader.peek();
        return Constant.NULL_BL_T_LINEBR.hasNo(c, "-?:,[]{}#&*!|>'\"%@`") || (Constant.NULL_BL_T_LINEBR.hasNo(this.reader.peek(1)) && (c == 45 || (this.flowLevel == 0 && "?:".indexOf(c) != -1)));
    }
    
    private void scanToNextToken() {
        if (this.reader.getIndex() == 0 && this.reader.peek() == 65279) {
            this.reader.forward();
        }
        boolean found = false;
        while (!found) {
            int ff;
            for (ff = 0; this.reader.peek(ff) == 32; ++ff) {}
            if (ff > 0) {
                this.reader.forward(ff);
            }
            if (this.reader.peek() == 35) {
                for (ff = 0; Constant.NULL_OR_LINEBR.hasNo(this.reader.peek(ff)); ++ff) {}
                if (ff > 0) {
                    this.reader.forward(ff);
                }
            }
            if (this.scanLineBreak().length() != 0) {
                if (this.flowLevel != 0) {
                    continue;
                }
                this.allowSimpleKey = true;
            }
            else {
                found = true;
            }
        }
    }
    
    private Token scanDirective() {
        final Mark startMark = this.reader.getMark();
        this.reader.forward();
        final String name = this.scanDirectiveName(startMark);
        List<?> value = null;
        Mark endMark;
        if ("YAML".equals(name)) {
            value = this.scanYamlDirectiveValue(startMark);
            endMark = this.reader.getMark();
        }
        else if ("TAG".equals(name)) {
            value = this.scanTagDirectiveValue(startMark);
            endMark = this.reader.getMark();
        }
        else {
            endMark = this.reader.getMark();
            int ff;
            for (ff = 0; Constant.NULL_OR_LINEBR.hasNo(this.reader.peek(ff)); ++ff) {}
            if (ff > 0) {
                this.reader.forward(ff);
            }
        }
        this.scanDirectiveIgnoredLine(startMark);
        return new DirectiveToken<Object>(name, value, startMark, endMark);
    }
    
    private String scanDirectiveName(final Mark startMark) {
        int length;
        int c;
        for (length = 0, c = this.reader.peek(length); Constant.ALPHA.has(c); c = this.reader.peek(length)) {
            ++length;
        }
        if (length == 0) {
            final String s = String.valueOf(Character.toChars(c));
            throw new ScannerException("while scanning a directive", startMark, "expected alphabetic or numeric character, but found " + s + "(" + c + ")", this.reader.getMark());
        }
        final String value = this.reader.prefixForward(length);
        c = this.reader.peek();
        if (Constant.NULL_BL_LINEBR.hasNo(c)) {
            final String s2 = String.valueOf(Character.toChars(c));
            throw new ScannerException("while scanning a directive", startMark, "expected alphabetic or numeric character, but found " + s2 + "(" + c + ")", this.reader.getMark());
        }
        return value;
    }
    
    private List<Integer> scanYamlDirectiveValue(final Mark startMark) {
        while (this.reader.peek() == 32) {
            this.reader.forward();
        }
        final Integer major = this.scanYamlDirectiveNumber(startMark);
        int c = this.reader.peek();
        if (c != 46) {
            final String s = String.valueOf(Character.toChars(c));
            throw new ScannerException("while scanning a directive", startMark, "expected a digit or '.', but found " + s + "(" + c + ")", this.reader.getMark());
        }
        this.reader.forward();
        final Integer minor = this.scanYamlDirectiveNumber(startMark);
        c = this.reader.peek();
        if (Constant.NULL_BL_LINEBR.hasNo(c)) {
            final String s2 = String.valueOf(Character.toChars(c));
            throw new ScannerException("while scanning a directive", startMark, "expected a digit or ' ', but found " + s2 + "(" + c + ")", this.reader.getMark());
        }
        final List<Integer> result = new ArrayList<Integer>(2);
        result.add(major);
        result.add(minor);
        return result;
    }
    
    private Integer scanYamlDirectiveNumber(final Mark startMark) {
        final int c = this.reader.peek();
        if (!Character.isDigit(c)) {
            final String s = String.valueOf(Character.toChars(c));
            throw new ScannerException("while scanning a directive", startMark, "expected a digit, but found " + s + "(" + c + ")", this.reader.getMark());
        }
        int length;
        for (length = 0; Character.isDigit(this.reader.peek(length)); ++length) {}
        final Integer value = Integer.parseInt(this.reader.prefixForward(length));
        return value;
    }
    
    private List<String> scanTagDirectiveValue(final Mark startMark) {
        while (this.reader.peek() == 32) {
            this.reader.forward();
        }
        final String handle = this.scanTagDirectiveHandle(startMark);
        while (this.reader.peek() == 32) {
            this.reader.forward();
        }
        final String prefix = this.scanTagDirectivePrefix(startMark);
        final List<String> result = new ArrayList<String>(2);
        result.add(handle);
        result.add(prefix);
        return result;
    }
    
    private String scanTagDirectiveHandle(final Mark startMark) {
        final String value = this.scanTagHandle("directive", startMark);
        final int c = this.reader.peek();
        if (c != 32) {
            final String s = String.valueOf(Character.toChars(c));
            throw new ScannerException("while scanning a directive", startMark, "expected ' ', but found " + s + "(" + c + ")", this.reader.getMark());
        }
        return value;
    }
    
    private String scanTagDirectivePrefix(final Mark startMark) {
        final String value = this.scanTagUri("directive", startMark);
        final int c = this.reader.peek();
        if (Constant.NULL_BL_LINEBR.hasNo(c)) {
            final String s = String.valueOf(Character.toChars(c));
            throw new ScannerException("while scanning a directive", startMark, "expected ' ', but found " + s + "(" + c + ")", this.reader.getMark());
        }
        return value;
    }
    
    private String scanDirectiveIgnoredLine(final Mark startMark) {
        while (this.reader.peek() == 32) {
            this.reader.forward();
        }
        if (this.reader.peek() == 35) {
            while (Constant.NULL_OR_LINEBR.hasNo(this.reader.peek())) {
                this.reader.forward();
            }
        }
        final int c = this.reader.peek();
        final String lineBreak = this.scanLineBreak();
        if (lineBreak.length() == 0 && c != 0) {
            final String s = String.valueOf(Character.toChars(c));
            throw new ScannerException("while scanning a directive", startMark, "expected a comment or a line break, but found " + s + "(" + c + ")", this.reader.getMark());
        }
        return lineBreak;
    }
    
    private Token scanAnchor(final boolean isAnchor) {
        final Mark startMark = this.reader.getMark();
        final int indicator = this.reader.peek();
        final String name = (indicator == 42) ? "alias" : "anchor";
        this.reader.forward();
        int length;
        int c;
        for (length = 0, c = this.reader.peek(length); Constant.ALPHA.has(c); c = this.reader.peek(length)) {
            ++length;
        }
        if (length == 0) {
            final String s = String.valueOf(Character.toChars(c));
            throw new ScannerException("while scanning an " + name, startMark, "expected alphabetic or numeric character, but found " + s + "(" + c + ")", this.reader.getMark());
        }
        final String value = this.reader.prefixForward(length);
        c = this.reader.peek();
        if (Constant.NULL_BL_T_LINEBR.hasNo(c, "?:,]}%@`")) {
            final String s2 = String.valueOf(Character.toChars(c));
            throw new ScannerException("while scanning an " + name, startMark, "expected alphabetic or numeric character, but found " + s2 + "(" + c + ")", this.reader.getMark());
        }
        final Mark endMark = this.reader.getMark();
        Token tok;
        if (isAnchor) {
            tok = new AnchorToken(value, startMark, endMark);
        }
        else {
            tok = new AliasToken(value, startMark, endMark);
        }
        return tok;
    }
    
    private Token scanTag() {
        final Mark startMark = this.reader.getMark();
        int c = this.reader.peek(1);
        String handle = null;
        String suffix = null;
        if (c == 60) {
            this.reader.forward(2);
            suffix = this.scanTagUri("tag", startMark);
            c = this.reader.peek();
            if (c != 62) {
                final String s = String.valueOf(Character.toChars(c));
                throw new ScannerException("while scanning a tag", startMark, "expected '>', but found '" + s + "' (" + c + ")", this.reader.getMark());
            }
            this.reader.forward();
        }
        else if (Constant.NULL_BL_T_LINEBR.has(c)) {
            suffix = "!";
            this.reader.forward();
        }
        else {
            int length = 1;
            boolean useHandle = false;
            while (Constant.NULL_BL_LINEBR.hasNo(c)) {
                if (c == 33) {
                    useHandle = true;
                    break;
                }
                ++length;
                c = this.reader.peek(length);
            }
            handle = "!";
            if (useHandle) {
                handle = this.scanTagHandle("tag", startMark);
            }
            else {
                handle = "!";
                this.reader.forward();
            }
            suffix = this.scanTagUri("tag", startMark);
        }
        c = this.reader.peek();
        if (Constant.NULL_BL_LINEBR.hasNo(c)) {
            final String s = String.valueOf(Character.toChars(c));
            throw new ScannerException("while scanning a tag", startMark, "expected ' ', but found '" + s + "' (" + c + ")", this.reader.getMark());
        }
        final TagTuple value = new TagTuple(handle, suffix);
        final Mark endMark = this.reader.getMark();
        return new TagToken(value, startMark, endMark);
    }
    
    private Token scanBlockScalar(final char style) {
        final boolean folded = style == '>';
        final StringBuilder chunks = new StringBuilder();
        final Mark startMark = this.reader.getMark();
        this.reader.forward();
        final Chomping chompi = this.scanBlockScalarIndicators(startMark);
        final int increment = chompi.getIncrement();
        this.scanBlockScalarIgnoredLine(startMark);
        int minIndent = this.indent + 1;
        if (minIndent < 1) {
            minIndent = 1;
        }
        String breaks = null;
        int maxIndent = 0;
        int indent = 0;
        Mark endMark;
        if (increment == -1) {
            final Object[] brme = this.scanBlockScalarIndentation();
            breaks = (String)brme[0];
            maxIndent = (int)brme[1];
            endMark = (Mark)brme[2];
            indent = Math.max(minIndent, maxIndent);
        }
        else {
            indent = minIndent + increment - 1;
            final Object[] brme = this.scanBlockScalarBreaks(indent);
            breaks = (String)brme[0];
            endMark = (Mark)brme[1];
        }
        String lineBreak = "";
        while (this.reader.getColumn() == indent && this.reader.peek() != 0) {
            chunks.append(breaks);
            final boolean leadingNonSpace = " \t".indexOf(this.reader.peek()) == -1;
            int length;
            for (length = 0; Constant.NULL_OR_LINEBR.hasNo(this.reader.peek(length)); ++length) {}
            chunks.append(this.reader.prefixForward(length));
            lineBreak = this.scanLineBreak();
            final Object[] brme2 = this.scanBlockScalarBreaks(indent);
            breaks = (String)brme2[0];
            endMark = (Mark)brme2[1];
            if (this.reader.getColumn() != indent || this.reader.peek() == 0) {
                break;
            }
            if (folded && "\n".equals(lineBreak) && leadingNonSpace && " \t".indexOf(this.reader.peek()) == -1) {
                if (breaks.length() != 0) {
                    continue;
                }
                chunks.append(" ");
            }
            else {
                chunks.append(lineBreak);
            }
        }
        if (chompi.chompTailIsNotFalse()) {
            chunks.append(lineBreak);
        }
        if (chompi.chompTailIsTrue()) {
            chunks.append(breaks);
        }
        return new ScalarToken(chunks.toString(), false, startMark, endMark, style);
    }
    
    private Chomping scanBlockScalarIndicators(final Mark startMark) {
        Boolean chomping = null;
        int increment = -1;
        int c = this.reader.peek();
        if (c == 45 || c == 43) {
            if (c == 43) {
                chomping = Boolean.TRUE;
            }
            else {
                chomping = Boolean.FALSE;
            }
            this.reader.forward();
            c = this.reader.peek();
            if (Character.isDigit(c)) {
                final String s = String.valueOf(Character.toChars(c));
                increment = Integer.parseInt(s);
                if (increment == 0) {
                    throw new ScannerException("while scanning a block scalar", startMark, "expected indentation indicator in the range 1-9, but found 0", this.reader.getMark());
                }
                this.reader.forward();
            }
        }
        else if (Character.isDigit(c)) {
            final String s = String.valueOf(Character.toChars(c));
            increment = Integer.parseInt(s);
            if (increment == 0) {
                throw new ScannerException("while scanning a block scalar", startMark, "expected indentation indicator in the range 1-9, but found 0", this.reader.getMark());
            }
            this.reader.forward();
            c = this.reader.peek();
            if (c == 45 || c == 43) {
                if (c == 43) {
                    chomping = Boolean.TRUE;
                }
                else {
                    chomping = Boolean.FALSE;
                }
                this.reader.forward();
            }
        }
        c = this.reader.peek();
        if (Constant.NULL_BL_LINEBR.hasNo(c)) {
            final String s = String.valueOf(Character.toChars(c));
            throw new ScannerException("while scanning a block scalar", startMark, "expected chomping or indentation indicators, but found " + s + "(" + c + ")", this.reader.getMark());
        }
        return new Chomping(chomping, increment);
    }
    
    private String scanBlockScalarIgnoredLine(final Mark startMark) {
        while (this.reader.peek() == 32) {
            this.reader.forward();
        }
        if (this.reader.peek() == 35) {
            while (Constant.NULL_OR_LINEBR.hasNo(this.reader.peek())) {
                this.reader.forward();
            }
        }
        final int c = this.reader.peek();
        final String lineBreak = this.scanLineBreak();
        if (lineBreak.length() == 0 && c != 0) {
            final String s = String.valueOf(Character.toChars(c));
            throw new ScannerException("while scanning a block scalar", startMark, "expected a comment or a line break, but found " + s + "(" + c + ")", this.reader.getMark());
        }
        return lineBreak;
    }
    
    private Object[] scanBlockScalarIndentation() {
        final StringBuilder chunks = new StringBuilder();
        int maxIndent = 0;
        Mark endMark = this.reader.getMark();
        while (Constant.LINEBR.has(this.reader.peek(), " \r")) {
            if (this.reader.peek() != 32) {
                chunks.append(this.scanLineBreak());
                endMark = this.reader.getMark();
            }
            else {
                this.reader.forward();
                if (this.reader.getColumn() <= maxIndent) {
                    continue;
                }
                maxIndent = this.reader.getColumn();
            }
        }
        return new Object[] { chunks.toString(), maxIndent, endMark };
    }
    
    private Object[] scanBlockScalarBreaks(final int indent) {
        final StringBuilder chunks = new StringBuilder();
        Mark endMark = this.reader.getMark();
        for (int col = this.reader.getColumn(); col < indent && this.reader.peek() == 32; ++col) {
            this.reader.forward();
        }
        String lineBreak = null;
        while ((lineBreak = this.scanLineBreak()).length() != 0) {
            chunks.append(lineBreak);
            endMark = this.reader.getMark();
            for (int col = this.reader.getColumn(); col < indent && this.reader.peek() == 32; ++col) {
                this.reader.forward();
            }
        }
        return new Object[] { chunks.toString(), endMark };
    }
    
    private Token scanFlowScalar(final char style) {
        final boolean _double = style == '\"';
        final StringBuilder chunks = new StringBuilder();
        final Mark startMark = this.reader.getMark();
        final int quote = this.reader.peek();
        this.reader.forward();
        chunks.append(this.scanFlowScalarNonSpaces(_double, startMark));
        while (this.reader.peek() != quote) {
            chunks.append(this.scanFlowScalarSpaces(startMark));
            chunks.append(this.scanFlowScalarNonSpaces(_double, startMark));
        }
        this.reader.forward();
        final Mark endMark = this.reader.getMark();
        return new ScalarToken(chunks.toString(), false, startMark, endMark, style);
    }
    
    private String scanFlowScalarNonSpaces(final boolean doubleQuoted, final Mark startMark) {
        final StringBuilder chunks = new StringBuilder();
        while (true) {
            int length;
            for (length = 0; Constant.NULL_BL_T_LINEBR.hasNo(this.reader.peek(length), "'\"\\"); ++length) {}
            if (length != 0) {
                chunks.append(this.reader.prefixForward(length));
            }
            int c = this.reader.peek();
            if (!doubleQuoted && c == 39 && this.reader.peek(1) == 39) {
                chunks.append("'");
                this.reader.forward(2);
            }
            else if ((doubleQuoted && c == 39) || (!doubleQuoted && "\"\\".indexOf(c) != -1)) {
                chunks.appendCodePoint(c);
                this.reader.forward();
            }
            else {
                if (!doubleQuoted || c != 92) {
                    return chunks.toString();
                }
                this.reader.forward();
                c = this.reader.peek();
                if (!Character.isSupplementaryCodePoint(c) && ScannerImpl.ESCAPE_REPLACEMENTS.containsKey((char)c)) {
                    chunks.append(ScannerImpl.ESCAPE_REPLACEMENTS.get((char)c));
                    this.reader.forward();
                }
                else if (!Character.isSupplementaryCodePoint(c) && ScannerImpl.ESCAPE_CODES.containsKey((char)c)) {
                    length = ScannerImpl.ESCAPE_CODES.get((char)c);
                    this.reader.forward();
                    final String hex = this.reader.prefix(length);
                    if (ScannerImpl.NOT_HEXA.matcher(hex).find()) {
                        throw new ScannerException("while scanning a double-quoted scalar", startMark, "expected escape sequence of " + length + " hexadecimal numbers, but found: " + hex, this.reader.getMark());
                    }
                    final int decimal = Integer.parseInt(hex, 16);
                    final String unicode = new String(Character.toChars(decimal));
                    chunks.append(unicode);
                    this.reader.forward(length);
                }
                else {
                    if (this.scanLineBreak().length() == 0) {
                        final String s = String.valueOf(Character.toChars(c));
                        throw new ScannerException("while scanning a double-quoted scalar", startMark, "found unknown escape character " + s + "(" + c + ")", this.reader.getMark());
                    }
                    chunks.append(this.scanFlowScalarBreaks(startMark));
                }
            }
        }
    }
    
    private String scanFlowScalarSpaces(final Mark startMark) {
        final StringBuilder chunks = new StringBuilder();
        int length;
        for (length = 0; " \t".indexOf(this.reader.peek(length)) != -1; ++length) {}
        final String whitespaces = this.reader.prefixForward(length);
        final int c = this.reader.peek();
        if (c == 0) {
            throw new ScannerException("while scanning a quoted scalar", startMark, "found unexpected end of stream", this.reader.getMark());
        }
        final String lineBreak = this.scanLineBreak();
        if (lineBreak.length() != 0) {
            final String breaks = this.scanFlowScalarBreaks(startMark);
            if (!"\n".equals(lineBreak)) {
                chunks.append(lineBreak);
            }
            else if (breaks.length() == 0) {
                chunks.append(" ");
            }
            chunks.append(breaks);
        }
        else {
            chunks.append(whitespaces);
        }
        return chunks.toString();
    }
    
    private String scanFlowScalarBreaks(final Mark startMark) {
        final StringBuilder chunks = new StringBuilder();
        while (true) {
            final String prefix = this.reader.prefix(3);
            if (("---".equals(prefix) || "...".equals(prefix)) && Constant.NULL_BL_T_LINEBR.has(this.reader.peek(3))) {
                throw new ScannerException("while scanning a quoted scalar", startMark, "found unexpected document separator", this.reader.getMark());
            }
            while (" \t".indexOf(this.reader.peek()) != -1) {
                this.reader.forward();
            }
            final String lineBreak = this.scanLineBreak();
            if (lineBreak.length() == 0) {
                return chunks.toString();
            }
            chunks.append(lineBreak);
        }
    }
    
    private Token scanPlain() {
        final StringBuilder chunks = new StringBuilder();
        Mark endMark;
        final Mark startMark = endMark = this.reader.getMark();
        final int indent = this.indent + 1;
        String spaces = "";
        do {
            int length = 0;
            if (this.reader.peek() == 35) {
                break;
            }
            int c;
            while (true) {
                c = this.reader.peek(length);
                if (Constant.NULL_BL_T_LINEBR.has(c) || (this.flowLevel == 0 && c == 58 && Constant.NULL_BL_T_LINEBR.has(this.reader.peek(length + 1))) || (this.flowLevel != 0 && ",:?[]{}".indexOf(c) != -1)) {
                    break;
                }
                ++length;
            }
            if (this.flowLevel != 0 && c == 58 && Constant.NULL_BL_T_LINEBR.hasNo(this.reader.peek(length + 1), ",[]{}")) {
                this.reader.forward(length);
                throw new ScannerException("while scanning a plain scalar", startMark, "found unexpected ':'", this.reader.getMark(), "Please check http://pyyaml.org/wiki/YAMLColonInFlowContext for details.");
            }
            if (length == 0) {
                break;
            }
            this.allowSimpleKey = false;
            chunks.append(spaces);
            chunks.append(this.reader.prefixForward(length));
            endMark = this.reader.getMark();
            spaces = this.scanPlainSpaces();
            if (spaces.length() == 0 || this.reader.peek() == 35) {
                break;
            }
        } while (this.flowLevel != 0 || this.reader.getColumn() >= indent);
        return new ScalarToken(chunks.toString(), startMark, endMark, true);
    }
    
    private String scanPlainSpaces() {
        int length;
        for (length = 0; this.reader.peek(length) == 32 || this.reader.peek(length) == 9; ++length) {}
        final String whitespaces = this.reader.prefixForward(length);
        final String lineBreak = this.scanLineBreak();
        if (lineBreak.length() == 0) {
            return whitespaces;
        }
        this.allowSimpleKey = true;
        String prefix = this.reader.prefix(3);
        if ("---".equals(prefix) || ("...".equals(prefix) && Constant.NULL_BL_T_LINEBR.has(this.reader.peek(3)))) {
            return "";
        }
        final StringBuilder breaks = new StringBuilder();
        while (true) {
            if (this.reader.peek() == 32) {
                this.reader.forward();
            }
            else {
                final String lb = this.scanLineBreak();
                if (lb.length() != 0) {
                    breaks.append(lb);
                    prefix = this.reader.prefix(3);
                    if ("---".equals(prefix) || ("...".equals(prefix) && Constant.NULL_BL_T_LINEBR.has(this.reader.peek(3)))) {
                        return "";
                    }
                    continue;
                }
                else {
                    if (!"\n".equals(lineBreak)) {
                        return lineBreak + (Object)breaks;
                    }
                    if (breaks.length() == 0) {
                        return " ";
                    }
                    return breaks.toString();
                }
            }
        }
    }
    
    private String scanTagHandle(final String name, final Mark startMark) {
        int c = this.reader.peek();
        if (c != 33) {
            final String s = String.valueOf(Character.toChars(c));
            throw new ScannerException("while scanning a " + name, startMark, "expected '!', but found " + s + "(" + c + ")", this.reader.getMark());
        }
        int length = 1;
        c = this.reader.peek(length);
        if (c != 32) {
            while (Constant.ALPHA.has(c)) {
                ++length;
                c = this.reader.peek(length);
            }
            if (c != 33) {
                this.reader.forward(length);
                final String s2 = String.valueOf(Character.toChars(c));
                throw new ScannerException("while scanning a " + name, startMark, "expected '!', but found " + s2 + "(" + c + ")", this.reader.getMark());
            }
            ++length;
        }
        final String value = this.reader.prefixForward(length);
        return value;
    }
    
    private String scanTagUri(final String name, final Mark startMark) {
        final StringBuilder chunks = new StringBuilder();
        int length;
        int c;
        for (length = 0, c = this.reader.peek(length); Constant.URI_CHARS.has(c); c = this.reader.peek(length)) {
            if (c == 37) {
                chunks.append(this.reader.prefixForward(length));
                length = 0;
                chunks.append(this.scanUriEscapes(name, startMark));
            }
            else {
                ++length;
            }
        }
        if (length != 0) {
            chunks.append(this.reader.prefixForward(length));
            length = 0;
        }
        if (chunks.length() == 0) {
            final String s = String.valueOf(Character.toChars(c));
            throw new ScannerException("while scanning a " + name, startMark, "expected URI, but found " + s + "(" + c + ")", this.reader.getMark());
        }
        return chunks.toString();
    }
    
    private String scanUriEscapes(final String name, final Mark startMark) {
        int length;
        for (length = 1; this.reader.peek(length * 3) == 37; ++length) {}
        final Mark beginningMark = this.reader.getMark();
        final ByteBuffer buff = ByteBuffer.allocate(length);
        while (this.reader.peek() == 37) {
            this.reader.forward();
            try {
                final byte code = (byte)Integer.parseInt(this.reader.prefix(2), 16);
                buff.put(code);
            }
            catch (NumberFormatException nfe) {
                final int c1 = this.reader.peek();
                final String s1 = String.valueOf(Character.toChars(c1));
                final int c2 = this.reader.peek(1);
                final String s2 = String.valueOf(Character.toChars(c2));
                throw new ScannerException("while scanning a " + name, startMark, "expected URI escape sequence of 2 hexadecimal numbers, but found " + s1 + "(" + c1 + ") and " + s2 + "(" + c2 + ")", this.reader.getMark());
            }
            this.reader.forward(2);
        }
        buff.flip();
        try {
            return UriEncoder.decode(buff);
        }
        catch (CharacterCodingException e) {
            throw new ScannerException("while scanning a " + name, startMark, "expected URI in UTF-8: " + e.getMessage(), beginningMark);
        }
    }
    
    private String scanLineBreak() {
        final int c = this.reader.peek();
        if (c == 13 || c == 10 || c == 133) {
            if (c == 13 && 10 == this.reader.peek(1)) {
                this.reader.forward(2);
            }
            else {
                this.reader.forward();
            }
            return "\n";
        }
        if (c == 8232 || c == 8233) {
            this.reader.forward();
            return String.valueOf(Character.toChars(c));
        }
        return "";
    }
    
    static {
        NOT_HEXA = Pattern.compile("[^0-9A-Fa-f]");
        ESCAPE_REPLACEMENTS = new HashMap<Character, String>();
        ESCAPE_CODES = new HashMap<Character, Integer>();
        ScannerImpl.ESCAPE_REPLACEMENTS.put('0', "\u0000");
        ScannerImpl.ESCAPE_REPLACEMENTS.put('a', "\u0007");
        ScannerImpl.ESCAPE_REPLACEMENTS.put('b', "\b");
        ScannerImpl.ESCAPE_REPLACEMENTS.put('t', "\t");
        ScannerImpl.ESCAPE_REPLACEMENTS.put('n', "\n");
        ScannerImpl.ESCAPE_REPLACEMENTS.put('v', "\u000b");
        ScannerImpl.ESCAPE_REPLACEMENTS.put('f', "\f");
        ScannerImpl.ESCAPE_REPLACEMENTS.put('r', "\r");
        ScannerImpl.ESCAPE_REPLACEMENTS.put('e', "\u001b");
        ScannerImpl.ESCAPE_REPLACEMENTS.put(' ', " ");
        ScannerImpl.ESCAPE_REPLACEMENTS.put('\"', "\"");
        ScannerImpl.ESCAPE_REPLACEMENTS.put('\\', "\\");
        ScannerImpl.ESCAPE_REPLACEMENTS.put('N', "\u0085");
        ScannerImpl.ESCAPE_REPLACEMENTS.put('_', " ");
        ScannerImpl.ESCAPE_REPLACEMENTS.put('L', "\u2028");
        ScannerImpl.ESCAPE_REPLACEMENTS.put('P', "\u2029");
        ScannerImpl.ESCAPE_CODES.put('x', 2);
        ScannerImpl.ESCAPE_CODES.put('u', 4);
        ScannerImpl.ESCAPE_CODES.put('U', 8);
    }
    
    private static class Chomping
    {
        private final Boolean value;
        private final int increment;
        
        public Chomping(final Boolean value, final int increment) {
            this.value = value;
            this.increment = increment;
        }
        
        public boolean chompTailIsNotFalse() {
            return this.value == null || this.value;
        }
        
        public boolean chompTailIsTrue() {
            return this.value != null && this.value;
        }
        
        public int getIncrement() {
            return this.increment;
        }
    }
}
