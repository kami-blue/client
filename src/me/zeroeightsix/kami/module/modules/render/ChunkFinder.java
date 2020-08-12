// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.render;

import org.apache.commons.lang3.SystemUtils;
import net.minecraft.client.Minecraft;
import java.io.IOException;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.File;
import java.nio.file.Path;
import me.zeroeightsix.kami.KamiMod;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import me.zeroeightsix.kami.command.Command;
import java.util.Iterator;
import me.zeroeightsix.kami.event.events.RenderEvent;
import java.util.function.Predicate;
import org.lwjgl.opengl.GL11;
import me.zeroeightsix.kami.setting.Settings;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.ChunkEvent;
import me.zero.alpine.listener.Listener;
import net.minecraft.world.chunk.Chunk;
import java.util.ArrayList;
import java.io.PrintWriter;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "ChunkFinder", description = "Highlights newly generated chunks", category = Category.RENDER)
public class ChunkFinder extends Module
{
    private Setting<Integer> yOffset;
    private Setting<Boolean> relative;
    private Setting<Boolean> saveNewChunks;
    private Setting<SaveOption> saveOption;
    private Setting<Boolean> saveInRegionFolder;
    private Setting<Boolean> alsoSaveNormalCoords;
    private Setting<Boolean> closeFile;
    private LastSetting lastSetting;
    private PrintWriter logWriter;
    static ArrayList<Chunk> chunks;
    private static boolean dirty;
    private int list;
    @EventHandler
    public Listener<ChunkEvent> listener;
    @EventHandler
    private Listener<net.minecraftforge.event.world.ChunkEvent.Unload> unloadListener;
    
    public ChunkFinder() {
        this.yOffset = this.register(Settings.i("Y Offset", 0));
        this.relative = this.register(Settings.b("Relative", true));
        this.saveNewChunks = this.register(Settings.b("Save New Chunks", false));
        this.saveOption = this.register((Setting<SaveOption>)Settings.enumBuilder(SaveOption.class).withValue(SaveOption.extraFolder).withName("Save Option").withVisibility(aBoolean -> this.saveNewChunks.getValue()).build());
        this.saveInRegionFolder = this.register(Settings.booleanBuilder("In Region").withValue(false).withVisibility(aBoolean -> this.saveNewChunks.getValue()).build());
        this.alsoSaveNormalCoords = this.register(Settings.booleanBuilder("Save Normal Coords").withValue(false).withVisibility(aBoolean -> this.saveNewChunks.getValue()).build());
        this.closeFile = this.register(Settings.booleanBuilder("Close File").withValue(false).withVisibility(aBoolean -> this.saveNewChunks.getValue()).build());
        this.lastSetting = new LastSetting();
        this.list = GL11.glGenLists(1);
        this.listener = new Listener<ChunkEvent>(event -> {
            if (!event.getPacket().func_149274_i()) {
                ChunkFinder.chunks.add(event.getChunk());
                ChunkFinder.dirty = true;
                if (this.saveNewChunks.getValue()) {
                    this.saveNewChunk(event.getChunk());
                }
            }
            return;
        }, (Predicate<ChunkEvent>[])new Predicate[0]);
        this.unloadListener = new Listener<net.minecraftforge.event.world.ChunkEvent.Unload>(event -> ChunkFinder.dirty = ChunkFinder.chunks.remove(event.getChunk()), (Predicate<net.minecraftforge.event.world.ChunkEvent.Unload>[])new Predicate[0]);
    }
    
