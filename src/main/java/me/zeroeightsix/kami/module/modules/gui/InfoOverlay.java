package me.zeroeightsix.kami.module.modules.gui;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.modules.movement.TimerSpeed;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.InfoCalculator;
import me.zeroeightsix.kami.util.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;

import static me.zeroeightsix.kami.command.Command.sendDisableMessage;

/**
 * @author S-B99
 * Created by S-B99 on 04/12/19
 * Updated by S-B99 on 05/03/20
 * PVP Information by Polymer on 04/03/20
 */
@Module.Info(name = "InfoOverlay", category = Module.Category.GUI, description = "Configures the game information overlay", showOnArray = Module.ShowOnArray.OFF)
public class InfoOverlay extends Module {
    /* This is so horrible but there's no other way */
    private Setting<Page> page = register(Settings.enumBuilder(Page.class).withName("Page").withValue(Page.ONE).build());
    /* Page One */
    private Setting<Boolean> version = register(Settings.booleanBuilder("Version").withValue(true).withVisibility(v -> page.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> username = register(Settings.booleanBuilder("Username").withValue(true).withVisibility(v -> page.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> tps = register(Settings.booleanBuilder("TPS").withValue(true).withVisibility(v -> page.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> fps = register(Settings.booleanBuilder("FPS").withValue(true).withVisibility(v -> page.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> ping = register(Settings.booleanBuilder("Ping").withValue(false).withVisibility(v -> page.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> durability = register(Settings.booleanBuilder("Item Damage").withValue(false).withVisibility(v -> page.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> memory = register(Settings.booleanBuilder("RAM Used").withValue(false).withVisibility(v -> page.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> timerSpeed = register(Settings.booleanBuilder("Timer Speed").withValue(false).withVisibility(v -> page.getValue().equals(Page.ONE)).build());
    /* Page Two */
    private Setting<Boolean> totems = register(Settings.booleanBuilder("Totems").withValue(false).withVisibility(v -> page.getValue().equals(Page.TWO)).build());
    private Setting<Boolean> endCrystals = register(Settings.booleanBuilder("End Crystals").withValue(false).withVisibility(v -> page.getValue().equals(Page.TWO)).build());
    private Setting<Boolean> expBottles = register(Settings.booleanBuilder("EXP Bottles").withValue(false).withVisibility(v -> page.getValue().equals(Page.TWO)).build());
    private Setting<Boolean> godApples = register(Settings.booleanBuilder("God Apples").withValue(false).withVisibility(v -> page.getValue().equals(Page.TWO)).build());
    /* Page Three */
    private Setting<Boolean> speed = register(Settings.booleanBuilder("Speed").withValue(true).withVisibility(v -> page.getValue().equals(Page.THREE)).build());
    private Setting<SpeedUnit> speedUnit = register(Settings.enumBuilder(SpeedUnit.class).withName("Speed Unit").withValue(SpeedUnit.KMH).withVisibility(v -> page.getValue().equals(Page.THREE) && speed.getValue()).build());
    private Setting<Boolean> time = register(Settings.booleanBuilder("Time").withValue(true).withVisibility(v -> page.getValue().equals(Page.THREE)).build());
    private Setting<TimeUtil.TimeType> timeTypeSetting = register(Settings.enumBuilder(TimeUtil.TimeType.class).withName("Time Format").withValue(TimeUtil.TimeType.HHMMSS).withVisibility(v -> page.getValue().equals(Page.THREE) && time.getValue()).build());
    private Setting<TimeUtil.TimeUnit> timeUnitSetting = register(Settings.enumBuilder(TimeUtil.TimeUnit.class).withName("Time Unit").withValue(TimeUtil.TimeUnit.H12).withVisibility(v -> page.getValue().equals(Page.THREE) && time.getValue()).build());
    private Setting<Boolean> doLocale = register(Settings.booleanBuilder("Time Show AMPM").withValue(true).withVisibility(v -> page.getValue().equals(Page.THREE) && time.getValue()).build());
    public Setting<TextFormatting> firstColour = register(Settings.enumBuilder(TextFormatting.class).withName("First Colour").withValue(TextFormatting.valueOf("WHITE")).withVisibility(v -> page.getValue().equals(Page.THREE)).build());
    public Setting<TextFormatting> secondColour = register(Settings.enumBuilder(TextFormatting.class).withName("Second Colour").withValue(TextFormatting.valueOf("BLUE")).withVisibility(v -> page.getValue().equals(Page.THREE)).build());

    private enum SpeedUnit { MPS, KMH }

    private enum Page { ONE, TWO, THREE }

    public boolean useUnitKmH() {
        return speedUnit.getValue().equals(SpeedUnit.KMH);
    }

    private String unitType(SpeedUnit s) {
        switch (s) {
            case MPS: return "m/s";
            case KMH: return "km/h";
            default: return "Invalid unit type (mps or kmh)";
        }
    }

    private String formatTimerSpeed() {
        String formatted = getStringColour(secondColour.getValue()) + "." + getStringColour(firstColour.getValue());
        return TimerSpeed.returnGui().replace(".", formatted);
    }

    public static String getStringColour(TextFormatting c) {
        return c.toString();
    }

    public static int getItems(Item i) {
        return mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == i).mapToInt(ItemStack::getCount).sum() + mc.player.inventory.offHandInventory.stream().filter(itemStack -> itemStack.getItem() == i).mapToInt(ItemStack::getCount).sum();
    }

    public ArrayList<String> infoContents() {
        ArrayList<String> infoContents = new ArrayList<>();
        if (version.getValue()) {
            infoContents.add(getStringColour(firstColour.getValue()) + KamiMod.KAMI_KANJI + getStringColour(secondColour.getValue()) + " " + KamiMod.MODVERSMALL);
        } if (username.getValue()) {
            infoContents.add(getStringColour(firstColour.getValue()) + "Welcome" + getStringColour(secondColour.getValue()) + " " + mc.getSession().getUsername() + "!");
        } if (time.getValue()) {
            infoContents.add(getStringColour(firstColour.getValue()) + TimeUtil.getFinalTime(secondColour.getValue(), firstColour.getValue(), timeUnitSetting.getValue(), timeTypeSetting.getValue(), doLocale.getValue()) + TextFormatting.RESET);
        } if (tps.getValue()) {
            infoContents.add(getStringColour(firstColour.getValue()) + InfoCalculator.tps() + getStringColour(secondColour.getValue()) + " tps");
        } if (fps.getValue()) {
            infoContents.add(getStringColour(firstColour.getValue()) + Minecraft.debugFPS + getStringColour(secondColour.getValue()) + " fps");
        } if (speed.getValue()) {
            infoContents.add(getStringColour(firstColour.getValue()) + InfoCalculator.speed(useUnitKmH()) + getStringColour(secondColour.getValue()) + " " + unitType(speedUnit.getValue()));
        } if (timerSpeed.getValue()) {
            infoContents.add(getStringColour(firstColour.getValue()) + formatTimerSpeed() + getStringColour(secondColour.getValue()) + "t");
        } if (ping.getValue()) {
            infoContents.add(getStringColour(firstColour.getValue()) + InfoCalculator.ping() + getStringColour(secondColour.getValue()) + " ms");
        } if (durability.getValue()) {
            infoContents.add(getStringColour(firstColour.getValue()) + InfoCalculator.dura() + getStringColour(secondColour.getValue()) + " dura");
        } if (memory.getValue()) {
            infoContents.add(getStringColour(firstColour.getValue()) + InfoCalculator.memory() + getStringColour(secondColour.getValue()) + "mB free");
        } if (totems.getValue()) {
        	infoContents.add(getStringColour(firstColour.getValue()) + getItems(Items.TOTEM_OF_UNDYING) + getStringColour(secondColour.getValue()) + " Totems");
        } if (endCrystals.getValue()) {
        	infoContents.add(getStringColour(firstColour.getValue()) + getItems(Items.END_CRYSTAL) + getStringColour(secondColour.getValue()) + " Crystals");
        } if (expBottles.getValue()) {
        	infoContents.add(getStringColour(firstColour.getValue()) + getItems(Items.EXPERIENCE_BOTTLE) + getStringColour(secondColour.getValue()) + " EXP Bottles");
        } if (godApples.getValue()) {
        	infoContents.add(getStringColour(firstColour.getValue()) + getItems(Items.GOLDEN_APPLE) + getStringColour(secondColour.getValue()) + " God Apples");
        }
        return infoContents;
    }

    public void onDisable() { sendDisableMessage(getName()); }
}
