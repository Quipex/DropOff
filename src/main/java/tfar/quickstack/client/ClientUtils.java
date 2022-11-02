package tfar.quickstack.client;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import tfar.quickstack.config.DropOffConfig;
import tfar.quickstack.networking.C2SPacketRequestDropoff;
import tfar.quickstack.networking.PacketHandler;

public class ClientUtils {

    public static void printToChat(String message) {
        message = "[" + ChatFormatting.BLUE + "DropOff" + ChatFormatting.RESET + "]: " + message;

        LocalPlayer player = Minecraft.getInstance().player;
        var textComponentString = Component.literal(message);

        player.sendSystemMessage(textComponentString);
    }

    public static void playSound(SoundEvent soundEvent) {
        SoundManager soundHandler = Minecraft.getInstance().getSoundManager();
        SimpleSoundInstance record = SimpleSoundInstance.forUI(soundEvent, 1.0f);

        soundHandler.play(record);
    }

    public static void sendNoSpectator(boolean dump) {
        if (Minecraft.getInstance().player.isSpectator()) {
            printToChat("Action not allowed in spectator mode.");
        } else {
            C2SPacketRequestDropoff dropoffMessage = new C2SPacketRequestDropoff(
                    DropOffConfig.Client.ignoreHotBar.get(),
                    dump,
                    DropOffConfig.blockEntityBlacklist,
                    DropOffConfig.Client.minSlotCount.get());
            PacketHandler.INSTANCE.sendToServer(dropoffMessage);
        }
    }

    public static void renderBlocks(RenderLevelStageEvent e, List<RendererCubeTarget> rendererCubeTargets) {
        if (e.getStage() != Stage.AFTER_SOLID_BLOCKS) {
            return;
        }
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

        PoseStack stack = e.getPoseStack();

        stack.pushPose();

        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        stack.translate(-cam.x, -cam.y, -cam.z);

        rendererCubeTargets.forEach(rendererCubeTarget -> {
            VertexConsumer builder = buffer.getBuffer(RenderType.LINES);
            AABB bb = Shapes.block().bounds().move(rendererCubeTarget.getBlockPos().getX(),
                    rendererCubeTarget.getBlockPos().getY(), rendererCubeTarget.getBlockPos().getZ());
            float red = (rendererCubeTarget.getColor() >> 16 & 0xff) / 255f;
            float green = (rendererCubeTarget.getColor() >> 8 & 0xff) / 255f;
            float blue = (rendererCubeTarget.getColor() & 0xff) / 255f;

            LevelRenderer.renderLineBox(stack, builder, bb, red, green, blue, 1);
            buffer.endBatch(RenderType.LINES);
        });

        stack.popPose();
    }
}