    @Override
    public void onWorldRender(final RenderEvent event) {
        if (ChunkFinder.dirty) {
            GL11.glNewList(this.list, 4864);
            GL11.glPushMatrix();
            GL11.glEnable(2848);
            GL11.glDisable(2929);
            GL11.glDisable(3553);
            GL11.glDepthMask(false);
            GL11.glBlendFunc(770, 771);
            GL11.glEnable(3042);
            GL11.glLineWidth(1.0f);
            for (final Chunk chunk : ChunkFinder.chunks) {
                final double posX = chunk.field_76635_g * 16;
                final double posY = 0.0;
                final double posZ = chunk.field_76647_h * 16;
                GL11.glColor3f(0.6f, 0.1f, 0.2f);
                GL11.glBegin(2);
                GL11.glVertex3d(posX, posY, posZ);
                GL11.glVertex3d(posX + 16.0, posY, posZ);
                GL11.glVertex3d(posX + 16.0, posY, posZ + 16.0);
                GL11.glVertex3d(posX, posY, posZ + 16.0);
                GL11.glVertex3d(posX, posY, posZ);
                GL11.glEnd();
            }
            GL11.glDisable(3042);
            GL11.glDepthMask(true);
            GL11.glEnable(3553);
            GL11.glEnable(2929);
            GL11.glDisable(2848);
            GL11.glPopMatrix();
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glEndList();
            ChunkFinder.dirty = false;
        }
        final double x = ChunkFinder.mc.func_175598_ae().field_78725_b;
        final double y = this.relative.getValue() ? 0.0 : (-ChunkFinder.mc.func_175598_ae().field_78726_c);
        final double z = ChunkFinder.mc.func_175598_ae().field_78723_d;
        GL11.glTranslated(-x, y + this.yOffset.getValue(), -z);
        GL11.glCallList(this.list);
        GL11.glTranslated(x, -(y + this.yOffset.getValue()), z);
    }
    
    @Override
    public void onUpdate() {
        if (!this.closeFile.getValue()) {
            return;
        }
        this.closeFile.setValue(false);
        Command.sendChatMessage("close file");
        this.logWriterClose();
    }
    
    @Override
    protected void onDisable() {
        Command.sendChatMessage("onDisable");
        this.logWriterClose();
        ChunkFinder.chunks.clear();
    }
    
    public void saveNewChunk(final Chunk chunk) {
        this.saveNewChunk(this.testAndGetLogWriter(), this.getNewChunkInfo(chunk));
    }
    
    private String getNewChunkInfo(final Chunk chunk) {
        String rV = String.format("%d,%d,%d", System.currentTimeMillis(), chunk.field_76635_g, chunk.field_76647_h);
        if (this.alsoSaveNormalCoords.getValue()) {
            rV += String.format(",%d,%d", chunk.field_76635_g * 16 + 8, chunk.field_76647_h * 16 + 8);
        }
        return rV;
    }
    
    private PrintWriter testAndGetLogWriter() {
        if (this.lastSetting.testChangeAndUpdate()) {
            this.logWriterClose();
            this.logWriterOpen();
        }
        return this.logWriter;
    }
    
    private void logWriterClose() {
        if (this.logWriter != null) {
            this.logWriter.close();
            this.logWriter = null;
            this.lastSetting = new LastSetting();
        }
    }
    
    private void logWriterOpen() {
        final String filepath = this.getPath().toString();
        try {
            this.logWriter = new PrintWriter(new BufferedWriter(new FileWriter(filepath, true)), true);
            String head = "timestamp,ChunkX,ChunkZ";
            if (this.alsoSaveNormalCoords.getValue()) {
                head += ",x coordinate,z coordinate";
            }
            this.logWriter.println(head);
        }
        catch (Exception e) {
            e.printStackTrace();
            KamiMod.log.error("some exception happened when trying to start the logging -> " + e.getMessage());
            Command.sendChatMessage("onLogStart: " + e.getMessage());
        }
    }
    
    private Path getPath() {
        File file = null;
        final int dimension = ChunkFinder.mc.field_71439_g.field_71093_bK;
        if (ChunkFinder.mc.func_71356_B()) {
            try {
                file = ChunkFinder.mc.func_71401_C().func_71218_a(dimension).getChunkSaveLocation();
            }
            catch (Exception e) {
                e.printStackTrace();
                KamiMod.log.error("some exception happened when getting canonicalFile -> " + e.getMessage());
                Command.sendChatMessage("onGetPath: " + e.getMessage());
            }
            if (file.toPath().relativize(ChunkFinder.mc.field_71412_D.toPath()).getNameCount() != 2) {
                file = file.getParentFile();
            }
        }
        else {
            file = this.makeMultiplayerDirectory().toFile();
        }
        if (dimension != 0) {
            file = new File(file, "DIM" + dimension);
        }
        if (this.saveInRegionFolder.getValue()) {
            file = new File(file, "region");
        }
        file = new File(file, "newChunkLogs");
        final String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        file = new File(file, ChunkFinder.mc.func_110432_I().func_111285_a() + "_" + date + ".csv");
        final Path rV = file.toPath();
        try {
            if (!Files.exists(rV, new LinkOption[0])) {
                Files.createDirectories(rV.getParent(), (FileAttribute<?>[])new FileAttribute[0]);
                Files.createFile(rV, (FileAttribute<?>[])new FileAttribute[0]);
            }
        }
        catch (IOException e2) {
            e2.printStackTrace();
            KamiMod.log.error("some exception happened when trying to make the file -> " + e2.getMessage());
            Command.sendChatMessage("onCreateFile: " + e2.getMessage());
        }
        return rV;
    }
    
