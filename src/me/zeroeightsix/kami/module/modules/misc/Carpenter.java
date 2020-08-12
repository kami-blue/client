// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.misc;

import me.zeroeightsix.kami.util.ColourUtils;
import java.util.List;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import me.zeroeightsix.kami.module.Module;

public class Carpenter extends Module
{
    private static final /* synthetic */ int[] lIlIlIIIlll;
    private /* synthetic */ int displayList;
    
    public Carpenter() {
        this.displayList = Carpenter.lIlIlIIIlll[0];
    }
    
    private static void llIlllllIlll() {
        (lIlIlIIIlll = new int[1])[0] = -" ".length();
    }
    
    static {
        llIlllllIlll();
    }
    
    public static class Selection
    {
        private /* synthetic */ BlockPos second;
        private static final /* synthetic */ int[] lIIIllIIll;
        private /* synthetic */ BlockPos first;
        
        public void moveSelection(final int lIlIIlIIIIlll, final int lIlIIlIIIIIlI, final int lIlIIlIIIIlIl) {
            this.first = this.first.func_177982_a(lIlIIlIIIIlll, lIlIIlIIIIIlI, lIlIIlIIIIlIl);
            this.second = this.second.func_177982_a(lIlIIlIIIIlll, lIlIIlIIIIIlI, lIlIIlIIIIlIl);
        }
        
        static {
            llIIIIIlllI();
        }
        
        public BlockPos getMaximum() {
            final int lIlIIlIIlllll = Math.min(this.first.func_177958_n(), this.second.func_177958_n()) + Selection.lIIIllIIll[0];
            final int lIlIIlIIllllI = Math.min(this.first.func_177956_o(), this.second.func_177956_o());
            final int lIlIIlIIlllIl = Math.min(this.first.func_177952_p(), this.second.func_177952_p()) + Selection.lIIIllIIll[0];
            return new BlockPos(lIlIIlIIlllll, lIlIIlIIllllI, lIlIIlIIlllIl);
        }
        
        public int getSize() {
            return this.getWidth() * this.getLength() * this.getHeight();
        }
        
        private static boolean llIIIIlIlII(final int lIlIIIlllIIlI, final int lIlIIIlllIIIl) {
            return lIlIIIlllIIlI < lIlIIIlllIIIl;
        }
        
        public int getWidth() {
            final int lIlIIllIIlIlI = Math.min(this.first.func_177958_n(), this.second.func_177958_n());
            final int lIlIIllIIlIIl = Math.max(this.first.func_177958_n(), this.second.func_177958_n()) + Selection.lIIIllIIll[0];
            return Math.abs(lIlIIllIIlIlI - lIlIIllIIlIIl);
        }
        
        public boolean isInvalid() {
            int n;
            if (!llIIIIIllll(this.first) || llIIIIlIIII(this.second)) {
                n = Selection.lIIIllIIll[0];
                "".length();
                if (((7 + 55 - 7 + 85 ^ 149 + 19 - 89 + 72) & (0xE5 ^ 0xBC ^ (0xF8 ^ 0xBA) ^ -" ".length())) != 0x0) {
                    return ((69 + 120 - 55 + 0 ^ 104 + 122 - 214 + 172) & (79 + 152 - 163 + 186 ^ 21 + 113 + 52 + 6 ^ -" ".length())) != 0x0;
                }
            }
            else {
                n = Selection.lIIIllIIll[1];
            }
            return n != 0;
        }
        
        public Selection(final BlockPos lIlIIlllIIlIl, final BlockPos lIlIIlllIIlII) {
            this.first = lIlIIlllIIlIl;
            this.second = lIlIIlllIIlII;
        }
        
        public BlockPos getSecond() {
            return this.second;
        }
        
        public void setSecond(final BlockPos lIlIIllIlIlII) {
            this.second = lIlIIllIlIlII;
        }
        
        private static void llIIIIIlllI() {
            (lIIIllIIll = new int[2])[0] = " ".length();
            Selection.lIIIllIIll[1] = ((67 + 97 - 99 + 105 ^ 35 + 184 - 120 + 91) & (0x5D ^ 0x10 ^ (0xDC ^ 0x85) ^ -" ".length()));
        }
        
        public BlockPos getFurthest(final int lIlIIlIIlIIll, final int lIlIIlIIlIIlI, final int lIlIIlIIIllIl) {
            if (llIIIIlIIIl(lIlIIlIIlIIll)) {
                if (llIIIIlIIlI(this.first.func_177958_n(), this.second.func_177958_n())) {
                    return this.first;
                }
                return this.second;
            }
            else if (llIIIIlIIll(lIlIIlIIlIIll)) {
                if (llIIIIlIlII(this.first.func_177958_n(), this.second.func_177958_n())) {
                    return this.first;
                }
                return this.second;
            }
            else if (llIIIIlIIIl(lIlIIlIIlIIlI)) {
                if (llIIIIlIIlI(this.first.func_177958_n(), this.second.func_177958_n())) {
                    return this.first;
                }
                return this.second;
            }
            else if (llIIIIlIIll(lIlIIlIIlIIlI)) {
                if (llIIIIlIlII(this.first.func_177956_o(), this.second.func_177956_o())) {
                    return this.first;
                }
                return this.second;
            }
            else if (llIIIIlIIIl(lIlIIlIIIllIl)) {
                if (llIIIIlIIlI(this.first.func_177952_p(), this.second.func_177952_p())) {
                    return this.first;
                }
                return this.second;
            }
            else {
                if (!llIIIIlIIll(lIlIIlIIIllIl)) {
                    return null;
                }
                if (llIIIIlIlII(this.first.func_177952_p(), this.second.func_177952_p())) {
                    return this.first;
                }
                return this.second;
            }
        }
        
