package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import org.apache.commons.lang3.StringUtils;
import org.jline.utils.Log;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage;

/**
 * @author dominikaaaa
 * Updated by dominikaaaa on 07/02/20
 * Updated by fw4hre0xxq 2020-09-16
 */
public class TroubleshootCommand extends Command {
    public TroubleshootCommand() {
        super("troubleshoot", new ChunkBuilder().append("filter").append("minified").build(), "tsc");
        setDescription("Prints troubleshooting information");
    }

    @Override
    public void call(String[] args) {

        AtomicReference<String> enabled = new AtomicReference<>("");
        AtomicReference<String> modInfoStr = new AtomicReference<>("");
        Module[] mods = ModuleManager.getModules();

        Boolean minifyOutput = args.length > 2 ? Boolean.TRUE : Boolean.FALSE;

        String f = "";
        if (args[0] != null) f = "(filter: " + args[0] + ")";

        for (Module module : mods) {
            if (args[0] == null) {
                if (module.isEnabled()) {
                    enabled.set(enabled + module.name.getValue() + ", ");
                }
            } else {
                if (module.isEnabled() && Pattern.compile(args[0], Pattern.CASE_INSENSITIVE).matcher(module.name.getValue()).find()) {
                    enabled.set(enabled + module.name.getValue() + ", ");
                }
            }
        }
        enabled.set(StringUtils.chop(StringUtils.chop(String.valueOf(enabled)))); // this looks horrible but I don't know how else to do it sorry

        if (minifyOutput) {
            sendChatMessage("Enabled modules: " + f + "\n" + TextFormatting.GRAY + enabled);
            return;
        }

        if (Loader.instance().getActiveModList().size() > 5) { //the 5 always active mods are listed a couple lines down
            for (ModContainer mod : Loader.instance().getActiveModList()) {
                if (Stream.of("minecraft", "mcp", "FML", "forge", "kamiblue").anyMatch(s -> mod.getModId().contains(s))) {
                    continue;
                } // exclude obvious and redundant
                modInfoStr.set(modInfoStr + mod.getName() + " " + mod.getVersion() + " | ");
            }
        }

        sendChatMessage("Troubleshooting Information:");
        sendChatMessage(KamiMod.MODNAME + " " + KamiMod.KAMI_KANJI + " " + KamiMod.VER_FULL_BETA);
        sendChatMessage("Forge Version: " + ForgeVersion.getMajorVersion() + "." + ForgeVersion.getMinorVersion() + "." + ForgeVersion.getRevisionVersion() + "." + ForgeVersion.getBuildVersion());
        if (FMLCommonHandler.instance().getModName().contains("LiteLoader")) sendChatMessage("LiteLoader found!");
        sendChatMessage("Enabled modules: " + f + "\n" + TextFormatting.GRAY + enabled);
        sendChatMessage("Other Forge mods:" + modInfoStr);
        sendChatMessage("Java: " + System.getProperty("java.runtime.name") + " " + System.getProperty("java.runtime.version") + " " + System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.vendor"));
        sendChatMessage("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + "CPU: " + OpenGlHelper.getCpu() + "GPU: " + GlStateManager.glGetString(GL11.GL_VENDOR) + " / " + GlStateManager.glGetString(GL11.GL_RENDERER));
        sendChatMessage("Please send a screenshot of the full output to the developer or moderator who's helping you!");
        }
    }

