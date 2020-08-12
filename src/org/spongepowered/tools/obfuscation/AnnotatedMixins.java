// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation;

import org.spongepowered.tools.obfuscation.mirror.TypeHandleSimulated;
import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Elements;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationMirror;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.Messager;
import org.spongepowered.tools.obfuscation.struct.InjectorRemap;
import javax.lang.model.element.VariableElement;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.gen.Invoker;
import java.lang.annotation.Annotation;
import org.spongepowered.tools.obfuscation.mirror.AnnotationHandle;
import org.spongepowered.asm.mixin.gen.Accessor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import org.spongepowered.tools.obfuscation.mirror.TypeHandle;
import java.util.Iterator;
import org.spongepowered.tools.obfuscation.mirror.TypeReference;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import javax.lang.model.element.TypeElement;
import java.io.InputStream;
import javax.tools.FileObject;
import javax.annotation.processing.Filer;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.File;
import com.google.common.collect.ImmutableList;
import org.spongepowered.tools.obfuscation.validation.TargetValidator;
import org.spongepowered.tools.obfuscation.validation.ParentValidator;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import org.spongepowered.tools.obfuscation.interfaces.IMixinValidator;
import org.spongepowered.tools.obfuscation.interfaces.IObfuscationManager;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import java.util.Map;
import org.spongepowered.tools.obfuscation.interfaces.IJavadocProvider;
import org.spongepowered.tools.obfuscation.interfaces.ITypeHandleProvider;
import org.spongepowered.asm.util.ITokenProvider;
import org.spongepowered.tools.obfuscation.interfaces.IMixinAnnotationProcessor;

final class AnnotatedMixins implements IMixinAnnotationProcessor, ITokenProvider, ITypeHandleProvider, IJavadocProvider
{
    private static final String MAPID_SYSTEM_PROPERTY = "mixin.target.mapid";
    private static Map<ProcessingEnvironment, AnnotatedMixins> instances;
    private final CompilerEnvironment env;
    private final ProcessingEnvironment processingEnv;
    private final Map<String, AnnotatedMixin> mixins;
    private final List<AnnotatedMixin> mixinsForPass;
    private final IObfuscationManager obf;
    private final List<IMixinValidator> validators;
    private final Map<String, Integer> tokenCache;
    private final TargetMap targets;
    private Properties properties;
    
    private AnnotatedMixins(final ProcessingEnvironment processingEnv) {
        this.mixins = new HashMap<String, AnnotatedMixin>();
        this.mixinsForPass = new ArrayList<AnnotatedMixin>();
        this.tokenCache = new HashMap<String, Integer>();
        this.env = this.detectEnvironment(processingEnv);
        this.processingEnv = processingEnv;
        this.printMessage(Diagnostic.Kind.NOTE, "SpongePowered MIXIN Annotation Processor Version=0.7.4");
        this.targets = this.initTargetMap();
        (this.obf = new ObfuscationManager(this)).init();
        this.validators = (List<IMixinValidator>)ImmutableList.of((Object)new ParentValidator(this), (Object)new TargetValidator(this));
        this.initTokenCache(this.getOption("tokens"));
    }
    
    protected TargetMap initTargetMap() {
        final TargetMap targets = TargetMap.create(System.getProperty("mixin.target.mapid"));
        System.setProperty("mixin.target.mapid", targets.getSessionId());
        final String targetsFileName = this.getOption("dependencyTargetsFile");
        if (targetsFileName != null) {
            try {
                targets.readImports(new File(targetsFileName));
            }
            catch (IOException ex) {
                this.printMessage(Diagnostic.Kind.WARNING, "Could not read from specified imports file: " + targetsFileName);
            }
        }
        return targets;
    }
    
    private void initTokenCache(final String tokens) {
        if (tokens != null) {
            final Pattern tokenPattern = Pattern.compile("^([A-Z0-9\\-_\\.]+)=([0-9]+)$");
            final String[] split;
            final String[] tokenValues = split = tokens.replaceAll("\\s", "").toUpperCase().split("[;,]");
            for (final String tokenValue : split) {
                final Matcher tokenMatcher = tokenPattern.matcher(tokenValue);
                if (tokenMatcher.matches()) {
                    this.tokenCache.put(tokenMatcher.group(1), Integer.parseInt(tokenMatcher.group(2)));
                }
            }
        }
    }
    
    @Override
    public ITypeHandleProvider getTypeProvider() {
        return this;
    }
    
    @Override
    public ITokenProvider getTokenProvider() {
        return this;
    }
    
    @Override
    public IObfuscationManager getObfuscationManager() {
        return this.obf;
    }
    
    @Override
    public IJavadocProvider getJavadocProvider() {
        return this;
    }
    
    @Override
    public ProcessingEnvironment getProcessingEnvironment() {
        return this.processingEnv;
    }
    
    @Override
    public CompilerEnvironment getCompilerEnvironment() {
        return this.env;
    }
    
