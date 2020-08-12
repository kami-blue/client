// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.kami;

import java.awt.Font;
import me.zeroeightsix.kami.gui.rgui.util.Docking;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.LinkedList;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.projectile.EntityWitherSkull;
import javax.annotation.Nonnull;
import java.text.NumberFormat;
import me.zeroeightsix.kami.gui.rgui.component.AbstractComponent;
import net.minecraft.item.ItemStack;
import java.text.DateFormat;
import me.zeroeightsix.kami.gui.kami.component.Radar;
import net.minecraft.util.text.TextFormatting;
import java.util.Comparator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import java.util.function.Predicate;
import java.util.Objects;
import java.util.Collection;
import net.minecraft.client.Minecraft;
import me.zeroeightsix.kami.gui.rgui.component.listen.TickListener;
import net.minecraft.entity.EntityLivingBase;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.entity.Entity;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import me.zeroeightsix.kami.util.Friends;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.init.Items;
import java.util.Date;
import java.text.SimpleDateFormat;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import java.util.function.Consumer;
import me.zeroeightsix.kami.module.modules.gui.InfoOverlay;
import me.zeroeightsix.kami.gui.rgui.component.use.Label;
import me.zeroeightsix.kami.gui.kami.component.ActiveModules;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.util.ContainerHelper;
import me.zeroeightsix.kami.util.Wrapper;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame;
import java.util.Map;
import me.zeroeightsix.kami.gui.rgui.poof.IPoof;
import me.zeroeightsix.kami.gui.rgui.poof.PoofInfo;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.use.CheckButton.CheckButtonPoof;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import me.zeroeightsix.kami.gui.rgui.component.use.CheckButton;
import me.zeroeightsix.kami.gui.rgui.layout.Layout;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.gui.kami.component.SettingsPanel;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Scrollpane;
import me.zeroeightsix.kami.util.Pair;
import me.zeroeightsix.kami.module.Module;
import java.util.HashMap;
import me.zeroeightsix.kami.gui.kami.theme.kami.KamiTheme;
import me.zeroeightsix.kami.util.ColourHolder;
import me.zeroeightsix.kami.gui.rgui.render.theme.Theme;
import me.zeroeightsix.kami.gui.font.CFontRenderer;
import me.zeroeightsix.kami.gui.rgui.GUI;

public class KamiGUI extends GUI
{
    public static RootFontRenderer fontRenderer;
    public static CFontRenderer cFontRenderer;
    public Theme theme;
    public static ColourHolder primaryColour;
    private static final int DOCK_OFFSET = 0;
    
    public KamiGUI() {
        super(new KamiTheme());
        this.theme = this.getTheme();
    }
    
    @Override
    public void drawGUI() {
        super.drawGUI();
    }
    
