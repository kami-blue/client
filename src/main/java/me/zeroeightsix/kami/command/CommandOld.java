package me.zeroeightsix.kami.command;

import me.zeroeightsix.kami.command.syntax.SyntaxChunk;
import net.minecraft.client.Minecraft;

import java.util.Arrays;
import java.util.List;

public abstract class CommandOld {

    protected String label;
    protected String syntax;
    protected String description;
    protected List<String> aliases;

    public final Minecraft mc = Minecraft.getMinecraft();

    protected SyntaxChunk[] syntaxChunks;

    public CommandOld(String label, SyntaxChunk[] syntaxChunks, String... aliases) {
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

    public String getLabel() {
        return label;
    }

    public abstract void call(String[] args);

    public SyntaxChunk[] getSyntaxChunks() {
        return syntaxChunks;
    }

    public List<String> getAliases() {
        return aliases;
    }

}
