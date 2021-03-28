package org.kamiblue.client.installer

import org.kamiblue.client.KamiMod
import org.kamiblue.client.util.WebUtils
import org.kamiblue.client.util.filesystem.FolderUtils
import java.awt.Dimension
import java.io.File
import java.io.IOException
import java.net.JarURLConnection
import java.net.URL
import java.nio.file.Files
import java.util.*
import javax.imageio.ImageIO
import javax.swing.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

object Installer : JPanel() {

    private var frame = JFrame()
    private val downloadsApi = WebUtils.getUrlContents(KamiMod.DOWNLOADS_API).replace("\n", "").split("\"")
    private var stableVersion = ""
    private var stableUrl = ""
    private var betaVersion = ""
    private var betaUrl = ""

    @JvmStatic
    fun main(args: Array<String>) {
        println("Running the ${KamiMod.NAME} ${KamiMod.VERSION} Installer")

        if (downloadsApi.size < 19) {
            notify("Error while loading the KAMI Blue Downloads API, couldn't connect to the URL or response is invalid. " +
                "Either your Firewall / ISP is blocking it or you're not connected to the internet!",
                "Error!"
            )
        }

        // Gson can't be included in the shadow jar, therefore we can't use it to parse the json.
        stableVersion = downloadsApi[5]
        stableUrl = downloadsApi[9]
        betaVersion = downloadsApi[15]
        betaUrl = downloadsApi[19]

        File(FolderUtils.modsFolder).mkdir() // make sure that the mods folder exists.

        val logoFile = Installer.javaClass.getResource("/installer/kami.png")
        frame = JFrame("KAMI Blue Installer")
        frame.iconImage = ImageIO.read(logoFile)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(600, 355)

        frame.contentPane.add(this)

        frame.pack()
        frame.setLocationRelativeTo(null)
        frame.isResizable = false
        frame.isVisible = true

        val kamiJars = getKamiJars()
        val hasForge = checkForForge()

        if (!hasForge) {
            val action = confirm("Attention! It seems like you don't have Forge installed, would you like to continue?")
            if (action == JOptionPane.NO_OPTION) {
                exitProcess(0)
            }
        }

        if (kamiJars.size > 0) {
            val action = confirm("Attention! It looks like you have installed KAMI Blue before. Do you want to delete these older versions?")
            if (action == JOptionPane.YES_OPTION) {
                kamiJars.deleteFiles()
            }
        }
    }

    init {
        val stableButton = JButton()
        val betaButton = JButton()
        val rand = Random()

        stableButton.isOpaque = true
        stableButton.isContentAreaFilled = false
        stableButton.isBorderPainted = true

        betaButton.isOpaque = true
        betaButton.isContentAreaFilled = false
        betaButton.isBorderPainted = true

        stableButton.toolTipText = "This version of KAMI Blue is the latest major release"
        betaButton.toolTipText = "A beta version of KAMI Blue, with frequent updates and fixes"

        // Load images and icons
        val backgroundImage = this.javaClass.getResource("/installer/0${rand.nextInt(4)}.jpg")
        val backgroundPane = JLabel(ImageIcon(ImageIO.read(backgroundImage)))

        val stableButtonImage = this.javaClass.getResource("/installer/stable.png")
        val stableButtonIcon = JLabel(ImageIcon(ImageIO.read(stableButtonImage)))

        val betaButtonImage = this.javaClass.getResource("/installer/beta.png")
        val betaButtonIcon = JLabel(ImageIcon(ImageIO.read(betaButtonImage)))

        val kamiImage = this.javaClass.getResource("/installer/kami.png")
        val kamiIcon = JLabel(ImageIcon(ImageIO.read(kamiImage)))

        val breadImage = this.javaClass.getResource("/installer/bread.png")
        val breadIcon = JLabel(ImageIcon(ImageIO.read(breadImage)))

        preferredSize = Dimension(600, 335)
        layout = null

        add(stableButton)
        add(betaButton)
        add(stableButtonIcon)
        add(betaButtonIcon)
        add(kamiIcon)

        val bread = rand.nextInt(50)
        if (bread == 1) {
            add(breadIcon)
        }

        add(backgroundPane) // make sure that this is added last

        stableButtonIcon.setBounds(90, 245, 200, 50)
        stableButton.setBounds(90, 245, 200, 50)
        betaButtonIcon.setBounds(310, 245, 200, 50)
        betaButton.setBounds(310, 245, 200, 50)
        kamiIcon.setBounds(236, 70, 128, 128)
        breadIcon.setBounds(200, 150, 128, 128)
        backgroundPane.setBounds(0, 0, 600, 355)

        betaButton.addActionListener {
            stableButton.isEnabled = false
            betaButton.isEnabled = false
            stableButton.isOpaque = false
            betaButton.isOpaque = false
            download(betaUrl)
            notify("KAMI Blue $betaVersion has been installed", "Installed", JOptionPane.INFORMATION_MESSAGE)
            exitProcess(0)
        }

        stableButton.addActionListener {
            stableButton.isEnabled = false
            betaButton.isEnabled = false
            stableButton.isOpaque = false
            betaButton.isOpaque = false
            download(stableUrl)
            notify("KAMI Blue $stableVersion has been installed", "Installed", JOptionPane.INFORMATION_MESSAGE)
            exitProcess(0)
        }
    }

