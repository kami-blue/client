// 
// Decompiled by Procyon v0.5.36
// 

package org.reflections;

import org.reflections.scanners.MemberUsageScanner;
import java.lang.reflect.Member;
import org.reflections.scanners.MethodParameterNamesScanner;
import java.util.regex.Pattern;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.FieldAnnotationsScanner;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import org.reflections.scanners.MethodParameterScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import java.lang.reflect.Method;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import java.lang.annotation.Inherited;
import org.reflections.scanners.TypeAnnotationsScanner;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.HashMultimap;
import java.util.Set;
import com.google.common.collect.Sets;
import org.reflections.scanners.SubTypesScanner;
import com.google.common.collect.Multimap;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import org.reflections.util.Utils;
import java.io.IOException;
import org.reflections.util.ClasspathHelper;
import org.reflections.serializers.XmlSerializer;
import org.reflections.serializers.Serializer;
import org.reflections.util.FilterBuilder;
import com.google.common.base.Predicate;
import org.reflections.vfs.Vfs;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Future;
import java.net.URL;
import com.google.common.collect.Lists;
import com.google.common.base.Joiner;
import org.reflections.util.ConfigurationBuilder;
import java.util.Iterator;
import org.reflections.scanners.Scanner;
import javax.annotation.Nullable;
import org.slf4j.Logger;

public class Reflections
{
    @Nullable
    public static Logger log;
    protected final transient Configuration configuration;
    protected Store store;
    
    public Reflections(final Configuration configuration) {
        this.configuration = configuration;
        this.store = new Store(configuration);
        if (configuration.getScanners() != null && !configuration.getScanners().isEmpty()) {
            for (final Scanner scanner : configuration.getScanners()) {
                scanner.setConfiguration(configuration);
                scanner.setStore(this.store.getOrCreate(scanner.getClass().getSimpleName()));
            }
            this.scan();
            if (configuration.shouldExpandSuperTypes()) {
                this.expandSuperTypes();
            }
        }
    }
    
    public Reflections(final String prefix, @Nullable final Scanner... scanners) {
        this(new Object[] { prefix, scanners });
    }
    
    public Reflections(final Object... params) {
        this(ConfigurationBuilder.build(params));
    }
    
    protected Reflections() {
        this.configuration = new ConfigurationBuilder();
        this.store = new Store(this.configuration);
    }
    
