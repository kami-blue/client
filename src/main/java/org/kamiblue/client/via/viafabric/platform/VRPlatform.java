/*
 * MIT License
 *
 * Copyright (c) 2018- creeper123123321 <https://creeper123123321.keybase.pub/>
 * Copyright (c) 2019- contributors <https://github.com/ViaVersion/ViaFabric/graphs/contributors>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.kamiblue.client.via.viafabric.platform;

import org.kamiblue.client.via.viafabric.ViaFabric;
import org.kamiblue.client.via.viafabric.util.FutureTaskId;
import org.kamiblue.client.via.viafabric.util.JLoggerToLog4j;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import us.myles.ViaVersion.api.ViaAPI;
import us.myles.ViaVersion.api.ViaVersionConfig;
import us.myles.ViaVersion.api.command.ViaCommandSender;
import us.myles.ViaVersion.api.configuration.ConfigurationProvider;
import us.myles.ViaVersion.api.platform.TaskId;
import us.myles.ViaVersion.api.platform.ViaConnectionManager;
import us.myles.ViaVersion.api.platform.ViaPlatform;
import us.myles.viaversion.libs.bungeecordchat.api.chat.TextComponent;
import us.myles.viaversion.libs.bungeecordchat.chat.ComponentSerializer;
import us.myles.viaversion.libs.gson.JsonObject;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class VRPlatform implements ViaPlatform<UUID> {
    private final Logger logger = new JLoggerToLog4j(LogManager.getLogger("ViaVersion"));
    private final VRViaConfig config;
    private final File dataFolder;
    private final ViaConnectionManager connectionManager;
    private final ViaAPI<UUID> api;

    public VRPlatform() {
        Path configDir = Minecraft.getMinecraft().gameDir.toPath().resolve("ViaFabric");
        config = new VRViaConfig(configDir.resolve("viaversion.yml").toFile());
        dataFolder = configDir.toFile();
        connectionManager = new VRConnectionManager();
        api = new VRViaAPI();
    }

    public static MinecraftServer getServer() {
        // In 1.8.9 integrated server instance exists even if it's not running
        if (!Minecraft.getMinecraft().isIntegratedServerRunning()) return null;
        return Minecraft.getMinecraft().getIntegratedServer();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getPlatformName() {
        return "ViaFabric";
    }

    @Override
    public String getPlatformVersion() {
        return ViaFabric.getVersion();
    }

    @Override
    public String getPluginVersion() {
        return "3.3.0";
    }

    @Override
    public TaskId runAsync(Runnable runnable) {
        return new FutureTaskId(CompletableFuture
                .runAsync(runnable, ViaFabric.ASYNC_EXECUTOR)
                .exceptionally(throwable -> {
                    if (!(throwable instanceof CancellationException)) {
                        throwable.printStackTrace();
                    }
                    return null;
                })
        );
    }

    @Override
    public TaskId runSync(Runnable runnable) {
        if (getServer() != null) {
            return runServerSync(runnable);
        } else {
            return runEventLoop(runnable);
        }
    }

    private TaskId runServerSync(Runnable runnable) {
        // Kick task needs to be on main thread, it does already have error logger
        return new FutureTaskId(CompletableFuture.runAsync(runnable, it -> getServer().callFromMainThread((Callable
                <Void>) () -> {
            it.run();
            return null;
        })));
    }

    private TaskId runEventLoop(Runnable runnable) {
        return new FutureTaskId(
                ViaFabric.EVENT_LOOP
                        .submit(runnable)
                        .addListener(errorLogger())
        );
    }

    @Override
    public TaskId runSync(Runnable runnable, Long ticks) {
        // ViaVersion seems to not need to run delayed tasks on main thread
        return new FutureTaskId(
                ViaFabric.EVENT_LOOP
                        .schedule(() -> runSync(runnable), ticks * 50, TimeUnit.MILLISECONDS)
                        .addListener(errorLogger())
        );
    }

    @Override
    public TaskId runRepeatingSync(Runnable runnable, Long ticks) {
        // ViaVersion seems to not need to run repeating tasks on main thread
        return new FutureTaskId(
                ViaFabric.EVENT_LOOP
                        .scheduleAtFixedRate(() -> runSync(runnable), 0, ticks * 50, TimeUnit.MILLISECONDS)
                        .addListener(errorLogger())
        );
    }

    private <T extends Future<?>> GenericFutureListener<T> errorLogger() {
        return future -> {
            if (!future.isCancelled() && future.cause() != null) {
                future.cause().printStackTrace();
            }
        };
    }

    @Override
    public void cancelTask(TaskId taskId) {
        if (taskId instanceof FutureTaskId) {
            ((FutureTaskId) taskId).getObject().cancel(false);
        }
    }

    @Override
    public ViaCommandSender[] getOnlinePlayers() {
        return new ViaCommandSender[0];
    }

    @Override
    public void sendMessage(UUID uuid, String s) {
    }

    @Override
    public boolean kickPlayer(UUID uuid, String s) {
        return kickServer(uuid, s);
    }

    private boolean kickServer(UUID uuid, String s) {
        return false;  // Can't know if it worked
    }

    @Override
    public boolean isPluginEnabled() {
        return true;
    }

    @Override
    public ViaAPI<UUID> getApi() {
        return api;
    }

    @Override
    public ViaVersionConfig getConf() {
        return config;
    }

    @Override
    public ConfigurationProvider getConfigurationProvider() {
        return config;
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public void onReload() {
        // Nothing to do
    }

    @Override
    public JsonObject getDump() {
        return new JsonObject();
    }

    @Override
    public boolean isOldClientsAllowed() {
        return true;
    }

    @Override
    public ViaConnectionManager getConnectionManager() {
        return connectionManager;
    }

    private String legacyToJson(String legacy) {
        return ComponentSerializer.toString(TextComponent.fromLegacyText(legacy));
    }
}
