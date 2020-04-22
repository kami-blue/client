package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.util.WebUtils;

import java.util.Arrays;
import java.util.List;

import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;

/**
 * Created by dominikaaaa on 01/12/2019.
 * Revamped by d1gress/Qther on 13 April 2020
 */

public class CreditsCommand extends Command {

    public CreditsCommand() {
        super("credits", null);
        setDescription("Prints KAMI Blue's authors and contributors");
    }

    @Override
    public void call(String[] args) {
        List<Integer> exceptions = Arrays.asList(
                17222512, // dominikaaaa / dominika
                27009727, // zeroeightysix
                48992448, // blockparole
                37771542, // iBuyMountainDew
                19880089, // EmotionalLove
                55198830, 24369412, // d1gress and Vonr
                51212427, // Cuhnt
                11698651, // jacksonellworth03
                44139104, // TheBritishMidget
                59456376, // Hamburger2k
                41800112, // PretendingToCode
                52386117, // Bluskript
                26636167, // AnotherEntity
                22961592, // ArisaSnowbell
                58238984, // Itistheend

                // Bots
                27856297 // dependabot
                );
        String message =
        "\nName (Github if not same as name)" +
                "\n&l&9Author:" +
                "\n086 (zeroeightysix)" +
                "\n&l&9Contributors:" +
                "\nDominika (dominikaaaa)" +
                "\nhub (blockparole)" +
                "\nDewy (iBuyMountainDew)" +
                "\nSasha (EmotionalLove)" +
                "\nQther (d1gress / Vonr)" +
                "\ncats (Cuhnt)" +
                "\nJack (jacksonellsworth03)" +
                "\nTheBritishMidget (TBM)" +
                "\nHamburger (Hamburger2k)" +
                "\n0x2E (PretendingToCode)" +
                "\nBattery Settings (Bluskript)" +
                "\nAn-En (AnotherEntity)" +
                "\nArisa (Arisa-Snowbell)" +
                "\nJamie (jamie27)" +
                "\nWaizy (WaizyNet)" +
                "\nIt is the end (Itistheend)";

                for (WebUtils.GithubUser u : WebUtils.getContributors(exceptions)) {
                    message = message.concat("\n" + u.login);
                }

                sendChatMessage(message);
    }
}
