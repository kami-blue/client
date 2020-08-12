// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.chat;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketChatMessage;
import java.util.Iterator;
import java.math.RoundingMode;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.server.SPacketUseBed;
import java.util.function.Predicate;
import net.minecraft.client.gui.GuiGameOver;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.Settings;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.setting.Setting;
import net.minecraft.util.math.Vec3d;
import me.zeroeightsix.kami.event.events.GuiScreenEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import me.zeroeightsix.kami.event.events.PacketEvent;
import java.util.Timer;
import java.util.TimerTask;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Queue;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Annoyer", category = Category.CHAT, description = "Annoyer")
public class Annoyer extends Module
{
    private static boolean isFirstRun;
    private static Queue<String> messageQueue;
    private static Map<String, Integer> minedBlocks;
    private static Map<String, Integer> placedBlocks;
    private static Map<String, Integer> droppedItems;
    private static Map<String, Integer> consumedItems;
    private static DecimalFormat df;
    private static TimerTask timerTask;
    private static Timer timer;
    private static PacketEvent.Receive lastEventReceive;
    private static PacketEvent.Send lastEventSend;
    private static LivingEntityUseItemEvent.Finish lastLivingEntityUseFinishEvent;
    private static GuiScreenEvent.Displayed lastGuiScreenDisplayedEvent;
    private static String lastmessage;
    private static Vec3d thisTickPos;
    private static Vec3d lastTickPos;
    private static double distanceTraveled;
    private static float thisTickHealth;
    private static float lastTickHealth;
    private static float gainedHealth;
    private static float lostHealth;
    private Setting<Boolean> distance;
    private Setting<Integer> mindistance;
    private Setting<Integer> maxdistance;
    private Setting<Boolean> blocks;
    private Setting<Boolean> items;
    private Setting<Boolean> playerheal;
    private Setting<Boolean> playerdamage;
    private Setting<Boolean> playerdeath;
    private Setting<Boolean> greentext;
    private Setting<Boolean> clientName;
    private Setting<Integer> delay;
    private Setting<Integer> queuesize;
    private Setting<Boolean> clearqueue;
    @EventHandler
    public Listener<GuiScreenEvent.Displayed> guiScreenEventDisplayedlistener;
    @EventHandler
    private Listener<PacketEvent.Receive> packetEventReceiveListener;
    @EventHandler
    private Listener<PacketEvent.Send> packetEventSendListener;
    @EventHandler
    public Listener<LivingEntityUseItemEvent.Finish> listener;
    
