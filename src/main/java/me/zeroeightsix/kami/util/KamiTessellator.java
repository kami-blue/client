package me.zeroeightsix.kami.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;

import static org.lwjgl.opengl.GL11.*;

/**
 * THE FOLLOWING CODE IS LICENSED UNDER MIT, AS PER the fr1kin/forgehax license
 * You can view the original code here:
 * <p>
 * https://github.com/fr1kin/ForgeHax/blob/master/src/main/java/com/matt/forgehax/util/tesselation/GeometryTessellator.java
 * <p>
 * Some is created by 086 on 9/07/2017.
 * Updated by dominikaaaa on 18/02/20
 * Updated by on Afel 08/06/20
 */
public class KamiTessellator extends Tessellator {

    public static KamiTessellator INSTANCE = new KamiTessellator();

    public KamiTessellator() {
        super(0x200000);
    }

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void prepare(int mode) {
        prepareGL();
        begin(mode);
    }

    public static void prepareGL() {
//        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(1.5F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableAlpha();
        GlStateManager.color(1, 1, 1);
    }

    public static void begin(int mode) {
        INSTANCE.getBuffer().begin(mode, DefaultVertexFormats.POSITION_COLOR);
    }

    public static void release() {
        render();
        releaseGL();
    }

    public static void render() {
        INSTANCE.draw();
    }

    public static void releaseGL() {
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
    }

    public static void drawBox(BlockPos blockPos, int argb, int sides) {
        final int a = (argb >>> 24) & 0xFF;
        final int r = (argb >>> 16) & 0xFF;
        final int g = (argb >>> 8) & 0xFF;
        final int b = argb & 0xFF;
        drawBox(blockPos, r, g, b, a, sides);
    }

    public static void drawBox(BlockPos blockPos, int colour, int a, int sides) {
        int r = (colour >> 16 & 0xFF);
        int g = (colour >> 8 & 0xFF);
        int b = (colour & 0xFF);
        drawBox(blockPos, r, g, b, a, sides);
    }

    public static void drawBox(AxisAlignedBB box, int colour, int a, int sides) {
        int r = (colour >> 16 & 0xFF);
        int g = (colour >> 8 & 0xFF);
        int b = (colour & 0xFF);
        drawBox(box, r, g, b, a, sides);
    }

    public static void drawBox(AxisAlignedBB box, int r, int g, int b, int a, int sides) {
        float w = (float) (box.maxX - box.minX);
        float h = (float) (box.maxY - box.minY);
        float d = (float) (box.maxZ - box.minZ);
        drawBox(INSTANCE.getBuffer(), box.minX, box.minY, box.minZ, w, h, d, r, g, b, a, sides);
    }

    public static void drawBox(double x, double y, double z, int argb, int sides) {
        final int a = (argb >>> 24) & 0xFF;
        final int r = (argb >>> 16) & 0xFF;
        final int g = (argb >>> 8) & 0xFF;
        final int b = argb & 0xFF;
        drawBox(INSTANCE.getBuffer(), x, y, z, 1, 1, 1, r, g, b, a, sides);
    }

    public static void drawBox(BlockPos blockPos, int argb, int sides, int h, int w, int d, int i) {
        final int a = (argb >>> 24) & 0xFF;
        final int r = (argb >>> 16) & 0xFF;
        final int g = (argb >>> 8) & 0xFF;
        final int b = argb & 0xFF;
        drawBox(INSTANCE.getBuffer(), blockPos.x, blockPos.y, blockPos.z, w, h, d, r, g, b, a, sides);
    }

    public static void drawBox(BlockPos blockPos, int r, int g, int b, int a, int sides) {
        drawBox(INSTANCE.getBuffer(), blockPos.x, blockPos.y, blockPos.z, 1, 1, 1, r, g, b, a, sides);
    }

    public static BufferBuilder getBufferBuilder() {
        return INSTANCE.getBuffer();
    }

