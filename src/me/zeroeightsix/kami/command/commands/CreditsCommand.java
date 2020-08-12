// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.syntax.SyntaxChunk;
import me.zeroeightsix.kami.command.Command;

public class CreditsCommand extends Command
{
    public CreditsCommand() {
        super("credits", null);
        this.setDescription("Prints KAMI Blue's authors and contributors");
    }
    
    @Override
    public void call(final String[] args) {
        Command.sendChatMessage("&3Author: &9CrawlerExE and Tux");
    }
}
