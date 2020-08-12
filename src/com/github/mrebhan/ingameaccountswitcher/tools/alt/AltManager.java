// 
// Decompiled by Procyon v0.5.36
// 

package com.github.mrebhan.ingameaccountswitcher.tools.alt;

import java.util.Iterator;
import the_fireplace.ias.config.ConfigValues;
import com.github.mrebhan.ingameaccountswitcher.MR;
import net.minecraft.util.Session;
import com.mojang.util.UUIDTypeAdapter;
import the_fireplace.ias.account.AlreadyLoggedInException;
import the_fireplace.iasencrypt.EncryptionTools;
import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.Agent;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.Minecraft;
import java.util.UUID;
import com.mojang.authlib.UserAuthentication;

public class AltManager
{
    private static AltManager manager;
    private final UserAuthentication auth;
    
    private AltManager() {
        final UUID uuid = UUID.randomUUID();
        final AuthenticationService authService = (AuthenticationService)new YggdrasilAuthenticationService(Minecraft.func_71410_x().func_110437_J(), uuid.toString());
        this.auth = authService.createUserAuthentication(Agent.MINECRAFT);
        authService.createMinecraftSessionService();
    }
    
    public static AltManager getInstance() {
        if (AltManager.manager == null) {
            AltManager.manager = new AltManager();
        }
        return AltManager.manager;
    }
    
    public Throwable setUser(final String username, final String password) {
        Throwable throwable = null;
        if (!Minecraft.func_71410_x().func_110432_I().func_111285_a().equals(EncryptionTools.decode(username)) || Minecraft.func_71410_x().func_110432_I().func_148254_d().equals("0")) {
            if (!Minecraft.func_71410_x().func_110432_I().func_148254_d().equals("0")) {
                for (final AccountData data : AltDatabase.getInstance().getAlts()) {
                    if (data.alias.equals(Minecraft.func_71410_x().func_110432_I().func_111285_a()) && data.user.equals(username)) {
                        throwable = new AlreadyLoggedInException();
                        return throwable;
                    }
                }
            }
            this.auth.logOut();
            this.auth.setUsername(EncryptionTools.decode(username));
            this.auth.setPassword(EncryptionTools.decode(password));
            try {
                this.auth.logIn();
                final Session session = new Session(this.auth.getSelectedProfile().getName(), UUIDTypeAdapter.fromUUID(this.auth.getSelectedProfile().getId()), this.auth.getAuthenticatedToken(), this.auth.getUserType().getName());
                MR.setSession(session);
                for (int i = 0; i < AltDatabase.getInstance().getAlts().size(); ++i) {
                    final AccountData data2 = AltDatabase.getInstance().getAlts().get(i);
                    if (data2.user.equals(username) && data2.pass.equals(password)) {
                        data2.alias = session.func_111285_a();
                    }
                }
            }
            catch (Exception e) {
                throwable = e;
            }
        }
        else if (!ConfigValues.ENABLERELOG) {
            throwable = new AlreadyLoggedInException();
        }
        return throwable;
    }
    
    public void setUserOffline(final String username) {
        this.auth.logOut();
        final Session session = new Session(username, username, "0", "legacy");
        try {
            MR.setSession(session);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    static {
        AltManager.manager = null;
    }
}
