package tfar.quickstack.networking;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import tfar.quickstack.client.RendererCubeTarget;
import tfar.quickstack.config.DropOffConfig;
import tfar.quickstack.util.ItemStackUtils;
import tfar.quickstack.util.SuccedableInventoryData;

public class C2SPacketRequestDropoff {

    private boolean ignoreHotbar;
    private boolean dump;
    private List<BlockEntityType<?>> teTypes = new ArrayList<>();
    private int minSlotCount;
    private int itemsCounter;

    /**
     * Leave public default constructor for Netty.
     */
    public C2SPacketRequestDropoff() {
    }

    public C2SPacketRequestDropoff(FriendlyByteBuf buf) {
        buf = new PacketBufferExt(buf);
        ignoreHotbar = buf.readBoolean();
        dump = buf.readBoolean();
        teTypes = ((PacketBufferExt) buf).readRegistryIdArray();
        minSlotCount = buf.readInt();
    }

    public C2SPacketRequestDropoff(boolean ignoreHotbar, boolean dump, List<BlockEntityType<?>> teTypes,
            int minSlotCount) {
        this.ignoreHotbar = ignoreHotbar;
        this.dump = dump;
        this.teTypes = teTypes;
        this.minSlotCount = minSlotCount;
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            Set<SuccedableInventoryData> nearbyInventories = getNearbyInventories(player);

            nearbyInventories.forEach(inventoryData -> {
                inventoryData.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER)
                        .ifPresent(
                                target -> {
                                    if (dump) {
                                        dropOff(player, target, inventoryData);
                                    } else {
                                        dropOffExisting(player, target, inventoryData);
                                    }
                                });
            });

            List<RendererCubeTarget> rendererCubeTargets = new ArrayList<>();
            int affectedContainers = 0;
            player.containerMenu.broadcastChanges();

            for (SuccedableInventoryData inventoryData : nearbyInventories) {
                int color;

                if (inventoryData.success) {
                    affectedContainers++;
                    color = 0x00FF00;
                } else {
                    color = 0xFF0000;
                }

                RendererCubeTarget rendererCubeTarget = new RendererCubeTarget(inventoryData.blockEntity.getBlockPos(),
                        color);
                rendererCubeTargets.add(rendererCubeTarget);
            }

            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                    new S2CReportPacket(itemsCounter, affectedContainers, nearbyInventories.size(),
                            rendererCubeTargets));
        });
        ctx.get().setPacketHandled(true);
    }

    public void dropOff(Player player, IItemHandler target, SuccedableInventoryData data) {
        IItemHandler playerstacks = new InvWrapper(player.getInventory());
        for (int i = 0; i < 36; ++i) {
            if (ignoreHotbar && i < 9)
                continue;
            ItemStack playerstack = playerstacks.getStackInSlot(i);

            if (playerstack.isEmpty() || ItemStackUtils.isFavorited(playerstack))
                continue;
            data.setSuccessful();
            itemsCounter += playerstack.getCount();
            ItemStack rem = playerstacks.extractItem(i, Integer.MAX_VALUE, false);
            for (int j = 0; j < target.getSlots(); ++j) {
                rem = target.insertItem(j, rem, false);
                if (rem.isEmpty())
                    break;
            }
            if (!rem.isEmpty()) {
                itemsCounter -= rem.getCount();
                playerstacks.insertItem(i, rem, false);
            }
        }
    }

    public void dropOffExisting(Player player, IItemHandler target, SuccedableInventoryData data) {
        IItemHandler playerstacks = new InvWrapper(player.getInventory());
        for (int i = 0; i < 36; ++i) {
            if (ignoreHotbar && i < 9)
                continue;
            ItemStack playerstack = playerstacks.getStackInSlot(i);
            if (playerstack.isEmpty() || ItemStackUtils.isFavorited(playerstack))
                continue;
            boolean hasExistingStack = IntStream.range(0, target.getSlots()).mapToObj(target::getStackInSlot)
                    .filter(existing -> !existing.isEmpty())
                    .anyMatch(existing -> existing.getItem() == playerstack.getItem());
            if (!hasExistingStack)
                continue;
            data.setSuccessful();
            itemsCounter += playerstack.getCount();
            ItemStack rem = playerstacks.extractItem(i, Integer.MAX_VALUE, false);
            for (int j = 0; j < target.getSlots(); ++j) {
                rem = target.insertItem(j, rem, false);
                if (rem.isEmpty())
                    break;
            }
            if (!rem.isEmpty()) {
                itemsCounter -= rem.getCount();
                playerstacks.insertItem(i, rem, false);
            }
        }
    }

    public Set<SuccedableInventoryData> getNearbyInventories(ServerPlayer player) {
        double playerX = player.position().x;
        double playerY = player.position().y;
        double playerZ = player.position().z;
        int minX = (int) (playerX - DropOffConfig.scanRadius.get());
        int maxX = (int) (playerX + DropOffConfig.scanRadius.get());

        int minY = (int) (playerY - DropOffConfig.scanRadius.get());
        int maxY = (int) (playerY + DropOffConfig.scanRadius.get());

        int minZ = (int) (playerZ - DropOffConfig.scanRadius.get());
        int maxZ = (int) (playerZ + DropOffConfig.scanRadius.get());

        Level world = player.getLevel();
        return BlockPos.betweenClosedStream(minX, minY, minZ, maxX, maxY, maxZ)
        .map(world::getBlockEntity)
        .filter(Objects::nonNull)
                .filter(tileEntity -> tileEntity.getCapability(ForgeCapabilities.ITEM_HANDLER)
                        .filter(iItemHandler -> iItemHandler.getSlots() >= minSlotCount)
                        .isPresent())
                .filter(tileEntity -> !teTypes.contains(tileEntity.getType()))
                .map(SuccedableInventoryData::new)
                .collect(Collectors.toSet());
    }

    public void encode(FriendlyByteBuf buf) {
        buf = new PacketBufferExt(buf);
        buf.writeBoolean(ignoreHotbar);
        buf.writeBoolean(dump);
        ((PacketBufferExt) buf).writeRegistryIdArray(teTypes);
        buf.writeInt(minSlotCount);
    }
}