        public BlockPos getMinimum() {
            final int lIlIIlIlIlIll = Math.min(this.first.func_177958_n(), this.second.func_177958_n());
            final int lIlIIlIlIlIlI = Math.min(this.first.func_177956_o(), this.second.func_177956_o());
            final int lIlIIlIlIlIIl = Math.min(this.first.func_177952_p(), this.second.func_177952_p());
            return new BlockPos(lIlIIlIlIlIll, lIlIIlIlIlIlI, lIlIIlIlIlIIl);
        }
        
        public void expand(final int lIlIIIlllIlll, final EnumFacing lIlIIIlllIllI) {
            BlockPos lIlIIIllllIIl = this.second;
            switch (Carpenter$1.$SwitchMap$net$minecraft$util$EnumFacing[lIlIIIlllIllI.ordinal()]) {
                case 1: {
                    BlockPos blockPos;
                    if (llIIIIlIlII(this.second.func_177956_o(), this.first.func_177956_o())) {
                        blockPos = (this.second = this.second.func_177982_a(Selection.lIIIllIIll[1], -lIlIIIlllIlll, Selection.lIIIllIIll[1]));
                        "".length();
                        if (-"   ".length() >= 0) {
                            return;
                        }
                    }
                    else {
                        blockPos = (this.first = this.first.func_177982_a(Selection.lIIIllIIll[1], -lIlIIIlllIlll, Selection.lIIIllIIll[1]));
                    }
                    lIlIIIllllIIl = blockPos;
                    "".length();
                    if (null != null) {
                        return;
                    }
                    break;
                }
                case 2: {
                    BlockPos blockPos2;
                    if (llIIIIlIIlI(this.second.func_177956_o(), this.first.func_177956_o())) {
                        blockPos2 = (this.second = this.second.func_177982_a(Selection.lIIIllIIll[1], lIlIIIlllIlll, Selection.lIIIllIIll[1]));
                        "".length();
                        if (-" ".length() >= "  ".length()) {
                            return;
                        }
                    }
                    else {
                        blockPos2 = (this.first = this.first.func_177982_a(Selection.lIIIllIIll[1], lIlIIIlllIlll, Selection.lIIIllIIll[1]));
                    }
                    lIlIIIllllIIl = blockPos2;
                    "".length();
                    if (((0xF5 ^ 0xC2) & ~(0x6A ^ 0x5D)) != 0x0) {
                        return;
                    }
                    break;
                }
                case 3: {
                    BlockPos blockPos3;
                    if (llIIIIlIlII(this.second.func_177952_p(), this.first.func_177952_p())) {
                        blockPos3 = (this.second = this.second.func_177982_a(Selection.lIIIllIIll[1], Selection.lIIIllIIll[1], -lIlIIIlllIlll));
                        "".length();
                        if (null != null) {
                            return;
                        }
                    }
                    else {
                        blockPos3 = (this.first = this.first.func_177982_a(Selection.lIIIllIIll[1], Selection.lIIIllIIll[1], -lIlIIIlllIlll));
                    }
                    lIlIIIllllIIl = blockPos3;
                    "".length();
                    if ((0x13 ^ 0x17) != (0x3B ^ 0x3F)) {
                        return;
                    }
                    break;
                }
                case 4: {
                    BlockPos blockPos4;
                    if (llIIIIlIIlI(this.second.func_177952_p(), this.first.func_177952_p())) {
                        blockPos4 = (this.second = this.second.func_177982_a(Selection.lIIIllIIll[1], Selection.lIIIllIIll[1], lIlIIIlllIlll));
                        "".length();
                        if (((0xE7 ^ 0xA5) & ~(0x3E ^ 0x7C)) != 0x0) {
                            return;
                        }
                    }
                    else {
                        blockPos4 = (this.first = this.first.func_177982_a(Selection.lIIIllIIll[1], Selection.lIIIllIIll[1], lIlIIIlllIlll));
                    }
                    lIlIIIllllIIl = blockPos4;
                    "".length();
                    if (((0xE9 ^ 0xA8) & ~(0xFE ^ 0xBF)) != ((0xE5 ^ 0xB1) & ~(0x8 ^ 0x5C))) {
                        return;
                    }
                    break;
                }
                case 5: {
                    BlockPos blockPos5;
                    if (llIIIIlIlII(this.second.func_177958_n(), this.first.func_177958_n())) {
                        blockPos5 = (this.second = this.second.func_177982_a(-lIlIIIlllIlll, Selection.lIIIllIIll[1], Selection.lIIIllIIll[1]));
                        "".length();
                        if (null != null) {
                            return;
                        }
                    }
                    else {
                        blockPos5 = (this.first = this.first.func_177982_a(-lIlIIIlllIlll, Selection.lIIIllIIll[1], Selection.lIIIllIIll[1]));
                    }
                    lIlIIIllllIIl = blockPos5;
                    "".length();
                    if ((61 + 159 - 181 + 152 ^ 135 + 144 - 120 + 28) < "   ".length()) {
                        return;
                    }
                    break;
                }
                case 6: {
                    BlockPos blockPos6;
                    if (llIIIIlIIlI(this.second.func_177958_n(), this.first.func_177958_n())) {
                        blockPos6 = (this.second = this.second.func_177982_a(lIlIIIlllIlll, Selection.lIIIllIIll[1], Selection.lIIIllIIll[1]));
                        "".length();
                        if (null != null) {
                            return;
                        }
                    }
                    else {
                        blockPos6 = (this.first = this.first.func_177982_a(lIlIIIlllIlll, Selection.lIIIllIIll[1], Selection.lIIIllIIll[1]));
                    }
                    lIlIIIllllIIl = blockPos6;
                    break;
                }
            }
        }
        
