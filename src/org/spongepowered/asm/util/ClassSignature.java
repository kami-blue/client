// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.util;

import org.spongepowered.asm.lib.signature.SignatureWriter;
import org.spongepowered.asm.lib.tree.ClassNode;
import java.util.Set;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Iterator;
import org.spongepowered.asm.lib.signature.SignatureVisitor;
import org.spongepowered.asm.lib.signature.SignatureReader;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class ClassSignature
{
    protected static final String OBJECT = "java/lang/Object";
    private final Map<TypeVar, TokenHandle> types;
    private Token superClass;
    private final List<Token> interfaces;
    private final Deque<String> rawInterfaces;
    
    ClassSignature() {
        this.types = new LinkedHashMap<TypeVar, TokenHandle>();
        this.superClass = new Token("java/lang/Object");
        this.interfaces = new ArrayList<Token>();
        this.rawInterfaces = new LinkedList<String>();
    }
    
    private ClassSignature read(final String signature) {
        if (signature != null) {
            try {
                new SignatureReader(signature).accept(new SignatureParser());
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return this;
    }
    
    protected TypeVar getTypeVar(final String varName) {
        for (final TypeVar typeVar : this.types.keySet()) {
            if (typeVar.matches(varName)) {
                return typeVar;
            }
        }
        return null;
    }
    
    protected TokenHandle getType(final String varName) {
        for (final TypeVar typeVar : this.types.keySet()) {
            if (typeVar.matches(varName)) {
                return this.types.get(typeVar);
            }
        }
        final TokenHandle handle = new TokenHandle();
        this.types.put(new TypeVar(varName), handle);
        return handle;
    }
    
    protected String getTypeVar(final TokenHandle handle) {
        for (final Map.Entry<TypeVar, TokenHandle> type : this.types.entrySet()) {
            final TypeVar typeVar = type.getKey();
            final TokenHandle typeHandle = type.getValue();
            if (handle == typeHandle || handle.asToken() == typeHandle.asToken()) {
                return "T" + typeVar + ";";
            }
        }
        return handle.token.asType();
    }
    
    protected void addTypeVar(final TypeVar typeVar, final TokenHandle handle) throws IllegalArgumentException {
        if (this.types.containsKey(typeVar)) {
            throw new IllegalArgumentException("TypeVar " + typeVar + " is already present on " + this);
        }
        this.types.put(typeVar, handle);
    }
    
    protected void setSuperClass(final Token superClass) {
        this.superClass = superClass;
    }
    
    public String getSuperClass() {
        return this.superClass.asType(true);
    }
    
    protected void addInterface(final Token iface) {
        if (!iface.isRaw()) {
            final String raw = iface.asType(true);
            final ListIterator<Token> iter = this.interfaces.listIterator();
            while (iter.hasNext()) {
                final Token intrface = iter.next();
                if (intrface.isRaw() && intrface.asType(true).equals(raw)) {
                    iter.set(iface);
                    return;
                }
            }
        }
        this.interfaces.add(iface);
    }
    
    public void addInterface(final String iface) {
        this.rawInterfaces.add(iface);
    }
    
    protected void addRawInterface(final String iface) {
        final Token token = new Token(iface);
        final String raw = token.asType(true);
        for (final Token intrface : this.interfaces) {
            if (intrface.asType(true).equals(raw)) {
                return;
            }
        }
        this.interfaces.add(token);
    }
    
    public void merge(final ClassSignature other) {
        try {
            final Set<String> typeVars = new HashSet<String>();
            for (final TypeVar typeVar : this.types.keySet()) {
                typeVars.add(typeVar.toString());
            }
            other.conform(typeVars);
        }
        catch (IllegalStateException ex) {
            ex.printStackTrace();
            return;
        }
        for (final Map.Entry<TypeVar, TokenHandle> type : other.types.entrySet()) {
            this.addTypeVar(type.getKey(), type.getValue());
        }
        for (final Token iface : other.interfaces) {
            this.addInterface(iface);
        }
    }
    
    private void conform(final Set<String> typeVars) {
        for (final TypeVar typeVar : this.types.keySet()) {
            final String name = this.findUniqueName(typeVar.getOriginalName(), typeVars);
            typeVar.rename(name);
            typeVars.add(name);
        }
    }
    
    private String findUniqueName(final String typeVar, final Set<String> typeVars) {
        if (!typeVars.contains(typeVar)) {
            return typeVar;
        }
        if (typeVar.length() == 1) {
            final String name = this.findOffsetName(typeVar.charAt(0), typeVars);
            if (name != null) {
                return name;
            }
        }
        String name = this.findOffsetName('T', typeVars, "", typeVar);
        if (name != null) {
            return name;
        }
        name = this.findOffsetName('T', typeVars, typeVar, "");
        if (name != null) {
            return name;
        }
        name = this.findOffsetName('T', typeVars, "T", typeVar);
        if (name != null) {
            return name;
        }
        name = this.findOffsetName('T', typeVars, "", typeVar + "Type");
        if (name != null) {
            return name;
        }
        throw new IllegalStateException("Failed to conform type var: " + typeVar);
    }
    
    private String findOffsetName(final char c, final Set<String> typeVars) {
        return this.findOffsetName(c, typeVars, "", "");
    }
    
    private String findOffsetName(final char c, final Set<String> typeVars, final String prefix, final String suffix) {
        String name = String.format("%s%s%s", prefix, c, suffix);
        if (!typeVars.contains(name)) {
            return name;
        }
        if (c > '@' && c < '[') {
            for (int s = c - '@'; s + 65 != c; s = ++s % 26) {
                name = String.format("%s%s%s", prefix, (char)(s + 65), suffix);
                if (!typeVars.contains(name)) {
                    return name;
                }
            }
        }
        return null;
    }
    
    public SignatureVisitor getRemapper() {
        return new SignatureRemapper();
    }
    
    @Override
    public String toString() {
        while (this.rawInterfaces.size() > 0) {
            this.addRawInterface(this.rawInterfaces.remove());
        }
        final StringBuilder sb = new StringBuilder();
        if (this.types.size() > 0) {
            boolean valid = false;
            final StringBuilder types = new StringBuilder();
            for (final Map.Entry<TypeVar, TokenHandle> type : this.types.entrySet()) {
                final String bound = type.getValue().asBound();
                if (!bound.isEmpty()) {
                    types.append(type.getKey()).append(':').append(bound);
                    valid = true;
                }
            }
            if (valid) {
                sb.append('<').append((CharSequence)types).append('>');
            }
        }
        sb.append(this.superClass.asType());
        for (final Token iface : this.interfaces) {
            sb.append(iface.asType());
        }
        return sb.toString();
    }
    
    public ClassSignature wake() {
        return this;
    }
    
    public static ClassSignature of(final String signature) {
        return new ClassSignature().read(signature);
    }
    
    public static ClassSignature of(final ClassNode classNode) {
        if (classNode.signature != null) {
            return of(classNode.signature);
        }
        return generate(classNode);
    }
    
    public static ClassSignature ofLazy(final ClassNode classNode) {
        if (classNode.signature != null) {
            return new Lazy(classNode.signature);
        }
        return generate(classNode);
    }
    
    private static ClassSignature generate(final ClassNode classNode) {
        final ClassSignature generated = new ClassSignature();
        generated.setSuperClass(new Token((classNode.superName != null) ? classNode.superName : "java/lang/Object"));
        for (final String iface : classNode.interfaces) {
            generated.addInterface(new Token(iface));
        }
        return generated;
    }
    
    static class Lazy extends ClassSignature
    {
        private final String sig;
        private ClassSignature generated;
        
        Lazy(final String sig) {
            this.sig = sig;
        }
        
        @Override
        public ClassSignature wake() {
            if (this.generated == null) {
                this.generated = ClassSignature.of(this.sig);
            }
            return this.generated;
        }
    }
    
    static class TypeVar implements Comparable<TypeVar>
    {
        private final String originalName;
        private String currentName;
        
        TypeVar(final String name) {
            this.originalName = name;
            this.currentName = name;
        }
        
        @Override
        public int compareTo(final TypeVar other) {
            return this.currentName.compareTo(other.currentName);
        }
        
        @Override
        public String toString() {
            return this.currentName;
        }
        
        String getOriginalName() {
            return this.originalName;
        }
        
        void rename(final String name) {
            this.currentName = name;
        }
        
        public boolean matches(final String originalName) {
            return this.originalName.equals(originalName);
        }
        
        @Override
        public boolean equals(final Object obj) {
            return this.currentName.equals(obj);
        }
        
        @Override
        public int hashCode() {
            return this.currentName.hashCode();
        }
    }
    
    static class Token implements IToken
    {
        static final String SYMBOLS = "+-*";
        private final boolean inner;
        private boolean array;
        private char symbol;
        private String type;
        private List<Token> classBound;
        private List<Token> ifaceBound;
        private List<IToken> signature;
        private List<IToken> suffix;
        private Token tail;
        
        Token() {
            this(false);
        }
        
        Token(final String type) {
            this(type, false);
        }
        
        Token(final char symbol) {
            this();
            this.symbol = symbol;
        }
        
        Token(final boolean inner) {
            this(null, inner);
        }
        
        Token(final String type, final boolean inner) {
            this.symbol = '\0';
            this.inner = inner;
            this.type = type;
        }
        
        Token setSymbol(final char symbol) {
            if (this.symbol == '\0' && "+-*".indexOf(symbol) > -1) {
                this.symbol = symbol;
            }
            return this;
        }
        
        Token setType(final String type) {
            if (this.type == null) {
                this.type = type;
            }
            return this;
        }
        
        boolean hasClassBound() {
            return this.classBound != null;
        }
        
        boolean hasInterfaceBound() {
            return this.ifaceBound != null;
        }
        
        @Override
        public IToken setArray(final boolean array) {
            this.array |= array;
            return this;
        }
        
        @Override
        public IToken setWildcard(final char wildcard) {
            if ("+-".indexOf(wildcard) == -1) {
                return this;
            }
            return this.setSymbol(wildcard);
        }
        
        private List<Token> getClassBound() {
            if (this.classBound == null) {
                this.classBound = new ArrayList<Token>();
            }
            return this.classBound;
        }
        
        private List<Token> getIfaceBound() {
            if (this.ifaceBound == null) {
                this.ifaceBound = new ArrayList<Token>();
            }
            return this.ifaceBound;
        }
        
        private List<IToken> getSignature() {
            if (this.signature == null) {
                this.signature = new ArrayList<IToken>();
            }
            return this.signature;
        }
        
        private List<IToken> getSuffix() {
            if (this.suffix == null) {
                this.suffix = new ArrayList<IToken>();
            }
            return this.suffix;
        }
        
        IToken addTypeArgument(final char symbol) {
            if (this.tail != null) {
                return this.tail.addTypeArgument(symbol);
            }
            final Token token = new Token(symbol);
            this.getSignature().add(token);
            return token;
        }
        
        IToken addTypeArgument(final String name) {
            if (this.tail != null) {
                return this.tail.addTypeArgument(name);
            }
            final Token token = new Token(name);
            this.getSignature().add(token);
            return token;
        }
        
        IToken addTypeArgument(final Token token) {
            if (this.tail != null) {
                return this.tail.addTypeArgument(token);
            }
            this.getSignature().add(token);
            return token;
        }
        
        IToken addTypeArgument(final TokenHandle token) {
            if (this.tail != null) {
                return this.tail.addTypeArgument(token);
            }
            final TokenHandle handle = token.clone();
            this.getSignature().add(handle);
            return handle;
        }
        
        Token addBound(final String bound, final boolean classBound) {
            if (classBound) {
                return this.addClassBound(bound);
            }
            return this.addInterfaceBound(bound);
        }
        
        Token addClassBound(final String bound) {
            final Token token = new Token(bound);
            this.getClassBound().add(token);
            return token;
        }
        
        Token addInterfaceBound(final String bound) {
            final Token token = new Token(bound);
            this.getIfaceBound().add(token);
            return token;
        }
        
        Token addInnerClass(final String name) {
            this.tail = new Token(name, true);
            this.getSuffix().add(this.tail);
            return this.tail;
        }
        
        @Override
        public String toString() {
            return this.asType();
        }
        
        @Override
        public String asBound() {
            final StringBuilder sb = new StringBuilder();
            if (this.type != null) {
                sb.append(this.type);
            }
            if (this.classBound != null) {
                for (final Token token : this.classBound) {
                    sb.append(token.asType());
                }
            }
            if (this.ifaceBound != null) {
                for (final Token token : this.ifaceBound) {
                    sb.append(':').append(token.asType());
                }
            }
            return sb.toString();
        }
        
        @Override
        public String asType() {
            return this.asType(false);
        }
        
        public String asType(final boolean raw) {
            final StringBuilder sb = new StringBuilder();
            if (this.array) {
                sb.append('[');
            }
            if (this.symbol != '\0') {
                sb.append(this.symbol);
            }
            if (this.type == null) {
                return sb.toString();
            }
            if (!this.inner) {
                sb.append('L');
            }
            sb.append(this.type);
            if (!raw) {
                if (this.signature != null) {
                    sb.append('<');
                    for (final IToken token : this.signature) {
                        sb.append(token.asType());
                    }
                    sb.append('>');
                }
                if (this.suffix != null) {
                    for (final IToken token : this.suffix) {
                        sb.append('.').append(token.asType());
                    }
                }
            }
            if (!this.inner) {
                sb.append(';');
            }
            return sb.toString();
        }
        
        boolean isRaw() {
            return this.signature == null;
        }
        
        String getClassType() {
            return (this.type != null) ? this.type : "java/lang/Object";
        }
        
        @Override
        public Token asToken() {
            return this;
        }
    }
    
    class TokenHandle implements IToken
    {
        final Token token;
        boolean array;
        char wildcard;
        
        TokenHandle(final ClassSignature this$0) {
            this(this$0, new Token());
        }
        
        TokenHandle(final Token token) {
            this.token = token;
        }
        
        @Override
        public IToken setArray(final boolean array) {
            this.array |= array;
            return this;
        }
        
        @Override
        public IToken setWildcard(final char wildcard) {
            if ("+-".indexOf(wildcard) > -1) {
                this.wildcard = wildcard;
            }
            return this;
        }
        
        @Override
        public String asBound() {
            return this.token.asBound();
        }
        
        @Override
        public String asType() {
            final StringBuilder sb = new StringBuilder();
            if (this.wildcard > '\0') {
                sb.append(this.wildcard);
            }
            if (this.array) {
                sb.append('[');
            }
            return sb.append(ClassSignature.this.getTypeVar(this)).toString();
        }
        
        @Override
        public Token asToken() {
            return this.token;
        }
        
        @Override
        public String toString() {
            return this.token.toString();
        }
        
        public TokenHandle clone() {
            return new TokenHandle(this.token);
        }
    }
    
    class SignatureParser extends SignatureVisitor
    {
        private FormalParamElement param;
        final /* synthetic */ ClassSignature this$0;
        
        SignatureParser() {
            super(327680);
        }
        
        @Override
        public void visitFormalTypeParameter(final String name) {
            this.param = new FormalParamElement(name);
        }
        
        @Override
        public SignatureVisitor visitClassBound() {
            return this.param.visitClassBound();
        }
        
        @Override
        public SignatureVisitor visitInterfaceBound() {
            return this.param.visitInterfaceBound();
        }
        
        @Override
        public SignatureVisitor visitSuperclass() {
            return new SuperClassElement();
        }
        
        @Override
        public SignatureVisitor visitInterface() {
            return new InterfaceElement();
        }
        
        abstract class SignatureElement extends SignatureVisitor
        {
            public SignatureElement() {
                super(327680);
            }
        }
        
        abstract class TokenElement extends SignatureElement
        {
            protected Token token;
            private boolean array;
            
            public Token getToken() {
                if (this.token == null) {
                    this.token = new Token();
                }
                return this.token;
            }
            
            protected void setArray() {
                this.array = true;
            }
            
            private boolean getArray() {
                final boolean array = this.array;
                this.array = false;
                return array;
            }
            
            @Override
            public void visitClassType(final String name) {
                this.getToken().setType(name);
            }
            
            @Override
            public SignatureVisitor visitClassBound() {
                this.getToken();
                return new BoundElement(this, true);
            }
            
            @Override
            public SignatureVisitor visitInterfaceBound() {
                this.getToken();
                return new BoundElement(this, false);
            }
            
            @Override
            public void visitInnerClassType(final String name) {
                this.token.addInnerClass(name);
            }
            
            @Override
            public SignatureVisitor visitArrayType() {
                this.setArray();
                return this;
            }
            
            @Override
            public SignatureVisitor visitTypeArgument(final char wildcard) {
                return new TypeArgElement(this, wildcard);
            }
            
            Token addTypeArgument() {
                return this.token.addTypeArgument('*').asToken();
            }
            
            IToken addTypeArgument(final char symbol) {
                return this.token.addTypeArgument(symbol).setArray(this.getArray());
            }
            
            IToken addTypeArgument(final String name) {
                return this.token.addTypeArgument(name).setArray(this.getArray());
            }
            
            IToken addTypeArgument(final Token token) {
                return this.token.addTypeArgument(token).setArray(this.getArray());
            }
            
            IToken addTypeArgument(final TokenHandle token) {
                return this.token.addTypeArgument(token).setArray(this.getArray());
            }
        }
        
        class FormalParamElement extends TokenElement
        {
            private final TokenHandle handle;
            
            FormalParamElement(final String param) {
                this.handle = SignatureParser.this.this$0.getType(param);
                this.token = this.handle.asToken();
            }
        }
        
        class TypeArgElement extends TokenElement
        {
            private final TokenElement type;
            private final char wildcard;
            
            TypeArgElement(final TokenElement type, final char wildcard) {
                this.type = type;
                this.wildcard = wildcard;
            }
            
            @Override
            public SignatureVisitor visitArrayType() {
                this.type.setArray();
                return this;
            }
            
            @Override
            public void visitBaseType(final char descriptor) {
                this.token = this.type.addTypeArgument(descriptor).asToken();
            }
            
            @Override
            public void visitTypeVariable(final String name) {
                final TokenHandle token = ClassSignature.this.getType(name);
                this.token = this.type.addTypeArgument(token).setWildcard(this.wildcard).asToken();
            }
            
            @Override
            public void visitClassType(final String name) {
                this.token = this.type.addTypeArgument(name).setWildcard(this.wildcard).asToken();
            }
            
            @Override
            public void visitTypeArgument() {
                this.token.addTypeArgument('*');
            }
            
            @Override
            public SignatureVisitor visitTypeArgument(final char wildcard) {
                return new TypeArgElement(this, wildcard);
            }
            
            @Override
            public void visitEnd() {
            }
        }
        
        class BoundElement extends TokenElement
        {
            private final TokenElement type;
            private final boolean classBound;
            
            BoundElement(final TokenElement type, final boolean classBound) {
                this.type = type;
                this.classBound = classBound;
            }
            
            @Override
            public void visitClassType(final String name) {
                this.token = this.type.token.addBound(name, this.classBound);
            }
            
            @Override
            public void visitTypeArgument() {
                this.token.addTypeArgument('*');
            }
            
            @Override
            public SignatureVisitor visitTypeArgument(final char wildcard) {
                return new TypeArgElement(this, wildcard);
            }
        }
        
        class SuperClassElement extends TokenElement
        {
            @Override
            public void visitEnd() {
                ClassSignature.this.setSuperClass(this.token);
            }
        }
        
        class InterfaceElement extends TokenElement
        {
            @Override
            public void visitEnd() {
                ClassSignature.this.addInterface(this.token);
            }
        }
    }
    
    class SignatureRemapper extends SignatureWriter
    {
        private final Set<String> localTypeVars;
        
        SignatureRemapper() {
            this.localTypeVars = new HashSet<String>();
        }
        
        @Override
        public void visitFormalTypeParameter(final String name) {
            this.localTypeVars.add(name);
            super.visitFormalTypeParameter(name);
        }
        
        @Override
        public void visitTypeVariable(final String name) {
            if (!this.localTypeVars.contains(name)) {
                final TypeVar typeVar = ClassSignature.this.getTypeVar(name);
                if (typeVar != null) {
                    super.visitTypeVariable(typeVar.toString());
                    return;
                }
            }
            super.visitTypeVariable(name);
        }
    }
    
    interface IToken
    {
        public static final String WILDCARDS = "+-";
        
        String asType();
        
        String asBound();
        
        Token asToken();
        
        IToken setArray(final boolean p0);
        
        IToken setWildcard(final char p0);
    }
}
