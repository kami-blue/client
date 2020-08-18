package me.zeroeightsix.kami.util;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.PacketEvent;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.EventListener;

public class LagCompensator implements EventListener {

    public static LagCompensator INSTANCE;

    private final float[] tickRates = new float[20];
    private int nextIndex = 0;
    private long timeLastTimeUpdate;

    @EventHandler
    Listener<PacketEvent.Receive> packetEventListener = new Listener<>(event -> {
        if (event.getPacket() instanceof SPacketTimeUpdate) {
            INSTANCE.onTimeUpdate();
        }
    });

    public LagCompensator() {
        KamiMod.EVENT_BUS.subscribe(this);
        reset();
    }

    public void reset() {
        this.nextIndex = 0;
        this.timeLastTimeUpdate = -1L;
        Arrays.fill(this.tickRates, 0.0F);
    }

    public float getTickRate() {
        float numTicks = 0.0F;
        float sumTickRates = 0.0F;
        for (float tickRate : this.tickRates) {
            if (tickRate > 0.0F) {
                sumTickRates += tickRate;
                numTicks += 1.0F;
            }
        }
        return MathHelper.clamp(sumTickRates / numTicks, 0.0F, 20.0F);
    }

    public void onTimeUpdate() {
        if (this.timeLastTimeUpdate != -1L) {
            float timeElapsed = (float) (System.currentTimeMillis() - this.timeLastTimeUpdate) / 1000.0F;
            this.tickRates[(this.nextIndex % this.tickRates.length)] = MathHelper.clamp(20.0F / timeElapsed, 0.0F, 20.0F);
            this.nextIndex += 1;
        }
        this.timeLastTimeUpdate = System.currentTimeMillis();
    }
}
