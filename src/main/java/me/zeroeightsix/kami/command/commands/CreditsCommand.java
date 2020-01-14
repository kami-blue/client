package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;

/**
 * Created by S-B99 on 01/12/2019.
 */

public class CreditsCommand extends Command {

    public CreditsCommand() {
        super("credits", null, "creds");
        setDescription("Prints KAMI Blue's authors and contributors");
    }

    @Override
    public void call(String[] args) {
        Command.sendChatMessage("\n&l&9Author: \n&b086\n&l&9Contributors: \n&bBella (S-B99)\n&bhub (blockparole)\n&bQther (d1gress)\n&bSasha (EmotionalLove)\n&bHHSGPA\n&bcats (Cuhnt)\n&b20kdc\n&bVonr\n&bKatatje\n&bDeauthorized\n&bkdb424\n&bElementars\n&bfsck\n&bJamie (jamie27)\n&bTBM\n&bWaizy\n&bcookiedragon234\n&b0x2E (PretendingToCode)");
    }
}