    @Override
    public void initializeGUI() {
        final HashMap<Module.Category, Pair<Scrollpane, SettingsPanel>> categoryScrollpaneHashMap = new HashMap<Module.Category, Pair<Scrollpane, SettingsPanel>>();
        for (final Module module : ModuleManager.getModules()) {
            if (module.getCategory().isHidden()) {
                continue;
            }
            final Module.Category moduleCategory = module.getCategory();
            if (!categoryScrollpaneHashMap.containsKey(moduleCategory)) {
                final Stretcherlayout stretcherlayout = new Stretcherlayout(1);
                stretcherlayout.setComponentOffsetWidth(0);
                final Scrollpane scrollpane = new Scrollpane(this.getTheme(), stretcherlayout, 300, 260);
                scrollpane.setMaximumHeight(180);
                categoryScrollpaneHashMap.put(moduleCategory, new Pair<Scrollpane, SettingsPanel>(scrollpane, new SettingsPanel(this.getTheme(), (Module)null)));
            }
            final Pair<Scrollpane, SettingsPanel> pair = categoryScrollpaneHashMap.get(moduleCategory);
            final Scrollpane scrollpane = pair.getKey();
            final CheckButton checkButton = new CheckButton(module.getName());
            checkButton.setToggled(module.isEnabled());
            final CheckButton checkButton2;
            final Module module2;
            checkButton.addTickListener(() -> {
                checkButton2.setToggled(module2.isEnabled());
                checkButton2.setName(module2.getName());
                return;
            });
            checkButton.addMouseListener(new MouseListener() {
                @Override
                public void onMouseDown(final MouseButtonEvent event) {
                    if (event.getButton() == 1) {
                        pair.getValue().setModule(module);
                        pair.getValue().setX(event.getX() + checkButton.getX());
                        pair.getValue().setY(event.getY() + checkButton.getY());
                    }
                }
                
                @Override
                public void onMouseRelease(final MouseButtonEvent event) {
                }
                
                @Override
                public void onMouseDrag(final MouseButtonEvent event) {
                }
                
                @Override
                public void onMouseMove(final MouseMoveEvent event) {
                }
                
                @Override
                public void onScroll(final MouseScrollEvent event) {
                }
            });
            checkButton.addPoof(new CheckButton.CheckButtonPoof<CheckButton, CheckButton.CheckButtonPoof.CheckButtonPoofInfo>() {
                @Override
                public void execute(final CheckButton component, final CheckButtonPoofInfo info) {
                    if (info.getAction().equals(CheckButtonPoofInfo.CheckButtonPoofInfoAction.TOGGLE)) {
                        module.setEnabled(checkButton.isToggled());
                    }
                }
            });
            scrollpane.addChild(checkButton);
        }
        int x = 10;
        int nexty;
        int y = nexty = 10;
        for (final Map.Entry<Module.Category, Pair<Scrollpane, SettingsPanel>> entry : categoryScrollpaneHashMap.entrySet()) {
            final Stretcherlayout stretcherlayout2 = new Stretcherlayout(1);
            stretcherlayout2.COMPONENT_OFFSET_Y = 1;
            final Frame frame = new Frame(this.getTheme(), stretcherlayout2, entry.getKey().getName());
            final Scrollpane scrollpane2 = entry.getValue().getKey();
            frame.addChild(scrollpane2);
            frame.addChild(entry.getValue().getValue());
            scrollpane2.setOriginOffsetY(0);
            scrollpane2.setOriginOffsetX(0);
            frame.setCloseable(false);
            frame.setX(x);
            frame.setY(y);
            this.addChild(frame);
            nexty = Math.max(y + frame.getHeight() + 10, nexty);
            x += frame.getWidth() + 10;
            if (x > Wrapper.getMinecraft().field_71443_c / 1.2f) {
                y = (nexty = nexty);
            }
        }
        this.addMouseListener(new MouseListener() {
            private boolean isBetween(final int min, final int val, final int max) {
                return val <= max && val >= min;
            }
            
            @Override
            public void onMouseDown(final MouseButtonEvent event) {
                final List<SettingsPanel> panels = ContainerHelper.getAllChildren((Class<? extends SettingsPanel>)SettingsPanel.class, (Container)KamiGUI.this);
                for (final SettingsPanel settingsPanel : panels) {
                    if (!settingsPanel.isVisible()) {
                        continue;
                    }
                    final int[] real = GUI.calculateRealPosition(settingsPanel);
                    final int pX = event.getX() - real[0];
                    final int pY = event.getY() - real[1];
                    if (this.isBetween(0, pX, settingsPanel.getWidth()) && this.isBetween(0, pY, settingsPanel.getHeight())) {
                        continue;
                    }
                    settingsPanel.setVisible(false);
                }
            }
            
            @Override
            public void onMouseRelease(final MouseButtonEvent event) {
            }
            
            @Override
            public void onMouseDrag(final MouseButtonEvent event) {
            }
            
            @Override
            public void onMouseMove(final MouseMoveEvent event) {
            }
            
            @Override
            public void onScroll(final MouseScrollEvent event) {
            }
        });
        final ArrayList<Frame> frames = new ArrayList<Frame>();
        Frame frame2 = new Frame(this.getTheme(), new Stretcherlayout(1), "Active modules");
        frame2.setCloseable(false);
        frame2.addChild(new ActiveModules());
        frame2.setPinneable(true);
        frames.add(frame2);
        frame2 = new Frame(this.getTheme(), new Stretcherlayout(1), "Info");
        frame2.setCloseable(false);
        frame2.setPinneable(true);
        final Label information = new Label("");
        information.setShadow(true);
        final InfoOverlay info;
        final Label label;
        information.addTickListener(() -> {
            info = (InfoOverlay)ModuleManager.getModuleByName("InfoOverlay");
            label.setText("");
            info.infoContents().forEach(label::addLine);
            return;
        });
        frame2.addChild(information);
        information.setFontRenderer(KamiGUI.fontRenderer);
        frames.add(frame2);
        frame2 = new Frame(this.getTheme(), new Stretcherlayout(1), "AstraMod-Watermark");
        frame2.setCloseable(false);
        frame2.setPinneable(true);
        final Label KONCClient = new Label("");
        final Label label2;
        KONCClient.addTickListener(() -> {
            label2.setText("");
            label2.addLine("AstraMod on top");
            return;
        });
        frame2.addChild(KONCClient);
        information.setFontRenderer(KamiGUI.fontRenderer);
        frames.add(frame2);
        frame2 = new Frame(this.getTheme(), new Stretcherlayout(1), "Hello");
        frame2.setCloseable(false);
        frame2.setPinneable(true);
        final Label Hello = new Label("");
        final Label label3;
        Hello.addTickListener(() -> {
            label3.setText("");
            label3.addLine("§9Hello§1 " + Wrapper.getPlayer().getDisplayNameString() + " §9AstraMod");
            return;
        });
        frame2.addChild(Hello);
        information.setFontRenderer(KamiGUI.fontRenderer);
        frames.add(frame2);
        frame2 = new Frame(this.getTheme(), new Stretcherlayout(1), "AstraMod-Devs");
        frame2.setCloseable(false);
        frame2.setPinneable(true);
        final Label KONCONTOP = new Label("");
        final Label label4;
        KONCONTOP.addTickListener(() -> {
            label4.setText("");
            label4.addLine("§9CrawlerExE and Tux");
            return;
        });
        frame2.addChild(KONCONTOP);
        information.setFontRenderer(KamiGUI.fontRenderer);
        frames.add(frame2);
        frame2 = new Frame(this.getTheme(), new Stretcherlayout(1), "Time");
        frame2.setCloseable(false);
        frame2.setPinneable(true);
        final Label time = new Label("");
        final DateFormat df;
        final Date dateobj;
        final Label label5;
        time.addTickListener(() -> {
            df = new SimpleDateFormat("HH:mm:ss");
            dateobj = new Date();
            label5.setText("§a§l" + df.format(dateobj));
            return;
        });
        frame2.addChild(time);
        information.setFontRenderer(KamiGUI.fontRenderer);
        frames.add(frame2);
        frame2 = new Frame(this.getTheme(), new Stretcherlayout(1), "Date");
        frame2.setCloseable(false);
        frame2.setPinneable(true);
        final Label date = new Label("");
        final DateFormat df2;
        final Date dateobj2;
        final Label label6;
        date.addTickListener(() -> {
            df2 = new SimpleDateFormat("yyyy/MM/dd");
            dateobj2 = new Date();
            label6.setText("§a§l" + df2.format(dateobj2));
            return;
        });
        frame2.addChild(date);
        information.setFontRenderer(KamiGUI.fontRenderer);
        frames.add(frame2);
        frame2 = new Frame(this.getTheme(), new Stretcherlayout(1), "pvp Info");
        frame2.setCloseable(false);
        frame2.setPinneable(true);
        final Label te = new Label("");
        te.setShadow(true);
        final Label label7;
        int totemCount;
        int XPCount;
        int crystalCount;
        int Apple;
        int i;
        ItemStack itemStack;
        int j;
        ItemStack itemStack2;
        int k;
        ItemStack itemStack3;
        int l;
        ItemStack itemStack4;
        te.addTickListener(() -> {
            label7.setText("");
            totemCount = 0;
            XPCount = 0;
            crystalCount = 0;
            Apple = 0;
            for (i = 0; i < 45; ++i) {
                itemStack = Wrapper.getMinecraft().field_71439_g.field_71071_by.func_70301_a(i);
                if (itemStack.func_77973_b() == Items.field_190929_cY) {
                    totemCount += itemStack.field_77994_a;
                }
            }
            for (j = 0; j < 45; ++j) {
                itemStack2 = Wrapper.getMinecraft().field_71439_g.field_71071_by.func_70301_a(j);
                if (itemStack2.func_77973_b() == Items.field_185158_cP) {
                    crystalCount += itemStack2.field_77994_a;
                }
            }
            for (k = 0; k < 45; ++k) {
                itemStack3 = Wrapper.getMinecraft().field_71439_g.field_71071_by.func_70301_a(k);
                if (itemStack3.func_77973_b() == Items.field_151062_by) {
                    XPCount += itemStack3.field_77994_a;
                }
            }
            for (l = 0; l < 45; ++l) {
                itemStack4 = Wrapper.getMinecraft().field_71439_g.field_71071_by.func_70301_a(l);
                if (itemStack4.func_77973_b() == Items.field_151153_ao) {
                    Apple += itemStack4.field_77994_a;
                }
            }
            label7.addLine("§9Totem: §1" + totemCount);
            label7.addLine("§9XP: §1" + XPCount);
            label7.addLine("§9Crystal: §1" + crystalCount);
            label7.addLine("§9Apple: §1" + Apple);
            return;
        });
        frame2.addChild(te);
        te.setFontRenderer(KamiGUI.fontRenderer);
        frames.add(frame2);
        frame2 = new Frame(this.getTheme(), new Stretcherlayout(1), "Inventory Viewer");
        frame2.setCloseable(false);
        frame2.setMinimizeable(false);
        frame2.setPinneable(true);
        frame2.setPinned(true);
        final Label inventory = new Label("");
        final AbstractComponent abstractComponent;
        inventory.addTickListener(() -> {
            abstractComponent.setWidth(151);
            abstractComponent.setHeight(40);
            abstractComponent.setOpacity(0.1f);
            return;
        });
        inventory.setShadow(false);
        frame2.addChild(inventory);
        inventory.setFontRenderer(KamiGUI.fontRenderer);
        frames.add(frame2);
        frame2 = new Frame(this.getTheme(), new Stretcherlayout(1), "Friends");
        frame2.setCloseable(false);
        frame2.setPinneable(true);
        final Label friends = new Label("");
        friends.setShadow(true);
        final Frame friendsFrame = frame2;
        final AtomicInteger friendsAmount = new AtomicInteger();
        final Frame frame4;
        final Label label8;
        final AtomicInteger atomicInteger;
        friends.addTickListener(() -> {
            if (!frame4.isMinimized()) {
                label8.setText("");
                Friends.friends.getValue().forEach(friend -> atomicInteger.getAndIncrement());
            }
            else {
                label8.setText("");
            }
            return;
        });
        frame2.addChild(friends);
        friends.setFontRenderer(KamiGUI.fontRenderer);
        frames.add(frame2);
        frame2 = new Frame(this.getTheme(), new Stretcherlayout(1), "Text Radar");
        final Label list = new Label("");
        final DecimalFormat dfHealth = new DecimalFormat("#.#");
        dfHealth.setRoundingMode(RoundingMode.HALF_UP);
        final StringBuilder healthSB = new StringBuilder();
        final Label label9;
        Minecraft mc;
        List<EntityPlayer> entityList;
        Map<String, Integer> players;
        final Iterator<EntityPlayer> iterator3;
        Entity e;
        String s;
        String posString;
        float hpRaw;
        final NumberFormat numberFormat;
        String hp;
        final StringBuilder sb;
        Map<String, Integer> players2;
        final Iterator<Map.Entry<String, Integer>> iterator4;
        Map.Entry<String, Integer> player;
        list.addTickListener(() -> {
            if (!label9.isVisible()) {
                return;
            }
            else {
                label9.setText("");
                mc = Wrapper.getMinecraft();
                if (mc.field_71439_g == null) {
                    return;
                }
                else {
                    entityList = (List<EntityPlayer>)mc.field_71441_e.field_73010_i;
                    players = new HashMap<String, Integer>();
                    entityList.iterator();
                    while (iterator3.hasNext()) {
                        e = (Entity)iterator3.next();
                        if (e.func_70005_c_().equals(mc.field_71439_g.func_70005_c_())) {
                            continue;
                        }
                        else {
                            if (e.field_70163_u > mc.field_71439_g.field_70163_u) {
                                s = ChatFormatting.DARK_GREEN + "+";
                            }
                            else if (e.field_70163_u == mc.field_71439_g.field_70163_u) {
                                s = "c";
                            }
                            else {
                                s = ChatFormatting.DARK_RED + "-";
                            }
                            posString = s;
                            hpRaw = ((EntityLivingBase)e).func_110143_aJ() + ((EntityLivingBase)e).func_110139_bj();
                            hp = numberFormat.format(hpRaw);
                            sb.append('§');
                            if (hpRaw >= 20.0f) {
                                sb.append("a");
                            }
                            else if (hpRaw >= 10.0f) {
                                sb.append("e");
                            }
                            else if (hpRaw >= 5.0f) {
                                sb.append("6");
                            }
                            else {
                                sb.append("c");
                            }
                            sb.append(hp);
                            players.put(ChatFormatting.GRAY + posString + "\u73a9\u5bb6" + sb.toString() + " " + ChatFormatting.GRAY + e.func_70005_c_(), (int)mc.field_71439_g.func_70032_d(e));
                            sb.setLength(0);
                        }
                    }
                    if (players.isEmpty()) {
                        label9.setText("");
                        return;
                    }
                    else {
                        players2 = sortByValue(players);
                        players2.entrySet().iterator();
                        while (iterator4.hasNext()) {
                            player = iterator4.next();
                            label9.addLine("§7" + player.getKey() + " " + '§' + "c" + player.getValue());
                        }
                        return;
                    }
                }
            }
        });
        frame2.setCloseable(false);
        frame2.setPinneable(true);
        frame2.setMinimumWidth(75);
        list.setShadow(true);
        frame2.addChild(list);
        list.setFontRenderer(KamiGUI.fontRenderer);
        frames.add(frame2);
        frame2 = new Frame(this.getTheme(), new Stretcherlayout(1), "Entities");
        final Label entityLabel = new Label("");
        frame2.setCloseable(false);
        entityLabel.addTickListener(new TickListener() {
            Minecraft mc = Wrapper.getMinecraft();
            
            @Override
            public void onTick() {
                if (this.mc.field_71439_g == null || !entityLabel.isVisible()) {
                    return;
                }
                final List<Entity> entityList = new ArrayList<Entity>(this.mc.field_71441_e.field_72996_f);
                if (entityList.size() <= 1) {
                    entityLabel.setText("");
                    return;
                }
                final Map<String, Integer> entityCounts = entityList.stream().filter(Objects::nonNull).filter(e -> !(e instanceof EntityPlayer)).collect(Collectors.groupingBy(x$0 -> getEntityName(x$0), (Collector<? super Object, ?, Integer>)Collectors.reducing((D)0, ent -> {
                    if (ent instanceof EntityItem) {
                        return Integer.valueOf(ent.func_92059_d().func_190916_E());
                    }
                    else {
                        return Integer.valueOf(1);
                    }
                }, Integer::sum)));
                entityLabel.setText("");
                entityCounts.entrySet().stream().sorted((Comparator<? super Object>)Map.Entry.comparingByValue()).map(entry -> TextFormatting.GRAY + entry.getKey() + " " + TextFormatting.DARK_GRAY + "x" + entry.getValue()).forEach((Consumer<? super Object>)entityLabel::addLine);
            }
        });
        frame2.addChild(entityLabel);
        frame2.setPinneable(true);
        entityLabel.setShadow(true);
        entityLabel.setFontRenderer(KamiGUI.fontRenderer);
        frames.add(frame2);
        frame2 = new Frame(this.getTheme(), new Stretcherlayout(1), "Coordinates");
        frame2.setCloseable(false);
        frame2.setPinneable(true);
        final Label coordsLabel = new Label("");
        coordsLabel.addTickListener(new TickListener() {
            Minecraft mc = Minecraft.func_71410_x();
            
            @Override
            public void onTick() {
                final boolean inHell = this.mc.field_71441_e.func_180494_b(this.mc.field_71439_g.func_180425_c()).func_185359_l().equals("Hell");
                final int posX = (int)this.mc.field_71439_g.field_70165_t;
                final int posY = (int)this.mc.field_71439_g.field_70163_u;
                final int posZ = (int)this.mc.field_71439_g.field_70161_v;
                final float f = inHell ? 8.0f : 0.125f;
                final int hposX = (int)(this.mc.field_71439_g.field_70165_t * f);
                final int hposZ = (int)(this.mc.field_71439_g.field_70161_v * f);
                coordsLabel.setText(String.format(" %sf%,d%s7, %sf%,d%s7, %sf%,d %s7(%sf%,d%s7, %sf%,d%s7, %sf%,d%s7)", '§', posX, '§', '§', posY, '§', '§', posZ, '§', '§', hposX, '§', '§', posY, '§', '§', hposZ, '§'));
            }
        });
        frame2.addChild(coordsLabel);
        coordsLabel.setFontRenderer(KamiGUI.fontRenderer);
        coordsLabel.setShadow(true);
        frame2.setHeight(20);
        frames.add(frame2);
        frame2 = new Frame(this.getTheme(), new Stretcherlayout(1), "Radar");
        frame2.setCloseable(false);
        frame2.setMinimizeable(true);
        frame2.setPinneable(true);
        frame2.addChild(new Radar());
        frame2.setWidth(100);
        frame2.setHeight(100);
        frames.add(frame2);
        for (final Frame frame3 : frames) {
            frame3.setX(x);
            frame3.setY(y);
            nexty = Math.max(y + frame3.getHeight() + 10, nexty);
            x += frame3.getWidth() + 10;
            if (x * DisplayGuiScreen.getScale() > Wrapper.getMinecraft().field_71443_c / 1.2f) {
                y = (nexty = nexty);
                x = 10;
            }
            this.addChild(frame3);
        }
    }
    
