package appeng.ext.mekeng.common;

import appeng.block.AEBaseItemBlock;
import appeng.block.AEBaseTileBlock;
import appeng.bootstrap.components.IItemRegistrationComponent;
import appeng.core.features.ActivityState;
import appeng.core.features.BlockStackSrc;
import appeng.tile.AEBaseTile;
import appeng.ext.mekeng.MekEng;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegistryHandler {

    protected final List<Pair<String, Block>> blocks = new ArrayList<>();
    protected final List<Pair<String, Item>> items = new ArrayList<>();
    protected final List<Pair<String, Class<? extends TileEntity>>> tiles = new ArrayList<>();

    /** Blocks whose item block is registered through a registration slot instead of behind every AE2 item. */
    protected final Set<String> deferredBlockItemNames = new HashSet<>();

    /**
     * Items registered through a registration slot. They deliberately stay out of {@link #items} (which is
     * what {@link #onRegisterItems} walks), but the client handler still has to see them to register their
     * models.
     */
    protected final List<Pair<String, Item>> slottedItems = new ArrayList<>();

    public void block(String name, Block block) {
        blocks.add(Pair.of(name, block));
    }

    public void item(String name, Item item) {
        items.add(Pair.of(name, item));
    }

    public void block(String name, Block block, Class<? extends TileEntity> tile) {
        blocks.add(Pair.of(name, block));
        tiles.add(Pair.of(name, tile));
    }

    /**
     * Prepares {@code item} for registration and returns the component that registers it. Use this for items
     * that have to end up in the middle of AE2's own item registration order -- see
     * {@code appeng.bootstrap.RegistrationSlots} -- instead of behind every AE2 item. The returned component
     * has to be handed to the matching slot, otherwise the item is never registered.
     */
    public IItemRegistrationComponent deferredItem(String name, Item item) {
        this.slottedItems.add(Pair.of(name, item));
        return (side, registry) -> registry.register(initItem(name, item));
    }

    /**
     * Same as {@link #deferredItem(String, Item)} for the item block of {@code block}; the item block is
     * created here so that {@link #onRegisterItems} does not generate a second one for the same block.
     */
    public IItemRegistrationComponent deferredBlockItem(String name, Block block) {
        this.deferredBlockItemNames.add(name);
        return (side, registry) -> registry.register(initItem(name, new AEBaseItemBlock(block)));
    }

    @SubscribeEvent
    public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        for (Pair<String, Block> entry : blocks) {
            String key = entry.getLeft();
            Block block = entry.getRight();
            block.setRegistryName(appeng.core.AppEng.MOD_ID, key);
            block.setTranslationKey(appeng.core.AppEng.MOD_ID + "." + key);
            block.setCreativeTab(ItemAndBlocks.TAB);
            event.getRegistry().register(block);
        }
    }

    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> event) {
        // TODO some way to handle blocks with custom ItemBlock
        for (Pair<String, Block> entry : blocks) {
            if (deferredBlockItemNames.contains(entry.getLeft())) {
                continue;
            }
            event.getRegistry().register(initItem(entry.getLeft(), new AEBaseItemBlock(entry.getRight())));
        }
        for (Pair<String, Item> entry : items) {
            event.getRegistry().register(initItem(entry.getLeft(), entry.getRight()));
        }
    }

    private static Item initItem(String key, Item item) {
        item.setRegistryName(appeng.core.AppEng.MOD_ID, key);
        item.setTranslationKey(appeng.core.AppEng.MOD_ID + "." + key);
        item.setCreativeTab(ItemAndBlocks.TAB);
        return item;
    }

    public void onInit() {
        for (Pair<String, Class<? extends TileEntity>> entry : tiles) {
            GameRegistry.registerTileEntity(entry.getRight(), MekEng.id(entry.getLeft()));
        }
        for (Pair<String, Block> entry : blocks) {
            Block block = ForgeRegistries.BLOCKS.getValue(MekEng.id(entry.getKey()));
            if (block instanceof AEBaseTileBlock) {
                AEBaseTile.registerTileItem(((AEBaseTileBlock)block).getTileEntityClass(), new BlockStackSrc(block, 0, ActivityState.Enabled));
            }
        }
    }

}
