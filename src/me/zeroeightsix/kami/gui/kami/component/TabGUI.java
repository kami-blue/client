// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.kami.component;

import me.zeroeightsix.kami.util.Wrapper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame;
import org.lwjgl.input.Keyboard;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.Collection;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.Module;
import java.util.LinkedHashMap;
import net.minecraftforge.fml.common.FMLCommonHandler;
import java.util.ArrayList;
import java.util.EventListener;
import me.zeroeightsix.kami.gui.rgui.component.AbstractComponent;

public class TabGUI extends AbstractComponent implements EventListener
{
    public final ArrayList<Tab> tabs;
    public int width;
    public int height;
    public int selected;
    public float selectedLerpY;
    public boolean tabOpened;
    
    public TabGUI() {
        this.tabs = new ArrayList<Tab>();
        FMLCommonHandler.instance().bus().register((Object)this);
        final LinkedHashMap<Module.Category, Tab> tabMap = new LinkedHashMap<Module.Category, Tab>();
        for (final Module.Category category : Module.Category.values()) {
            tabMap.put(category, new Tab(category.getName()));
        }
        final ArrayList<Module> features = new ArrayList<Module>();
        features.addAll(ModuleManager.getModules());
        for (final Module feature : features) {
            if (feature.getCategory() != null && !feature.getCategory().isHidden()) {
                tabMap.get(feature.getCategory()).add(feature);
            }
        }
        final Iterator<Map.Entry<Module.Category, Tab>> iterator = tabMap.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<Module.Category, Tab> entry = iterator.next();
            if (entry.getValue().features.isEmpty()) {
                iterator.remove();
            }
        }
        this.tabs.addAll(tabMap.values());
        this.tabs.forEach(tab -> tab.updateSize());
        this.updateSize();
    }
    
    @SubscribeEvent
    public void onKeyPress(final InputEvent.KeyInputEvent event) {
        if (!Keyboard.getEventKeyState()) {
            return;
        }
        Component framep;
        for (framep = this.getParent(); !(framep instanceof Frame); framep = framep.getParent()) {}
        if (!((Frame)framep).isPinned()) {
            return;
        }
        if (this.tabOpened) {
            switch (Keyboard.getEventKey()) {
                case 203: {
                    this.tabOpened = false;
                    break;
                }
                default: {
                    this.tabs.get(this.selected).onKeyPress(Keyboard.getEventKey());
                    break;
                }
            }
        }
        else {
            switch (Keyboard.getEventKey()) {
                case 208: {
                    if (this.selected < this.tabs.size() - 1) {
                        ++this.selected;
                        break;
                    }
                    this.selected = 0;
                    break;
                }
                case 200: {
                    if (this.selected > 0) {
                        --this.selected;
                        break;
                    }
                    this.selected = this.tabs.size() - 1;
                    break;
                }
                case 205: {
                    this.tabOpened = true;
                    break;
                }
            }
        }
    }
    
    private void updateSize() {
        this.width = 64;
        for (final Tab tab : this.tabs) {
            final int tabWidth = Wrapper.getFontRenderer().getStringWidth(tab.name) + 10;
            if (tabWidth > this.width) {
                this.width = tabWidth;
            }
        }
        this.height = this.tabs.size() * 10;
    }
    
    public static final class Tab
    {
        public final String name;
        public final ArrayList<Module> features;
        public int width;
        public int height;
        public int selected;
        public float lerpSelectY;
        
        public Tab(final String name) {
            this.features = new ArrayList<Module>();
            this.lerpSelectY = 0.0f;
            this.name = name;
        }
        
        public void updateSize() {
            this.width = 64;
            for (final Module feature : this.features) {
                final int fWidth = Wrapper.getFontRenderer().getStringWidth(feature.getName()) + 10;
                if (fWidth > this.width) {
                    this.width = fWidth;
                }
            }
            this.height = this.features.size() * 10;
        }
        
        public void onKeyPress(final int keyCode) {
            switch (keyCode) {
                case 208: {
                    if (this.selected < this.features.size() - 1) {
                        ++this.selected;
                        break;
                    }
                    this.selected = 0;
                    break;
                }
                case 200: {
                    if (this.selected > 0) {
                        --this.selected;
                        break;
                    }
                    this.selected = this.features.size() - 1;
                    break;
                }
                case 205: {
                    this.features.get(this.selected).toggle();
                    break;
                }
            }
        }
        
        public void add(final Module feature) {
            this.features.add(feature);
        }
    }
}
