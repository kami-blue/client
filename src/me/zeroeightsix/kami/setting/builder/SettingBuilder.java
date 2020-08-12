// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.setting.builder;

import me.zeroeightsix.kami.setting.SettingsRegister;
import me.zeroeightsix.kami.setting.Setting;
import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class SettingBuilder<T>
{
    protected String name;
    protected T initialValue;
    protected BiConsumer<T, T> consumer;
    protected List<Predicate<T>> predicateList;
    private Predicate<T> visibilityPredicate;
    
    public SettingBuilder() {
        this.predicateList = new ArrayList<Predicate<T>>();
    }
    
    public SettingBuilder<T> withValue(final T value) {
        this.initialValue = value;
        return this;
    }
    
    protected Predicate<T> predicate() {
        return this.predicateList.isEmpty() ? (t -> true) : (t -> this.predicateList.stream().allMatch(tPredicate -> tPredicate.test(t)));
    }
    
    protected Predicate<T> visibilityPredicate() {
        return (Predicate<T>)MoreObjects.firstNonNull((Object)this.visibilityPredicate, t -> true);
    }
    
    protected BiConsumer<T, T> consumer() {
        return (BiConsumer<T, T>)MoreObjects.firstNonNull((Object)this.consumer, (a, b) -> {});
    }
    
    public SettingBuilder<T> withConsumer(final BiConsumer<T, T> consumer) {
        this.consumer = consumer;
        return this;
    }
    
    public SettingBuilder<T> withVisibility(final Predicate<T> predicate) {
        this.visibilityPredicate = predicate;
        return this;
    }
    
    public SettingBuilder<T> withName(final String name) {
        this.name = name;
        return this;
    }
    
    public SettingBuilder<T> withRestriction(final Predicate<T> predicate) {
        this.predicateList.add(predicate);
        return this;
    }
    
    public abstract Setting<T> build();
    
    public final Setting<T> buildAndRegister(final String group) {
        return register(this.build(), group);
    }
    
    public static <T> Setting<T> register(final Setting<T> setting, final String group) {
        final String name = setting.getName();
        if (name == null || name.isEmpty()) {
            throw new RuntimeException("Can't register nameless setting");
        }
        SettingsRegister.register(group + "." + name, setting);
        return setting;
    }
}
