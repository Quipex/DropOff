package tfar.quickstack.networking;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ForgeRegistries;
import tfar.quickstack.client.RendererCubeTarget;

public class PacketBufferExt extends FriendlyByteBuf {

    public PacketBufferExt(FriendlyByteBuf buf) {
        super(buf);
    }

    public <T extends BlockEntityType<?>> void writeRegistryIdArray(@Nonnull List<T> entries) {
        writeInt(entries.size());
        entries.forEach(i -> writeRegistryId(ForgeRegistries.BLOCK_ENTITY_TYPES, i));
    }

    public <T extends BlockEntityType<?>> List<T> readRegistryIdArray() {
        int size = readInt();
        return IntStream.range(0, size).<T>mapToObj(value -> readRegistryId()).collect(Collectors.toList());
    }

    public void writeRendererCubeTargets(List<RendererCubeTarget> rendererCubeTargets) {
        writeInt(rendererCubeTargets.size());
        rendererCubeTargets.forEach(this::writeRendererCubeTarget);
    }

    public List<RendererCubeTarget> readRendererCubeTargets() {
        int targetsLen = readInt();
        return IntStream.range(0, targetsLen).mapToObj(i -> readRendererCubeTarget()).collect(Collectors.toList());
    }

    private void writeRendererCubeTarget(RendererCubeTarget rendererCubeTarget) {
        writeLong(rendererCubeTarget.getBlockPos().asLong());
        writeInt(rendererCubeTarget.getColor());
    }

    private RendererCubeTarget readRendererCubeTarget() {
        BlockPos blockPos = BlockPos.of(readLong());
        int color = readInt();
        return new RendererCubeTarget(blockPos, color);
    }
}
