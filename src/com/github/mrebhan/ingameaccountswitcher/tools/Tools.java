// 
// Decompiled by Procyon v0.5.36
// 

package com.github.mrebhan.ingameaccountswitcher.tools;

import net.minecraft.client.gui.Gui;

public class Tools
{
    public static void drawBorderedRect(final int x, final int y, final int x1, final int y1, final int size, final int borderColor, final int insideColor) {
        Gui.func_73734_a(x + size, y + size, x1 - size, y1 - size, insideColor);
        Gui.func_73734_a(x + size, y + size, x1, y, borderColor);
        Gui.func_73734_a(x, y, x + size, y1, borderColor);
        Gui.func_73734_a(x1, y1, x1 - size, y + size, borderColor);
        Gui.func_73734_a(x, y1 - size, x1, y1, borderColor);
    }
}