    @Override
    public Integer getToken(final String token) {
        if (this.tokenCache.containsKey(token)) {
            return this.tokenCache.get(token);
        }
        final String option = this.getOption(token);
        Integer value = null;
        try {
            value = Integer.parseInt(option);
        }
        catch (Exception ex) {}
        this.tokenCache.put(token, value);
        return value;
    }
    
    @Override
    public String getOption(final String option) {
        if (option == null) {
            return null;
        }
        final String value = this.processingEnv.getOptions().get(option);
        if (value != null) {
            return value;
        }
        return this.getProperties().getProperty(option);
    }
    
    public Properties getProperties() {
        if (this.properties == null) {
            this.properties = new Properties();
            try {
                final Filer filer = this.processingEnv.getFiler();
                final FileObject propertyFile = filer.getResource(StandardLocation.SOURCE_PATH, "", "mixin.properties");
                if (propertyFile != null) {
                    final InputStream inputStream = propertyFile.openInputStream();
                    this.properties.load(inputStream);
                    inputStream.close();
                }
            }
            catch (Exception ex) {}
        }
        return this.properties;
    }
    
    private CompilerEnvironment detectEnvironment(final ProcessingEnvironment processingEnv) {
        if (processingEnv.getClass().getName().contains("jdt")) {
            return CompilerEnvironment.JDT;
        }
        return CompilerEnvironment.JAVAC;
    }
    
    public void writeMappings() {
        this.obf.writeMappings();
    }
    
    public void writeReferences() {
        this.obf.writeReferences();
    }
    
    public void clear() {
        this.mixins.clear();
    }
    
    public void registerMixin(final TypeElement mixinType) {
        final String name = mixinType.getQualifiedName().toString();
        if (!this.mixins.containsKey(name)) {
            final AnnotatedMixin mixin = new AnnotatedMixin(this, mixinType);
            this.targets.registerTargets(mixin);
            mixin.runValidators(IMixinValidator.ValidationPass.EARLY, this.validators);
            this.mixins.put(name, mixin);
            this.mixinsForPass.add(mixin);
        }
    }
    
    public AnnotatedMixin getMixin(final TypeElement mixinType) {
        return this.getMixin(mixinType.getQualifiedName().toString());
    }
    
    public AnnotatedMixin getMixin(final String mixinType) {
        return this.mixins.get(mixinType);
    }
    
    public Collection<TypeMirror> getMixinsTargeting(final TypeMirror targetType) {
        return this.getMixinsTargeting((TypeElement)((DeclaredType)targetType).asElement());
    }
    
    public Collection<TypeMirror> getMixinsTargeting(final TypeElement targetType) {
        final List<TypeMirror> minions = new ArrayList<TypeMirror>();
        for (final TypeReference mixin : this.targets.getMixinsTargeting(targetType)) {
            final TypeHandle handle = mixin.getHandle(this.processingEnv);
            if (handle != null) {
                minions.add(handle.getType());
            }
        }
        return minions;
    }
    
    public void registerAccessor(final TypeElement mixinType, final ExecutableElement method) {
        final AnnotatedMixin mixinClass = this.getMixin(mixinType);
        if (mixinClass == null) {
            this.printMessage(Diagnostic.Kind.ERROR, "Found @Accessor annotation on a non-mixin method", method);
            return;
        }
        final AnnotationHandle accessor = AnnotationHandle.of(method, Accessor.class);
        mixinClass.registerAccessor(method, accessor, this.shouldRemap(mixinClass, accessor));
    }
    
    public void registerInvoker(final TypeElement mixinType, final ExecutableElement method) {
        final AnnotatedMixin mixinClass = this.getMixin(mixinType);
        if (mixinClass == null) {
            this.printMessage(Diagnostic.Kind.ERROR, "Found @Accessor annotation on a non-mixin method", method);
            return;
        }
        final AnnotationHandle invoker = AnnotationHandle.of(method, Invoker.class);
        mixinClass.registerInvoker(method, invoker, this.shouldRemap(mixinClass, invoker));
    }
    
    public void registerOverwrite(final TypeElement mixinType, final ExecutableElement method) {
        final AnnotatedMixin mixinClass = this.getMixin(mixinType);
        if (mixinClass == null) {
            this.printMessage(Diagnostic.Kind.ERROR, "Found @Overwrite annotation on a non-mixin method", method);
            return;
        }
        final AnnotationHandle overwrite = AnnotationHandle.of(method, Overwrite.class);
        mixinClass.registerOverwrite(method, overwrite, this.shouldRemap(mixinClass, overwrite));
    }
    
    public void registerShadow(final TypeElement mixinType, final VariableElement field, final AnnotationHandle shadow) {
        final AnnotatedMixin mixinClass = this.getMixin(mixinType);
        if (mixinClass == null) {
            this.printMessage(Diagnostic.Kind.ERROR, "Found @Shadow annotation on a non-mixin field", field);
            return;
        }
        mixinClass.registerShadow(field, shadow, this.shouldRemap(mixinClass, shadow));
    }
    
