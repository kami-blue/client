// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import javassist.ClassPool;
import javassist.CtPrimitiveType;
import javassist.NotFoundException;
import java.util.Map;
import javassist.CtClass;

public class Descriptor
{
    public static String toJvmName(final String classname) {
        return classname.replace('.', '/');
    }
    
    public static String toJavaName(final String classname) {
        return classname.replace('/', '.');
    }
    
    public static String toJvmName(final CtClass clazz) {
        if (clazz.isArray()) {
            return of(clazz);
        }
        return toJvmName(clazz.getName());
    }
    
    public static String toClassName(final String descriptor) {
        int arrayDim = 0;
        int i;
        char c;
        for (i = 0, c = descriptor.charAt(0); c == '['; c = descriptor.charAt(++i)) {
            ++arrayDim;
        }
        String name;
        if (c == 'L') {
            final int i2 = descriptor.indexOf(59, i++);
            name = descriptor.substring(i, i2).replace('/', '.');
            i = i2;
        }
        else if (c == 'V') {
            name = "void";
        }
        else if (c == 'I') {
            name = "int";
        }
        else if (c == 'B') {
            name = "byte";
        }
        else if (c == 'J') {
            name = "long";
        }
        else if (c == 'D') {
            name = "double";
        }
        else if (c == 'F') {
            name = "float";
        }
        else if (c == 'C') {
            name = "char";
        }
        else if (c == 'S') {
            name = "short";
        }
        else {
            if (c != 'Z') {
                throw new RuntimeException("bad descriptor: " + descriptor);
            }
            name = "boolean";
        }
        if (i + 1 != descriptor.length()) {
            throw new RuntimeException("multiple descriptors?: " + descriptor);
        }
        if (arrayDim == 0) {
            return name;
        }
        final StringBuffer sbuf = new StringBuffer(name);
        do {
            sbuf.append("[]");
        } while (--arrayDim > 0);
        return sbuf.toString();
    }
    
    public static String of(final String classname) {
        if (classname.equals("void")) {
            return "V";
        }
        if (classname.equals("int")) {
            return "I";
        }
        if (classname.equals("byte")) {
            return "B";
        }
        if (classname.equals("long")) {
            return "J";
        }
        if (classname.equals("double")) {
            return "D";
        }
        if (classname.equals("float")) {
            return "F";
        }
        if (classname.equals("char")) {
            return "C";
        }
        if (classname.equals("short")) {
            return "S";
        }
        if (classname.equals("boolean")) {
            return "Z";
        }
        return "L" + toJvmName(classname) + ";";
    }
    
    public static String rename(final String desc, final String oldname, final String newname) {
        if (desc.indexOf(oldname) < 0) {
            return desc;
        }
        final StringBuffer newdesc = new StringBuffer();
        int head = 0;
        int i = 0;
        while (true) {
            final int j = desc.indexOf(76, i);
            if (j < 0) {
                break;
            }
            if (desc.startsWith(oldname, j + 1) && desc.charAt(j + oldname.length() + 1) == ';') {
                newdesc.append(desc.substring(head, j));
                newdesc.append('L');
                newdesc.append(newname);
                newdesc.append(';');
                i = (head = j + oldname.length() + 2);
            }
            else {
                i = desc.indexOf(59, j) + 1;
                if (i < 1) {
                    break;
                }
                continue;
            }
        }
        if (head == 0) {
            return desc;
        }
        final int len = desc.length();
        if (head < len) {
            newdesc.append(desc.substring(head, len));
        }
        return newdesc.toString();
    }
    