    private fun getKamiJars(): ArrayList<File> {
        val mods = File(FolderUtils.modsFolder)
        var files = mods.listFiles() ?: return ArrayList()
        val foundFiles = ArrayList<File>()

        val forgeSubfile = files.firstOrNull { (it.isDirectory and (it.name == "1.12.2")) }

        if (forgeSubfile != null) {
            files = arrayOf(*forgeSubfile.listFiles() ?: return ArrayList(), *files)
        }


        try {
            for (file in files) {
                if (file.extension != "jar") continue
                val jarUrl = URL("jar:file:/${file.absolutePath}!/")
                val jarFile = (jarUrl.openConnection() as JarURLConnection).jarFile
                val manifest = jarFile.manifest
                println("Scanning $jarUrl")

                if (manifest.mainAttributes.getValue("MixinConfigs") == "mixins.kami.json") { // this is the most unique unchanged thing.
                    foundFiles.add(file)
                }

                jarFile.close()
            }
        } catch (e: Exception) {
            println("Error whilst checking the jar, defaulting to name checking")

            for (file in files) {
                if (file.name.matches(".*[Kk][Aa][Mm][Ii].*".toRegex())) {
                    foundFiles.add(file)
                }
            }
        }

        return foundFiles
    }

    private fun ArrayList<File>.deleteFiles() {
        forEach { file -> Files.delete(file.toPath()) }
    }

    private fun notify(message: String, title: String = "KAMI Blue Installer", type: Int = JOptionPane.WARNING_MESSAGE) {
        JOptionPane.showMessageDialog(frame, message, title, type)
    }

    private fun confirm(message: String): Int {
        return JOptionPane.showConfirmDialog(frame, message, "Attention!", JOptionPane.YES_NO_OPTION)
    }

    private fun download(url: String) {
        val dialog = arrayOf<JDialog?>(null)

        Thread {
            dialog[0] = JOptionPane("", JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION)
                .createDialog(null, "KAMI Blue - Downloading")

            dialog[0]?.isResizable = false
            dialog[0]?.setSize(300, 0)
            dialog[0]?.isVisible = true
        }.start()

        println("Download started")

        try {
            WebUtils.downloadUsingNIO(url, FolderUtils.modsFolder + getJarName(url))
        } catch (e: IOException) {
            notify("Error while downloading, couldn't connect to the URL. " +
                "Either your Firewall / ISP is blocking it or you're not connected to the internet",
                "Error!"
            )
        }

        dialog[0]?.dispose()

        println("Download finished.")
    }

    private fun getJarName(url: String): String {
        return url.split("/").last()
    }

    private fun checkForForge(): Boolean {
        val versionFolder = File(FolderUtils.versionsFolder)

        val versionList = versionFolder.listFiles() ?: return false

        for (file in versionList) {
            if (file.name.matches(".*1.12.2.*[Ff]orge.*".toRegex())) {
                return true
            }
        }
        return false
    }
}