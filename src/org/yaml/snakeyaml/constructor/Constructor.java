// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.constructor;

import java.util.ArrayList;
import java.util.UUID;
import java.util.Calendar;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.introspector.Property;
import java.util.List;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.NodeTuple;
import java.util.Set;
import org.yaml.snakeyaml.nodes.CollectionNode;
import java.util.Map;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.error.YAMLException;
import java.util.Iterator;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Tag;
import java.util.Collection;
import org.yaml.snakeyaml.TypeDescription;

public class Constructor extends SafeConstructor
{
    public Constructor() {
        this(Object.class);
    }
    
    public Constructor(final Class<?> theRoot) {
        this(new TypeDescription(checkRoot(theRoot)));
    }
    
    private static Class<?> checkRoot(final Class<?> theRoot) {
        if (theRoot == null) {
            throw new NullPointerException("Root class must be provided.");
        }
        return theRoot;
    }
    
    public Constructor(final TypeDescription theRoot) {
        this(theRoot, null);
    }
    
    public Constructor(final TypeDescription theRoot, final Collection<TypeDescription> moreTDs) {
        if (theRoot == null) {
            throw new NullPointerException("Root type must be provided.");
        }
        this.yamlConstructors.put(null, new ConstructYamlObject());
        if (!Object.class.equals(theRoot.getType())) {
            this.rootTag = new Tag(theRoot.getType());
        }
        this.yamlClassConstructors.put(NodeId.scalar, new ConstructScalar());
        this.yamlClassConstructors.put(NodeId.mapping, new ConstructMapping());
        this.yamlClassConstructors.put(NodeId.sequence, new ConstructSequence());
        this.addTypeDescription(theRoot);
        if (moreTDs != null) {
            for (final TypeDescription td : moreTDs) {
                this.addTypeDescription(td);
            }
        }
    }
    
    public Constructor(final String theRoot) throws ClassNotFoundException {
        this(Class.forName(check(theRoot)));
    }
    
    private static final String check(final String s) {
        if (s == null) {
            throw new NullPointerException("Root type must be provided.");
        }
        if (s.trim().length() == 0) {
            throw new YAMLException("Root type must be provided.");
        }
        return s;
    }
    
    protected Class<?> getClassForNode(final Node node) {
        final Class<?> classForTag = this.typeTags.get(node.getTag());
        if (classForTag == null) {
            final String name = node.getTag().getClassName();
            Class<?> cl;
            try {
                cl = this.getClassForName(name);
            }
            catch (ClassNotFoundException e) {
                throw new YAMLException("Class not found: " + name);
            }
            this.typeTags.put(node.getTag(), cl);
            return cl;
        }
        return classForTag;
    }
    
    protected Class<?> getClassForName(final String name) throws ClassNotFoundException {
        try {
            return Class.forName(name, true, Thread.currentThread().getContextClassLoader());
        }
        catch (ClassNotFoundException e) {
            return Class.forName(name);
        }
    }
    
    protected class ConstructMapping implements Construct
    {
        @Override
        public Object construct(final Node node) {
            final MappingNode mnode = (MappingNode)node;
            if (Map.class.isAssignableFrom(node.getType())) {
                if (node.isTwoStepsConstruction()) {
                    return Constructor.this.newMap(mnode);
                }
                return Constructor.this.constructMapping(mnode);
            }
            else if (Collection.class.isAssignableFrom(node.getType())) {
                if (node.isTwoStepsConstruction()) {
                    return Constructor.this.newSet(mnode);
                }
                return Constructor.this.constructSet(mnode);
            }
            else {
                final Object obj = Constructor.this.newInstance(mnode);
                if (node.isTwoStepsConstruction()) {
                    return obj;
                }
                return this.constructJavaBean2ndStep(mnode, obj);
            }
        }
        