    protected void scan() {
        if (this.configuration.getUrls() == null || this.configuration.getUrls().isEmpty()) {
            if (Reflections.log != null) {
                Reflections.log.warn("given scan urls are empty. set urls in the configuration");
            }
            return;
        }
        if (Reflections.log != null && Reflections.log.isDebugEnabled()) {
            Reflections.log.debug("going to scan these urls:\n" + Joiner.on("\n").join((Iterable)this.configuration.getUrls()));
        }
        long time = System.currentTimeMillis();
        int scannedUrls = 0;
        final ExecutorService executorService = this.configuration.getExecutorService();
        final List<Future<?>> futures = (List<Future<?>>)Lists.newArrayList();
        for (final URL url : this.configuration.getUrls()) {
            try {
                if (executorService != null) {
                    futures.add(executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            if (Reflections.log != null && Reflections.log.isDebugEnabled()) {
                                Reflections.log.debug("[" + Thread.currentThread().toString() + "] scanning " + url);
                            }
                            Reflections.this.scan(url);
                        }
                    }));
                }
                else {
                    this.scan(url);
                }
                ++scannedUrls;
            }
            catch (ReflectionsException e) {
                if (Reflections.log == null || !Reflections.log.isWarnEnabled()) {
                    continue;
                }
                Reflections.log.warn("could not create Vfs.Dir from url. ignoring the exception and continuing", (Throwable)e);
            }
        }
        if (executorService != null) {
            for (final Future future : futures) {
                try {
                    future.get();
                }
                catch (Exception e2) {
                    throw new RuntimeException(e2);
                }
            }
        }
        time = System.currentTimeMillis() - time;
        if (executorService != null) {
            executorService.shutdown();
        }
        if (Reflections.log != null) {
            int keys = 0;
            int values = 0;
            for (final String index : this.store.keySet()) {
                keys += this.store.get(index).keySet().size();
                values += this.store.get(index).size();
            }
            Reflections.log.info(String.format("Reflections took %d ms to scan %d urls, producing %d keys and %d values %s", time, scannedUrls, keys, values, (executorService != null && executorService instanceof ThreadPoolExecutor) ? String.format("[using %d cores]", ((ThreadPoolExecutor)executorService).getMaximumPoolSize()) : ""));
        }
    }
    
    protected void scan(final URL url) {
        final Vfs.Dir dir = Vfs.fromURL(url);
        try {
            for (final Vfs.File file : dir.getFiles()) {
                final Predicate<String> inputsFilter = this.configuration.getInputsFilter();
                final String path = file.getRelativePath();
                final String fqn = path.replace('/', '.');
                if (inputsFilter == null || inputsFilter.apply((Object)path) || inputsFilter.apply((Object)fqn)) {
                    Object classObject = null;
                    for (final Scanner scanner : this.configuration.getScanners()) {
                        try {
                            if (!scanner.acceptsInput(path) && !scanner.acceptResult(fqn)) {
                                continue;
                            }
                            classObject = scanner.scan(file, classObject);
                        }
                        catch (Exception e) {
                            if (Reflections.log == null || !Reflections.log.isDebugEnabled()) {
                                continue;
                            }
                            Reflections.log.debug("could not scan file " + file.getRelativePath() + " in url " + url.toExternalForm() + " with scanner " + scanner.getClass().getSimpleName(), (Throwable)e);
                        }
                    }
                }
            }
        }
        finally {
            dir.close();
        }
    }
    
    public static Reflections collect() {
        return collect("META-INF/reflections/", (Predicate<String>)new FilterBuilder().include(".*-reflections.xml"), new Serializer[0]);
    }
    
    public static Reflections collect(final String packagePrefix, final Predicate<String> resourceNameFilter, @Nullable final Serializer... optionalSerializer) {
        final Serializer serializer = (optionalSerializer != null && optionalSerializer.length == 1) ? optionalSerializer[0] : new XmlSerializer();
        final Collection<URL> urls = ClasspathHelper.forPackage(packagePrefix, new ClassLoader[0]);
        if (urls.isEmpty()) {
            return null;
        }
        final long start = System.currentTimeMillis();
        final Reflections reflections = new Reflections();
        final Iterable<Vfs.File> files = Vfs.findFiles(urls, packagePrefix, resourceNameFilter);
        for (final Vfs.File file : files) {
            InputStream inputStream = null;
            try {
                inputStream = file.openInputStream();
                reflections.merge(serializer.read(inputStream));
            }
            catch (IOException e) {
                throw new ReflectionsException("could not merge " + file, e);
            }
            finally {
                Utils.close(inputStream);
            }
        }
        if (Reflections.log != null) {
            final Store store = reflections.getStore();
            int keys = 0;
            int values = 0;
            for (final String index : store.keySet()) {
                keys += store.get(index).keySet().size();
                values += store.get(index).size();
            }
            Reflections.log.info(String.format("Reflections took %d ms to collect %d url%s, producing %d keys and %d values [%s]", System.currentTimeMillis() - start, urls.size(), (urls.size() > 1) ? "s" : "", keys, values, Joiner.on(", ").join((Iterable)urls)));
        }
        return reflections;
    }
    
    public Reflections collect(final InputStream inputStream) {
        try {
            this.merge(this.configuration.getSerializer().read(inputStream));
            if (Reflections.log != null) {
                Reflections.log.info("Reflections collected metadata from input stream using serializer " + this.configuration.getSerializer().getClass().getName());
            }
        }
        catch (Exception ex) {
            throw new ReflectionsException("could not merge input stream", ex);
        }
        return this;
    }
    
    public Reflections collect(final File file) {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            return this.collect(inputStream);
        }
        catch (FileNotFoundException e) {
            throw new ReflectionsException("could not obtain input stream from file " + file, e);
        }
        finally {
            Utils.close(inputStream);
        }
    }
    
    public Reflections merge(final Reflections reflections) {
        if (reflections.store != null) {
            for (final String indexName : reflections.store.keySet()) {
                final Multimap<String, String> index = reflections.store.get(indexName);
                for (final String key : index.keySet()) {
                    for (final String string : index.get((Object)key)) {
                        this.store.getOrCreate(indexName).put((Object)key, (Object)string);
                    }
                }
            }
        }
        return this;
    }
    
    public void expandSuperTypes() {
        if (this.store.keySet().contains(index(SubTypesScanner.class))) {
            final Multimap<String, String> mmap = this.store.get(index(SubTypesScanner.class));
            final Sets.SetView<String> keys = (Sets.SetView<String>)Sets.difference(mmap.keySet(), (Set)Sets.newHashSet((Iterable)mmap.values()));
            final Multimap<String, String> expand = (Multimap<String, String>)HashMultimap.create();
            for (final String key : keys) {
                final Class<?> type = ReflectionUtils.forName(key, new ClassLoader[0]);
                if (type != null) {
                    this.expandSupertypes(expand, key, type);
                }
            }
            mmap.putAll((Multimap)expand);
        }
    }
    
    private void expandSupertypes(final Multimap<String, String> mmap, final String key, final Class<?> type) {
        for (final Class<?> supertype : ReflectionUtils.getSuperTypes(type)) {
            if (mmap.put((Object)supertype.getName(), (Object)key)) {
                if (Reflections.log != null) {
                    Reflections.log.debug("expanded subtype {} -> {}", (Object)supertype.getName(), (Object)key);
                }
                this.expandSupertypes(mmap, supertype.getName(), supertype);
            }
        }
    }
    
    public <T> Set<Class<? extends T>> getSubTypesOf(final Class<T> type) {
        return (Set<Class<? extends T>>)Sets.newHashSet((Iterable)ReflectionUtils.forNames(this.store.getAll(index(SubTypesScanner.class), Arrays.asList(type.getName())), this.loaders()));
    }
    
    public Set<Class<?>> getTypesAnnotatedWith(final Class<? extends Annotation> annotation) {
        return this.getTypesAnnotatedWith(annotation, false);
    }
    
    public Set<Class<?>> getTypesAnnotatedWith(final Class<? extends Annotation> annotation, final boolean honorInherited) {
        final Iterable<String> annotated = this.store.get(index(TypeAnnotationsScanner.class), annotation.getName());
        final Iterable<String> classes = this.getAllAnnotated(annotated, annotation.isAnnotationPresent(Inherited.class), honorInherited);
        return (Set<Class<?>>)Sets.newHashSet(Iterables.concat((Iterable)ReflectionUtils.forNames(annotated, this.loaders()), (Iterable)ReflectionUtils.forNames(classes, this.loaders())));
    }
    
    public Set<Class<?>> getTypesAnnotatedWith(final Annotation annotation) {
        return this.getTypesAnnotatedWith(annotation, false);
    }
    
    public Set<Class<?>> getTypesAnnotatedWith(final Annotation annotation, final boolean honorInherited) {
        final Iterable<String> annotated = this.store.get(index(TypeAnnotationsScanner.class), annotation.annotationType().getName());
        final Iterable<Class<?>> filter = ReflectionUtils.filter(ReflectionUtils.forNames(annotated, this.loaders()), ReflectionUtils.withAnnotation(annotation));
        final Iterable<String> classes = this.getAllAnnotated(Utils.names(filter), annotation.annotationType().isAnnotationPresent(Inherited.class), honorInherited);
        return (Set<Class<?>>)Sets.newHashSet(Iterables.concat((Iterable)filter, (Iterable)ReflectionUtils.forNames(ReflectionUtils.filter(classes, Predicates.not(Predicates.in((Collection)Sets.newHashSet((Iterable)annotated)))), this.loaders())));
    }
    
    protected Iterable<String> getAllAnnotated(final Iterable<String> annotated, final boolean inherited, final boolean honorInherited) {
        if (!honorInherited) {
            final Iterable<String> subTypes = (Iterable<String>)Iterables.concat((Iterable)annotated, (Iterable)this.store.getAll(index(TypeAnnotationsScanner.class), annotated));
            return (Iterable<String>)Iterables.concat((Iterable)subTypes, (Iterable)this.store.getAll(index(SubTypesScanner.class), subTypes));
        }
        if (inherited) {
            final Iterable<String> subTypes = this.store.get(index(SubTypesScanner.class), ReflectionUtils.filter(annotated, (Predicate)new Predicate<String>() {
                public boolean apply(@Nullable final String input) {
                    final Class<?> type = ReflectionUtils.forName(input, Reflections.this.loaders());
                    return type != null && !type.isInterface();
                }
            }));
            return (Iterable<String>)Iterables.concat((Iterable)subTypes, (Iterable)this.store.getAll(index(SubTypesScanner.class), subTypes));
        }
        return annotated;
    }
    
    public Set<Method> getMethodsAnnotatedWith(final Class<? extends Annotation> annotation) {
        final Iterable<String> methods = this.store.get(index(MethodAnnotationsScanner.class), annotation.getName());
        return Utils.getMethodsFromDescriptors(methods, this.loaders());
    }
    
    public Set<Method> getMethodsAnnotatedWith(final Annotation annotation) {
        return ReflectionUtils.filter(this.getMethodsAnnotatedWith(annotation.annotationType()), ReflectionUtils.withAnnotation(annotation));
    }
    
    public Set<Method> getMethodsMatchParams(final Class<?>... types) {
        return Utils.getMethodsFromDescriptors(this.store.get(index(MethodParameterScanner.class), Utils.names(types).toString()), this.loaders());
    }
    
    public Set<Method> getMethodsReturn(final Class returnType) {
        return Utils.getMethodsFromDescriptors(this.store.get(index(MethodParameterScanner.class), Utils.names(returnType)), this.loaders());
    }
    
    public Set<Method> getMethodsWithAnyParamAnnotated(final Class<? extends Annotation> annotation) {
        return Utils.getMethodsFromDescriptors(this.store.get(index(MethodParameterScanner.class), annotation.getName()), this.loaders());
    }
    
    public Set<Method> getMethodsWithAnyParamAnnotated(final Annotation annotation) {
        return ReflectionUtils.filter(this.getMethodsWithAnyParamAnnotated(annotation.annotationType()), ReflectionUtils.withAnyParameterAnnotation(annotation));
    }
    
    public Set<Constructor> getConstructorsAnnotatedWith(final Class<? extends Annotation> annotation) {
        final Iterable<String> methods = this.store.get(index(MethodAnnotationsScanner.class), annotation.getName());
        return Utils.getConstructorsFromDescriptors(methods, this.loaders());
    }
    
    public Set<Constructor> getConstructorsAnnotatedWith(final Annotation annotation) {
        return (Set<Constructor>)ReflectionUtils.filter(this.getConstructorsAnnotatedWith(annotation.annotationType()), ReflectionUtils.withAnnotation(annotation));
    }
    
    public Set<Constructor> getConstructorsMatchParams(final Class<?>... types) {
        return Utils.getConstructorsFromDescriptors(this.store.get(index(MethodParameterScanner.class), Utils.names(types).toString()), this.loaders());
    }
    
    public Set<Constructor> getConstructorsWithAnyParamAnnotated(final Class<? extends Annotation> annotation) {
        return Utils.getConstructorsFromDescriptors(this.store.get(index(MethodParameterScanner.class), annotation.getName()), this.loaders());
    }
    
    public Set<Constructor> getConstructorsWithAnyParamAnnotated(final Annotation annotation) {
        return (Set<Constructor>)ReflectionUtils.filter(this.getConstructorsWithAnyParamAnnotated(annotation.annotationType()), ReflectionUtils.withAnyParameterAnnotation(annotation));
    }
    
    public Set<Field> getFieldsAnnotatedWith(final Class<? extends Annotation> annotation) {
        final Set<Field> result = (Set<Field>)Sets.newHashSet();
        for (final String annotated : this.store.get(index(FieldAnnotationsScanner.class), annotation.getName())) {
            result.add(Utils.getFieldFromString(annotated, this.loaders()));
        }
        return result;
    }
    
    public Set<Field> getFieldsAnnotatedWith(final Annotation annotation) {
        return ReflectionUtils.filter(this.getFieldsAnnotatedWith(annotation.annotationType()), ReflectionUtils.withAnnotation(annotation));
    }
    
    public Set<String> getResources(final Predicate<String> namePredicate) {
        final Iterable<String> resources = (Iterable<String>)Iterables.filter((Iterable)this.store.get(index(ResourcesScanner.class)).keySet(), (Predicate)namePredicate);
        return (Set<String>)Sets.newHashSet((Iterable)this.store.get(index(ResourcesScanner.class), resources));
    }
    
    public Set<String> getResources(final Pattern pattern) {
        return this.getResources((Predicate<String>)new Predicate<String>() {
            public boolean apply(final String input) {
                return pattern.matcher(input).matches();
            }
        });
    }
    
    public List<String> getMethodParamNames(final Method method) {
        final Iterable<String> names = this.store.get(index(MethodParameterNamesScanner.class), Utils.name(method));
        return Iterables.isEmpty((Iterable)names) ? Arrays.asList(new String[0]) : Arrays.asList(((String)Iterables.getOnlyElement((Iterable)names)).split(", "));
    }
    
    public List<String> getConstructorParamNames(final Constructor constructor) {
        final Iterable<String> names = this.store.get(index(MethodParameterNamesScanner.class), Utils.name(constructor));
        return Iterables.isEmpty((Iterable)names) ? Arrays.asList(new String[0]) : Arrays.asList(((String)Iterables.getOnlyElement((Iterable)names)).split(", "));
    }
    
    public Set<Member> getFieldUsage(final Field field) {
        return Utils.getMembersFromDescriptors(this.store.get(index(MemberUsageScanner.class), Utils.name(field)), new ClassLoader[0]);
    }
    
    public Set<Member> getMethodUsage(final Method method) {
        return Utils.getMembersFromDescriptors(this.store.get(index(MemberUsageScanner.class), Utils.name(method)), new ClassLoader[0]);
    }
    
    public Set<Member> getConstructorUsage(final Constructor constructor) {
        return Utils.getMembersFromDescriptors(this.store.get(index(MemberUsageScanner.class), Utils.name(constructor)), new ClassLoader[0]);
    }
    
    public Set<String> getAllTypes() {
        final Set<String> allTypes = (Set<String>)Sets.newHashSet((Iterable)this.store.getAll(index(SubTypesScanner.class), Object.class.getName()));
        if (allTypes.isEmpty()) {
            throw new ReflectionsException("Couldn't find subtypes of Object. Make sure SubTypesScanner initialized to include Object class - new SubTypesScanner(false)");
        }
        return allTypes;
    }
    
    public Store getStore() {
        return this.store;
    }
    
    public Configuration getConfiguration() {
        return this.configuration;
    }
    
    public File save(final String filename) {
        return this.save(filename, this.configuration.getSerializer());
    }
    
    public File save(final String filename, final Serializer serializer) {
        final File file = serializer.save(this, filename);
        if (Reflections.log != null) {
            Reflections.log.info("Reflections successfully saved in " + file.getAbsolutePath() + " using " + serializer.getClass().getSimpleName());
        }
        return file;
    }
    
    private static String index(final Class<? extends Scanner> scannerClass) {
        return scannerClass.getSimpleName();
    }
    
    private ClassLoader[] loaders() {
        return this.configuration.getClassLoaders();
    }
    
    static {
        Reflections.log = Utils.findLogger(Reflections.class);
    }
}
