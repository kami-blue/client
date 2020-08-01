package me.zeroeightsix.kami.module.modules.render;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.Coordinate;
import me.zeroeightsix.kami.util.GeometryMasks;
import me.zeroeightsix.kami.util.KamiTessellator;
import me.zeroeightsix.kami.util.Waypoint;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPortal;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static me.zeroeightsix.kami.util.ColourConverter.rgbToInt;
import static me.zeroeightsix.kami.util.ColourUtils.toRGBA;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendErrorMessage;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendWarningMessage;

/**
 * @author wnuke
 * Updated by dominikaaaa on 20/04/20
 * Updated by Afel on 08/06/20
 */
@Module.Info(
        name = "Search",
        description = "Highlights blocks in the world",
        category = Module.Category.RENDER
)
public class Search extends Module {
    private Set<Block> espBlocks;
    private static final String DEFAULT_BLOCK_ESP_CONFIG = "minecraft:portal, minecraft:end_portal_frame, minecraft:bed";

    private final Setting<Integer> alpha = register(Settings.integerBuilder("Transparency").withMinimum(1).withMaximum(255).withValue(120).build());
    private final Setting<Integer> update = register(Settings.integerBuilder("UpdateInterval").withMinimum(100).withMaximum(10000).withValue(1500).build());
    public Setting<Boolean> overrideWarning = register(Settings.booleanBuilder("overrideWarning").withValue(false).withVisibility(v -> false).build());
    private final Setting<String> espBlockNames = register(Settings.stringBuilder("HiddenBlocks").withValue(DEFAULT_BLOCK_ESP_CONFIG).withConsumer((old, value) -> refreshESPBlocksSet(value)).build());
    private final Setting<Boolean> tracers = register(Settings.booleanBuilder("Tracers").withValue(true).build());

    public String extGet() {
        return extGetInternal(null);
    }

    // Add entry by arbitrary user-provided string
    public void extAdd(String s) {
        espBlockNames.setValue(extGetInternal(null) + ", " + s);
    }

    // Remove entry by arbitrary user-provided string
    public void extRemove(String s) {
        espBlockNames.setValue(extGetInternal(Block.getBlockFromName(s)));
    }

    // Clears the list.
    public void extClear() {
        espBlockNames.setValue("");
    }

    // Resets the list to default
    public void extDefaults() {
        extClear();
        extAdd(DEFAULT_BLOCK_ESP_CONFIG);
    }

    // Set the list to 1 value
    public void extSet(String s) {
        extClear();
        extAdd(s);
    }

    private String extGetInternal(Block filter) {
        StringBuilder sb = new StringBuilder();
        boolean notFirst = false;
        for (Block b : espBlocks) {
            if (b == filter)
                continue;
            if (notFirst)
                sb.append(", ");
            notFirst = true;
            sb.append(Block.REGISTRY.getNameForObject(b));
        }
        return sb.toString();
    }