    public static String rename(final String desc, final Map map) {
        if (map == null) {
            return desc;
        }
        final StringBuffer newdesc = new StringBuffer();
        int head = 0;
        int i = 0;
        while (true) {
            final int j = desc.indexOf(76, i);
            if (j < 0) {
                break;
            }
            final int k = desc.indexOf(59, j);
            if (k < 0) {
                break;
            }
            i = k + 1;
            final String name = desc.substring(j + 1, k);
            final String name2 = map.get(name);
            if (name2 == null) {
                continue;
            }
            newdesc.append(desc.substring(head, j));
            newdesc.append('L');
            newdesc.append(name2);
            newdesc.append(';');
            head = i;
        }
        if (head == 0) {
            return desc;
        }
        final int len = desc.length();
        if (head < len) {
            newdesc.append(desc.substring(head, len));
        }
        return newdesc.toString();
    }
    
    public static String of(final CtClass type) {
        final StringBuffer sbuf = new StringBuffer();
        toDescriptor(sbuf, type);
        return sbuf.toString();
    }
    
    private static void toDescriptor(final StringBuffer desc, final CtClass type) {
        if (type.isArray()) {
            desc.append('[');
            try {
                toDescriptor(desc, type.getComponentType());
            }
            catch (NotFoundException e) {
                desc.append('L');
                final String name = type.getName();
                desc.append(toJvmName(name.substring(0, name.length() - 2)));
                desc.append(';');
            }
        }
        else if (type.isPrimitive()) {
            final CtPrimitiveType pt = (CtPrimitiveType)type;
            desc.append(pt.getDescriptor());
        }
        else {
            desc.append('L');
            desc.append(type.getName().replace('.', '/'));
            desc.append(';');
        }
    }
    
    public static String ofConstructor(final CtClass[] paramTypes) {
        return ofMethod(CtClass.voidType, paramTypes);
    }
    
    public static String ofMethod(final CtClass returnType, final CtClass[] paramTypes) {
        final StringBuffer desc = new StringBuffer();
        desc.append('(');
        if (paramTypes != null) {
            for (int n = paramTypes.length, i = 0; i < n; ++i) {
                toDescriptor(desc, paramTypes[i]);
            }
        }
        desc.append(')');
        if (returnType != null) {
            toDescriptor(desc, returnType);
        }
        return desc.toString();
    }
    
    public static String ofParameters(final CtClass[] paramTypes) {
        return ofMethod(null, paramTypes);
    }
    
    public static String appendParameter(final String classname, final String desc) {
        final int i = desc.indexOf(41);
        if (i < 0) {
            return desc;
        }
        final StringBuffer newdesc = new StringBuffer();
        newdesc.append(desc.substring(0, i));
        newdesc.append('L');
        newdesc.append(classname.replace('.', '/'));
        newdesc.append(';');
        newdesc.append(desc.substring(i));
        return newdesc.toString();
    }
    
    public static String insertParameter(final String classname, final String desc) {
        if (desc.charAt(0) != '(') {
            return desc;
        }
        return "(L" + classname.replace('.', '/') + ';' + desc.substring(1);
    }
    
    public static String appendParameter(final CtClass type, final String descriptor) {
        final int i = descriptor.indexOf(41);
        if (i < 0) {
            return descriptor;
        }
        final StringBuffer newdesc = new StringBuffer();
        newdesc.append(descriptor.substring(0, i));
        toDescriptor(newdesc, type);
        newdesc.append(descriptor.substring(i));
        return newdesc.toString();
    }
    
    public static String insertParameter(final CtClass type, final String descriptor) {
        if (descriptor.charAt(0) != '(') {
            return descriptor;
        }
        return "(" + of(type) + descriptor.substring(1);
    }
    
    public static String changeReturnType(final String classname, final String desc) {
        final int i = desc.indexOf(41);
        if (i < 0) {
            return desc;
        }
        final StringBuffer newdesc = new StringBuffer();
        newdesc.append(desc.substring(0, i + 1));
        newdesc.append('L');
        newdesc.append(classname.replace('.', '/'));
        newdesc.append(';');
        return newdesc.toString();
    }
    