    public Annoyer() {
        this.distance = this.register(Settings.b("Distance", true));
        this.mindistance = this.register((Setting<Integer>)Settings.integerBuilder("Min Distance").withRange(1, 100).withValue(10).build());
        this.maxdistance = this.register((Setting<Integer>)Settings.integerBuilder("Max Distance").withRange(100, 10000).withValue(150).build());
        this.blocks = this.register(Settings.b("Blocks", true));
        this.items = this.register(Settings.b("Items", true));
        this.playerheal = this.register(Settings.b("Player Heal", true));
        this.playerdamage = this.register(Settings.b("Player Damage", true));
        this.playerdeath = this.register(Settings.b("Death", true));
        this.greentext = this.register(Settings.b("Greentext", false));
        this.clientName = this.register(Settings.b("ClientName", false));
        this.delay = this.register((Setting<Integer>)Settings.integerBuilder("Send Delay").withRange(1, 10).withValue(2).build());
        this.queuesize = this.register((Setting<Integer>)Settings.integerBuilder("Queue Size").withRange(1, 100).withValue(5).build());
        this.clearqueue = this.register(Settings.b("Clear Queue", false));
        String message;
        this.guiScreenEventDisplayedlistener = new Listener<GuiScreenEvent.Displayed>(event -> {
            if (this.isDisabled() || Annoyer.mc.field_71439_g == null || ModuleManager.isModuleEnabled("Freecam")) {
                return;
            }
            else if (Annoyer.lastGuiScreenDisplayedEvent != null && Annoyer.lastGuiScreenDisplayedEvent.equals(event)) {
                return;
            }
            else if (this.playerdeath.getValue() && event.getScreen() instanceof GuiGameOver) {
                if (this.clientName.getValue()) {
                    message = "I just fucking died becuase of my shit client!";
                }
                else {
                    message = "I fucking died!";
                }
                this.queueMessage(message);
                return;
            }
            else {
                Annoyer.lastGuiScreenDisplayedEvent = event;
                return;
            }
        }, (Predicate<GuiScreenEvent.Displayed>[])new Predicate[0]);
        String message2;
        this.packetEventReceiveListener = new Listener<PacketEvent.Receive>(event -> {
            if (this.isDisabled() || Annoyer.mc.field_71439_g == null || ModuleManager.isModuleEnabled("Freecam")) {
                return;
            }
            else if (Annoyer.lastEventReceive != null && Annoyer.lastEventReceive.equals(event)) {
                return;
            }
            else if (event.getPacket() instanceof SPacketUseBed) {
                if (this.clientName.getValue()) {
                    message2 = "I used a sleepy sleepy object, thanks to AstraMod!";
                }
                else {
                    message2 = "I used a sleepy sleepy object!";
                }
                this.queueMessage(message2);
                Annoyer.lastEventReceive = event;
                return;
            }
            else {
                return;
            }
        }, (Predicate<PacketEvent.Receive>[])new Predicate[0]);
        CPacketPlayerDigging p;
        String name;
        String name2;
        String message3;
        ItemStack itemStack;
        String name3;
        this.packetEventSendListener = new Listener<PacketEvent.Send>(event -> {
            if (this.isDisabled() || Annoyer.mc.field_71439_g == null || ModuleManager.isModuleEnabled("Freecam")) {
                return;
            }
            else if (Annoyer.lastEventSend != null && Annoyer.lastEventSend.equals(event)) {
                return;
            }
            else {
                if ((this.items.getValue() || this.blocks.getValue()) && event.getPacket() instanceof CPacketPlayerDigging) {
                    p = (CPacketPlayerDigging)event.getPacket();
                    if (this.items.getValue() && Annoyer.mc.field_71439_g.func_184614_ca().func_77973_b() != Items.field_190931_a && (p.func_180762_c().equals((Object)CPacketPlayerDigging.Action.DROP_ITEM) || p.func_180762_c().equals((Object)CPacketPlayerDigging.Action.DROP_ALL_ITEMS))) {
                        name = Annoyer.mc.field_71439_g.field_71071_by.func_70448_g().func_82833_r();
                        if (Annoyer.droppedItems.containsKey(name)) {
                            Annoyer.droppedItems.put(name, Annoyer.droppedItems.get(name) + 1);
                        }
                        else {
                            Annoyer.droppedItems.put(name, 1);
                        }
                        Annoyer.lastEventSend = event;
                    }
                    else if (this.blocks.getValue() && p.func_180762_c().equals((Object)CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK)) {
                        name2 = Annoyer.mc.field_71441_e.func_180495_p(p.func_179715_a()).func_177230_c().func_149732_F();
                        if (Annoyer.minedBlocks.containsKey(name2)) {
                            Annoyer.minedBlocks.put(name2, Annoyer.minedBlocks.get(name2) + 1);
                        }
                        else {
                            Annoyer.minedBlocks.put(name2, 1);
                        }
                        Annoyer.lastEventSend = event;
                    }
                }
                else if (this.items.getValue() && event.getPacket() instanceof CPacketUpdateSign) {
                    if (this.clientName.getValue()) {
                        message3 = "I placed a Sign, I can finally write! thanks to AstraMod!";
                    }
                    else {
                        message3 = "I placed a Sign, I can finally write!";
                    }
                    this.queueMessage(message3);
                    Annoyer.lastEventSend = event;
                }
                else if (this.blocks.getValue() && event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
                    itemStack = Annoyer.mc.field_71439_g.field_71071_by.func_70448_g();
                    if (itemStack.field_190928_g) {
                        Annoyer.lastEventSend = event;
                    }
                    else if (itemStack.func_77973_b() instanceof ItemBlock) {
                        name3 = Annoyer.mc.field_71439_g.field_71071_by.func_70448_g().func_82833_r();
                        if (Annoyer.placedBlocks.containsKey(name3)) {
                            Annoyer.placedBlocks.put(name3, Annoyer.placedBlocks.get(name3) + 1);
                        }
                        else {
                            Annoyer.placedBlocks.put(name3, 1);
                        }
                        Annoyer.lastEventSend = event;
                    }
                }
                return;
            }
        }, (Predicate<PacketEvent.Send>[])new Predicate[0]);
        String name4;
        this.listener = new Listener<LivingEntityUseItemEvent.Finish>(event -> {
            if (event.getEntity().equals((Object)Annoyer.mc.field_71439_g) && event.getItem().func_77973_b() instanceof ItemFood) {
                name4 = event.getItem().func_82833_r();
                if (Annoyer.consumedItems.containsKey(name4)) {
                    Annoyer.consumedItems.put(name4, Annoyer.consumedItems.get(name4) + 1);
                }
                else {
                    Annoyer.consumedItems.put(name4, 1);
                }
                Annoyer.lastLivingEntityUseFinishEvent = event;
            }
        }, (Predicate<LivingEntityUseItemEvent.Finish>[])new Predicate[0]);
    }
    
