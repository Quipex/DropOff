package scp002.mod.dropoff.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import scp002.mod.dropoff.config.DropOffConfig;

public class GuiOpenEventHandler {
    public static final GuiOpenEventHandler INSTANCE = new GuiOpenEventHandler();

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!DropOffConfig.INSTANCE.showInventoryButton ||
                !(event.getGui() instanceof GuiInventory || event.getGui() instanceof GuiContainerCreative)) {
            return;
        }

        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

        if (player.capabilities.isCreativeMode) {
            event.setGui(new DropOffGuiContainerCreative(player));
        } else {
            event.setGui(new DropOffGuiInventory(player));
        }
    }
}
