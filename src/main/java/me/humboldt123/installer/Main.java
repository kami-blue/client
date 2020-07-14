package me.humboldt123.installer;

import me.zeroeightsix.kami.KamiMod;

import java.awt.*;
import java.io.*;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

/**
 * Created by humboldt123 on 7/14/2020.
 */

// TODO:
    //test if already installed
    //close
    //fix images


public class Main extends JPanel{
    private JButton jcomp1;
    private JButton jcomp2;
    private JLabel BackgroundPane;
    private JLabel StableButtonImage;
    private JLabel NightlyButtonImage;
    private JLabel KamiLogo;
    private JLabel BREAD;

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
        throw new RuntimeException("Cannot find minecraft folder!"); // Add fancy GUI here too~!
    }

    public static void downloadKami(String version) {
        String[] lv = getUrlContents().split("\""); //Thanks to bella for doing this faster than I could in JavaScript.
        /**
         * 5 = stable name
         * 9 = stable url
         * 15 = beta name
         * 19 = beta url
         */
        //System.out.println("Download " + lv[5] + ": " + lv[9]);
        //System.out.println("Download " + lv[15] + ": " + lv[19]);

        if (version.equals("stable")) {
            try {
                downloadUsingNIO(lv[9], getMinecraftFolder()+"mods\\kamiblue-"+lv[5]+".jar");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                downloadUsingNIO(lv[19], getMinecraftFolder()+"mods\\kamiblue-"+lv[15]+".jar");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void downloadUsingNIO(String urlStr, String file) throws IOException {
        URL url = new URL(urlStr);
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }

    private static String getUrlContents() {
        StringBuilder content = new StringBuilder();

        try {
            URL url = new URL(KamiMod.DOWNLOADS_API);

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
        //construct components
        jcomp1 = new JButton();// ("Stable");
        jcomp2 = new JButton();// ("Nightly");
        Random rand = new Random();
        String installedStable = "The latest stable version of KAMI Blue was installed. You need to have forge installed \nto run it if you do not already. If you wish to install a separate version of KAMI;\nmake sure to delete the already existing KAMI in your .minecraft folder (" + getMinecraftFolder() +")";
        String installedBeta = "The latest BETA version of KAMI Blue was installed. You need to have forge installed \nto run it if you do not already. If you wish to install a separate version of KAMI;\nmake sure to delete the already existing KAMI in your .minecraft folder (" + getMinecraftFolder() +")";


        jcomp1.setOpaque(false);
        jcomp1.setContentAreaFilled(false);
        jcomp1.setBorderPainted(false);
        jcomp2.setOpaque(false);
        jcomp2.setContentAreaFilled(false);
        jcomp2.setBorderPainted(false);

        //set components properties
        jcomp1.setToolTipText ("This version of Kami Blue is the latest release with bugfixes and polish.");
        jcomp2.setToolTipText ("A beta version of Kami; new one every night!");

        URL imageBG = Main.class.getResource("/installer/0"+Integer.toString(rand.nextInt(4))+".png");
        BackgroundPane = new JLabel(new ImageIcon(ImageIO.read(imageBG)));
        URL imageStable = Main.class.getResource("/installer/stable.png");
        StableButtonImage = new JLabel(new ImageIcon(ImageIO.read(imageStable)));
        URL imageNightly = Main.class.getResource("/installer/nightly.png");
        NightlyButtonImage = new JLabel(new ImageIcon(ImageIO.read(imageNightly)));
        URL imageKamiLogo = Main.class.getResource("/installer/kami.png");
        KamiLogo = new JLabel(new ImageIcon(ImageIO.read(imageKamiLogo)));
        URL breadimg = Main.class.getResource("/installer/breaduwu.png");
        BREAD = new JLabel(new ImageIcon(ImageIO.read(breadimg)));

        //adjust size and set layout
        setPreferredSize(new Dimension (600, 335));
        setLayout(null);

        //; <- replaces semicolons with this

        //add components
        add (jcomp1);
        add (jcomp2);
        add(StableButtonImage);
        add(NightlyButtonImage);
        add(KamiLogo);
        int bread = rand.nextInt(20);
        if (bread == 1) {
            add(BREAD); // <@!703341270260514916> (@ them on the kami discord, see who they are)
        }



        add(BackgroundPane); // Add this *LAST* it renders over everything else.


        StableButtonImage.setBounds (70, 245, 200, 50);
        jcomp1.setBounds (70, 245, 200, 50);
        NightlyButtonImage.setBounds (310, 245, 200, 50);
        jcomp2.setBounds (310, 245, 200, 50);
        KamiLogo.setBounds(248, 70, 128, 128);
        BREAD.setBounds(200, 150, 128, 128);
        BackgroundPane.setBounds(0, 0, 600, 355);

        jcomp1.addActionListener(e -> {
           System.out.println("Pressed Install Stable Button");
            jcomp1.disable();
            jcomp2.disable();
            StableButtonImage.setOpaque(false);
            NightlyButtonImage.setOpaque(false);
            downloadKami("stable");
            JOptionPane.showMessageDialog(null, installedStable);
            System.exit(0);
        });

        jcomp2.addActionListener(e -> {
            System.out.println("Pressed Install Nightly Button");
            jcomp1.disable();
            jcomp2.disable();
            StableButtonImage.setOpaque(false);
            NightlyButtonImage.setOpaque(false);
            downloadKami("beta");
            JOptionPane.showMessageDialog(null, installedBeta);
            System.exit(0);

        });


    }


    public static void main(String[] args) throws IOException {
        System.out.println("ran installer!!!");

        Path modfolder = Paths.get(getMinecraftFolder() + "mods");

        if (Files.notExists(modfolder)) {
            new File(getMinecraftFolder()+"mods").mkdirs();
            // make warning about not having forge yada yada
        }
        //  in this space the mods folder is ensured to exist


        URL kamiLogo = Main.class.getResource("/installer/kami.png");
        JFrame frame = new JFrame ("KAMI Blue Installer");
        frame.setIconImage(ImageIO.read(kamiLogo));
        frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add (new Main());
        frame.pack();
        frame.setResizable(false);
        frame.setVisible (true);



    }


}