        @Override
        public void construct2ndStep(final Node node, final Object object) {
            if (Map.class.isAssignableFrom(node.getType())) {
                Constructor.this.constructMapping2ndStep((MappingNode)node, (Map<Object, Object>)object);
            }
            else if (Set.class.isAssignableFrom(node.getType())) {
                Constructor.this.constructSet2ndStep((MappingNode)node, (Set<Object>)object);
            }
            else {
                this.constructJavaBean2ndStep((MappingNode)node, object);
            }
        }
        
        protected Object constructJavaBean2ndStep(final MappingNode node, final Object object) {
            Constructor.this.flattenMapping(node);
            final Class<?> beanType = node.getType();
            final List<NodeTuple> nodeValue = node.getValue();
            for (final NodeTuple tuple : nodeValue) {
                if (!(tuple.getKeyNode() instanceof ScalarNode)) {
                    throw new YAMLException("Keys must be scalars but found: " + tuple.getKeyNode());
                }
                final ScalarNode keyNode = (ScalarNode)tuple.getKeyNode();
                final Node valueNode = tuple.getValueNode();
                keyNode.setType(String.class);
                final String key = (String)Constructor.this.constructObject(keyNode);
                try {
                    final TypeDescription memberDescription = Constructor.this.typeDefinitions.get(beanType);
                    final Property property = (memberDescription == null) ? this.getProperty(beanType, key) : memberDescription.getProperty(key);
                    if (!property.isWritable()) {
                        throw new YAMLException("No writable property '" + key + "' on class: " + beanType.getName());
                    }
                    valueNode.setType(property.getType());
                    final boolean typeDetected = memberDescription != null && memberDescription.setupPropertyType(key, valueNode);
                    if (!typeDetected && valueNode.getNodeId() != NodeId.scalar) {
                        final Class<?>[] arguments = property.getActualTypeArguments();
                        if (arguments != null && arguments.length > 0) {
                            if (valueNode.getNodeId() == NodeId.sequence) {
                                final Class<?> t = arguments[0];
                                final SequenceNode snode = (SequenceNode)valueNode;
                                snode.setListType(t);
                            }
                            else if (Set.class.isAssignableFrom(valueNode.getType())) {
                                final Class<?> t = arguments[0];
                                final MappingNode mnode = (MappingNode)valueNode;
                                mnode.setOnlyKeyType(t);
                                mnode.setUseClassConstructor(true);
                            }
                            else if (Map.class.isAssignableFrom(valueNode.getType())) {
                                final Class<?> ketType = arguments[0];
                                final Class<?> valueType = arguments[1];
                                final MappingNode mnode2 = (MappingNode)valueNode;
                                mnode2.setTypes(ketType, valueType);
                                mnode2.setUseClassConstructor(true);
                            }
                        }
                    }
                    Object value = (memberDescription != null) ? this.newInstance(memberDescription, key, valueNode) : Constructor.this.constructObject(valueNode);
                    if ((property.getType() == Float.TYPE || property.getType() == Float.class) && value instanceof Double) {
                        value = ((Double)value).floatValue();
                    }
                    if (property.getType() == String.class && Tag.BINARY.equals(valueNode.getTag()) && value instanceof byte[]) {
                        value = new String((byte[])value);
                    }
                    if (memberDescription != null && memberDescription.setProperty(object, key, value)) {
                        continue;
                    }
                    property.set(object, value);
                }
                catch (Exception e) {
                    throw new ConstructorException("Cannot create property=" + key + " for JavaBean=" + object, node.getStartMark(), e.getMessage(), valueNode.getStartMark(), e);
                }
            }
            return object;
        }
        
        private Object newInstance(final TypeDescription memberDescription, final String propertyName, final Node node) {
            final Object newInstance = memberDescription.newInstance(propertyName, node);
            if (newInstance != null) {
                Constructor.this.constructedObjects.put(node, newInstance);
                return Constructor.this.constructObjectNoCheck(node);
            }
            return Constructor.this.constructObject(node);
        }
        
        protected Property getProperty(final Class<?> type, final String name) {
            return Constructor.this.getPropertyUtils().getProperty(type, name);
        }
    }
    
