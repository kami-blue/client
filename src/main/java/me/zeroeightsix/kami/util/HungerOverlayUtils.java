package me.zeroeightsix.kami.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

public class HungerOverlayUtils {
    protected static final Field foodExhaustion = ReflectionHelper.findField(FoodStats.class, "foodExhaustionLevel", "field_75126_c", "c");

    public static class BasicFoodValues {
        public final int hunger;
        public final float saturationModifier;

        public BasicFoodValues(int hunger, float saturationModifier) {
            this.hunger = hunger;
            this.saturationModifier = saturationModifier;
        }

        public float getSaturationIncrement() {
            return hunger * saturationModifier * 2f;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof BasicFoodValues)) {
                return false;
            }

            BasicFoodValues that = (BasicFoodValues) o;

            return hunger == that.hunger && Float.compare(that.saturationModifier, saturationModifier) == 0;
        }

        @Override
        public int hashCode() {
            int result = hunger;
            result = 31 * result + (saturationModifier != +0.0f ? Float.floatToIntBits(saturationModifier) : 0);

            return result;
        }
    }

    public static boolean isFood(ItemStack itemStack)
    {
        return itemStack.getItem() instanceof ItemFood;
    }

    public static BasicFoodValues getDefaultFoodValues(ItemStack itemStack)
    {
        ItemFood itemFood = (ItemFood) itemStack.getItem();

        int hunger = itemFood.getHealAmount(itemStack);
        float saturationModifier = itemFood.getSaturationModifier(itemStack);

        return new BasicFoodValues(hunger, saturationModifier);
    }

    public static float getMaxExhaustion(EntityPlayer player) {
        return 4.0f;
    }

    public static float getExhaustion(EntityPlayer player) {
        try {
            return foodExhaustion.getFloat(player.getFoodStats());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setExhaustion(EntityPlayer player, float exhaustion) {
        try {
            foodExhaustion.setFloat(player.getFoodStats(), exhaustion);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
