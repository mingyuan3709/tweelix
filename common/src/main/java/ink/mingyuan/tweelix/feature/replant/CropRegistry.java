package ink.mingyuan.tweelix.feature.replant;

import ink.mingyuan.tweelix.config.subconfig.AutoReplantSub;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;

public class CropRegistry {
    private static final Map<Block, Item> SEED_MAP = new HashMap<>();

    // 原版映射
    private static void addVanillaMappings() {
        // 农作物
        put(Blocks.WHEAT, Items.WHEAT_SEEDS);
        put(Blocks.BEETROOTS, Items.BEETROOT_SEEDS);
        put(Blocks.CARROTS, Items.CARROT);
        put(Blocks.POTATOES, Items.POTATO);
        put(Blocks.MELON_STEM, Items.MELON_SEEDS);
        put(Blocks.PUMPKIN_STEM, Items.PUMPKIN_SEEDS);
        put(Blocks.TORCHFLOWER_CROP, Items.TORCHFLOWER_SEEDS);
        put(Blocks.PITCHER_CROP, Items.PITCHER_POD);
        put(Blocks.NETHER_WART, Items.NETHER_WART);
        put(Blocks.COCOA, Items.COCOA_BEANS);

        // 树木
        put(Blocks.OAK_LOG, Items.OAK_SAPLING);
        put(Blocks.SPRUCE_LOG, Items.SPRUCE_SAPLING);
        put(Blocks.BIRCH_LOG, Items.BIRCH_SAPLING);
        put(Blocks.JUNGLE_LOG, Items.JUNGLE_SAPLING);
        put(Blocks.ACACIA_LOG, Items.ACACIA_SAPLING);
        put(Blocks.DARK_OAK_LOG, Items.DARK_OAK_SAPLING);
        put(Blocks.MANGROVE_LOG, Items.MANGROVE_PROPAGULE);
        put(Blocks.CHERRY_LOG, Items.CHERRY_SAPLING);
        put(Blocks.CRIMSON_STEM, Items.CRIMSON_FUNGUS);
        put(Blocks.WARPED_STEM, Items.WARPED_FUNGUS);
    }

    static {
        addVanillaMappings();
    }

    public static void init() {
        loadCustomMappings();
    }

    public static void reloadCustomMappings() {
        SEED_MAP.clear();
        addVanillaMappings();
        loadCustomMappings();
    }

    private static void loadCustomMappings() {
        for (String mapping : AutoReplantSub.CUSTOM_CROP_MAPPINGS.getStrings()) {
            String[] parts = mapping.split("=", 2);
            if (parts.length != 2) continue;

            Identifier blockId = Identifier.tryParse(parts[0].trim());
            Identifier seedId = Identifier.tryParse(parts[1].trim());
            if (blockId == null || seedId == null) continue;

            Block block = BuiltInRegistries.BLOCK.get(blockId)
                    .map(Holder.Reference::value)
                    .orElse(null);
            Item seed = BuiltInRegistries.ITEM.get(seedId)
                    .map(Holder.Reference::value)
                    .orElse(null);

            if (block != null && seed != null) {
                put(block, seed);
            }
        }
    }

    private static void put(Block block, Item seed) {
        SEED_MAP.put(block, seed);
    }

    public static Item getSeed(Block block) {
        return SEED_MAP.get(block);
    }

    public static void register(Block block, Item seed) {
        put(block, seed);
    }
}