    public void onEnable() {
        Annoyer.timer = new Timer();
        if (Annoyer.mc.field_71439_g == null) {
            this.disable();
            return;
        }
        (Annoyer.df = new DecimalFormat("#.#")).setRoundingMode(RoundingMode.CEILING);
        Annoyer.timerTask = new TimerTask() {
            @Override
            public void run() {
                Annoyer.this.sendMessageCycle();
            }
        };
        Annoyer.timer.schedule(Annoyer.timerTask, 0L, this.delay.getValue() * 1000);
    }
    
    public void onDisable() {
        Annoyer.timer.cancel();
        Annoyer.timer.purge();
        Annoyer.messageQueue.clear();
    }
    
    @Override
    public void onUpdate() {
        if (this.isDisabled() || Annoyer.mc.field_71439_g == null || ModuleManager.isModuleEnabled("Freecam")) {
            return;
        }
        if (this.clearqueue.getValue()) {
            this.clearqueue.setValue(false);
            Annoyer.messageQueue.clear();
        }
        this.getGameTickData();
    }
    
    private void getGameTickData() {
        if (this.distance.getValue()) {
            Annoyer.lastTickPos = Annoyer.thisTickPos;
            Annoyer.thisTickPos = Annoyer.mc.field_71439_g.func_174791_d();
            Annoyer.distanceTraveled += Annoyer.thisTickPos.func_72438_d(Annoyer.lastTickPos);
        }
        if (this.playerheal.getValue() || this.playerdamage.getValue()) {
            Annoyer.lastTickHealth = Annoyer.thisTickHealth;
            Annoyer.thisTickHealth = Annoyer.mc.field_71439_g.func_110143_aJ() + Annoyer.mc.field_71439_g.func_110139_bj();
            final float healthDiff = Annoyer.thisTickHealth - Annoyer.lastTickHealth;
            if (healthDiff < 0.0f) {
                Annoyer.lostHealth += healthDiff * -1.0f;
            }
            else {
                Annoyer.gainedHealth += healthDiff;
            }
        }
    }
    
    private void composeGameTickData() {
        if (Annoyer.isFirstRun) {
            Annoyer.isFirstRun = false;
            this.clearTickData();
            return;
        }
        String suffix;
        if (this.clientName.getValue()) {
            suffix = ", thanks to AstraMod!";
        }
        else {
            suffix = "!";
        }
        if (this.distance.getValue() && Annoyer.distanceTraveled >= 1.0) {
            if (Annoyer.distanceTraveled < this.delay.getValue() * this.mindistance.getValue()) {
                return;
            }
            if (Annoyer.distanceTraveled > this.delay.getValue() * this.maxdistance.getValue()) {
                Annoyer.distanceTraveled = 0.0;
                return;
            }
            final StringBuilder sb = new StringBuilder();
            sb.append("I just ran");
            sb.append((int)Annoyer.distanceTraveled);
            if ((int)Annoyer.distanceTraveled == 1) {
                sb.append(" Block from xormios in my new elytra");
            }
            else {
                sb.append(" Blocks from xormios in my new elytra");
            }
            sb.append(suffix);
            this.queueMessage(sb.toString());
            Annoyer.distanceTraveled = 0.0;
        }
        if (this.playerdamage.getValue() && Annoyer.lostHealth != 0.0f) {
            this.queueMessage("I lost " + Annoyer.df.format(Annoyer.lostHealth) + " Health FUCK!" + suffix);
            Annoyer.lostHealth = 0.0f;
        }
        if (this.playerheal.getValue() && Annoyer.gainedHealth != 0.0f) {
            this.queueMessage("I recovered " + Annoyer.df.format(Annoyer.gainedHealth) + " Health ez bro" + suffix);
            Annoyer.gainedHealth = 0.0f;
        }
    }
    