    protected class ConstructYamlObject implements Construct
    {
        private Construct getConstructor(final Node node) {
            final Class<?> cl = Constructor.this.getClassForNode(node);
            node.setType(cl);
            final Construct constructor = Constructor.this.yamlClassConstructors.get(node.getNodeId());
            return constructor;
        }
        
        @Override
        public Object construct(final Node node) {
            Object result = null;
            try {
                result = this.getConstructor(node).construct(node);
            }
            catch (ConstructorException e) {
                throw e;
            }
            catch (Exception e2) {
                throw new ConstructorException(null, null, "Can't construct a java object for " + node.getTag() + "; exception=" + e2.getMessage(), node.getStartMark(), e2);
            }
            return result;
        }
        
        @Override
        public void construct2ndStep(final Node node, final Object object) {
            try {
                this.getConstructor(node).construct2ndStep(node, object);
            }
            catch (Exception e) {
                throw new ConstructorException(null, null, "Can't construct a second step for a java object for " + node.getTag() + "; exception=" + e.getMessage(), node.getStartMark(), e);
            }
        }
    }
    
    protected class ConstructScalar extends AbstractConstruct
    {
        @Override
        public Object construct(final Node nnode) {
            final ScalarNode node = (ScalarNode)nnode;
            final Class<?> type = node.getType();
            try {
                return Constructor.this.newInstance(type, node, false);
            }
            catch (InstantiationException ex) {
                Object result;
                if (type.isPrimitive() || type == String.class || Number.class.isAssignableFrom(type) || type == Boolean.class || Date.class.isAssignableFrom(type) || type == Character.class || type == BigInteger.class || type == BigDecimal.class || Enum.class.isAssignableFrom(type) || Tag.BINARY.equals(node.getTag()) || Calendar.class.isAssignableFrom(type) || type == UUID.class) {
                    result = this.constructStandardJavaInstance(type, node);
                }
                else {
                    final java.lang.reflect.Constructor<?>[] javaConstructors = type.getDeclaredConstructors();
                    int oneArgCount = 0;
                    java.lang.reflect.Constructor<?> javaConstructor = null;
                    for (final java.lang.reflect.Constructor<?> c : javaConstructors) {
                        if (c.getParameterTypes().length == 1) {
                            ++oneArgCount;
                            javaConstructor = c;
                        }
                    }
                    if (javaConstructor == null) {
                        try {
                            return Constructor.this.newInstance(type, node, false);
                        }
                        catch (InstantiationException ie) {
                            throw new YAMLException("No single argument constructor found for " + type + " : " + ie.getMessage());
                        }
                    }
                    Object argument;
                    if (oneArgCount == 1) {
                        argument = this.constructStandardJavaInstance(javaConstructor.getParameterTypes()[0], node);
                    }
                    else {
                        argument = Constructor.this.constructScalar(node);
                        try {
                            javaConstructor = type.getDeclaredConstructor(String.class);
                        }
                        catch (Exception e) {
                            throw new YAMLException("Can't construct a java object for scalar " + node.getTag() + "; No String constructor found. Exception=" + e.getMessage(), e);
                        }
                    }
                    try {
                        javaConstructor.setAccessible(true);
                        result = javaConstructor.newInstance(argument);
                    }
                    catch (Exception e) {
                        throw new ConstructorException(null, null, "Can't construct a java object for scalar " + node.getTag() + "; exception=" + e.getMessage(), node.getStartMark(), e);
                    }
                }
                return result;
            }
        }
        
