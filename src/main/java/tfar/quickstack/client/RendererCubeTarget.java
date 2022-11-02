package tfar.quickstack.client;

import java.util.Objects;

import net.minecraft.core.BlockPos;

public class RendererCubeTarget {

    private final BlockPos blockPos;
    private final int color;

    public RendererCubeTarget(BlockPos blockPos, int color) {
        this.blockPos = blockPos;
        this.color = color;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public int getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "BlockPos: [" + blockPos.toString() + "] " +
                "Color: [" + color + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            return true;
        }

        if (!(o instanceof RendererCubeTarget)) {
            return false;
        }

        RendererCubeTarget rendererCubeTarget = (RendererCubeTarget) o;

        return Objects.equals(blockPos, rendererCubeTarget.blockPos) &&
                Objects.equals(color, rendererCubeTarget.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockPos, color);
    }

}