    public static CtClass[] getParameterTypes(final String desc, final ClassPool cp) throws NotFoundException {
        if (desc.charAt(0) != '(') {
            return null;
        }
        final int num = numOfParameters(desc);
        final CtClass[] args = new CtClass[num];
        int n = 0;
        int i = 1;
        do {
            i = toCtClass(cp, desc, i, args, n++);
        } while (i > 0);
        return args;
    }
    
    public static boolean eqParamTypes(final String desc1, final String desc2) {
        if (desc1.charAt(0) != '(') {
            return false;
        }
        int i = 0;
        while (true) {
            final char c = desc1.charAt(i);
            if (c != desc2.charAt(i)) {
                return false;
            }
            if (c == ')') {
                return true;
            }
            ++i;
        }
    }
    
    public static String getParamDescriptor(final String decl) {
        return decl.substring(0, decl.indexOf(41) + 1);
    }
    
    public static CtClass getReturnType(final String desc, final ClassPool cp) throws NotFoundException {
        final int i = desc.indexOf(41);
        if (i < 0) {
            return null;
        }
        final CtClass[] type = { null };
        toCtClass(cp, desc, i + 1, type, 0);
        return type[0];
    }
    
    public static int numOfParameters(final String desc) {
        int n = 0;
        int i = 1;
        while (true) {
            char c = desc.charAt(i);
            if (c == ')') {
                return n;
            }
            while (c == '[') {
                c = desc.charAt(++i);
            }
            if (c == 'L') {
                i = desc.indexOf(59, i) + 1;
                if (i <= 0) {
                    throw new IndexOutOfBoundsException("bad descriptor");
                }
            }
            else {
                ++i;
            }
            ++n;
        }
    }
    
    public static CtClass toCtClass(final String desc, final ClassPool cp) throws NotFoundException {
        final CtClass[] clazz = { null };
        final int res = toCtClass(cp, desc, 0, clazz, 0);
        if (res >= 0) {
            return clazz[0];
        }
        return cp.get(desc.replace('/', '.'));
    }
    
    private static int toCtClass(final ClassPool cp, final String desc, int i, final CtClass[] args, final int n) throws NotFoundException {
        int arrayDim = 0;
        char c;
        for (c = desc.charAt(i); c == '['; c = desc.charAt(++i)) {
            ++arrayDim;
        }
        int i2;
        String name;
        if (c == 'L') {
            i2 = desc.indexOf(59, ++i);
            name = desc.substring(i, i2++).replace('/', '.');
        }
        else {
            final CtClass type = toPrimitiveClass(c);
            if (type == null) {
                return -1;
            }
            i2 = i + 1;
            if (arrayDim == 0) {
                args[n] = type;
                return i2;
            }
            name = type.getName();
        }
        if (arrayDim > 0) {
            final StringBuffer sbuf = new StringBuffer(name);
            while (arrayDim-- > 0) {
                sbuf.append("[]");
            }
            name = sbuf.toString();
        }
        args[n] = cp.get(name);
        return i2;
    }
    
    static CtClass toPrimitiveClass(final char c) {
        CtClass type = null;
        switch (c) {
            case 'Z': {
                type = CtClass.booleanType;
                break;
            }
            case 'C': {
                type = CtClass.charType;
                break;
            }
            case 'B': {
                type = CtClass.byteType;
                break;
            }
            case 'S': {
                type = CtClass.shortType;
                break;
            }
            case 'I': {
                type = CtClass.intType;
                break;
            }
            case 'J': {
                type = CtClass.longType;
                break;
            }
            case 'F': {
                type = CtClass.floatType;
                break;
            }
            case 'D': {
                type = CtClass.doubleType;
                break;
            }
            case 'V': {
                type = CtClass.voidType;
                break;
            }
        }
        return type;
    }
    
    public static int arrayDimension(final String desc) {
        int dim;
        for (dim = 0; desc.charAt(dim) == '['; ++dim) {}
        return dim;
    }
    
    public static String toArrayComponent(final String desc, final int dim) {
        return desc.substring(dim);
    }
    
