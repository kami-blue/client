package me.zeroeightsix.kami.event;

import me.zero.alpine.type.Cancellable;
import me.zeroeightsix.kami.util.Wrapper;

/**
 * Created by 086 on 16/11/2017.
 */
public class KamiEvent extends Cancellable {

    private Era era = Era.PRE;
    private final float partialTicks;

    public KamiEvent() {
        partialTicks = Wrapper.getMinecraft().getRenderPartialTicks();
    }

    public Era getEra() {
        return era;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public enum Era {
        PRE, PERI, POST
    }

}
