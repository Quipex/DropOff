package tfar.quickstack.config;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import tfar.quickstack.DropOff;

public class DropOffConfig {

    final String categoryGeneral = "general";

    public static ForgeConfigSpec.IntValue scanRadius;

    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    public static final DropOffConfig SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
        final Pair<DropOffConfig, ForgeConfigSpec> specPair2 = new ForgeConfigSpec.Builder()
                .configure(DropOffConfig::new);
        SERVER_SPEC = specPair2.getRight();
        SERVER = specPair2.getLeft();
    }

    public DropOffConfig(ForgeConfigSpec.Builder builder) {
        builder.push(categoryGeneral);

        scanRadius = builder.comment("Radius in blocks to check containers around the player.")
                .defineInRange("Scan radius", DefaultValues.scanRadius, 0, Integer.MAX_VALUE);

        builder.pop();
    }

    private static class DefaultValues {

        private static final boolean ignoreHotbar = true;
        private static final boolean displayMessage = true;
        private static final boolean highlightContainers = true;
        private static final boolean showInventoryButton = true;

        private static final int minSlots = 6;
        private static final int creativeInventoryButtonXOffset = 71;
        private static final int creativeInventoryButtonYOffset = -63;
        private static final int highlightDelay = 3000;
        private static final int scanRadius = 6;
        private static final int survivalInventoryButtonXOffset = 50;
        private static final int survivalInventoryButtonYOffset = -18;
        private static final List<String> blacklist = Lists.newArrayList("minecraft:furnace", "minecraft:blast_furnace",
                "minecraft:smoker");

        private static final List<String> container_whitelist = Lists.newArrayList("curios:curios_container");

    }

    public static class Client {

        public static ForgeConfigSpec.BooleanValue highlightContainers;
        public static ForgeConfigSpec.BooleanValue showInventoryButton;
        public static ForgeConfigSpec.BooleanValue displayMessage;
        public static ForgeConfigSpec.BooleanValue ignoreHotBar;
        public static ForgeConfigSpec.BooleanValue enableDump;

        public static ForgeConfigSpec.IntValue creativeInventoryButtonXOffset;
        public static ForgeConfigSpec.IntValue creativeInventoryButtonYOffset;
        public static ForgeConfigSpec.IntValue minSlotCount;

        public static ForgeConfigSpec.IntValue survivalInventoryButtonXOffset;
        public static ForgeConfigSpec.IntValue survivalInventoryButtonYOffset;

        public static ForgeConfigSpec.IntValue highlightDelay;

        private static ForgeConfigSpec.ConfigValue<List<String>> blacklistedTes;

        public static ForgeConfigSpec.ConfigValue<List<String>> whitelistedContainers;

        public Client(ForgeConfigSpec.Builder builder) {
            builder.push("general");
            // booleans
            enableDump = builder.comment("Enable dump button.").define("Enable Dump Button", true);
            ignoreHotBar = builder.comment("Ignore hotbar when transferring.").define("Ignore Hotbar", true);
            highlightContainers = builder.comment("Highlight nearby containers.").define("Highlight containers",
                    DefaultValues.highlightContainers);
            displayMessage = builder.comment(" information to the chat when task is complete.")
                    .define("Display Message", DefaultValues.displayMessage);
            showInventoryButton = builder.comment("Show button in the player inventory.")
                    .define("Show inventory button", DefaultValues.showInventoryButton);

            // Integers
            creativeInventoryButtonXOffset = builder.comment("Creative inventory button position width offset.")
                    .defineInRange("Creative inventory button X offset",
                            DefaultValues.creativeInventoryButtonXOffset, Integer.MIN_VALUE, Integer.MAX_VALUE);

            creativeInventoryButtonYOffset = builder.comment("Creative inventory button position height offset.")
                    .defineInRange("Creative inventory button Y offset",
                            DefaultValues.creativeInventoryButtonYOffset, Integer.MIN_VALUE, Integer.MAX_VALUE);

            highlightDelay = builder.comment("Blocks highlighting delay in milliseconds. Delay < 0 means forever.")
                    .defineInRange("Highlight delay", DefaultValues.highlightDelay, -1, Integer.MAX_VALUE);
            minSlotCount = builder.comment("Min number of slots that a " +
                    "container can be eligible for transfer to, this will exclude furnaces and most machines with a low slot count.")
                    .defineInRange("Minimum Slots", DefaultValues.minSlots, 0, Integer.MAX_VALUE);

            survivalInventoryButtonXOffset = builder.comment("Survival inventory button position width offset.")
                    .defineInRange("Survival inventory button X offset",
                            DefaultValues.survivalInventoryButtonXOffset, Integer.MIN_VALUE, Integer.MAX_VALUE);

            survivalInventoryButtonYOffset = builder.comment("Survival inventory button position height offset.")
                    .defineInRange("Survival inventory button Y offset",
                            DefaultValues.survivalInventoryButtonYOffset, Integer.MIN_VALUE, Integer.MAX_VALUE);

            blacklistedTes = builder.define("Blacklisted Block Entities", DefaultValues.blacklist);

            whitelistedContainers = builder.define("Whitelisted containers", DefaultValues.container_whitelist);

        }
    }

    public static List<BlockEntityType<?>> blockEntityBlacklist;

    public static void onConfigChanged(ModConfigEvent event) {
        if (!event.getConfig().getModId().equals(DropOff.MOD_ID)) {
            return;
        }

        blockEntityBlacklist = Client.blacklistedTes.get()
                .stream()
                .map(ResourceLocation::new).filter(resourceLocation -> {
                    boolean b = ForgeRegistries.BLOCK_ENTITY_TYPES.containsKey(resourceLocation);
                    if (!b) {
                        DropOff.LOGGER.warn("Ignoring unknown blockentity: " + resourceLocation);
                    }
                    return b;
                })
                .map(((ForgeRegistry<BlockEntityType<?>>) ForgeRegistries.BLOCK_ENTITY_TYPES)::getValue)
                .collect(Collectors.toList());

        DropOff.LOGGER.info("Configuration changed.");
    }
}