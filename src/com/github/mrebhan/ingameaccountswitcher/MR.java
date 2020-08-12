// 
// Decompiled by Procyon v0.5.36
// 

package com.github.mrebhan.ingameaccountswitcher;

import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import com.github.mrebhan.ingameaccountswitcher.tools.Config;

public class MR
{
    public static void init() {
        Config.load();
    }
    
    public static void setSession(final Session s) throws Exception {
        final Class<? extends Minecraft> mc = Minecraft.func_71410_x().getClass();
        try {
            Field session = null;
            for (final Field f : mc.getDeclaredFields()) {
                if (f.getType().isInstance(s)) {
                    session = f;
                    System.out.println("Found field " + f.toString() + ", injecting...");
                }
            }
            if (session == null) {
                throw new IllegalStateException("No field of type " + Session.class.getCanonicalName() + " declared.");
            }
            session.setAccessible(true);
            session.set(Minecraft.func_71410_x(), s);
            session.setAccessible(false);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
