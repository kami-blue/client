/*
Taken from net.minecraft.client.gui.MapItemRenderer
 */

package me.zeroeightsix.kami.util;

import com.google.common.collect.Maps;
import me.zeroeightsix.kami.module.modules.render.NoMaps;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

public class FakeMapItemRenderer extends MapItemRenderer {

    private final TextureManager textureManager;
    private static final ResourceLocation TEXTURE_MAP_ICONS = new ResourceLocation("textures/map/map_icons.png");
    private final Map<String, FakeMapItemRenderer.Instance> loadedMaps = Maps.newHashMap();

    public FakeMapItemRenderer(TextureManager textureManagerIn) {
        super(textureManagerIn);
        this.textureManager = textureManagerIn;
    }

    @Override
    public void renderMap(MapData mapdataIn, boolean noOverlayRendering) {
        FakeMapItemRenderer.Instance instance = this.getMapRendererInstance(mapdataIn);
        instance.render(noOverlayRendering);
    }

    public FakeMapItemRenderer.Instance getMapRendererInstance(MapData mapdataIn) {
        FakeMapItemRenderer.Instance mapitemrenderer$instance = (FakeMapItemRenderer.Instance)this.loadedMaps.get(mapdataIn.mapName);
        if (mapitemrenderer$instance == null) {
            mapitemrenderer$instance = new FakeMapItemRenderer.Instance(mapdataIn);
            this.loadedMaps.put(mapdataIn.mapName, mapitemrenderer$instance);
        }

        return mapitemrenderer$instance;
    }

    @Override
    public void updateMapTexture(MapData mapdataIn) {
        this.getMapRendererInstance(mapdataIn).updateMapTexture();
    }

    @Override
    public void clearLoadedMaps() {
        Iterator var1 = this.loadedMaps.values().iterator();

        while(var1.hasNext()) {
            FakeMapItemRenderer.Instance mapitemrenderer$instance = (FakeMapItemRenderer.Instance)var1.next();
            this.textureManager.deleteTexture(mapitemrenderer$instance.location);
        }

        this.loadedMaps.clear();
    }

    @Nullable
    public MapData getData(@Nullable FakeMapItemRenderer.Instance p_191207_1_) {
        return p_191207_1_ != null ? p_191207_1_.mapData : null;
    }

    @SideOnly(Side.CLIENT)
    public class Instance {

        private ResourceLocation hiddenLocation = new ResourceLocation("kamiblue/large.png");
        private ResourceLocation realLocation;
        private final MapData mapData;
        private final DynamicTexture mapTexture;
        private ResourceLocation location;
        private final int[] mapTextureData;

        public Instance(MapData mapdataIn) {
            this.mapData = mapdataIn;
            this.mapTexture = new DynamicTexture(128, 128);
            this.mapTextureData = this.mapTexture.getTextureData();
            this.hiddenLocation = new ResourceLocation("kamiblue/logo128x128.png");
            this.realLocation = FakeMapItemRenderer.this.textureManager.getDynamicTextureLocation("map/" + mapdataIn.mapName, this.mapTexture);
            if (MODULE_MANAGER.isModuleEnabled(NoMaps.class)) {
                this.location = hiddenLocation;
            } else {
                this.location = realLocation;
            }

            for(int i = 0; i < this.mapTextureData.length; ++i) {
                this.mapTextureData[i] = 0;
            }

        }

        public void updateMapTexture() {
            for(int i = 0; i < 16384; ++i) {
                int j = this.mapData.colors[i] & 255;
                if (j / 4 == 0) {
                    this.mapTextureData[i] = (i + i / 128 & 1) * 8 + 16 << 24;
                } else {
                    this.mapTextureData[i] = MapColor.COLORS[j / 4].getMapColor(j & 3);
                }
            }

            this.mapTexture.updateDynamicTexture();
        }

        public void render(boolean noOverlayRendering) {
            if (MODULE_MANAGER.isModuleEnabled(NoMaps.class)) {
                this.location = hiddenLocation;
            } else {
                this.location = realLocation;
            }
            int i = 0;
            int j = 0;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            float f = 0.0F;
            FakeMapItemRenderer.this.textureManager.bindTexture(this.location);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            GlStateManager.disableAlpha();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            bufferbuilder.pos(0.0D, 128.0D, -0.009999999776482582D).tex(0.0D, 1.0D).endVertex();
            bufferbuilder.pos(128.0D, 128.0D, -0.009999999776482582D).tex(1.0D, 1.0D).endVertex();
            bufferbuilder.pos(128.0D, 0.0D, -0.009999999776482582D).tex(1.0D, 0.0D).endVertex();
            bufferbuilder.pos(0.0D, 0.0D, -0.009999999776482582D).tex(0.0D, 0.0D).endVertex();
            tessellator.draw();
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
            FakeMapItemRenderer.this.textureManager.bindTexture(FakeMapItemRenderer.TEXTURE_MAP_ICONS);
            int k = 0;
            Iterator var8 = this.mapData.mapDecorations.values().iterator();

            while(true) {
                MapDecoration mapdecoration;
                do {
                    if (!var8.hasNext()) {
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(0.0F, 0.0F, -0.04F);
                        GlStateManager.scale(1.0F, 1.0F, 1.0F);
                        GlStateManager.popMatrix();
                        return;
                    }

                    mapdecoration = (MapDecoration)var8.next();
                } while(noOverlayRendering && !mapdecoration.renderOnFrame());

                if (mapdecoration.render(k)) {
                    ++k;
                } else {
                    FakeMapItemRenderer.this.textureManager.bindTexture(FakeMapItemRenderer.TEXTURE_MAP_ICONS);
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(0.0F + (float)mapdecoration.getX() / 2.0F + 64.0F, 0.0F + (float)mapdecoration.getY() / 2.0F + 64.0F, -0.02F);
                    GlStateManager.rotate((float)(mapdecoration.getRotation() * 360) / 16.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.scale(4.0F, 4.0F, 3.0F);
                    GlStateManager.translate(-0.125F, 0.125F, 0.0F);
                    byte b0 = mapdecoration.getImage();
                    float f1 = (float)(b0 % 4 + 0) / 4.0F;
                    float f2 = (float)(b0 / 4 + 0) / 4.0F;
                    float f3 = (float)(b0 % 4 + 1) / 4.0F;
                    float f4 = (float)(b0 / 4 + 1) / 4.0F;
                    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                    float f5 = -0.001F;
                    bufferbuilder.pos(-1.0D, 1.0D, (double)((float)k * -0.001F)).tex((double)f1, (double)f2).endVertex();
                    bufferbuilder.pos(1.0D, 1.0D, (double)((float)k * -0.001F)).tex((double)f3, (double)f2).endVertex();
                    bufferbuilder.pos(1.0D, -1.0D, (double)((float)k * -0.001F)).tex((double)f3, (double)f4).endVertex();
                    bufferbuilder.pos(-1.0D, -1.0D, (double)((float)k * -0.001F)).tex((double)f1, (double)f4).endVertex();
                    tessellator.draw();
                    GlStateManager.popMatrix();
                    ++k;
                }
            }
        }
    }
}
