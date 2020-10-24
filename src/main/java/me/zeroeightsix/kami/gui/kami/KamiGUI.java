package me.zeroeightsix.kami.gui.kami;

import baritone.api.BaritoneAPI;
import baritone.api.process.IBaritoneProcess;
import com.mojang.realmsclient.gui.ChatFormatting;
import kotlin.Pair;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.gui.kami.component.*;
import me.zeroeightsix.kami.gui.kami.theme.kami.KamiTheme;
import me.zeroeightsix.kami.gui.rgui.GUI;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Scrollpane;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import me.zeroeightsix.kami.gui.rgui.component.listen.TickListener;
import me.zeroeightsix.kami.gui.rgui.component.use.CheckButton;
import me.zeroeightsix.kami.gui.rgui.component.use.Label;
import me.zeroeightsix.kami.gui.rgui.render.theme.Theme;
import me.zeroeightsix.kami.gui.rgui.util.ContainerHelper;
import me.zeroeightsix.kami.gui.rgui.util.Docking;
import me.zeroeightsix.kami.manager.managers.FriendManager;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.client.InfoOverlay;
import me.zeroeightsix.kami.module.modules.movement.AutoWalk;
import me.zeroeightsix.kami.process.TemporaryPauseProcess;
import me.zeroeightsix.kami.util.Wrapper;
import me.zeroeightsix.kami.util.math.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.MobEffects;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class KamiGUI extends GUI {

    private static final int DOCK_OFFSET = 0;
    public Theme theme;

    public KamiGUI() {
        super(new KamiTheme());
        theme = getTheme();
    }

    private static String getEntityName(@Nonnull Entity entity) {
        if (entity instanceof EntityItem) {
            return TextFormatting.DARK_AQUA + ((EntityItem) entity).getItem().getItem().getItemStackDisplayName(((EntityItem) entity).getItem());
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

        return entity.getName();
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list =
                new LinkedList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static void dock(Frame component) {
        Docking docking = component.getDocking();
        if (docking.isTop())
            component.setY(DOCK_OFFSET);
        if (docking.isBottom())
            component.setY((int) ((Wrapper.getMinecraft().displayHeight / DisplayGuiScreen.getScale()) - component.getHeight() - DOCK_OFFSET));
        if (docking.isLeft())
            component.setX(DOCK_OFFSET);
        if (docking.isRight())
            component.setX((int) ((Wrapper.getMinecraft().displayWidth / DisplayGuiScreen.getScale()) - component.getWidth() - DOCK_OFFSET));
        if (docking.isCenterHorizontal())
            component.setX((int) (Wrapper.getMinecraft().displayWidth / (DisplayGuiScreen.getScale() * 2) - component.getWidth() / 2));
        if (docking.isCenterVertical())
            component.setY((int) (Wrapper.getMinecraft().displayHeight / (DisplayGuiScreen.getScale() * 2) - component.getHeight() / 2));

    }

    @Override
    public void drawGUI() {
        super.drawGUI();
    }

    @Override
    public void initializeGUI() {
        HashMap<Module.Category, Pair<Scrollpane, SettingsPanel>> categoryScrollpaneHashMap = new HashMap<>();
        for (Module module : ModuleManager.getModules()) {
            if (module.category.isHidden()) continue;
            Module.Category moduleCategory = module.category;
            if (!categoryScrollpaneHashMap.containsKey(moduleCategory)) {
                Stretcherlayout stretcherlayout = new Stretcherlayout(1);
                stretcherlayout.setComponentOffsetWidth(0);
                Scrollpane scrollpane = new Scrollpane(getTheme(), stretcherlayout, 300, 260);
                scrollpane.setMaximumHeight(180);
                categoryScrollpaneHashMap.put(moduleCategory, new Pair<>(scrollpane, new SettingsPanel(getTheme(), null)));
            }

            Pair<Scrollpane, SettingsPanel> pair = categoryScrollpaneHashMap.get(moduleCategory);
            Scrollpane scrollpane = pair.getFirst();
            CheckButton checkButton = new CheckButton(module.name.getValue(), module.description);
            checkButton.setToggled(module.isEnabled());

            /* descriptions aren't changed ever, so you don't need a tick listener */
            checkButton.setDescription(module.description);
            checkButton.addTickListener(() -> { // dear god
                checkButton.setToggled(module.isEnabled());
                checkButton.setName(module.name.getValue());
            });

            checkButton.addMouseListener(new MouseListener() {
                @Override
                public void onMouseDown(MouseButtonEvent event) {
                    if (event.getButton() == 1) { // Right click
                        pair.getSecond().setModule(module);
                        pair.getSecond().setX(event.getX() + checkButton.getX());
                        pair.getSecond().setY(event.getY() + checkButton.getY());
                    }
                }

                @Override
                public void onMouseRelease(MouseButtonEvent event) {

                }

                @Override
                public void onMouseDrag(MouseButtonEvent event) {

                }

                @Override
                public void onMouseMove(MouseMoveEvent event) {

                }

                @Override
                public void onScroll(MouseScrollEvent event) {

                }
            });
            checkButton.addPoof(new CheckButton.CheckButtonPoof<CheckButton, CheckButton.CheckButtonPoof.CheckButtonPoofInfo>() {
                @Override
                public void execute(CheckButton component, CheckButtonPoofInfo info) {
                    if (info.getAction().equals(CheckButton.CheckButtonPoof.CheckButtonPoofInfo.CheckButtonPoofInfoAction.TOGGLE)) {
                        module.setEnabled(checkButton.isToggled());
                    }
                }
            });
            scrollpane.addChild(checkButton);
        }

        int x = 10;
        int y = 10;
        int nexty = y;
        for (Map.Entry<Module.Category, Pair<Scrollpane, SettingsPanel>> entry : categoryScrollpaneHashMap.entrySet()) {
            Stretcherlayout stretcherlayout = new Stretcherlayout(1);
            stretcherlayout.COMPONENT_OFFSET_Y = 1;
            Frame frame = new Frame(getTheme(), stretcherlayout, entry.getKey().getCategoryName());
            Scrollpane scrollpane = entry.getValue().getFirst();
            frame.addChild(scrollpane);
            frame.addChild(entry.getValue().getSecond());
            scrollpane.setOriginOffsetY(0);
            scrollpane.setOriginOffsetX(0);
            frame.setCloseable(false);

            frame.setX(x);
            frame.setY(y);

            addChild(frame);

            nexty = Math.max(y + frame.getHeight() + 10, nexty);
            x += frame.getWidth() + 10;
            if (x > Wrapper.getMinecraft().displayWidth / 1.2f) {
                y = nexty;
                nexty = y;
            }
        }

        this.addMouseListener(new MouseListener() {
            private boolean isNotBetween(int val, int max) {
                return val > max || val < 0;
            }

            @Override
            public void onMouseDown(MouseButtonEvent event) {
                List<SettingsPanel> panels = ContainerHelper.getAllChildren(SettingsPanel.class, KamiGUI.this);
                for (SettingsPanel settingsPanel : panels) {
                    if (!settingsPanel.isVisible()) continue;
                    int[] real = GUI.calculateRealPosition(settingsPanel);
                    int pX = event.getX() - real[0];
                    int pY = event.getY() - real[1];
                    if (isNotBetween(pX, settingsPanel.getWidth()) || isNotBetween(pY, settingsPanel.getHeight()))
                        settingsPanel.setVisible(false);
                }
            }

            @Override
            public void onMouseRelease(MouseButtonEvent event) {
            }

            @Override
            public void onMouseDrag(MouseButtonEvent event) {
            }

            @Override
            public void onMouseMove(MouseMoveEvent event) {
            }

            @Override
            public void onScroll(MouseScrollEvent event) {
            }
        });

        ArrayList<Frame> frames = new ArrayList<>();

        /*
         * Active modules
         */
        final Frame activeModules = new Frame(getTheme(), new Stretcherlayout(1), "Active modules");
        activeModules.setCloseable(false);
        activeModules.addChild(new ActiveModules());
        activeModules.setPinnable(true);
        frames.add(activeModules);

        /*
         * Potions
         */
        final Frame potionEffects = new Frame(getTheme(), new Stretcherlayout(1), "Potion Effects");
        potionEffects.setCloseable(false);
        potionEffects.setMinimizeable(true);
        potionEffects.setPinnable(true);
        potionEffects.addChild(new Potions());
        frames.add(potionEffects);

        /*
         * InfoOverlay
         */
        final Frame infoOverlay = new Frame(getTheme(), new Stretcherlayout(1), "Info");
        infoOverlay.setCloseable(false);
        infoOverlay.setPinnable(true);
        Label information = new Label("");
        information.setShadow(true);
        information.addTickListener(() -> {
            information.setText("");
            InfoOverlay.INSTANCE.infoContents().forEach(information::addLine);
        });
        infoOverlay.addChild(information);
        frames.add(infoOverlay);

        /*
         * Inventory Viewer
         *
         * {@link me.zeroeightsix.kami.module.modules.client.InventoryViewer}
         * {@link me.zeroeightsix.kami.gui.kami.theme.staticui.InventoryViewerUI}
         */
        final Frame inventoryViewer = new Frame(getTheme(), new Stretcherlayout(1), "Inventory Viewer");
        inventoryViewer.setCloseable(false);
        inventoryViewer.setMinimizeable(true);
        inventoryViewer.setPinnable(true);
        inventoryViewer.setWidth(176);
        inventoryViewer.setHeight(64);
        inventoryViewer.addChild(new InventoryViewerComponent());
        frames.add(inventoryViewer);

        /*
         * Friends List
         */
        final Frame friendList = new Frame(getTheme(), new Stretcherlayout(1), "Friends");
        friendList.setCloseable(false);
        friendList.setPinnable(false);
        friendList.setMinimizeable(true);
        friendList.setMinimumWidth(50);
        friendList.setMinimumHeight(10);
        Label friends = new Label("");
        friends.setShadow(false);
        friends.addTickListener(() -> {
            friends.setText("");
            if (!friendList.isMinimized()) {
                if (FriendManager.INSTANCE.getEnabled()) {
                    for (FriendManager.Friend friend : FriendManager.INSTANCE.getFriends().values()) {
                        final String name = friend.getUsername();
                        if (name.isEmpty()) continue;
                        friends.addLine(name);
                    }
                } else {
                    friends.addLine(KamiMod.color + "cDisabled");
                }
            }
        });
        friendList.addChild(friends);
        frames.add(friendList);

        /*
         * Baritone
         */
        final Frame baritone = new Frame(getTheme(), new Stretcherlayout(1), "Baritone");
        baritone.setCloseable(false);
        baritone.setPinnable(true);
        baritone.setMinimumWidth(85);
        Label processes = new Label("");
        processes.setShadow(true);
        processes.addTickListener(() -> {
            processes.setText("");
            Optional<IBaritoneProcess> process = BaritoneAPI.getProvider().getPrimaryBaritone().getPathingControlManager().mostRecentInControl();
            if (!baritone.isMinimized() && process.isPresent()) {
                if (process.get() != TemporaryPauseProcess.INSTANCE && AutoWalk.INSTANCE.isEnabled() && AutoWalk.INSTANCE.getMode().getValue() == AutoWalk.AutoWalkMode.BARITONE && AutoWalk.INSTANCE.getDirection() != null) {
                    processes.addLine("Process: AutoWalk (" + AutoWalk.INSTANCE.getDirection() + ")");
                } else {
                    processes.addLine("Process: " + process.get().displayName());
                }
            }
        });
        baritone.addChild(processes);
        frames.add(baritone);

        /*
         * Text Radar
         */
        final Frame textRadar = new Frame(getTheme(), new Stretcherlayout(1), "Text Radar");
        Label list = new Label("");
        DecimalFormat dfHealth = new DecimalFormat("#.#");
        dfHealth.setRoundingMode(RoundingMode.HALF_UP);
        StringBuilder healthSB = new StringBuilder();
        list.addTickListener(() -> {
            if (!list.isVisible()) return;
            list.setText("");

            Minecraft mc = Wrapper.getMinecraft();

            if (mc.player == null) return;
            List<EntityPlayer> entityList = mc.world.playerEntities;

            Map<String, Integer> players = new HashMap<>();
            for (Entity e : entityList) {
                if (e.getName().equals(mc.player.getName())) continue;

                String posString = (e.posY > mc.player.posY ? ChatFormatting.DARK_GREEN + "+" : (e.posY == mc.player.posY ? " " : ChatFormatting.DARK_RED + "-"));
                String weaknessFactor;
                String strengthFactor;
                String extraPaddingForFactors;
                EntityPlayer ePlayer = (EntityPlayer) e;

                if (ePlayer.isPotionActive(MobEffects.WEAKNESS)) weaknessFactor = "W";
                else weaknessFactor = "";
                if (ePlayer.isPotionActive(MobEffects.STRENGTH)) strengthFactor = "S";
                else strengthFactor = "";
                if (weaknessFactor.equals("") && strengthFactor.equals("")) extraPaddingForFactors = "";
                else extraPaddingForFactors = " ";

                float hpRaw = ((EntityLivingBase) e).getHealth() + ((EntityLivingBase) e).getAbsorptionAmount();
                String hp = dfHealth.format(hpRaw);
                healthSB.append(KamiMod.color);
                if (hpRaw >= 20) {
                    healthSB.append("a");
                } else if (hpRaw >= 10) {
                    healthSB.append("e");
                } else if (hpRaw >= 5) {
                    healthSB.append("6");
                } else {
                    healthSB.append("c");
                }
                healthSB.append(hp);

                players.put(ChatFormatting.GRAY + posString + " " + healthSB.toString() + " " + ChatFormatting.DARK_GRAY + weaknessFactor + ChatFormatting.DARK_PURPLE + strengthFactor + ChatFormatting.GRAY + extraPaddingForFactors + e.getName(), (int) mc.player.getDistance(e));
                healthSB.setLength(0);
            }

            if (players.isEmpty()) {
                list.setText("");
                return;
            }

            players = sortByValue(players);

            for (Map.Entry<String, Integer> player : players.entrySet()) {
                list.addLine(KamiMod.color + "7" + player.getKey() + " " + KamiMod.color + "8" + player.getValue());
            }
        });
        textRadar.setCloseable(false);
        textRadar.setPinnable(true);
        textRadar.setMinimumWidth(100);
        list.setShadow(true);
        textRadar.addChild(list);
        frames.add(textRadar);

        /*
         * Entity List
         */
        final Frame entityList = new Frame(getTheme(), new Stretcherlayout(1), "Entities");
        Label entityLabel = new Label("");
        entityList.setCloseable(false);
        entityList.setMinimumWidth(80);
        entityLabel.addTickListener(new TickListener() {
            final Minecraft mc = Wrapper.getMinecraft();

            @Override
            public void onTick() {
                if (!entityList.isMinimized()) {
                    if (mc.player == null || !entityLabel.isVisible()) return;

                    final List<Entity> entityList = new ArrayList<>(mc.world.loadedEntityList);
                    if (entityList.size() <= 1) {
                        entityLabel.setText("");
                        return;
                    }
                    final Map<String, Integer> entityCounts = entityList.stream()
                            .filter(Objects::nonNull)
                            .filter(e -> !(e instanceof EntityPlayer))
                            .collect(Collectors.groupingBy(KamiGUI::getEntityName,
                                    Collectors.reducing(0, ent -> {
                                        if (ent instanceof EntityItem)
                                            return ((EntityItem) ent).getItem().getCount();
                                        return 1;
                                    }, Integer::sum)
                            ));

                    entityLabel.setText("");
                    entityCounts.entrySet().stream()
                            .sorted(Map.Entry.comparingByValue())
                            .map(entry -> TextFormatting.GRAY + entry.getKey() + " " + TextFormatting.DARK_GRAY + "x" + entry.getValue())
                            .forEach(entityLabel::addLine);

                    //entityLabel.getParent().setHeight(entityLabel.getLines().length * (entityLabel.getTheme().getFontRenderer().getFontHeight()+1) + 3);
                }
            }
        });
        entityList.addChild(entityLabel);
        entityList.setPinnable(true);
        entityLabel.setShadow(true);
        frames.add(entityList);

        /*
         * Coordinates
         */
        Frame coords  = new Frame(getTheme(), new Stretcherlayout(1), "Coordinates");
        coords.setCloseable(false);
        coords.setPinnable(true);
        Label coordsLabel = new Label("");
        coordsLabel.addTickListener(() -> {
            EntityPlayer player;
            if (Wrapper.getMinecraft().getRenderViewEntity() instanceof EntityPlayer) {
                player = (EntityPlayer) Wrapper.getMinecraft().getRenderViewEntity();
            } else {
                player = Wrapper.getPlayer();
            }
            if (player == null) return;

            boolean inHell = player.dimension == -1;

            int posX = (int) player.posX;
            int posY = (int) player.posY;
            int posZ = (int) player.posZ;

            float f = !inHell ? 0.125f : 8;
            int hposX = (int) (player.posX * f);
            int hposZ = (int) (player.posZ * f);

            /* The 7 and f in the string formatter is the color */
            String colouredSeparator = KamiMod.color + "7 " + KamiMod.separator + KamiMod.color + "r";
            String ow = String.format("%sf%,d%s7, %sf%,d%s7, %sf%,d %s7",
                    KamiMod.color,
                    posX,
                    KamiMod.color,
                    KamiMod.color,
                    posY,
                    KamiMod.color,
                    KamiMod.color,
                    posZ,
                    KamiMod.color
            );
            String nether = String.format(" (%sf%,d%s7, %sf%,d%s7, %sf%,d%s7)",
                    KamiMod.color,
                    hposX,
                    KamiMod.color,
                    KamiMod.color,
                    posY,
                    KamiMod.color,
                    KamiMod.color,
                    hposZ,
                    KamiMod.color
            );
            coordsLabel.setText("");
            coordsLabel.addLine(ow);
            coordsLabel.addLine(MathUtils.getPlayerCardinal(player).cardinalName + colouredSeparator + nether);
        });
        coords.addChild(coordsLabel);
        coordsLabel.setShadow(true);
        coords.setHeight(20);
        frames.add(coords);

        /*
         * Radar
         */
        final Frame radar = new Frame(getTheme(), new Stretcherlayout(1), "Radar");
        radar.setCloseable(false);
        radar.setMinimizeable(true);
        radar.setPinnable(true);
        radar.addChild(new Radar());
        radar.setWidth(100);
        radar.setHeight(100);
        frames.add(radar);

        for (Frame frame1 : frames) {
            frame1.setX(x);
            frame1.setY(y);

            nexty = Math.max(y + frame1.getHeight() + 10, nexty);
            x += frame1.getWidth() + 10;
            if (x * DisplayGuiScreen.getScale() > Wrapper.getMinecraft().displayWidth / 1.2f) {
                y = nexty;
                nexty = y;
                x = 10;
            }

            addChild(frame1);
        }
    }

    @Override
    public void destroyGUI() {
        kill();
    }
}