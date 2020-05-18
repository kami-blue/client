package me.zeroeightsix.kami.module.modules.client

import baritone.api.BaritoneAPI
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MessageSendHelper

/**
 * Created by Dewy on the 21st of April, 2020
 */
@Module.Info(
        name = "Baritone",
        category = Module.Category.CLIENT,
        description = "Configures Baritone settings",
        showOnArray = Module.ShowOnArray.OFF
)
class Baritone : Module() {
    var pauseDuringAutoEat: Setting<Boolean> = register(Settings.b("Pause During AutoEat", true))
    var pauseForAura: Setting<Boolean> = register(Settings.b("Pause For Aura", false))

    private var allowBreak = register(Settings.b("Allow Break", true))
    private var allowSprint = register(Settings.b("Allow Sprint", true))
    private var allowPlace = register(Settings.b("Allow Place", true))
    private var allowInventory = register(Settings.b("Allow Inventory", true))
    private var freeLook = register(Settings.b("Free Look", true))
    private var allowDownwardTunneling = register(Settings.b("Downward Tunneling", true))
    private var allowParkour = register(Settings.b("Allow Parkour", true))
    private var allowParkourPlace = register(Settings.b("Allow Parkour Place", true))
    private var avoidPortals = register(Settings.b("Avoid Portals", false))
    private var mapArtMode = register(Settings.b("Map Art Mode", false))
    private var renderGoal = register(Settings.b("Render Goals", true))

    public override fun onDisable() {
        MessageSendHelper.sendErrorMessage("Error: The Baritone module is for configuring Baritone integration, not toggling it.")
        enable()
    }

    // ._.
    override fun onUpdate() {
        BaritoneAPI.getSettings().allowBreak.value = allowBreak.value
        BaritoneAPI.getSettings().allowSprint.value = allowSprint.value
        BaritoneAPI.getSettings().allowPlace.value = allowPlace.value
        BaritoneAPI.getSettings().allowInventory.value = allowInventory.value
        BaritoneAPI.getSettings().freeLook.value = freeLook.value
        BaritoneAPI.getSettings().allowDownward.value = allowDownwardTunneling.value
        BaritoneAPI.getSettings().allowParkour.value = allowParkour.value
        BaritoneAPI.getSettings().allowParkourPlace.value = allowParkourPlace.value
        BaritoneAPI.getSettings().enterPortal.value = !avoidPortals.value
        BaritoneAPI.getSettings().mapArtMode.value = mapArtMode.value
        BaritoneAPI.getSettings().renderGoal.value = renderGoal.value
    }
}