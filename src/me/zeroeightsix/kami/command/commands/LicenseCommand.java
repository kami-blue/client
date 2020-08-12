// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.syntax.SyntaxChunk;
import me.zeroeightsix.kami.command.Command;

public class LicenseCommand extends Command
{
    public LicenseCommand() {
        super("license", null);
        this.setDescription("Prints KAMI Blue's license");
    }
    
    @Override
    public void call(final String[] args) {
        Command.sendChatMessage("License: AstraMod on top, Ezzz");
    }
}