        private Object constructStandardJavaInstance(final Class type, final ScalarNode node) {
            Object result;
            if (type == String.class) {
                final Construct stringConstructor = Constructor.this.yamlConstructors.get(Tag.STR);
                result = stringConstructor.construct(node);
            }
            else if (type == Boolean.class || type == Boolean.TYPE) {
                final Construct boolConstructor = Constructor.this.yamlConstructors.get(Tag.BOOL);
                result = boolConstructor.construct(node);
            }
            else if (type == Character.class || type == Character.TYPE) {
                final Construct charConstructor = Constructor.this.yamlConstructors.get(Tag.STR);
                final String ch = (String)charConstructor.construct(node);
                if (ch.length() == 0) {
                    result = null;
                }
                else {
                    if (ch.length() != 1) {
                        throw new YAMLException("Invalid node Character: '" + ch + "'; length: " + ch.length());
                    }
                    result = ch.charAt(0);
                }
            }
            else if (Date.class.isAssignableFrom(type)) {
                final Construct dateConstructor = Constructor.this.yamlConstructors.get(Tag.TIMESTAMP);
                final Date date = (Date)dateConstructor.construct(node);
                if (type == Date.class) {
                    result = date;
                }
                else {
                    try {
                        final java.lang.reflect.Constructor<?> constr = type.getConstructor(Long.TYPE);
                        result = constr.newInstance(date.getTime());
                    }
                    catch (RuntimeException e) {
                        throw e;
                    }
                    catch (Exception e2) {
                        throw new YAMLException("Cannot construct: '" + type + "'");
                    }
                }
            }
            else if (type == Float.class || type == Double.class || type == Float.TYPE || type == Double.TYPE || type == BigDecimal.class) {
                if (type == BigDecimal.class) {
                    result = new BigDecimal(node.getValue());
                }
                else {
                    final Construct doubleConstructor = Constructor.this.yamlConstructors.get(Tag.FLOAT);
                    result = doubleConstructor.construct(node);
                    if (type == Float.class || type == Float.TYPE) {
                        result = new Float((double)result);
                    }
                }
            }
            else if (type == Byte.class || type == Short.class || type == Integer.class || type == Long.class || type == BigInteger.class || type == Byte.TYPE || type == Short.TYPE || type == Integer.TYPE || type == Long.TYPE) {
                final Construct intConstructor = Constructor.this.yamlConstructors.get(Tag.INT);
                result = intConstructor.construct(node);
                if (type == Byte.class || type == Byte.TYPE) {
                    result = Byte.valueOf(result.toString());
                }
                else if (type == Short.class || type == Short.TYPE) {
                    result = Short.valueOf(result.toString());
                }
                else if (type == Integer.class || type == Integer.TYPE) {
                    result = Integer.parseInt(result.toString());
                }
                else if (type == Long.class || type == Long.TYPE) {
                    result = Long.valueOf(result.toString());
                }
                else {
                    result = new BigInteger(result.toString());
                }
            }
            else if (Enum.class.isAssignableFrom(type)) {
                final String enumValueName = node.getValue();
                try {
                    result = Enum.valueOf((Class<Object>)type, enumValueName);
                }
                catch (Exception ex) {
                    throw new YAMLException("Unable to find enum value '" + enumValueName + "' for enum class: " + type.getName());
                }
            }
            else if (Calendar.class.isAssignableFrom(type)) {
                final ConstructYamlTimestamp contr = new ConstructYamlTimestamp();
                contr.construct(node);
                result = contr.getCalendar();
            }
            else if (Number.class.isAssignableFrom(type)) {
                final ConstructYamlFloat contr2 = new ConstructYamlFloat();
                result = contr2.construct(node);
            }
            else if (UUID.class == type) {
                result = UUID.fromString(node.getValue());
            }
            else {
                if (!Constructor.this.yamlConstructors.containsKey(node.getTag())) {
                    throw new YAMLException("Unsupported class: " + type);
                }
                result = Constructor.this.yamlConstructors.get(node.getTag()).construct(node);
            }
            return result;
        }
    }
    
