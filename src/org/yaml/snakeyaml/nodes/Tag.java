// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.nodes;

import java.sql.Timestamp;
import java.util.Date;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.HashMap;
import org.yaml.snakeyaml.error.YAMLException;
import java.net.URI;
import org.yaml.snakeyaml.util.UriEncoder;
import java.util.Set;
import java.util.Map;

public final class Tag implements Comparable<Tag>
{
    public static final String PREFIX = "tag:yaml.org,2002:";
    public static final Tag YAML;
    public static final Tag MERGE;
    public static final Tag SET;
    public static final Tag PAIRS;
    public static final Tag OMAP;
    public static final Tag BINARY;
    public static final Tag INT;
    public static final Tag FLOAT;
    public static final Tag TIMESTAMP;
    public static final Tag BOOL;
    public static final Tag NULL;
    public static final Tag STR;
    public static final Tag SEQ;
    public static final Tag MAP;
    public static final Map<Tag, Set<Class<?>>> COMPATIBILITY_MAP;
    private final String value;
    private boolean secondary;
    
    public Tag(final String tag) {
        this.secondary = false;
        if (tag == null) {
            throw new NullPointerException("Tag must be provided.");
        }
        if (tag.length() == 0) {
            throw new IllegalArgumentException("Tag must not be empty.");
        }
        if (tag.trim().length() != tag.length()) {
            throw new IllegalArgumentException("Tag must not contain leading or trailing spaces.");
        }
        this.value = UriEncoder.encode(tag);
        this.secondary = !tag.startsWith("tag:yaml.org,2002:");
    }
    
    public Tag(final Class<?> clazz) {
        this.secondary = false;
        if (clazz == null) {
            throw new NullPointerException("Class for tag must be provided.");
        }
        this.value = "tag:yaml.org,2002:" + UriEncoder.encode(clazz.getName());
    }
    
    public Tag(final URI uri) {
        this.secondary = false;
        if (uri == null) {
            throw new NullPointerException("URI for tag must be provided.");
        }
        this.value = uri.toASCIIString();
    }
    
    public boolean isSecondary() {
        return this.secondary;
    }
    
    public String getValue() {
        return this.value;
    }
    
    public boolean startsWith(final String prefix) {
        return this.value.startsWith(prefix);
    }
    
    public String getClassName() {
        if (!this.value.startsWith("tag:yaml.org,2002:")) {
            throw new YAMLException("Invalid tag: " + this.value);
        }
        return UriEncoder.decode(this.value.substring("tag:yaml.org,2002:".length()));
    }
    
    public int getLength() {
        return this.value.length();
    }
    
    @Override
    public String toString() {
        return this.value;
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Tag && this.value.equals(((Tag)obj).getValue());
    }
    
    @Override
    public int hashCode() {
        return this.value.hashCode();
    }
    
    public boolean isCompatible(final Class<?> clazz) {
        final Set<Class<?>> set = Tag.COMPATIBILITY_MAP.get(this);
        return set != null && set.contains(clazz);
    }
    
    public boolean matches(final Class<?> clazz) {
        return this.value.equals("tag:yaml.org,2002:" + clazz.getName());
    }
    
    @Override
    public int compareTo(final Tag o) {
        return this.value.compareTo(o.getValue());
    }
    
    static {
        YAML = new Tag("tag:yaml.org,2002:yaml");
        MERGE = new Tag("tag:yaml.org,2002:merge");
        SET = new Tag("tag:yaml.org,2002:set");
        PAIRS = new Tag("tag:yaml.org,2002:pairs");
        OMAP = new Tag("tag:yaml.org,2002:omap");
        BINARY = new Tag("tag:yaml.org,2002:binary");
        INT = new Tag("tag:yaml.org,2002:int");
        FLOAT = new Tag("tag:yaml.org,2002:float");
        TIMESTAMP = new Tag("tag:yaml.org,2002:timestamp");
        BOOL = new Tag("tag:yaml.org,2002:bool");
        NULL = new Tag("tag:yaml.org,2002:null");
        STR = new Tag("tag:yaml.org,2002:str");
        SEQ = new Tag("tag:yaml.org,2002:seq");
        MAP = new Tag("tag:yaml.org,2002:map");
        COMPATIBILITY_MAP = new HashMap<Tag, Set<Class<?>>>();
        final Set<Class<?>> floatSet = new HashSet<Class<?>>();
        floatSet.add(Double.class);
        floatSet.add(Float.class);
        floatSet.add(BigDecimal.class);
        Tag.COMPATIBILITY_MAP.put(Tag.FLOAT, floatSet);
        final Set<Class<?>> intSet = new HashSet<Class<?>>();
        intSet.add(Integer.class);
        intSet.add(Long.class);
        intSet.add(BigInteger.class);
        Tag.COMPATIBILITY_MAP.put(Tag.INT, intSet);
        final Set<Class<?>> timestampSet = new HashSet<Class<?>>();
        timestampSet.add(Date.class);
        timestampSet.add(java.sql.Date.class);
        timestampSet.add(Timestamp.class);
        Tag.COMPATIBILITY_MAP.put(Tag.TIMESTAMP, timestampSet);
    }
}
