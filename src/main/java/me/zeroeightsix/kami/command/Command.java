package me.zeroeightsix.kami.command;

import me.zeroeightsix.kami.command.syntax.SyntaxChunk;
import me.zeroeightsix.kami.module.modules.client.CommandConfig;
import me.zeroeightsix.kami.setting.impl.primitive.StringSetting;
import net.minecraft.client.Minecraft;

import java.util.Arrays;
import java.util.List;

public abstract class Command {

    protected String label;
    protected String syntax;
    protected String description;
    protected List<String> aliases;

    public final Minecraft mc = Minecraft.getMinecraft();

    protected SyntaxChunk[] syntaxChunks;

    public final static StringSetting commandPrefix = CommandConfig.INSTANCE.getCommandPrefix();

    public Command(String label, SyntaxChunk[] syntaxChunks, String... aliases) {
        this.label = label;
        this.syntaxChunks = syntaxChunks;
        this.description = "Descriptionless";
        this.aliases = Arrays.asList(aliases);
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

    public String getChatLabel() {
        return "[" + label + "] ";
    }

    public abstract void call(String[] args);

    public SyntaxChunk[] getSyntaxChunks() {
        return syntaxChunks;
    }

    public List<String> getAliases() {
        return aliases;
    }
}