    public static void drawBox(final BufferBuilder buffer, double x, double y, double z, float w, float h, float d, int r, int g, int b, int a, int sides) {
        if ((sides & GeometryMasks.Quad.DOWN) != 0) {
            buffer.pos(x + w, y, z).color(r, g, b, a).endVertex();
            buffer.pos(x + w, y, z + d).color(r, g, b, a).endVertex();
            buffer.pos(x, y, z + d).color(r, g, b, a).endVertex();
            buffer.pos(x, y, z).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Quad.UP) != 0) {
            buffer.pos(x + w, y + h, z).color(r, g, b, a).endVertex();
            buffer.pos(x, y + h, z).color(r, g, b, a).endVertex();
            buffer.pos(x, y + h, z + d).color(r, g, b, a).endVertex();
            buffer.pos(x + w, y + h, z + d).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Quad.NORTH) != 0) {
            buffer.pos(x + w, y, z).color(r, g, b, a).endVertex();
            buffer.pos(x, y, z).color(r, g, b, a).endVertex();
            buffer.pos(x, y + h, z).color(r, g, b, a).endVertex();
            buffer.pos(x + w, y + h, z).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Quad.SOUTH) != 0) {
            buffer.pos(x, y, z + d).color(r, g, b, a).endVertex();
            buffer.pos(x + w, y, z + d).color(r, g, b, a).endVertex();
            buffer.pos(x + w, y + h, z + d).color(r, g, b, a).endVertex();
            buffer.pos(x, y + h, z + d).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Quad.WEST) != 0) {
            buffer.pos(x, y, z).color(r, g, b, a).endVertex();
            buffer.pos(x, y, z + d).color(r, g, b, a).endVertex();
            buffer.pos(x, y + h, z + d).color(r, g, b, a).endVertex();
            buffer.pos(x, y + h, z).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Quad.EAST) != 0) {
            buffer.pos(x + w, y, z + d).color(r, g, b, a).endVertex();
            buffer.pos(x + w, y, z).color(r, g, b, a).endVertex();
            buffer.pos(x + w, y + h, z).color(r, g, b, a).endVertex();
            buffer.pos(x + w, y + h, z + d).color(r, g, b, a).endVertex();
        }
    }

    public static void drawLines(final BufferBuilder buffer, double x, double y, double z, float w, float h, float d, int r, int g, int b, int a, int sides) {
        if ((sides & GeometryMasks.Line.DOWN_WEST) != 0) {
            buffer.pos(x, y, z).color(r, g, b, a).endVertex();
            buffer.pos(x, y, z + d).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.UP_WEST) != 0) {
            buffer.pos(x, y + h, z).color(r, g, b, a).endVertex();
            buffer.pos(x, y + h, z + d).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.DOWN_EAST) != 0) {
            buffer.pos(x + w, y, z).color(r, g, b, a).endVertex();
            buffer.pos(x + w, y, z + d).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.UP_EAST) != 0) {
            buffer.pos(x + w, y + h, z).color(r, g, b, a).endVertex();
            buffer.pos(x + w, y + h, z + d).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.DOWN_NORTH) != 0) {
            buffer.pos(x, y, z).color(r, g, b, a).endVertex();
            buffer.pos(x + w, y, z).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.UP_NORTH) != 0) {
            buffer.pos(x, y + h, z).color(r, g, b, a).endVertex();
            buffer.pos(x + w, y + h, z).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.DOWN_SOUTH) != 0) {
            buffer.pos(x, y, z + d).color(r, g, b, a).endVertex();
            buffer.pos(x + w, y, z + d).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.UP_SOUTH) != 0) {
            buffer.pos(x, y + h, z + d).color(r, g, b, a).endVertex();
            buffer.pos(x + w, y + h, z + d).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.NORTH_WEST) != 0) {
            buffer.pos(x, y, z).color(r, g, b, a).endVertex();
            buffer.pos(x, y + h, z).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.NORTH_EAST) != 0) {
            buffer.pos(x + w, y, z).color(r, g, b, a).endVertex();
            buffer.pos(x + w, y + h, z).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.SOUTH_WEST) != 0) {
            buffer.pos(x, y, z + d).color(r, g, b, a).endVertex();
            buffer.pos(x, y + h, z + d).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.SOUTH_EAST) != 0) {
            buffer.pos(x + w, y, z + d).color(r, g, b, a).endVertex();
            buffer.pos(x + w, y + h, z + d).color(r, g, b, a).endVertex();
        }
    }

    public static void drawLineToBlock(BlockPos pos, int colour, int alpha, float thickness) {
        drawLineToPos(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, colour, alpha, thickness);
    }

    public static void drawLineToEntity(Entity entity, int colour, int a, float partialTicks, float thickness) {
        //Interpolate
        int red = (colour >> 16 & 0xFF);
        int green = (colour >> 8 & 0xFF);
        int blue = (colour & 0xFF);
        drawLineToEntity(entity, red, green, blue, a, partialTicks, thickness);
    }

    public static void drawLineToEntity(Entity entity, int r, int g, int b, int a, float partialTicks, float thickness) {
        //Interpolate
        double x = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
        double y = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks;
        double z = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;
        drawLineToPos(x, y, z, r, g, b, a, thickness);
    }

    public static void drawLineToPos(Vec3d vec3d, int r, int g, int b, int alpha, float thickness) {
        drawLineToPos(vec3d.x, vec3d.y, vec3d.z, r / 255f, g / 255f, b / 255f, alpha / 255f, thickness);
    }

    public static void drawLineToPos(double x, double y, double z, int colour, int alpha, float thickness) {
        int red = (colour >> 16 & 0xFF);
        int green = (colour >> 8 & 0xFF);
        int blue = (colour & 0xFF);
        drawLineToPos(x, y, z, red, green, blue, alpha, thickness);
    }

    public static void drawLineToPos(double x, double y, double z, int r, int g, int b, int a, float thickness) {
        float red = r / 255f;
        float green = g / 255f;
        float blue = b / 255f;
        float alpha = a / 255f;
        drawLineToPos(x, y, z, red, green, blue, alpha, thickness);
    }

    public static void drawLineToPos(double x, double y, double z, float red, float green, float blue, float alpha, float thickness) {
        x -= mc.renderManager.renderPosX;
        y -= mc.renderManager.renderPosY;
        z -= mc.renderManager.renderPosZ;
        float pTicks = mc.getRenderPartialTicks();
        Vec3d eyes = mc.player.getLook(pTicks);
        final boolean bobbing = mc.gameSettings.viewBobbing;

        prepareLine(true);
        GL11.glLineWidth(thickness);
        GL11.glColor4f(red, green, blue, alpha);
        mc.gameSettings.viewBobbing = false;
        mc.entityRenderer.setupCameraTransform(pTicks, 0);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(eyes.x, eyes.y + mc.player.getEyeHeight(), eyes.z);
        GL11.glVertex3d(x, y, z);
        GL11.glEnd();
        mc.gameSettings.viewBobbing = bobbing;
        mc.entityRenderer.setupCameraTransform(pTicks, 0);
        GL11.glColor3d(1.0, 1.0, 1.0);
        releaseLine(true);
    }


    /**
     * @author polymer
     */
    public static void drawBoxSmall(double x, double y, double z, int argb, int sides) {
        final int a = (argb >>> 24) & 0xFF;
        final int r = (argb >>> 16) & 0xFF;
        final int g = (argb >>> 8) & 0xFF;
        final int b = argb & 0xFF;
        drawBox(INSTANCE.getBuffer(), x, y, z, 0.25f, 0.25f, 0.25f, r, g, b, a, sides);
    }

    public static void drawRectangle(double x, double y, float w, float h, int color) {
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        float a = (float) (color >> 24 & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, h, 0.0D).color(r, g, b, a).endVertex();
        bufferbuilder.pos(w, h, 0.0D).color(r, g, b, a).endVertex();
        bufferbuilder.pos(w, y, 0.0D).color(r, g, b, a).endVertex();
        bufferbuilder.pos(x, y, 0.0D).color(r, g, b, a).endVertex();
        tessellator.draw();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    /**
     * @author Xiaro
     * <p>
     * Draws outline of given axis aligned bounding box, uses int rgba (0 - 255)
     */
    public static void drawBoundingBox(AxisAlignedBB box, int r, int g, int b, int a, float thickness, boolean through) {
        drawBoundingBox(box, r / 255f, g / 255f, b / 255f, a / 255f, thickness, through);
    }

    /**
     * @author Xiaro
     * <p>
     * Draws outline of given axis aligned bounding box
     */
    public static void drawBoundingBox(AxisAlignedBB box, float r, float g, float b, float a, float thickness, boolean through) {
        Minecraft mc = Minecraft.getMinecraft();
        double xOffset = mc.renderManager.renderPosX;
        double yOffset = mc.renderManager.renderPosY;
        double zOffset = mc.renderManager.renderPosZ;
        box = box.offset(-xOffset, -yOffset, -zOffset);
        double[] xArray = new double[]{box.minX, box.maxX};
        double[] yArray = new double[]{box.minY, box.maxY};
        double[] zArray = new double[]{box.minZ, box.maxZ};

        prepareLine(through);
        GL11.glLineWidth(thickness);
        GL11.glColor4f(r, g, b, a);
        for (double y : yArray) { /* Draw out top edges and bottom edges */
            GL11.glBegin(GL_LINE_LOOP);
            {
                GL11.glVertex3d(xArray[0], y, zArray[0]);
                GL11.glVertex3d(xArray[0], y, zArray[1]);
                GL11.glVertex3d(xArray[1], y, zArray[1]);
                GL11.glVertex3d(xArray[1], y, zArray[0]);
            }
            GL11.glEnd();
        }
        /* Draw out side edges */
        GL11.glBegin(GL_LINES);
        {
            for (double x : xArray)
                for (double z : zArray)
                    for (double y : yArray) {
                        GL11.glVertex3d(x, y, z);
                    }
        }
        GL11.glEnd();
        GL11.glColor3d(1.0, 1.0, 1.0);
        releaseLine(through);
    }

    public static void prepareLine(boolean through) {
        GlStateManager.pushMatrix();
        glDepthMask(false);
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL32.GL_DEPTH_CLAMP);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        GlStateManager.shadeModel(GL_SMOOTH);
        GlStateManager.enableBlend();
        if (through) {
            GlStateManager.disableDepth();
        } else {
            GlStateManager.enableDepth();
        }
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
    }

    public static void releaseLine(boolean through) {
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        if (through) {
            GlStateManager.enableDepth();
        } else {
            GlStateManager.disableDepth();
        }
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL_FLAT);
        glDisable(GL32.GL_DEPTH_CLAMP);
        glDisable(GL_LINE_SMOOTH);
        glDepthMask(true);
        GlStateManager.popMatrix();
    }
}
