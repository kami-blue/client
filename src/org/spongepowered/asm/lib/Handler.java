// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib;

class Handler
{
    Label start;
    Label end;
    Label handler;
    String desc;
    int type;
    Handler next;
    
    static Handler remove(Handler h, final Label start, final Label end) {
        if (h == null) {
            return null;
        }
        h.next = remove(h.next, start, end);
        final int hstart = h.start.position;
        final int hend = h.end.position;
        final int s = start.position;
        final int e = (end == null) ? Integer.MAX_VALUE : end.position;
        if (s < hend && e > hstart) {
            if (s <= hstart) {
                if (e >= hend) {
                    h = h.next;
                }
                else {
                    h.start = end;
                }
            }
            else if (e >= hend) {
                h.end = start;
            }
            else {
                final Handler g = new Handler();
                g.start = end;
                g.end = h.end;
                g.handler = h.handler;
                g.desc = h.desc;
                g.type = h.type;
                g.next = h.next;
                h.end = start;
                h.next = g;
            }
        }
        return h;
    }
}
