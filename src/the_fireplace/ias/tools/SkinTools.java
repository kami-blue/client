// 
// Decompiled by Procyon v0.5.36
// 

package the_fireplace.ias.tools;

import java.io.OutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.io.FileOutputStream;
import java.net.URL;
import com.github.mrebhan.ingameaccountswitcher.tools.alt.AccountData;
import com.github.mrebhan.ingameaccountswitcher.tools.alt.AltDatabase;
import net.minecraft.client.Minecraft;
import java.awt.image.RenderedImage;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.File;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SkinTools
{
    public static final File cachedir;
    private static final File skinOut;
    
    public static void buildSkin(final String name) {
        BufferedImage skin;
        try {
            skin = ImageIO.read(new File(SkinTools.cachedir, name + ".png"));
        }
        catch (IOException e2) {
            if (SkinTools.skinOut.exists()) {
                SkinTools.skinOut.delete();
            }
            return;
        }
        final BufferedImage drawing = new BufferedImage(16, 32, 2);
        if (skin.getHeight() == 64) {
            final int[] head = skin.getRGB(8, 8, 8, 8, null, 0, 8);
            final int[] torso = skin.getRGB(20, 20, 8, 12, null, 0, 8);
            final int[] larm = skin.getRGB(44, 20, 4, 12, null, 0, 4);
            final int[] rarm = skin.getRGB(36, 52, 4, 12, null, 0, 4);
            final int[] lleg = skin.getRGB(4, 20, 4, 12, null, 0, 4);
            final int[] rleg = skin.getRGB(20, 52, 4, 12, null, 0, 4);
            final int[] hat = skin.getRGB(40, 8, 8, 8, null, 0, 8);
            final int[] jacket = skin.getRGB(20, 36, 8, 12, null, 0, 8);
            final int[] larm2 = skin.getRGB(44, 36, 4, 12, null, 0, 4);
            final int[] rarm2 = skin.getRGB(52, 52, 4, 12, null, 0, 4);
            final int[] lleg2 = skin.getRGB(4, 36, 4, 12, null, 0, 4);
            final int[] rleg2 = skin.getRGB(4, 52, 4, 12, null, 0, 4);
            for (int i = 0; i < hat.length; ++i) {
                if (hat[i] == 0) {
                    hat[i] = head[i];
                }
            }
            for (int i = 0; i < jacket.length; ++i) {
                if (jacket[i] == 0) {
                    jacket[i] = torso[i];
                }
            }
            for (int i = 0; i < larm2.length; ++i) {
                if (larm2[i] == 0) {
                    larm2[i] = larm[i];
                }
            }
            for (int i = 0; i < rarm2.length; ++i) {
                if (rarm2[i] == 0) {
                    rarm2[i] = rarm[i];
                }
            }
            for (int i = 0; i < lleg2.length; ++i) {
                if (lleg2[i] == 0) {
                    lleg2[i] = lleg[i];
                }
            }
            for (int i = 0; i < rleg2.length; ++i) {
                if (rleg2[i] == 0) {
                    rleg2[i] = rleg[i];
                }
            }
            drawing.setRGB(4, 0, 8, 8, hat, 0, 8);
            drawing.setRGB(4, 8, 8, 12, jacket, 0, 8);
            drawing.setRGB(0, 8, 4, 12, larm2, 0, 4);
            drawing.setRGB(12, 8, 4, 12, rarm2, 0, 4);
            drawing.setRGB(4, 20, 4, 12, lleg2, 0, 4);
            drawing.setRGB(8, 20, 4, 12, rleg2, 0, 4);
        }
        else {
            final int[] head = skin.getRGB(8, 8, 8, 8, null, 0, 8);
            final int[] torso = skin.getRGB(20, 20, 8, 12, null, 0, 8);
            final int[] arm = skin.getRGB(44, 20, 4, 12, null, 0, 4);
            final int[] leg = skin.getRGB(4, 20, 4, 12, null, 0, 4);
            final int[] hat2 = skin.getRGB(40, 8, 8, 8, null, 0, 8);
            for (int j = 0; j < hat2.length; ++j) {
                if (hat2[j] == 0) {
                    hat2[j] = head[j];
                }
            }
            drawing.setRGB(4, 0, 8, 8, hat2, 0, 8);
            drawing.setRGB(4, 8, 8, 12, torso, 0, 8);
            drawing.setRGB(0, 8, 4, 12, arm, 0, 4);
            drawing.setRGB(12, 8, 4, 12, arm, 0, 4);
            drawing.setRGB(4, 20, 4, 12, leg, 0, 4);
            drawing.setRGB(8, 20, 4, 12, leg, 0, 4);
        }
        try {
            ImageIO.write(drawing, "png", SkinTools.skinOut);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void javDrawSkin(final int x, final int y, final int width, final int height) {
        if (!SkinTools.skinOut.exists()) {
            return;
        }
        final SkinRender r = new SkinRender(Minecraft.func_71410_x().func_110434_K(), SkinTools.skinOut);
        r.drawImage(x, y, width, height);
    }
    
    public static void cacheSkins() {
        if (!SkinTools.cachedir.exists() && !SkinTools.cachedir.mkdirs()) {
            System.out.println("Skin cache directory creation failed.");
        }
        for (final AccountData data : AltDatabase.getInstance().getAlts()) {
            final File file = new File(SkinTools.cachedir, data.alias + ".png");
            try {
                final URL url = new URL(String.format("http://skins.minecraft.net/MinecraftSkins/%s.png", data.alias));
                final InputStream is = url.openStream();
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                final OutputStream os = new FileOutputStream(file);
                final byte[] b = new byte[2048];
                int length;
                while ((length = is.read(b)) != -1) {
                    os.write(b, 0, length);
                }
                is.close();
                os.close();
            }
            catch (IOException e) {
                try {
                    final URL url2 = new URL("http://skins.minecraft.net/MinecraftSkins/direwolf20.png");
                    final InputStream is2 = url2.openStream();
                    if (file.exists()) {
                        file.delete();
                    }
                    file.createNewFile();
                    final OutputStream os2 = new FileOutputStream(file);
                    final byte[] b2 = new byte[2048];
                    int length2;
                    while ((length2 = is2.read(b2)) != -1) {
                        os2.write(b2, 0, length2);
                    }
                    is2.close();
                    os2.close();
                }
                catch (IOException ex) {}
            }
        }
    }
    
    static {
        cachedir = new File(Minecraft.func_71410_x().field_71412_D, "cachedImages/skins/");
        skinOut = new File(SkinTools.cachedir, "temp.png");
    }
}
