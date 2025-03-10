package tfar.quickstack.client.events;

import static net.minecraft.client.gui.GuiComponent.fill;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;
import tfar.quickstack.DropOff;
import tfar.quickstack.client.ClientUtils;
import tfar.quickstack.client.gui.DropOffGuiButton;
import tfar.quickstack.config.DropOffConfig;
import tfar.quickstack.networking.C2SFavoriteItemPacket;
import tfar.quickstack.networking.PacketHandler;
import tfar.quickstack.util.ItemStackUtils;

@Mod.EventBusSubscriber(modid = DropOff.MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)
public class GuiEventHandler {

	@SubscribeEvent
	public static void onGuiOpen(ScreenEvent.Init.Post event) {
		if (!canDisplay(event.getScreen()) || !DropOffConfig.Client.showInventoryButton.get()) {
			return;
		}

		AbstractContainerScreen containerScreen = (AbstractContainerScreen) event.getScreen();

		boolean isCreative = Minecraft.getInstance().player.getAbilities().instabuild;

		int xPos = containerScreen.getGuiLeft() + 80 +
				(isCreative ? DropOffConfig.Client.creativeInventoryButtonXOffset.get()
						: DropOffConfig.Client.survivalInventoryButtonXOffset.get());
		int yPos = containerScreen.getGuiTop() + 80
				+ (isCreative ? DropOffConfig.Client.creativeInventoryButtonYOffset.get()
						: DropOffConfig.Client.survivalInventoryButtonYOffset.get());
		if (DropOffConfig.Client.enableDump.get()) {
			Button dump = new DropOffGuiButton(xPos, yPos, GuiEventHandler::actionPerformed, true);
			event.addListener(dump);
		}
		Button deposit = new DropOffGuiButton(xPos + 12, yPos, GuiEventHandler::actionPerformed, false);
		event.addListener(deposit);
	}

	private static void actionPerformed(@Nonnull Button button) {
		ClientUtils.sendNoSpectator(((DropOffGuiButton) button).dump);
	}

	@SubscribeEvent
	public static <T extends AbstractContainerMenu> void onItemClick(ScreenEvent.MouseButtonPressed.Pre event) {
		if (!canDisplay(event.getScreen()) || !(event.getScreen() instanceof InventoryScreen containerScreen)
				|| !Screen.hasControlDown())
			return;

        Slot slotUnderMouse = containerScreen.getSlotUnderMouse();
        if (slotUnderMouse != null && slotUnderMouse.hasItem()) {
			event.setCanceled(true);
			PacketHandler.INSTANCE.sendToServer(new C2SFavoriteItemPacket(containerScreen.getSlotUnderMouse().index));
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	@SuppressWarnings("unchecked")
	public static <T extends AbstractContainerMenu> void drawFavorites(ContainerScreenEvent.Render.Background event) {
		AbstractContainerScreen<T> containerScreen = (AbstractContainerScreen<T>) event.getContainerScreen();
		if (!canDisplay(containerScreen))
			return;
		T playerContainer = containerScreen.getMenu();
		PoseStack matrices = event.getPoseStack();

		for (int k = 0; k < 3; ++k) {
			for (int j = 0; j < 9; ++j) {
				Slot slot = playerContainer.slots.get(j + (k + 1) * 9);
				ItemStack stack = slot.getItem();
				if (ItemStackUtils.isFavorited(stack)) {
					int xoffset = 8;
					int yoffset = 84;
					fill(matrices, containerScreen.getGuiLeft() + j * 18 + xoffset,
							containerScreen.getGuiTop() + k * 18 + yoffset,
							containerScreen.getGuiLeft() + j * 18 + 16 + xoffset,
							containerScreen.getGuiTop() + k * 18 + 16 + yoffset,
							0xFFFFBB00);
				}
			}
		}
		List<ItemStack> stacks = playerContainer.getItems();

		for (int i = 0; i < 9; ++i) {
			ItemStack stack = stacks.get(i + 36);
			if (ItemStackUtils.isFavorited(stack)) {
				int xoffset = 8;
				int yoffset = 142;
				fill(matrices, containerScreen.getGuiLeft() + i * 18 + xoffset,
						containerScreen.getGuiTop() + yoffset,
						containerScreen.getGuiLeft() + i * 18 + 16 + xoffset,
						containerScreen.getGuiTop() + 16 + yoffset,
						0xFFFFBB00);
			}
		}

		RenderSystem.clearColor(1, 1, 1, 1);
	}

	public static boolean canDisplay(Screen screen) {
		return screen instanceof AbstractContainerScreen && canDisplay((AbstractContainerScreen) screen);
	}

	private static final Set<Class<?>> bad_classes = new HashSet<>();

	public static <T extends AbstractContainerMenu> boolean canDisplay(AbstractContainerScreen<T> screen) {
		if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen)
			return true;
		if (screen instanceof HorseInventoryScreen)
			return false;
		try {
			var screenMenuRegistry = ForgeRegistries.MENU_TYPES.getKey(screen.getMenu().getType()).toString();
			return DropOffConfig.Client.whitelistedContainers.get().contains(screenMenuRegistry);
		} catch (Exception e) {
			Class<?> clazz = screen.getMenu().getClass();
			if (!bad_classes.contains(clazz)) {
				DropOff.LOGGER.error(clazz + " does not have a container type registered to it! " +
						"This is a bug in the other mod and should be reported to them.  The buttons will not display in this gui!");
				bad_classes.add(clazz);
			}
			return false;
		}
	}

	@SubscribeEvent
	public static void tooltip(ItemTooltipEvent e) {
		ItemStack stack = e.getItemStack();
		if (stack.hasTag() && stack.getTag().getBoolean("favorite")) {
			e.getToolTip().add(Component.literal("Favorited!"));
		}
	}
}