    private Path makeMultiplayerDirectory() {
        File rV = Minecraft.func_71410_x().field_71412_D;
        switch (this.saveOption.getValue()) {
            case liteLoaderWdl: {
                final String folderName = ChunkFinder.mc.func_147104_D().field_78847_a;
                rV = new File(rV, "saves");
                rV = new File(rV, folderName);
                break;
            }
            case nhackWdl: {
                final String folderName = this.getNHackInetName();
                rV = new File(rV, "config");
                rV = new File(rV, "wdl-saves");
                rV = new File(rV, folderName);
                if (!rV.exists()) {
                    Command.sendChatMessage("nhack wdl directory doesnt exist: " + folderName);
                    Command.sendChatMessage("creating the directory now. It is recommended to update the ip");
                    break;
                }
                break;
            }
            default: {
                String folderName = ChunkFinder.mc.func_147104_D().field_78847_a + "-" + ChunkFinder.mc.func_147104_D().field_78845_b;
                if (SystemUtils.IS_OS_WINDOWS) {
                    folderName = folderName.replace(":", "_");
                }
                rV = new File(rV, "KAMI_NewChunks");
                rV = new File(rV, folderName);
                break;
            }
        }
        return rV.toPath();
    }
    
    private String getNHackInetName() {
        String folderName = ChunkFinder.mc.func_147104_D().field_78845_b;
        if (SystemUtils.IS_OS_WINDOWS) {
            folderName = folderName.replace(":", "_");
        }
        if (this.hasNoPort(folderName)) {
            folderName += "_25565";
        }
        return folderName;
    }
    
    private boolean hasNoPort(final String ip) {
        if (!ip.contains("_")) {
            return true;
        }
        final String[] sp = ip.split("_");
        final String ending = sp[sp.length - 1];
        return !this.isInteger(ending);
    }
    
    private boolean isInteger(final String s) {
        try {
            Integer.parseInt(s);
        }
        catch (NumberFormatException | NullPointerException ex2) {
            final RuntimeException ex;
            final RuntimeException e = ex;
            return false;
        }
        return true;
    }
    
    private void saveNewChunk(final PrintWriter log, final String data) {
        log.println(data);
    }
    
    @Override
    public void destroy() {
        GL11.glDeleteLists(1, 1);
    }
    
    static {
        ChunkFinder.chunks = new ArrayList<Chunk>();
        ChunkFinder.dirty = true;
    }
    
    private enum SaveOption
    {
        extraFolder, 
        liteLoaderWdl, 
        nhackWdl;
    }
    
    private class LastSetting
    {
        SaveOption lastSaveOption;
        boolean lastInRegion;
        boolean lastSaveNormal;
        int dimension;
        String ip;
        
        public boolean testChangeAndUpdate() {
            if (this.testChange()) {
                this.update();
                return true;
            }
            return false;
        }
        
        public boolean testChange() {
            return ChunkFinder.this.saveOption.getValue() != this.lastSaveOption || ChunkFinder.this.saveInRegionFolder.getValue() != this.lastInRegion || ChunkFinder.this.alsoSaveNormalCoords.getValue() != this.lastSaveNormal || this.dimension != ChunkFinder.mc.field_71439_g.field_71093_bK || !ChunkFinder.mc.func_147104_D().field_78845_b.equals(this.ip);
        }
        
        private void update() {
            this.lastSaveOption = ChunkFinder.this.saveOption.getValue();
            this.lastInRegion = ChunkFinder.this.saveInRegionFolder.getValue();
            this.lastSaveNormal = ChunkFinder.this.alsoSaveNormalCoords.getValue();
            this.dimension = ChunkFinder.mc.field_71439_g.field_71093_bK;
            this.ip = ChunkFinder.mc.func_147104_D().field_78845_b;
        }
    }
}