        public void setFirst(final BlockPos lIlIIllIllIlI) {
            this.first = lIlIIllIllIlI;
        }
        
        private static boolean llIIIIlIIlI(final int lIlIIIllIlllI, final int lIlIIIllIllIl) {
            return lIlIIIllIlllI > lIlIIIllIllIl;
        }
        
        public int getHeight() {
            final int lIlIIlIlllIII = Math.min(this.first.func_177956_o(), this.second.func_177956_o()) + Selection.lIIIllIIll[0];
            final int lIlIIlIllIlll = Math.max(this.first.func_177956_o(), this.second.func_177956_o());
            return Math.abs(lIlIIlIlllIII - lIlIIlIllIlll);
        }
        
        private static boolean llIIIIIllll(final Object lIlIIIllIlIll) {
            return lIlIIIllIlIll != null;
        }
        
        private static boolean llIIIIlIIIl(final int lIlIIIllIIlIl) {
            return lIlIIIllIIlIl > 0;
        }
        
        public int getLength() {
            final int lIlIIllIIIIIl = Math.min(this.first.func_177952_p(), this.second.func_177952_p());
            final int lIlIIllIIIIII = Math.max(this.first.func_177952_p(), this.second.func_177952_p()) + Selection.lIIIllIIll[0];
            return Math.abs(lIlIIllIIIIIl - lIlIIllIIIIII);
        }
        
        private static boolean llIIIIlIIII(final Object lIlIIIllIlIIl) {
            return lIlIIIllIlIIl == null;
        }
        
        public BlockPos getFirst() {
            return this.first;
        }
        
        private static boolean llIIIIlIIll(final int lIlIIIllIIlll) {
            return lIlIIIllIIlll < 0;
        }
    }
    
    public static class ShapeBuilder
    {
        private static BlockPos from(final double llllllllllllllllIllIlllIIllIllll, final double llllllllllllllllIllIlllIIllIlllI, final double llllllllllllllllIllIlllIIllIlIlI) {
            return new BlockPos(Math.floor(llllllllllllllllIllIlllIIllIllll), Math.floor(llllllllllllllllIllIlllIIllIlllI), Math.floor(llllllllllllllllIllIlllIIllIlIlI));
        }
        
        public static Shape oval(final BlockPos llllllllllllllllIllIlllIIllIlIIl, final double llllllllllllllllIllIlllIIllIlIII, final double llllllllllllllllIllIlllIIllIIlll) {
            return null;
        }
    }
    
    public class Shape
    {
        private final /* synthetic */ int colour;
        private static final /* synthetic */ int[] llIIlIlIlll;
        final /* synthetic */ BlockPos[] blocks;
        
        public BlockPos[] getBlocks() {
            return this.blocks;
        }
        
        static {
            lIIIIlIllllIl();
        }
        
        private static void lIIIIlIllllIl() {
            (llIIlIlIlll = new int[1])[0] = ((0x87 ^ 0xB2 ^ (0x15 ^ 0x9)) & (0x7 ^ 0x21 ^ (0x7E ^ 0x71) ^ -" ".length()));
        }
        
        public int getColour() {
            return this.colour;
        }
        
        Shape(final List<BlockPos> llllllllllllllllIllIlIIlIlIIIIll) {
            this.blocks = llllllllllllllllIllIlIIlIlIIIIll.toArray(new BlockPos[Shape.llIIlIlIlll[0]]);
            this.colour = ColourUtils.toRGBA(0.5 + Math.random() * 0.5, 0.5 + Math.random() * 0.5, 0.5 + Math.random() * 0.5, 1.0);
        }
    }
}
