// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib;

final class Item
{
    int index;
    int type;
    int intVal;
    long longVal;
    String strVal1;
    String strVal2;
    String strVal3;
    int hashCode;
    Item next;
    
    Item() {
    }
    
    Item(final int index) {
        this.index = index;
    }
    
    Item(final int index, final Item i) {
        this.index = index;
        this.type = i.type;
        this.intVal = i.intVal;
        this.longVal = i.longVal;
        this.strVal1 = i.strVal1;
        this.strVal2 = i.strVal2;
        this.strVal3 = i.strVal3;
        this.hashCode = i.hashCode;
    }
    
    void set(final int intVal) {
        this.type = 3;
        this.intVal = intVal;
        this.hashCode = (Integer.MAX_VALUE & this.type + intVal);
    }
    
    void set(final long longVal) {
        this.type = 5;
        this.longVal = longVal;
        this.hashCode = (Integer.MAX_VALUE & this.type + (int)longVal);
    }
    
    void set(final float floatVal) {
        this.type = 4;
        this.intVal = Float.floatToRawIntBits(floatVal);
        this.hashCode = (Integer.MAX_VALUE & this.type + (int)floatVal);
    }
    
    void set(final double doubleVal) {
        this.type = 6;
        this.longVal = Double.doubleToRawLongBits(doubleVal);
        this.hashCode = (Integer.MAX_VALUE & this.type + (int)doubleVal);
    }
    
    void set(final int type, final String strVal1, final String strVal2, final String strVal3) {
        this.type = type;
        this.strVal1 = strVal1;
        this.strVal2 = strVal2;
        this.strVal3 = strVal3;
        switch (type) {
            case 7: {
                this.intVal = 0;
            }
            case 1:
            case 8:
            case 16:
            case 30: {
                this.hashCode = (Integer.MAX_VALUE & type + strVal1.hashCode());
            }
            case 12: {
                this.hashCode = (Integer.MAX_VALUE & type + strVal1.hashCode() * strVal2.hashCode());
            }
            default: {
                this.hashCode = (Integer.MAX_VALUE & type + strVal1.hashCode() * strVal2.hashCode() * strVal3.hashCode());
            }
        }
    }
    
    void set(final String name, final String desc, final int bsmIndex) {
        this.type = 18;
        this.longVal = bsmIndex;
        this.strVal1 = name;
        this.strVal2 = desc;
        this.hashCode = (Integer.MAX_VALUE & 18 + bsmIndex * this.strVal1.hashCode() * this.strVal2.hashCode());
    }
    
    void set(final int position, final int hashCode) {
        this.type = 33;
        this.intVal = position;
        this.hashCode = hashCode;
    }
    
    boolean isEqualTo(final Item i) {
        switch (this.type) {
            case 1:
            case 7:
            case 8:
            case 16:
            case 30: {
                return i.strVal1.equals(this.strVal1);
            }
            case 5:
            case 6:
            case 32: {
                return i.longVal == this.longVal;
            }
            case 3:
            case 4: {
                return i.intVal == this.intVal;
            }
            case 31: {
                return i.intVal == this.intVal && i.strVal1.equals(this.strVal1);
            }
            case 12: {
                return i.strVal1.equals(this.strVal1) && i.strVal2.equals(this.strVal2);
            }
            case 18: {
                return i.longVal == this.longVal && i.strVal1.equals(this.strVal1) && i.strVal2.equals(this.strVal2);
            }
            default: {
                return i.strVal1.equals(this.strVal1) && i.strVal2.equals(this.strVal2) && i.strVal3.equals(this.strVal3);
            }
        }
    }
}
