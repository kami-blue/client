package me.zeroeightsix.installer;

import me.zeroeightsix.kami.NecronClient;
import me.zeroeightsix.kami.util.WebUtils;
import me.zeroeightsix.kami.util.filesystem.FolderHelper;
import me.zeroeightsix.kami.util.filesystem.OperatingSystemHelper;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by humboldt123 on 14/07/20
 * Rewritten almost entirely by l1ving on 14/07/20
 * Added more background images by humboldt123 on 15/08/20
 */
public class Installer extends JPanel {
    String[] downloadsAPI = WebUtils.INSTANCE.getUrlContents(NecronClient.DOWNLOADS_API).replace("\n", "").split("\"");
    public static void main(String[] args) throws IOException {
        System.out.println("Ran the " + NecronClient.NAME + " " + NecronClient.VERSION + " installer!");

        /* ensure mods exists */
        new File(getModsFolder()).mkdirs();

        URL necronLogo = Installer.class.getResource("/installer/necron_icon.png");
        JFrame frame = new JFrame("NECRON Client Installer");
        frame.setIconImage(ImageIO.read(necronLogo));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new Installer());
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);

        boolean hasForge = checkForForge();
        ArrayList<File> NecronJars = getNecronJars();

        if (!hasForge) {
            notify("Attention! It looks like Forge 1.12.2 is not installed. You need Forge 1.12.2 in order to use NECRON Client. ");
        }
        if (NecronJars != null) {
            notify("Attention! It looks like you had NECRON Client installed before. Closing this popup will delete the older versions, " +
                    "so if you want to save those jars you should go and make a copy somewhere else");
            deleteNecronJars(NecronJars);
        }
    }

    /**
     * @throws IOException won't happen due to the files being inside the jar themselves
     */
    private Installer() throws IOException {
        JButton installButton = new JButton();
        Random rand = new Random();

        String installedStable = "The latest version (" + downloadsAPI[5] + ") of NECRON Client was installed.";

        installButton.setOpaque(false);
        installButton.setContentAreaFilled(false);
        installButton.setBorderPainted(false);

        installButton.setToolTipText("Install the latest version of NECRON Client");

        URL backgroundImage = Installer.class.getResource("/installer/0" + rand.nextInt(4) + ".jpg");
        JLabel backgroundPane = new JLabel(new ImageIcon(ImageIO.read(backgroundImage)));

        URL installButtonImage = Installer.class.getResource("/installer/install.png");
        JLabel installButtonIcon = new JLabel(new ImageIcon(ImageIO.read(installButtonImage)));

        URL necronImage = Installer.class.getResource("/installer/necron.png");
        JLabel necronIcon = new JLabel(new ImageIcon(ImageIO.read(necronImage)));

        setPreferredSize(new Dimension(600, 335));
        setLayout(null);

        add(installButton);
        add(installButtonIcon);
        add(necronIcon);


        add(backgroundPane); // Add this *LAST* so renders over everything else.

        installButtonIcon.setBounds(200, 245, 200, 60);
        installButton.setBounds(200, 245, 200, 60);
        necronIcon.setBounds(190, 70, 220, 128);
        backgroundPane.setBounds(0, 0, 600, 355);

        installButton.addActionListener(e -> {
            installButton.disable();
            installButtonIcon.setOpaque(false);
            download();
            notify(installedStable);
            System.exit(0);
        });
    }

    private void download() {
        System.out.println(downloadsAPI[9]);

        final JDialog[] dialog = {null};
        new Thread(() -> {
            dialog[0] = new JOptionPane("", JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION).createDialog(null, "NECRON Client - Downloading");
            dialog[0].setResizable(false);
            dialog[0].setSize(300, 0);
            dialog[0].show();
//            notify("NECRON Client is currently being downloaded, please wait")
        }).start();

        /* please ignore the clusterfuck of code that this is */
        System.out.println(NecronClient.NAME + " download started!");
        try {
            WebUtils.INSTANCE.downloadUsingNIO(downloadsAPI[9], getModsFolder() + getFullJarName(downloadsAPI[9]));
            dialog[0].hide();
            System.out.println(NecronClient.NAME + " download finished!");
        } catch (IOException e) {
            notifyAndExitWeb(e);
        }
    }

    /**
     * Deletes all the older NECRON Jars
     *
     * @param files list of NECRON jar Files
     */
    private static void deleteNecronJars(ArrayList<File> files) {
        for (File file : files) {
            file.delete();
        }
    }

    /**
     * @return null if there were no NECRON jars, otherwise returns a list of files to delete
     */
    private static ArrayList<File> getNecronJars() {
        File mods = new File(getModsFolder());
        File[] files = mods.listFiles();
        ArrayList<File> foundFiles = new ArrayList<>();
        boolean found = false;

        for (File file : files) {
            boolean match = file.getName().matches(".*[Nn][En][Cc][Rr][Oo][Nn].*");
            if (match) {
                foundFiles.add(file);
                found = true;
            }
        }

        if (found) return foundFiles;
        else return null;
    }

    /**
     * Checks if Forge is installed
     *
     * @return true if Forge is installed
     */
    private static boolean checkForForge() {
        File ver = new File(getVersionsFolder());
        File[] files = ver.listFiles();
        boolean found = false;

        for (File file : files) {
            boolean match = file.getName().matches(".*1.12.2.*[Ff]orge.*");
            if (match) found = true;
        }

        return found;
    }

    /**
     * @param message that you want to display to the user
     */
    private static void notify(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    private static String getVersionsFolder() {
        return FolderHelper.INSTANCE.getVersionsFolder(OperatingSystemHelper.INSTANCE.getOS());
    }

    private static String getModsFolder() {
        return FolderHelper.INSTANCE.getModsFolder(OperatingSystemHelper.INSTANCE.getOS());
    }

    private static String getMinecraftFolder() {
        return FolderHelper.INSTANCE.getMinecraftFolder(OperatingSystemHelper.INSTANCE.getOS());
    }

    /**
     * @param url jar download url
     * @return the last section of the url, ie the full file name
     */
    private static String getFullJarName(String url) {
        String[] split = url.split("/");
        return split[split.length - 1];
    }

    private void notifyAndExitWeb(Exception e) {
        notify("Error when downloading, couldn't connect to URL. Firewall / ISP is blocking it or you're offline");
        e.printStackTrace();
        System.exit(1);
    }
}