    private static String getEntityName(@Nonnull final Entity entity) {
        if (entity instanceof EntityItem) {
            return TextFormatting.DARK_AQUA + ((EntityItem)entity).func_92059_d().func_77973_b().func_77653_i(((EntityItem)entity).func_92059_d());
        }
        if (entity instanceof EntityWitherSkull) {
            return TextFormatting.DARK_GRAY + "Wither skull";
        }
        if (entity instanceof EntityEnderCrystal) {
            return TextFormatting.LIGHT_PURPLE + "End crystal";
        }
        if (entity instanceof EntityEnderPearl) {
            return "Thrown ender pearl";
        }
        if (entity instanceof EntityMinecart) {
            return "Minecart";
        }
        if (entity instanceof EntityItemFrame) {
            return "Item frame";
        }
        if (entity instanceof EntityEgg) {
            return "Thrown egg";
        }
        if (entity instanceof EntitySnowball) {
            return "Thrown snowball";
        }
        return entity.func_70005_c_();
    }
    
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(final Map<K, V> map) {
        final List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, Comparator.comparing(o -> o.getValue()));
        final Map<K, V> result = new LinkedHashMap<K, V>();
        for (final Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    @Override
    public void destroyGUI() {
        this.kill();
    }
    
    public static void dock(final Frame component) {
        final Docking docking = component.getDocking();
        if (docking.isTop()) {
            component.setY(0);
        }
        if (docking.isBottom()) {
            component.setY(Wrapper.getMinecraft().field_71440_d / DisplayGuiScreen.getScale() - component.getHeight() - 0);
        }
        if (docking.isLeft()) {
            component.setX(0);
        }
        if (docking.isRight()) {
            component.setX(Wrapper.getMinecraft().field_71443_c / DisplayGuiScreen.getScale() - component.getWidth() - 0);
        }
        if (docking.isCenterHorizontal()) {
            component.setX(Wrapper.getMinecraft().field_71443_c / (DisplayGuiScreen.getScale() * 2) - component.getWidth() / 2);
        }
        if (docking.isCenterVertical()) {
            component.setY(Wrapper.getMinecraft().field_71440_d / (DisplayGuiScreen.getScale() * 2) - component.getHeight() / 2);
        }
    }
    
    @Override
    public String getTitle() {
        return null;
    }
    
    @Override
    public boolean isPinned() {
        return false;
    }
    
    @Override
    public boolean isMinimized() {
        return false;
    }
    
    @Override
    public Docking getDocking() {
        return null;
    }
    
    static {
        KamiGUI.fontRenderer = new RootFontRenderer(1.0f);
        KamiGUI.primaryColour = new ColourHolder(29, 29, 29);
        KamiGUI.fontRenderer = new RootFontRenderer(1.0f);
        KamiGUI.cFontRenderer = new CFontRenderer(new Font("Verdana", 0, 18), true, false);
        KamiGUI.primaryColour = new ColourHolder(29, 29, 29);
    }
}