    public void registerShadow(final TypeElement mixinType, final ExecutableElement method, final AnnotationHandle shadow) {
        final AnnotatedMixin mixinClass = this.getMixin(mixinType);
        if (mixinClass == null) {
            this.printMessage(Diagnostic.Kind.ERROR, "Found @Shadow annotation on a non-mixin method", method);
            return;
        }
        mixinClass.registerShadow(method, shadow, this.shouldRemap(mixinClass, shadow));
    }
    
    public void registerInjector(final TypeElement mixinType, final ExecutableElement method, final AnnotationHandle inject) {
        final AnnotatedMixin mixinClass = this.getMixin(mixinType);
        if (mixinClass == null) {
            this.printMessage(Diagnostic.Kind.ERROR, "Found " + inject + " annotation on a non-mixin method", method);
            return;
        }
        final InjectorRemap remap = new InjectorRemap(this.shouldRemap(mixinClass, inject));
        mixinClass.registerInjector(method, inject, remap);
        remap.dispatchPendingMessages(this);
    }
    
    public void registerSoftImplements(final TypeElement mixin, final AnnotationHandle implementsAnnotation) {
        final AnnotatedMixin mixinClass = this.getMixin(mixin);
        if (mixinClass == null) {
            this.printMessage(Diagnostic.Kind.ERROR, "Found @Implements annotation on a non-mixin class");
            return;
        }
        mixinClass.registerSoftImplements(implementsAnnotation);
    }
    
    public void onPassStarted() {
        this.mixinsForPass.clear();
    }
    
    public void onPassCompleted(final RoundEnvironment roundEnv) {
        if (!"true".equalsIgnoreCase(this.getOption("disableTargetExport"))) {
            this.targets.write(true);
        }
        for (final AnnotatedMixin mixin : roundEnv.processingOver() ? this.mixins.values() : this.mixinsForPass) {
            mixin.runValidators(roundEnv.processingOver() ? IMixinValidator.ValidationPass.FINAL : IMixinValidator.ValidationPass.LATE, this.validators);
        }
    }
    
    private boolean shouldRemap(final AnnotatedMixin mixinClass, final AnnotationHandle annotation) {
        return annotation.getBoolean("remap", mixinClass.remap());
    }
    
    @Override
    public void printMessage(final Diagnostic.Kind kind, final CharSequence msg) {
        if (this.env == CompilerEnvironment.JAVAC || kind != Diagnostic.Kind.NOTE) {
            this.processingEnv.getMessager().printMessage(kind, msg);
        }
    }
    
    @Override
    public void printMessage(final Diagnostic.Kind kind, final CharSequence msg, final Element element) {
        this.processingEnv.getMessager().printMessage(kind, msg, element);
    }
    
    @Override
    public void printMessage(final Diagnostic.Kind kind, final CharSequence msg, final Element element, final AnnotationMirror annotation) {
        this.processingEnv.getMessager().printMessage(kind, msg, element, annotation);
    }
    
    @Override
    public void printMessage(final Diagnostic.Kind kind, final CharSequence msg, final Element element, final AnnotationMirror annotation, final AnnotationValue value) {
        this.processingEnv.getMessager().printMessage(kind, msg, element, annotation, value);
    }
    
    @Override
    public TypeHandle getTypeHandle(String name) {
        name = name.replace('/', '.');
        final Elements elements = this.processingEnv.getElementUtils();
        final TypeElement element = elements.getTypeElement(name);
        if (element != null) {
            try {
                return new TypeHandle(element);
            }
            catch (NullPointerException ex) {}
        }
        final int lastDotPos = name.lastIndexOf(46);
        if (lastDotPos > -1) {
            final String pkg = name.substring(0, lastDotPos);
            final PackageElement packageElement = elements.getPackageElement(pkg);
            if (packageElement != null) {
                return new TypeHandle(packageElement, name);
            }
        }
        return null;
    }
    
    @Override
    public TypeHandle getSimulatedHandle(String name, final TypeMirror simulatedTarget) {
        name = name.replace('/', '.');
        final int lastDotPos = name.lastIndexOf(46);
        if (lastDotPos > -1) {
            final String pkg = name.substring(0, lastDotPos);
            final PackageElement packageElement = this.processingEnv.getElementUtils().getPackageElement(pkg);
            if (packageElement != null) {
                return new TypeHandleSimulated(packageElement, name, simulatedTarget);
            }
        }
        return new TypeHandleSimulated(name, simulatedTarget);
    }
    
    @Override
    public String getJavadoc(final Element element) {
        final Elements elements = this.processingEnv.getElementUtils();
        return elements.getDocComment(element);
    }
    
    public static AnnotatedMixins getMixinsForEnvironment(final ProcessingEnvironment processingEnv) {
        AnnotatedMixins mixins = AnnotatedMixins.instances.get(processingEnv);
        if (mixins == null) {
            mixins = new AnnotatedMixins(processingEnv);
            AnnotatedMixins.instances.put(processingEnv, mixins);
        }
        return mixins;
    }
    
    static {
        AnnotatedMixins.instances = new HashMap<ProcessingEnvironment, AnnotatedMixins>();
    }
}