    protected class ConstructSequence implements Construct
    {
        @Override
        public Object construct(final Node node) {
            final SequenceNode snode = (SequenceNode)node;
            if (Set.class.isAssignableFrom(node.getType())) {
                if (node.isTwoStepsConstruction()) {
                    throw new YAMLException("Set cannot be recursive.");
                }
                return Constructor.this.constructSet(snode);
            }
            else if (Collection.class.isAssignableFrom(node.getType())) {
                if (node.isTwoStepsConstruction()) {
                    return Constructor.this.newList(snode);
                }
                return Constructor.this.constructSequence(snode);
            }
            else {
                if (!node.getType().isArray()) {
                    final List<java.lang.reflect.Constructor<?>> possibleConstructors = new ArrayList<java.lang.reflect.Constructor<?>>(snode.getValue().size());
                    for (final java.lang.reflect.Constructor<?> constructor : node.getType().getDeclaredConstructors()) {
                        if (snode.getValue().size() == constructor.getParameterTypes().length) {
                            possibleConstructors.add(constructor);
                        }
                    }
                    if (!possibleConstructors.isEmpty()) {
                        if (possibleConstructors.size() == 1) {
                            final Object[] argumentList = new Object[snode.getValue().size()];
                            final java.lang.reflect.Constructor<?> c = possibleConstructors.get(0);
                            int index = 0;
                            for (final Node argumentNode : snode.getValue()) {
                                final Class<?> type = c.getParameterTypes()[index];
                                argumentNode.setType(type);
                                argumentList[index++] = Constructor.this.constructObject(argumentNode);
                            }
                            try {
                                c.setAccessible(true);
                                return c.newInstance(argumentList);
                            }
                            catch (Exception e) {
                                throw new YAMLException(e);
                            }
                        }
                        final List<Object> argumentList2 = (List<Object>)Constructor.this.constructSequence(snode);
                        final Class<?>[] parameterTypes = (Class<?>[])new Class[argumentList2.size()];
                        int index = 0;
                        for (final Object parameter : argumentList2) {
                            parameterTypes[index] = parameter.getClass();
                            ++index;
                        }
                        for (final java.lang.reflect.Constructor<?> c2 : possibleConstructors) {
                            final Class<?>[] argTypes = c2.getParameterTypes();
                            boolean foundConstructor = true;
                            for (int i = 0; i < argTypes.length; ++i) {
                                if (!this.wrapIfPrimitive(argTypes[i]).isAssignableFrom(parameterTypes[i])) {
                                    foundConstructor = false;
                                    break;
                                }
                            }
                            if (foundConstructor) {
                                try {
                                    c2.setAccessible(true);
                                    return c2.newInstance(argumentList2.toArray());
                                }
                                catch (Exception e2) {
                                    throw new YAMLException(e2);
                                }
                            }
                        }
                    }
                    throw new YAMLException("No suitable constructor with " + String.valueOf(snode.getValue().size()) + " arguments found for " + node.getType());
                }
                if (node.isTwoStepsConstruction()) {
                    return Constructor.this.createArray(node.getType(), snode.getValue().size());
                }
                return Constructor.this.constructArray(snode);
            }
        }
        
        private final Class<?> wrapIfPrimitive(final Class<?> clazz) {
            if (!clazz.isPrimitive()) {
                return clazz;
            }
            if (clazz == Integer.TYPE) {
                return Integer.class;
            }
            if (clazz == Float.TYPE) {
                return Float.class;
            }
            if (clazz == Double.TYPE) {
                return Double.class;
            }
            if (clazz == Boolean.TYPE) {
                return Boolean.class;
            }
            if (clazz == Long.TYPE) {
                return Long.class;
            }
            if (clazz == Character.TYPE) {
                return Character.class;
            }
            if (clazz == Short.TYPE) {
                return Short.class;
            }
            if (clazz == Byte.TYPE) {
                return Byte.class;
            }
            throw new YAMLException("Unexpected primitive " + clazz);
        }
        
        @Override
        public void construct2ndStep(final Node node, final Object object) {
            final SequenceNode snode = (SequenceNode)node;
            if (List.class.isAssignableFrom(node.getType())) {
                final List<Object> list = (List<Object>)object;
                Constructor.this.constructSequenceStep2(snode, list);
            }
            else {
                if (!node.getType().isArray()) {
                    throw new YAMLException("Immutable objects cannot be recursive.");
                }
                Constructor.this.constructArrayStep2(snode, object);
            }
        }
    }
}
