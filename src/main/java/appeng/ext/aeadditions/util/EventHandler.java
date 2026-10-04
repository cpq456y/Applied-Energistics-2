package appeng.ext.aeadditions.util;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import appeng.ext.aeadditions.registries.BlockEnum;
import appeng.ext.aeadditions.registries.ItemEnum;

/** Ported from EventHandler.kt. */
public final class EventHandler {

    public static final EventHandler INSTANCE = new EventHandler();

    private static final String LEGACY_NAMESPACE = "extracells";

    private EventHandler() {
    }

    @SubscribeEvent
    public void onMappingsMissingEventBlock(final RegistryEvent.MissingMappings<Block> event) {
        final Map<String, BlockEnum> blockMap = new HashMap<>();
        for (final BlockEnum e : BlockEnum.values()) {
            blockMap.put(e.getInternalName(), e);
        }

        for (final RegistryEvent.MissingMappings.Mapping<Block> mapping : event.getAllMappings()) {
            if (!LEGACY_NAMESPACE.equals(mapping.key.getNamespace())) {
                continue;
            }
            final BlockEnum remap = blockMap.get(mapping.key.getPath());
            if (remap != null) {
                mapping.remap(remap.getBlock());
            }
        }
    }

    @SubscribeEvent
    public void onMappingsMissingEventItem(final RegistryEvent.MissingMappings<Item> event) {
        final Map<String, ItemEnum> itemMap = new HashMap<>();
        for (final ItemEnum e : ItemEnum.values()) {
            itemMap.put(e.getInternalName(), e);
        }

        final Map<String, BlockEnum> itemBlockMap = new HashMap<>();
        for (final BlockEnum e : BlockEnum.values()) {
            itemBlockMap.put(e.getInternalName(), e);
        }

        for (final RegistryEvent.MissingMappings.Mapping<Item> mapping : event.getAllMappings()) {
            if (!LEGACY_NAMESPACE.equals(mapping.key.getNamespace())) {
                continue;
            }
            final ItemEnum item = itemMap.get(mapping.key.getPath());
            if (item != null) {
                mapping.remap(item.getItem());
            }
            final BlockEnum block = itemBlockMap.get(mapping.key.getPath());
            if (block != null) {
                mapping.remap(block.getItem());
            }
        }
    }
}
