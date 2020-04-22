package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.ColourUtils;
import me.zeroeightsix.kami.util.GeometryMasks;
import me.zeroeightsix.kami.util.KamiTessellator;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

/**
 * Created by 086 on 10/12/2017.
 * Updated by dominikaaaa on 14/12/19
 */
@Module.Info(
        name = "StorageESP",
        description = "Draws nice little lines around storage items",
        category = Module.Category.RENDER
)
public class StorageESP extends Module {

    private Setting<Boolean> chest = register(Settings.b("Chest", true));
    private Setting<Boolean> dispenser = register(Settings.b("Dispenser", true));
    private Setting<Boolean> shulker = register(Settings.b("Shulker", true));
    private Setting<Boolean> echest = register(Settings.b("Ender Chest", true));
    private Setting<Boolean> furnace = register(Settings.b("Furnace", true));
    private Setting<Boolean> hopper = register(Settings.b("Hopper", true));
    private Setting<Boolean> cart = register(Settings.b("Minecart", true));
    private Setting<Boolean> frame = register(Settings.b("Item Frame", true));


    private int getTileEntityColor(TileEntity tileEntity) {
        if (tileEntity instanceof TileEntityChest || tileEntity instanceof TileEntityDispenser)
            return ColourUtils.Colors.ORANGE;
        else if (tileEntity instanceof TileEntityShulkerBox)
            return ColourUtils.Colors.RED;
        else if (tileEntity instanceof TileEntityEnderChest)
            return ColourUtils.Colors.PURPLE;
        else if (tileEntity instanceof TileEntityFurnace)
            return ColourUtils.Colors.GRAY;
        else if (tileEntity instanceof TileEntityHopper)
            return ColourUtils.Colors.DARK_RED;
        else
            return -1;
    }

    private int getEntityColor(Entity entity) {
        if (entity instanceof EntityMinecartChest)
            return ColourUtils.Colors.ORANGE;
        else if (entity instanceof EntityItemFrame &&
                ((EntityItemFrame) entity).getDisplayedItem().getItem() instanceof ItemShulkerBox)
            return ColourUtils.Colors.YELLOW;
        else if (entity instanceof EntityItemFrame &&
                (!(((EntityItemFrame) entity).getDisplayedItem().getItem() instanceof ItemShulkerBox)))
            return ColourUtils.Colors.ORANGE;
        else
            return -1;
    }

    @Override
    public void onWorldRender(RenderEvent event) {
        ArrayList<Triplet<BlockPos, Integer, Integer>> a = new ArrayList<>();
        GlStateManager.pushMatrix();

        for (TileEntity tileEntity : Wrapper.getWorld().loadedTileEntityList) {
            BlockPos pos = tileEntity.getPos();
            int color = getTileEntityColor(tileEntity);
            int side = GeometryMasks.Quad.ALL;
            if (tileEntity instanceof TileEntityChest) {
                TileEntityChest chest = (TileEntityChest) tileEntity;
                // Leave only the colliding face and then flip the bits (~) to have ALL but that face
                if (chest.adjacentChestZNeg != null) side = ~(side & GeometryMasks.Quad.NORTH);
                if (chest.adjacentChestXPos != null) side = ~(side & GeometryMasks.Quad.EAST);
                if (chest.adjacentChestZPos != null) side = ~(side & GeometryMasks.Quad.SOUTH);
                if (chest.adjacentChestXNeg != null) side = ~(side & GeometryMasks.Quad.WEST);
            }
            if ((tileEntity instanceof TileEntityChest && chest.getValue()) || (tileEntity instanceof TileEntityDispenser && dispenser.getValue()) || (tileEntity instanceof TileEntityShulkerBox && shulker.getValue()) || (tileEntity instanceof TileEntityEnderChest && echest.getValue()) || (tileEntity instanceof TileEntityFurnace && furnace.getValue()) || (tileEntity instanceof TileEntityHopper && hopper.getValue()))
                if (color != -1)
                a.add(new Triplet<>(pos, color, side)); //GeometryTessellator.drawCuboid(event.getBuffer(), pos, GeometryMasks.Line.ALL, color);
        }

        for (Entity entity : Wrapper.getWorld().loadedEntityList) {
            BlockPos pos = entity.getPosition();
            int color = getEntityColor(entity);
            if ((entity instanceof EntityItemFrame && frame.getValue()) || (entity instanceof EntityMinecartChest && cart.getValue()))
                if (color != -1)
                    a.add(new Triplet<>(entity instanceof EntityItemFrame ? pos.add(0, -1, 0) : pos, color, GeometryMasks.Quad.ALL)); //GeometryTessellator.drawCuboid(event.getBuffer(), entity instanceof EntityItemFrame ? pos.add(0, -1, 0) : pos, GeometryMasks.Line.ALL, color);
        }

        KamiTessellator.prepare(GL11.GL_QUADS);
        for (Triplet<BlockPos, Integer, Integer> pair : a)
            KamiTessellator.drawBox(pair.getFirst(), changeAlpha(pair.getSecond(), 100), pair.getThird());
        KamiTessellator.release();

        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
    }

    int changeAlpha(int origColor, int userInputedAlpha) {
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