    Executor exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
            r -> {
                Thread t = new Thread(r);
                t.setPriority(Thread.NORM_PRIORITY - 2); //LOW priority
                return t;
            });
    Executor cachedExec = Executors.newCachedThreadPool();

    @Override
    public void onUpdate() {
        if (espBlocks == null) {
            refreshESPBlocksSet(espBlockNames.getValue());
        }
        if (mc.player == null) return;
        if (shouldRun() && isEnabled()) reloadChunks();
    }

    @Override
    public void onEnable() {
        if (!overrideWarning.getValue() && GlStateManager.glGetString(GL11.GL_VENDOR).contains("Intel")) {
            sendErrorMessage(getChatName() + " Warning: Running Search with an Intel Integrated GPU is not recommended, as it has a &llarge&r impact on performance.");
            sendWarningMessage(getChatName() + " If you're sure you want to try, run the &7" + Command.getCommandPrefix() + "search override&f command");
            disable();
            return;
        }
        refreshESPBlocksSet(espBlockNames.getValue());
        startTime = 0;
        //reloadChunks();
    }

    @Override
    protected void onDisable() {
        mainList.clear();
    }

    private void refreshESPBlocksSet(String v) {
        espBlocks = Collections.synchronizedSet(new HashSet<>());
        for (String s : v.split(",")) {
            String s2 = s.trim();
            if (!s2.equals("minecraft:air")) {
                Block block = Block.getBlockFromName(s2);
                if (block != null)
                    espBlocks.add(block);
            }
        }
        mainList.clear();
        startTime = 0;
    }


    private long startTime = 0;
    private final Map<ChunkPos, Map<BlockPos, Tuple<Integer, Integer>>> mainList = new ConcurrentHashMap<>();

    private boolean shouldRun() {
        if (mc.world == null) return false;
        if (startTime == 0)
            startTime = System.currentTimeMillis();
        if (startTime + update.getValue() <= System.currentTimeMillis()) { // 1 timeout = 1 second = 1000 ms
            startTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    @EventHandler
    public Listener<ChunkEvent.Load> chunkLoadListener = new Listener<>(event -> {
        if (isEnabled()) {
            cachedExec.execute(() -> {
                Chunk chunk = event.getChunk();
                ChunkPos pos = chunk.getPos();
                Map<BlockPos, Tuple<Integer, Integer>> found = findBlocksInChunk(chunk, espBlocks);
                if (!found.isEmpty()) {
                    Map<BlockPos, Tuple<Integer, Integer>> actual = mainList.computeIfAbsent(pos, (p) -> new ConcurrentHashMap<>());
                    actual.clear();
                    actual.putAll(found);
                }
            });
        }
    });

    @EventHandler
    public Listener<ChunkEvent.Unload> chunkUnLoadListener = new Listener<>(event -> {
        if (isEnabled())
            mainList.remove(event.getChunk().getPos());
    });

    private void reloadChunks() {
        Coordinate pcoords = Waypoint.INSTANCE.getCurrentCoord();
        int renderdist = mc.gameSettings.renderDistanceChunks;
        if (renderdist > 8) {
            renderdist = 8;
        }
        ChunkProviderClient providerClient = mc.world.getChunkProvider();
        for (int x = -renderdist; x < renderdist; x++) {
            for (int z = -renderdist; z < renderdist; z++) {
                Chunk chunk = providerClient.getLoadedChunk((pcoords.x >> 4) + x, (pcoords.z >> 4) + z);
                if (chunk != null)
                    exec.execute(() ->
                            loadChunk(chunk)
                    );
            }
        }
    }

    private void loadChunk(Chunk chunk) {
        Map<BlockPos, Tuple<Integer, Integer>> actual = mainList.get(chunk.getPos());
        Map<BlockPos, Tuple<Integer, Integer>> found = findBlocksInChunk(chunk, espBlocks);
        if (!found.isEmpty() || actual != null) {
            actual = mainList.computeIfAbsent(chunk.getPos(), (p) -> new ConcurrentHashMap<>());
            actual.clear();
            actual.putAll(found);
        }
    }

    private Map<BlockPos, Tuple<Integer, Integer>> findBlocksInChunk(Chunk chunk, Set<Block> blocksToFind) {
        BlockPos pos1 = new BlockPos(chunk.getPos().getXStart(), 0, chunk.getPos().getZStart());
        BlockPos pos2 = new BlockPos(chunk.getPos().getXEnd(), 256, chunk.getPos().getZEnd());
        Iterable<BlockPos> blocks = BlockPos.getAllInBox(pos1, pos2);
        Map<BlockPos, Tuple<Integer, Integer>> foundBlocks = new HashMap<>();
        try {
            for (BlockPos blockPos : blocks) {
                int side = GeometryMasks.Quad.ALL;
                Block block = chunk.getBlockState(blockPos).getBlock();
                if (blocksToFind.contains(block)) {
                    Tuple<Integer, Integer> tuple = getTuple(side, block);
                    foundBlocks.put(blockPos, tuple);
                }
            }
        } catch (NullPointerException ignored) {
        } //to fix ghost chunks get loaded and generating NullPointerExceptions
        return foundBlocks;
    }

    private Tuple<Integer, Integer> getTuple(int side, Block block) {
        int c = block.blockMapColor.colorValue;
        if (block instanceof BlockPortal) {
            c = rgbToInt(82, 49, 153);
        }
        int[] cia = {c >> 16, c >> 8 & 255, c & 255};
        int blockColor = toRGBA(cia[0], cia[1], cia[2], alpha.getValue());
        return new Tuple<>(blockColor, side);
    }

    Map<BlockPos, Tuple<Integer, Integer>> blocksToShow;

    @Override
    public void onWorldRender(RenderEvent event) {
        if (mainList != null && shouldUpdate()) {
            blocksToShow = mainList.values().stream()
                    .flatMap((e) -> e.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        if (blocksToShow != null) {
            GlStateManager.pushMatrix();
            KamiTessellator.prepare(GL11.GL_QUADS);
            for (Map.Entry<BlockPos, Tuple<Integer, Integer>> entry : blocksToShow.entrySet()) {
                KamiTessellator.drawBox(entry.getKey(), entry.getValue().getFirst(), entry.getValue().getSecond());
            }
            KamiTessellator.release();
            GlStateManager.popMatrix();
            GlStateManager.enableTexture2D();

            if (tracers.getValue()) {
                for (Map.Entry<BlockPos, Tuple<Integer, Integer>> entry : blocksToShow.entrySet()) {
                    KamiTessellator.drawLineToBlock(entry.getKey(), entry.getValue().getFirst(), ((float) alpha.getValue()) / 255);
                }
            }
        }
    }

    private long previousTime = 0;

    private boolean shouldUpdate() {
        if (previousTime + 100 <= System.currentTimeMillis()) {
            previousTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public static class Triplet<T, U, V> {

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

