package tfar.quickstack.networking;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import tfar.quickstack.DropOff;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(DropOff.MOD_ID, DropOff.MOD_ID),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void registerMessages() {
        int id = 0;
        INSTANCE.registerMessage(id++, C2SPacketRequestDropoff.class,
                C2SPacketRequestDropoff::encode,
                C2SPacketRequestDropoff::new,
                C2SPacketRequestDropoff::handle);

        INSTANCE.registerMessage(id++, S2CReportPacket.class,
                S2CReportPacket::encode,
                S2CReportPacket::new,
                S2CReportPacket::handle);

        INSTANCE.registerMessage(id, C2SFavoriteItemPacket.class,
                C2SFavoriteItemPacket::encode,
                C2SFavoriteItemPacket::new,
                C2SFavoriteItemPacket::handle);
    }
}
