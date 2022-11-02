package tfar.quickstack.networking;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import tfar.quickstack.client.RendererCubeTarget;
import tfar.quickstack.task.ReportTask;

public class S2CReportPacket {

    private int itemsCounter;
    private int affectedContainers;
    private int totalContainers;
    private List<RendererCubeTarget> rendererCubeTargets = new ArrayList<>();

    /**
     * Leave public default constructor for Netty.
     */
    public S2CReportPacket() {
    }

    public S2CReportPacket(FriendlyByteBuf buf) {
        itemsCounter = buf.readInt();
        affectedContainers = buf.readInt();
        totalContainers = buf.readInt();

        PacketBufferExt packetBufferExt = new PacketBufferExt(buf);
        rendererCubeTargets = packetBufferExt.readRendererCubeTargets();
    }

    S2CReportPacket(int itemsCounter, int affectedContainers, int totalContainers,
            List<RendererCubeTarget> rendererCubeTargets) {
        this.itemsCounter = itemsCounter;
        this.affectedContainers = affectedContainers;
        this.totalContainers = totalContainers;
        this.rendererCubeTargets = rendererCubeTargets;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(itemsCounter);
        buf.writeInt(affectedContainers);
        buf.writeInt(totalContainers);

        PacketBufferExt packetBufferExt = new PacketBufferExt(buf);
        packetBufferExt.writeRendererCubeTargets(rendererCubeTargets);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ReportTask reportTask = new ReportTask(itemsCounter, affectedContainers,
                totalContainers, rendererCubeTargets);

        reportTask.run();
        ctx.get().setPacketHandled(true);
    }
}
