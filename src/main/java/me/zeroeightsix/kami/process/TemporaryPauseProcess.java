package me.zeroeightsix.kami.process;

import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.module.modules.client.Baritone;
import me.zeroeightsix.kami.module.modules.combat.Aura;
import me.zeroeightsix.kami.module.modules.player.AutoEat;
import me.zeroeightsix.kami.module.modules.player.LagNotifier;

/**
 * Created by Dewy on the 17th of May, 2020
 *
 * thanks leijurv for pseudocode
 */
public class TemporaryPauseProcess implements IBaritoneProcess
{
    @Override
    public boolean isTemporary()
    {
        return true;
    }

    @Override
    public double priority()
    {
        return 1;
    }

    @Override
    public boolean isActive()
    {
        return (KamiMod.MODULE_MANAGER.isModuleEnabled(AutoEat.class)
                && KamiMod.MODULE_MANAGER.getModuleT(AutoEat.class).getEating()
                && KamiMod.MODULE_MANAGER.getModuleT(AutoEat.class).getPauseBaritone().getValue())
                ||
                (KamiMod.MODULE_MANAGER.isModuleEnabled(LagNotifier.class)
                && !KamiMod.MODULE_MANAGER.getModuleT(LagNotifier.class).getHasUnpaused()
                && KamiMod.MODULE_MANAGER.getModuleT(LagNotifier.class).getPauseBaritone().getValue())
                ||
                (KamiMod.MODULE_MANAGER.isModuleEnabled(Aura.class)
                && KamiMod.MODULE_MANAGER.getModuleT(Aura.class).isAttacking()
                && KamiMod.MODULE_MANAGER.getModuleT(Aura.class).getPauseBaritone().getValue());
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel)
    {
        if (!isSafeToCancel)
        {
            KamiMod.MODULE_MANAGER.getModuleT(AutoEat.class).setEating(false);
        }

        return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
    }

    @Override
    public void onLostControl()
    {
        // nothing :p
    }

    @Override
    public String displayName0()
    {
        return "Kami Blue Pauser";
    }
}