    public static int dataSize(final String desc) {
        return dataSize(desc, true);
    }
    
    public static int paramSize(final String desc) {
        return -dataSize(desc, false);
    }
    
    private static int dataSize(final String desc, final boolean withRet) {
        int n = 0;
        char c = desc.charAt(0);
        if (c == '(') {
            int i = 1;
            while (true) {
                c = desc.charAt(i);
                if (c == ')') {
                    c = desc.charAt(i + 1);
                    break;
                }
                boolean array = false;
                while (c == '[') {
                    array = true;
                    c = desc.charAt(++i);
                }
                if (c == 'L') {
                    i = desc.indexOf(59, i) + 1;
                    if (i <= 0) {
                        throw new IndexOutOfBoundsException("bad descriptor");
                    }
                }
                else {
                    ++i;
                }
                if (!array && (c == 'J' || c == 'D')) {
                    n -= 2;
                }
                else {
                    --n;
                }
            }
        }
        if (withRet) {
            if (c == 'J' || c == 'D') {
                n += 2;
            }
            else if (c != 'V') {
                ++n;
            }
        }
        return n;
    }
    
    public static String toString(final String desc) {
        return PrettyPrinter.toString(desc);
    }
    
    static class PrettyPrinter
    {
        static String toString(final String desc) {
            final StringBuffer sbuf = new StringBuffer();
            if (desc.charAt(0) == '(') {
                int pos = 1;
                sbuf.append('(');
                while (desc.charAt(pos) != ')') {
                    if (pos > 1) {
                        sbuf.append(',');
                    }
                    pos = readType(sbuf, pos, desc);
                }
                sbuf.append(')');
            }
            else {
                readType(sbuf, 0, desc);
            }
            return sbuf.toString();
        }
        
        static int readType(final StringBuffer sbuf, int pos, final String desc) {
            char c = desc.charAt(pos);
            int arrayDim = 0;
            while (c == '[') {
                ++arrayDim;
                c = desc.charAt(++pos);
            }
            if (c == 'L') {
                while (true) {
                    c = desc.charAt(++pos);
                    if (c == ';') {
                        break;
                    }
                    if (c == '/') {
                        c = '.';
                    }
                    sbuf.append(c);
                }
            }
            else {
                final CtClass t = Descriptor.toPrimitiveClass(c);
                sbuf.append(t.getName());
            }
            while (arrayDim-- > 0) {
                sbuf.append("[]");
            }
            return pos + 1;
        }
    }
    
    public static class Iterator
    {
        private String desc;
        private int index;
        private int curPos;
        private boolean param;
        
        public Iterator(final String s) {
            this.desc = s;
            final int n = 0;
            this.curPos = n;
            this.index = n;
            this.param = false;
        }
        
        public boolean hasNext() {
            return this.index < this.desc.length();
        }
        
        public boolean isParameter() {
            return this.param;
        }
        
        public char currentChar() {
            return this.desc.charAt(this.curPos);
        }
        
        public boolean is2byte() {
            final char c = this.currentChar();
            return c == 'D' || c == 'J';
        }
        
        public int next() {
            int nextPos = this.index;
            char c = this.desc.charAt(nextPos);
            if (c == '(') {
                ++this.index;
                c = this.desc.charAt(++nextPos);
                this.param = true;
            }
            if (c == ')') {
                ++this.index;
                c = this.desc.charAt(++nextPos);
                this.param = false;
            }
            while (c == '[') {
                c = this.desc.charAt(++nextPos);
            }
            if (c == 'L') {
                nextPos = this.desc.indexOf(59, nextPos) + 1;
                if (nextPos <= 0) {
                    throw new IndexOutOfBoundsException("bad descriptor");
                }
            }
            else {
                ++nextPos;
            }
            this.curPos = this.index;
            this.index = nextPos;
            return this.curPos;
        }
    }
}
