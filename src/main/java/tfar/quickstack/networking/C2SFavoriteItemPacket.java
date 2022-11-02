package tfar.quickstack.networking;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import tfar.quickstack.util.ItemStackUtils;

import java.util.function.Supplier;

public class C2SFavoriteItemPacket {

    /**
     * Leave public default constructor for Netty.
     */
    public C2SFavoriteItemPacket() {
    }

    public C2SFavoriteItemPacket(FriendlyByteBuf buf) {
        slotId = buf.readInt();
    }

    public C2SFavoriteItemPacket(int slotId) {
        this.slotId = slotId;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(slotId);
    }

    int slotId;

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            Slot slot = player.containerMenu.getSlot(slotId);
            ItemStack stack = slot.getItem();

            CompoundTag stackTag = stack.getTag();
            if (stackTag != null && ItemStackUtils.isFavorited(stack)) {
                // kill empty tags
                stackTag.remove("favorite");
                if (stackTag.isEmpty()) {
                    stack.setTag(null);
                }
            } else {
                stack.getOrCreateTag().putBoolean("favorite", true);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
