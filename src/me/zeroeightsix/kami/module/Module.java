// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import org.lwjgl.input.Keyboard;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonElement;
import me.zeroeightsix.kami.setting.builder.SettingBuilder;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.RenderEvent;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import com.google.common.base.Converter;
import me.zeroeightsix.kami.setting.Settings;
import java.util.List;
import net.minecraft.client.Minecraft;
import me.zeroeightsix.kami.util.Bind;
import me.zeroeightsix.kami.setting.Setting;

public class Module
{
    private final String originalName;
    private final Category category;
    private final String description;
    private final Setting<String> name;
    private Setting<Bind> bind;
    private Setting<Boolean> enabled;
    private Setting<ShowOnArray> showOnArray;
    public boolean alwaysListening;
    protected static final Minecraft mc;
    public List<Setting> settingList;
    
    public Module() {
        this.originalName = this.getAnnotation().name();
        this.category = this.getAnnotation().category();
        this.description = this.getAnnotation().description();
        this.name = this.register(Settings.s("Name", this.originalName));
        this.bind = this.register(Settings.custom("Bind", Bind.none(), new BindConverter()).build());
        this.enabled = this.register(Settings.booleanBuilder("Enabled").withVisibility(aBoolean -> false).withValue(false).build());
        this.showOnArray = this.register(Settings.e("Visible", this.getAnnotation().showOnArray()));
        this.settingList = new ArrayList<Setting>();
        this.alwaysListening = this.getAnnotation().alwaysListening();
        this.registerAll(this.bind, this.enabled, this.showOnArray);
    }
    
    private Info getAnnotation() {
        if (this.getClass().isAnnotationPresent(Info.class)) {
            return this.getClass().getAnnotation(Info.class);
        }
        throw new IllegalStateException("No Annotation on class " + this.getClass().getCanonicalName() + "!");
    }
    
    public void onUpdate() {
    }
    
    public void onRender() {
    }
    
    public void onWorldRender(final RenderEvent event) {
    }
    
    public Bind getBind() {
        return this.bind.getValue();
    }
    
    public ShowOnArray getShowOnArray() {
        return this.showOnArray.getValue();
    }
    
    public String getBindName() {
        return this.bind.getValue().toString();
    }
    
    public void setName(final String name) {
        this.name.setValue(name);
    }
    
    public String getOriginalName() {
        return this.originalName;
    }
    
    public String getName() {
        return this.name.getValue();
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public Category getCategory() {
        return this.category;
    }
    
    public boolean isEnabled() {
        return this.enabled.getValue();
    }
    
    protected void onEnable() {
    }
    
    protected void onDisable() {
    }
    
    public void toggle() {
        this.setEnabled(!this.isEnabled());
    }
    
    public void enable() {
        this.enabled.setValue(true);
        this.onEnable();
        if (!this.alwaysListening) {
            KamiMod.EVENT_BUS.subscribe(this);
        }
    }
    
    public void disable() {
        this.enabled.setValue(false);
        this.onDisable();
        if (!this.alwaysListening) {
            KamiMod.EVENT_BUS.unsubscribe(this);
        }
    }
    
    public boolean isDisabled() {
        return !this.isEnabled();
    }
    
    public void setEnabled(final boolean enabled) {
        final boolean prev = this.enabled.getValue();
        if (prev != enabled) {
            if (enabled) {
                this.enable();
            }
            else {
                this.disable();
            }
        }
    }
    
    public String getHudInfo() {
        return null;
    }
    
    protected final void setAlwaysListening(final boolean alwaysListening) {
        this.alwaysListening = alwaysListening;
        if (alwaysListening) {
            KamiMod.EVENT_BUS.subscribe(this);
        }
        if (!alwaysListening && this.isDisabled()) {
            KamiMod.EVENT_BUS.unsubscribe(this);
        }
    }
    
    public void destroy() {
    }
    
    protected void registerAll(final Setting... settings) {
        for (final Setting setting : settings) {
            this.register((Setting<Object>)setting);
        }
    }
    
    protected <T> Setting<T> register(final Setting<T> setting) {
        if (this.settingList == null) {
            this.settingList = new ArrayList<Setting>();
        }
        this.settingList.add(setting);
        return SettingBuilder.register(setting, "modules." + this.originalName);
    }
    
    protected <T> Setting<T> register(final SettingBuilder<T> builder) {
        if (this.settingList == null) {
            this.settingList = new ArrayList<Setting>();
        }
        final Setting<T> setting = builder.buildAndRegister("modules." + this.name);
        this.settingList.add(setting);
        return setting;
    }
    
    static {
        mc = Minecraft.func_71410_x();
    }
    
    public enum ShowOnArray
    {
        ON, 
        OFF;
    }
    
    public enum Category
    {
        COMBAT("Combat", false), 
        EXPLOITS("Exploits", false), 
        RENDER("Render", false), 
        MISC("Misc", false), 
        PLAYER("Player", false), 
        MOVEMENT("Movement", false), 
        EXPERIMENTAL("Experimental", false), 
        GUI("GUI", false), 
        CHAT("Chat", false), 
        DEV("Dev", false), 
        SOMETHING("Something", false), 
        CRYSTAL("Crystal", false), 
        HIDDEN("Hidden", true);
        
        boolean hidden;
        String name;
        
        private Category(final String name, final boolean hidden) {
            this.name = name;
            this.hidden = hidden;
        }
        
        public boolean isHidden() {
            return this.hidden;
        }
        
        public String getName() {
            return this.name;
        }
    }
    
    private class BindConverter extends Converter<Bind, JsonElement>
    {
        protected JsonElement doForward(final Bind bind) {
            return (JsonElement)new JsonPrimitive(bind.toString());
        }
        
        protected Bind doBackward(final JsonElement jsonElement) {
            String s = jsonElement.getAsString();
            if (s.equalsIgnoreCase("None")) {
                return Bind.none();
            }
            boolean ctrl = false;
            boolean alt = false;
            boolean shift = false;
            if (s.startsWith("Ctrl+")) {
                ctrl = true;
                s = s.substring(5);
            }
            if (s.startsWith("Alt+")) {
                alt = true;
                s = s.substring(4);
            }
            if (s.startsWith("Shift+")) {
                shift = true;
                s = s.substring(6);
            }
            int key = -1;
            try {
                key = Keyboard.getKeyIndex(s.toUpperCase());
            }
            catch (Exception ex) {}
            if (key == 0) {
                return Bind.none();
            }
            return new Bind(ctrl, alt, shift, key);
        }
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Info {
        String name();
        
        String description() default "Descriptionless";
        
        Category category();
        
        boolean alwaysListening() default false;
        
        ShowOnArray showOnArray() default ShowOnArray.ON;
    }
}
