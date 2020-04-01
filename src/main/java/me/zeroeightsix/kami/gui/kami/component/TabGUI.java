package me.zeroeightsix.kami.gui.kami.component;

import me.zeroeightsix.kami.gui.rgui.component.AbstractComponent;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.util.*;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

/**
 * Created by 086 on 11/08/2017.
 */
public class TabGUI extends AbstractComponent implements EventListener {
    public final ArrayList<Tab> tabs = new ArrayList<>();

    public int width;
    public int height;
    public int selected;
    public float selectedLerpY;
    public boolean tabOpened;

    public TabGUI() {
        FMLCommonHandler.instance().bus().register(this);

        LinkedHashMap<Module.Category, Tab> tabMap = new LinkedHashMap<>();
        for (Module.Category category : Module.Category.values())
            tabMap.put(category, new Tab(category.getName()));

        ArrayList<Module> features = new ArrayList<>(MODULE_MANAGER.getModules());

        for (Module feature : features)
            if (feature.getCategory() != null && !feature.getCategory().isHidden())
                tabMap.get(feature.getCategory()).add(feature);

        Iterator<Map.Entry<Module.Category, Tab>> iterator = tabMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Module.Category, Tab> entry = iterator.next();
            if (entry.getValue().features.isEmpty())
                iterator.remove();
        }

        tabs.addAll(tabMap.values());
        tabs.forEach(tab -> tab.updateSize());
        updateSize();
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKeyState() == false) return;
        Component framep = getParent();
        while (!(framep instanceof Frame))
            framep = framep.getParent();
        if (!((Frame) framep).isPinned())
            return;

        if (tabOpened)
            switch (Keyboard.getEventKey()) {
                case Keyboard.KEY_LEFT:
                    tabOpened = false;
                    break;

                default:
                    tabs.get(selected).onKeyPress(Keyboard.getEventKey());
                    break;
            }
        else
            switch (Keyboard.getEventKey()) {
                case Keyboard.KEY_DOWN:
                    if (selected < tabs.size() - 1)
                        selected++;
                    else
                        selected = 0;
                    break;

                case Keyboard.KEY_UP:
                    if (selected > 0)
                        selected--;
                    else
                        selected = tabs.size() - 1;
                    break;

                case Keyboard.KEY_RIGHT:
                    tabOpened = true;
                    break;
            }
    }

    private void updateSize() {
        width = 64;
        for (Tab tab : tabs) {
            int tabWidth = Wrapper.getFontRenderer().getStringWidth(tab.name) + 10;
            if (tabWidth > width)
                width = tabWidth;
        }
        height = tabs.size() * 10;
    }

    public static final class Tab {
        public final String name;
        public final ArrayList<Module> features = new ArrayList<>();

        public int width;
        public int height;
        public int selected;

        public float lerpSelectY = 0;

        public Tab(String name) {
            this.name = name;
        }

        public void updateSize() {
            width = 64;
            for (Module feature : features) {
                int fWidth = Wrapper.getFontRenderer().getStringWidth(feature.getName()) + 10;
                if (fWidth > width)
                    width = fWidth;
            }

            height = features.size() * 10;
        }

        public void onKeyPress(int keyCode) {
            switch (keyCode) {
                case Keyboard.KEY_DOWN:
                    if (selected < features.size() - 1)
                        selected++;
                    else
                        selected = 0;
                    break;

                case Keyboard.KEY_UP:
                    if (selected > 0)
                        selected--;
                    else
                        selected = features.size() - 1;
                    break;

                case Keyboard.KEY_RIGHT:
                    features.get(selected).toggle();
                    break;
            }
        }

        public void add(Module feature) {
            features.add(feature);
        }
    }
}
