package me.humboldt123.installer;

import me.zeroeightsix.kami.KamiMod;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

/**
 * Created by humboldt123 on 14/07/20
 * Updated by dominikaaaa on 14/07/20
 * TODO: Remove old jars and warn
 * TODO: Close on confirmation of installation
 * TODO: Warn about Forge not installed
 * TODO: Fix images
 * TODO: GUI for all exceptions
 */
public class Main extends JPanel {
    private final JButton stableButton;
    private final JButton betaButton;
    private final JLabel stableButtonIcon;
    private final JLabel betaButtonIcon;

    private enum VersionType {
        STABLE, BETA
    }

    public static String getMinecraftFolder() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return System.getenv("APPDATA") + File.separator + ".minecraft" + File.separator;
        } else if (System.getProperty("os.name").toLowerCase().contains("nux")) {
            return System.getProperty("user.home") + File.separator + ".minecraft" + File.separator;
        } else if (System.getProperty("os.name").toLowerCase().contains("darwin") || System.getProperty("os.name").toLowerCase().contains("mac")) {
            return System.getProperty("user.home") + File.separator + "Library" + File.separator + "Application Support"
                    + File.separator + "minecraft" + File.separator;
        } else if (System.getProperty("os.name").toLowerCase().contains("temple")) {
            throw new RuntimeException("They glow in the dark!");
        }
        throw new RuntimeException("Cannot find Minecraft folder!"); // Add fancy GUI here too~!
    }

    public static void download(VersionType version) {
        String[] downloadsAPI = getUrlContents(KamiMod.DOWNLOADS_API).split("\"");
        /**
         * This wasn't supposed to be hardcoded, but we cannot include gson in the shadowjar because Minecraft already provides it
         * And the installer does not have access to Minecraft's libraries when run, so we are forced to manually parse the json
         *
         * 5 = stable name
         * 9 = stable url
         * 15 = beta name
         * 19 = beta url
         */
        if (version == VersionType.STABLE) {
            try {
                downloadUsingNIO(downloadsAPI[9], getMinecraftFolder() + "mods\\kamiblue-" + downloadsAPI[5] + ".jar");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (version == VersionType.BETA) {
            try {
                downloadUsingNIO(downloadsAPI[19], getMinecraftFolder() + "mods\\kamiblue-" + downloadsAPI[15] + ".jar");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else throw new IllegalStateException("Invalid version type!");

    }

    public static void downloadUsingNIO(String urlStr, String file) throws IOException {
        URL url = new URL(urlStr);
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }

    private static String getUrlContents(String _url) {
        StringBuilder content = new StringBuilder();

        try {
            URL url = new URL(_url);

            URLConnection urlConnection = url.openConnection();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }


    public Main() throws IOException {
        stableButton = new JButton();
        betaButton = new JButton();
        Random rand = new Random();

        String installedStable = "The latest stable version of KAMI Blue was installed. You need to have Forge installed " +
                "\nto run it if you do not already. If you wish to install a separate version of KAMI;" +
                "\nmake sure to delete the already existing KAMI in your .minecraft folder (" + getMinecraftFolder() + ")";
        String installedBeta = "The latest beta version of KAMI Blue was installed. You need to have Forge installed " +
                "\nto run it if you do not already. If you wish to install a separate version of KAMI;" +
                "\nmake sure to delete the already existing KAMI in your .minecraft folder (" + getMinecraftFolder() + ")";


        stableButton.setOpaque(false);
        stableButton.setContentAreaFilled(false);
        stableButton.setBorderPainted(false);
        betaButton.setOpaque(false);
        betaButton.setContentAreaFilled(false);
        betaButton.setBorderPainted(false);

        //set components properties
        stableButton.setToolTipText("This version of KAMI Blue is the latest major release");
        betaButton.setToolTipText("A beta version of KAMI Blue, with frequent updates and bug fixes");

        URL backgroundImage = Main.class.getResource("/installer/0" + rand.nextInt(4) + ".png");
        JLabel backgroundPane = new JLabel(new ImageIcon(ImageIO.read(backgroundImage)));

        URL stableButtonImage = Main.class.getResource("/installer/stable.png");
        stableButtonIcon = new JLabel(new ImageIcon(ImageIO.read(stableButtonImage)));

        URL betaButtonImage = Main.class.getResource("/installer/beta.png");
        betaButtonIcon = new JLabel(new ImageIcon(ImageIO.read(betaButtonImage)));

        URL kamiImage = Main.class.getResource("/installer/kami.png");
        JLabel kamiIcon = new JLabel(new ImageIcon(ImageIO.read(kamiImage)));

        URL breadImage = Main.class.getResource("/installer/breaduwu.png");
        JLabel breadIcon = new JLabel(new ImageIcon(ImageIO.read(breadImage)));

        setPreferredSize(new Dimension(600, 335));
        setLayout(null);

        add(stableButton);
        add(betaButton);
        add(stableButtonIcon);
        add(betaButtonIcon);
        add(kamiIcon);

        int bread = rand.nextInt(50);
        if (bread == 1) {
            add(breadIcon);
        }

        add(backgroundPane); // Add this *LAST* it renders over everything else.


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
            JOptionPane.showMessageDialog(null, installedStable);
            System.exit(0);
        });

        betaButton.addActionListener(e -> {
            stableButton.disable();
            betaButton.disable();
            stableButtonIcon.setOpaque(false);
            betaButtonIcon.setOpaque(false);
            download(VersionType.BETA);
            JOptionPane.showMessageDialog(null, installedBeta);
            System.exit(0);
        });
    }


    public static void main(String[] args) throws IOException {
        System.out.println("ran installer!!!");

        Path modfolder = Paths.get(getMinecraftFolder() + "mods");

        if (Files.notExists(modfolder)) {
            new File(getMinecraftFolder() + "mods").mkdirs();
            // make warning about not having forge yada yada
        }
        //  in this space the mods folder is ensured to exist

        URL kamiLogo = Main.class.getResource("/installer/kami.png");
        JFrame frame = new JFrame("KAMI Blue Installer");
        frame.setIconImage(ImageIO.read(kamiLogo));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new Main());
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
    }
}

