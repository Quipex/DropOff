package tfar.quickstack.client.events;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import tfar.quickstack.DropOff;

@Mod.EventBusSubscriber(modid = DropOff.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class HotkeysRegistrar {
    public static final Lazy<KeyMapping> DUMP_MAPPING = Lazy
            .of(() -> new KeyMapping("dropoff.key.dump", GLFW.GLFW_KEY_X, DropOff.MOD_ID));
    public static final Lazy<KeyMapping> DEPOSIT_MAPPING = Lazy
            .of(() -> new KeyMapping("dropoff.key.deposit", GLFW.GLFW_KEY_C, DropOff.MOD_ID));

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent e) {
        e.register(DUMP_MAPPING.get());
        e.register(DEPOSIT_MAPPING.get());
    }
}
