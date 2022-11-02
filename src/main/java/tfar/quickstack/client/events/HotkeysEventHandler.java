package tfar.quickstack.client.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import tfar.quickstack.DropOff;
import tfar.quickstack.client.ClientUtils;

@Mod.EventBusSubscriber(modid = DropOff.MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)
public class HotkeysEventHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            while (HotkeysRegistrar.DEPOSIT_MAPPING.get().consumeClick()) {
                ClientUtils.sendNoSpectator(false);
            }
            while (HotkeysRegistrar.DUMP_MAPPING.get().consumeClick()) {
                ClientUtils.sendNoSpectator(true);
            }
        }
    }
}
