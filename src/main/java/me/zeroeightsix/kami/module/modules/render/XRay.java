package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * TODO: Rewrite this mess
 */
@Module.Info(
        name = "XRay",
        category = Module.Category.RENDER,
        description = "See through common blocks!")
@EventBusSubscriber(modid = KamiMod.MODID
)
public class XRay extends Module {
    // A default reasonable configuration for the XRay. Most people will want to use it like this.
    private static final String DEFAULT_XRAY_CONFIG = "minecraft:grass,minecraft:dirt,minecraft:netherrack,minecraft:gravel,minecraft:sand,minecraft:stone";
    // A static mirror of the state.
    private static final Set<Block> hiddenBlocks = Collections.synchronizedSet(new HashSet<>());
    public static XRay INSTANCE;
    // This is used as part of a mechanism to make the Minecraft renderer play along with the XRay.
    // Essentially, the XRay primitive is just a block state transformer.
    // Then this implements a custom block that the block state transformer can use for hidden blocks.
    public static Block transparentBlock;
    private static boolean invertStatic, outlinesStatic = true;
    // This is the state used for hidden blocks.
    private static IBlockState transparentState;
    // Split by ',' & each element trimmed (this is a bit weird but it works for now?)
    private final Setting<String> hiddenBlockNames = register(Settings.stringBuilder("HiddenBlocks").withValue(DEFAULT_XRAY_CONFIG).withConsumer((old, value) -> {
        refreshHiddenBlocksSet(value);
        if (isEnabled())
            mc.renderGlobal.loadRenderers();
    }).build());
    public Setting<Boolean> invert = register(Settings.booleanBuilder("Invert").withValue(false).withConsumer((old, value) -> {
        invertStatic = value;
        if (isEnabled())
            mc.renderGlobal.loadRenderers();
    }).build());

    public XRay() {
        invertStatic = invert.getValue();
        Setting<Boolean> outlines = register(Settings.booleanBuilder("Outlines").withValue(true).withConsumer((old, value) -> {
            outlinesStatic = value;
            if (isEnabled())
                mc.renderGlobal.loadRenderers();
        }).build());
        outlinesStatic = outlines.getValue();
        refreshHiddenBlocksSet(hiddenBlockNames.getValue());
        INSTANCE = this;
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        transparentBlock = new Block(Material.GLASS) {
            // did you know this name's new
            @Override
            public BlockRenderLayer getRenderLayer() {
                return BlockRenderLayer.CUTOUT;
            }

            // Not opaque so other materials (such as, of course, ores) will render
            @Override
            public boolean isOpaqueCube(IBlockState blah) {
                return false;
            }

            // Essentially, the hidden-block world should be a projected grid-like thing...?
            @Override
            public boolean shouldSideBeRendered(IBlockState blah, IBlockAccess w, BlockPos pos, EnumFacing side) {
                BlockPos adj = pos.offset(side);
                IBlockState other = w.getBlockState(adj);
                // this directly adj. to this must never be rendered
                if (other.getBlock() == this)
                    return false;
                // if it contacts something opaque, don't render as we'll probably accidentally make it harder to see
                return !other.isOpaqueCube();
            }
        };
        transparentBlock.setRegistryName("kami_xray_transparent");
        transparentState = transparentBlock.getDefaultState();
        event.getRegistry().registerAll(transparentBlock);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        // this runs after transparentBlock is set, right?
        event.getRegistry().registerAll(new ItemBlock(transparentBlock).setRegistryName(Objects.requireNonNull(transparentBlock.getRegistryName())));
    }

    public static IBlockState transform(IBlockState input) {
        Block b = input.getBlock();
        boolean hide = hiddenBlocks.contains(b);
        if (invertStatic)
            hide = !hide;
        if (hide) {
            IBlockState target = Blocks.AIR.getDefaultState();
            if (outlinesStatic && (transparentState != null))
                target = transparentState;
            return target;
        }
        return input;
    }

    // Get hidden block list for command display
    public String extGet() {
        return extGetInternal(null);
    }

    // Add entry by arbitrary user-provided string
    public void extAdd(String s) {
        hiddenBlockNames.setValue(extGetInternal(null) + ", " + s);
    }

    // Remove entry by arbitrary user-provided string
    public void extRemove(String s) {
        hiddenBlockNames.setValue(extGetInternal(Block.getBlockFromName(s)));
    }

    // Clears the list.
    public void extClear() {
        hiddenBlockNames.setValue("");
    }

    // Resets the list to default
    public void extDefaults() {
        extClear();
        extAdd(DEFAULT_XRAY_CONFIG);
    }

    // Set the list to 1 value
    public void extSet(String s) {
        extClear();
        extAdd(s);
    }

    private String extGetInternal(Block filter) {
        StringBuilder sb = new StringBuilder();
        boolean notFirst = false;
        for (Block b : hiddenBlocks) {
            if (b == filter)
                continue;
            if (notFirst)
                sb.append(", ");
            notFirst = true;
            sb.append(Block.REGISTRY.getNameForObject(b));
        }
        return sb.toString();
    }

    private void refreshHiddenBlocksSet(String v) {
        hiddenBlocks.clear();
        for (String s : v.split(",")) {
            String s2 = s.trim();
            Block block = Block.getBlockFromName(s2);
            if (block != null)
                hiddenBlocks.add(block);
        }
    }

    @Override
    protected void onEnable() {
        // This is important because otherwise the changes in ChunkCache behavior won't propagate.
        // Also needs to be done if shouldHide effects change.
        mc.renderGlobal.loadRenderers();
    }

    @Override
    protected void onDisable() {
        // This is important because otherwise the changes in ChunkCache behavior won't propagate.
        // Also needs to be done if shouldHide effects change.
        mc.renderGlobal.loadRenderers();
    }

}
