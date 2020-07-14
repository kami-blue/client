package me.humboldt123.installer;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.util.WebHelper;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

/**
 * Created by humboldt123 on 14/07/20
 * Updated by dominikaaaa on 14/07/20
 * TODO: Remove old jars and warn
 * TODO: Warn about Forge not installed
 * TODO: Automatically-closing prompt when downloading
 */
public class Installer extends JPanel {
    public static void main(String[] args) throws IOException {
        System.out.println("Ran the " + KamiMod.MODNAME + " " + KamiMod.VER_FULL_BETA + " installer!");

        Path modsFolder = Paths.get(getModsFolder());

        if (Files.notExists(modsFolder)) {
            new File(getModsFolder()).mkdirs();
            // make warning about not having forge yada yada
        }
        //  in this space the mods folder is ensured to exist

        URL kamiLogo = Installer.class.getResource("/installer/kami.png");
        JFrame frame = new JFrame("KAMI Blue Installer");
        frame.setIconImage(ImageIO.read(kamiLogo));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new Installer());
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
    }

    /**
     * @throws IOException won't happen due to the files being inside the jar themselves
     */
    private Installer() throws IOException {
        JButton stableButton = new JButton();
        JButton betaButton = new JButton();
        Random rand = new Random();

        String installedStable = "The latest stable version of KAMI Blue was installed. You need to have Forge installed " +
                "\nto run it if you do not already. If you wish to install a separate version of KAMI;" +
                "\nmake sure to delete the already existing KAMI in your mods folder (" + getModsFolder() + ")";
        String installedBeta = "The latest beta version of KAMI Blue was installed. You need to have Forge installed " +
                "\nto run it if you do not already. If you wish to install a separate version of KAMI;" +
                "\nmake sure to delete the already existing KAMI in your mods (" + getModsFolder() + ")";

        stableButton.setOpaque(false);
        stableButton.setContentAreaFilled(false);
        stableButton.setBorderPainted(false);
        betaButton.setOpaque(false);
        betaButton.setContentAreaFilled(false);
        betaButton.setBorderPainted(false);

        stableButton.setToolTipText("This version of KAMI Blue is the latest major release");
        betaButton.setToolTipText("A beta version of KAMI Blue, with frequent updates and bug fixes");

        URL backgroundImage = Installer.class.getResource("/installer/0" + rand.nextInt(4) + ".png");
        JLabel backgroundPane = new JLabel(new ImageIcon(ImageIO.read(backgroundImage)));

        URL stableButtonImage = Installer.class.getResource("/installer/stable.png");
        JLabel stableButtonIcon = new JLabel(new ImageIcon(ImageIO.read(stableButtonImage)));

        URL betaButtonImage = Installer.class.getResource("/installer/beta.png");
        JLabel betaButtonIcon = new JLabel(new ImageIcon(ImageIO.read(betaButtonImage)));

        URL kamiImage = Installer.class.getResource("/installer/kami.png");
        JLabel kamiIcon = new JLabel(new ImageIcon(ImageIO.read(kamiImage)));

        URL breadImage = Installer.class.getResource("/installer/bread.png");
        JLabel breadIcon = new JLabel(new ImageIcon(ImageIO.read(breadImage)));

        setPreferredSize(new Dimension(600, 335));
        setLayout(null);

        add(stableButton);
        add(betaButton);
        add(stableButtonIcon);
        add(betaButtonIcon);
        add(kamiIcon);

        int bread = rand.nextInt(50);
        if (bread == 1) { /* easter egg :3 */
            add(breadIcon);
        }

        add(backgroundPane); // Add this *LAST* so renders over everything else.

        stableButtonIcon.setBounds(70, 245, 200, 50);
        stableButton.setBounds(70, 245, 200, 50);
        betaButtonIcon.setBounds(310, 245, 200, 50);
        betaButton.setBounds(310, 245, 200, 50);
        kamiIcon.setBounds(248, 70, 128, 128);
        breadIcon.setBounds(200, 150, 128, 128);
        backgroundPane.setBounds(0, 0, 600, 355);

        stableButton.addActionListener(e -> {
            stableButton.disable();
            betaButton.disable();
            stableButtonIcon.setOpaque(false);
            betaButtonIcon.setOpaque(false);
            download(VersionType.STABLE);
            notify(installedStable);
            System.exit(0);
        });

        betaButton.addActionListener(e -> {
            stableButton.disable();
            betaButton.disable();
            stableButtonIcon.setOpaque(false);
            betaButtonIcon.setOpaque(false);
            download(VersionType.BETA);
            notify(installedBeta);
            System.exit(0);
        });
    }

    /**
     * This wasn't supposed to be hardcoded, but we cannot include gson in the shadowjar because Minecraft already provides it
     * And the installer does not have access to Minecraft's libraries when run, so we are forced to manually parse the json
     * <p>
     * 5 = stable name
     * 9 = stable url
     * 15 = beta name
     * 19 = beta url
     *
     * @param version which type you want to download, stable or the beta
     */
    public void download(VersionType version) {
        String[] downloadsAPI = WebHelper.INSTANCE.getUrlContents(KamiMod.DOWNLOADS_API).replace("\n", "").split("\"");
        if (version == VersionType.STABLE) {
            try {
                WebHelper.INSTANCE.downloadUsingNIO(downloadsAPI[9], getModsFolder() + getFullJarName(downloadsAPI[9]));
            } catch (IOException e) {
                notify("Error when downloading, couldn't connect to URL. Firewall / ISP is blocking it or you're offline");
                e.printStackTrace();
                System.exit(1);
            }
        } else if (version == VersionType.BETA) {
            try {
                WebHelper.INSTANCE.downloadUsingNIO(downloadsAPI[19], getModsFolder() + getFullJarName(downloadsAPI[19]));
            } catch (IOException e) {
                notify("Error when downloading, couldn't connect to URL. Firewall / ISP is blocking it or you're offline");
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            notify("Error when downloading, invalid VersionType entered!");
            throw new IllegalStateException();
        }

    }

    /**
     * @param message that you want to display to the user
     */
    private static void notify(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    /**
     * @return the minecraft/mods folder specific to the OS
     */
    private static String getModsFolder() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return System.getenv("APPDATA") + File.separator + ".minecraft" + File.separator;
        } else if (System.getProperty("os.name").toLowerCase().contains("nux")) {
            return System.getProperty("user.home") + "/.minecraft/mods/";
        } else if (System.getProperty("os.name").toLowerCase().contains("darwin") || System.getProperty("os.name").toLowerCase().contains("mac")) {
            return System.getProperty("user.home") + "/Library/Application Support/minecraft/mods";
        } else if (System.getProperty("os.name").toLowerCase().contains("temple")) {
            throw new RuntimeException("They glow in the dark!");
        }
        notify("Couldn't detect Minecraft folder, not on standard *NIX / OSX / Windows. Report this to the developers!");
        throw new RuntimeException("Cannot find Minecraft folder!");
    }

    /**
     * @param url jar download url
     * @return the last section of the url, ie the full file name
     */
    private static String getFullJarName(String url) {
        String[] split = url.split("/");
        return split[split.length - 1];
    }

    private enum VersionType {
        STABLE, BETA
    }
}