    private void composeEventData() {
        String suffix;
        if (this.clientName.getValue()) {
            suffix = ", thanks to AstraMod!";
        }
        else {
            suffix = "!";
        }
        for (final Map.Entry<String, Integer> kv : Annoyer.minedBlocks.entrySet()) {
            this.queueMessage("I nuked the fuck of  " + kv.getValue() + " " + kv.getKey() + suffix);
            Annoyer.minedBlocks.remove(kv.getKey());
        }
        for (final Map.Entry<String, Integer> kv : Annoyer.placedBlocks.entrySet()) {
            this.queueMessage("I placed " + kv.getValue() + " " + kv.getKey() + suffix);
            Annoyer.placedBlocks.remove(kv.getKey());
        }
        for (final Map.Entry<String, Integer> kv : Annoyer.droppedItems.entrySet()) {
            this.queueMessage("FUCK! I dropped " + kv.getValue() + " " + kv.getKey() + suffix);
            Annoyer.droppedItems.remove(kv.getKey());
        }
        for (final Map.Entry<String, Integer> kv : Annoyer.consumedItems.entrySet()) {
            this.queueMessage("I snacced " + kv.getValue() + " " + kv.getKey() + suffix);
            Annoyer.consumedItems.remove(kv.getKey());
        }
    }
    
    private void sendMessageCycle() {
        if (this.isDisabled() || Annoyer.mc.field_71439_g == null || ModuleManager.isModuleEnabled("Freecam")) {
            return;
        }
        this.composeGameTickData();
        this.composeEventData();
        final Iterator<String> iterator = Annoyer.messageQueue.iterator();
        if (iterator.hasNext()) {
            final String message = iterator.next();
            this.sendMessage(message);
            Annoyer.messageQueue.remove(message);
        }
    }
    
    private void sendMessage(final String s) {
        final StringBuilder sb = new StringBuilder();
        if (this.greentext.getValue()) {
            sb.append("> ");
        }
        sb.append(s);
        Annoyer.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketChatMessage(sb.toString().replaceAll("§", "")));
    }
    
    private void clearTickData() {
        final Vec3d pos = Annoyer.thisTickPos = (Annoyer.lastTickPos = Annoyer.mc.field_71439_g.func_174791_d());
        Annoyer.distanceTraveled = 0.0;
        final float health = Annoyer.thisTickHealth = (Annoyer.lastTickHealth = Annoyer.mc.field_71439_g.func_110143_aJ() + Annoyer.mc.field_71439_g.func_110139_bj());
        Annoyer.lostHealth = 0.0f;
        Annoyer.gainedHealth = 0.0f;
    }
    
    private Block getBlock(final BlockPos pos) {
        return Annoyer.mc.field_71441_e.func_180495_p(pos).func_177230_c();
    }
    
    private void queueMessage(final String message) {
        if (Annoyer.messageQueue.size() > this.queuesize.getValue()) {
            return;
        }
        Annoyer.messageQueue.add(message);
    }
    
    static {
        Annoyer.isFirstRun = true;
        Annoyer.messageQueue = new ConcurrentLinkedQueue<String>();
        Annoyer.minedBlocks = new ConcurrentHashMap<String, Integer>();
        Annoyer.placedBlocks = new ConcurrentHashMap<String, Integer>();
        Annoyer.droppedItems = new ConcurrentHashMap<String, Integer>();
        Annoyer.consumedItems = new ConcurrentHashMap<String, Integer>();
        Annoyer.df = new DecimalFormat();
        Annoyer.lastmessage = "";
        Annoyer.distanceTraveled = 0.0;
    }
}
