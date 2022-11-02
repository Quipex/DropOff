package tfar.quickstack.util;

import net.minecraft.world.item.ItemStack;

public class ItemStackUtils {
    public static boolean isFavorited(ItemStack stack) {
        var stackTag = stack.getTag();
        if (stackTag == null) {
            return false;
        }
        return stack.hasTag() && stackTag.getBoolean("favorite");
    }
}
