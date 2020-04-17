package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.ColourUtils;
import me.zeroeightsix.kami.util.GeometryMasks;
import me.zeroeightsix.kami.util.KamiTessellator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

import static me.zeroeightsix.kami.util.LogUtil.getCurrentCoord;

@Module.Info(name = "BlockESP", description = "Shows you where blocks are", category = Module.Category.RENDER)
public class BlockESP extends Module {
    private static final String DEFAULT_BLOCK_ESP_CONFIG = "minecraft:concrete,minecraft:white_glazed_terracotta";
    private Setting<String> searchBlockNames = register(Settings.stringBuilder("highlightedBlocks").withValue(DEFAULT_BLOCK_ESP_CONFIG).build());
    Minecraft mc = Minecraft.getMinecraft();

    @Override
    public void onUpdate() {
        if (mc.player != null) {
            timeout();
        }
    }

    private static long startTime = 0;
    ArrayList<BlockESP.Triplet<BlockPos, Integer, Integer>> a;

    private void timeout() {
        if (startTime == 0)
            startTime = System.currentTimeMillis();
        if (startTime + 500 <= System.currentTimeMillis()) { // 1 timeout = 1 second = 1000 ms
            startTime = System.currentTimeMillis();
            a = new ArrayList<>();
            int[] pcoords = getCurrentCoord(false);
            int renderdist = 16;
            BlockPos pos1 = new BlockPos(pcoords[0] - renderdist, 0, pcoords[2] - renderdist);
            BlockPos pos2 = new BlockPos(pcoords[0] + renderdist, 255, pcoords[2] + renderdist);
            Iterable<BlockPos> blocks = BlockPos.getAllInBox(pos1, pos2);
            for (BlockPos block : blocks) {
                // TODO: make this user configurable and make it support multiple blocks.
                String blockLocalisedName = mc.world.getBlockState(block).getBlock().getLocalizedName();
                String concreteBlock = Blocks.CONCRETE.getLocalizedName();
                int side = GeometryMasks.Quad.ALL;
                if (blockLocalisedName.equals(concreteBlock)) {
                    a.add(new BlockESP.Triplet<>(block, ColourUtils.Colors.ORANGE, side));
                }
            }
        }
    }

    @Override
    public void onWorldRender(RenderEvent event) {
        if (a != null) {
            GlStateManager.pushMatrix();
            KamiTessellator.prepare(GL11.GL_QUADS);
            for (BlockESP.Triplet<BlockPos, Integer, Integer> pair : a)
                KamiTessellator.drawBox(pair.getFirst(), changeAlpha(pair.getSecond(), 100), pair.getThird());
            KamiTessellator.release();

            GlStateManager.popMatrix();
            GlStateManager.enableTexture2D();
        }
    }

    static int changeAlpha(int origColor, int userInputedAlpha) {
        origColor = origColor & 0x00ffffff; //drop the previous alpha value
        return (userInputedAlpha << 24) | origColor; //add the one the user inputted
    }

    public class Triplet<T, U, V> {

        private final T first;
        private final U second;
        private final V third;

        public Triplet(T first, U second, V third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        public T getFirst() {
            return first;
        }

        public U getSecond() {
            return second;
        }

        public V getThird() {
            return third;
        }
    }
}

