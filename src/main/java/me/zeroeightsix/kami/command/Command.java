package me.zeroeightsix.kami.command;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.syntax.SyntaxChunk;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.LogWrapper;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentBase;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

public abstract class Command {

    protected String label;
    protected String syntax;
    protected String description;
    protected List<String> aliases;

    public final Minecraft mc = Minecraft.getMinecraft();

    protected SyntaxChunk[] syntaxChunks;

    public static Setting<String> commandPrefix = Settings.s("commandPrefix", ".");

    public Command(String label, SyntaxChunk[] syntaxChunks, String... aliases) {
        this.label = label;
        this.syntaxChunks = syntaxChunks;
        this.description = "Descriptionless";
        this.aliases = Arrays.asList(aliases);
    }

    public static void sendChatMessage(String message) {
        sendRawChatMessage("&7[&9" + KamiMod.KAMI_KANJI + "&7] &r" + message);
    }

    public static void sendWarningMessage(String message) {
        sendRawChatMessage("&7[&6" + KamiMod.KAMI_KANJI + "&7] &r" + message);
    }

    public static void sendErrorMessage(String message) {
        sendRawChatMessage("&7[&4" + KamiMod.KAMI_KANJI + "&7] &r" + message);
    }

    public static void sendCustomMessage(String message, String colour) {
        sendRawChatMessage("&7[" + colour + KamiMod.KAMI_KANJI + "&7] &r" + message);
    }

    public static void sendStringChatMessage(String[] messages) {
        sendChatMessage("");
        for (String s : messages) sendRawChatMessage(s);
    }

    public static void sendDisableMessage(Class clazz) {
        sendErrorMessage("Error: The " + MODULE_MANAGER.getModule(clazz).getName() + " module is only for configuring the GUI element. In order to show the GUI element you need to hit the pin in the upper left of the GUI element");
        MODULE_MANAGER.getModule(clazz).enable();
    }

    public static void sendRawChatMessage(String message) {
        if (Minecraft.getMinecraft().player != null) {
            Wrapper.getPlayer().sendMessage(new ChatMessage(message));
        } else {
            LogWrapper.info(message);
        }
    }

    public static void sendServerMessage(String message) {
        if (Minecraft.getMinecraft().player != null) {
            Wrapper.getPlayer().connection.sendPacket(new CPacketChatMessage(message));
        } else {
            LogWrapper.warning("Could not send server message: \"" + message + "\"");
        }
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static String getCommandPrefix() {
        return commandPrefix.getValue();
    }

    public String getLabel() {
        return label;
    }

    public abstract void call(String[] args);

    public SyntaxChunk[] getSyntaxChunks() {
        return syntaxChunks;
    }

    public static class ChatMessage extends TextComponentBase {

        String text;

        public ChatMessage(String text) {

            Pattern p = Pattern.compile("&[0123456789abcdefrlosmk]");
            Matcher m = p.matcher(text);
            StringBuffer sb = new StringBuffer();

            while (m.find()) {
                String replacement = "\u00A7" + m.group().substring(1);
                m.appendReplacement(sb, replacement);
            }

            m.appendTail(sb);

            this.text = sb.toString();
        }

        public String getUnformattedComponentText() {
            return text;
        }

        @Override
        public ITextComponent createCopy() {
            return new ChatMessage(text);
        }

    }

    protected SyntaxChunk getSyntaxChunk(String name) {
        for (SyntaxChunk c : syntaxChunks) {
            if (c.getType().equals(name))
                return c;
        }
        return null;
    }

    public List<String> getAliases() {
        return aliases;
    }
}
