package appeng.ext.aeadditions.tileentity;

import java.util.Optional;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

import appeng.ext.aeadditions.api.AEAApi;
import appeng.ext.aeadditions.registries.BlockEnum;
import appeng.tile.crafting.TileCraftingStorageTile;
import appeng.tile.crafting.TileCraftingTile;

/** Ported from TileEntityCraftingStorage.kt. */
public class TileEntityCraftingStorage extends TileCraftingStorageTile {

    private static final int KILO_SCALAR = 1024;

    @Override
    public ItemStack getItemFromTile(final Object obj) {
        final appeng.ext.aeadditions.api.definitions.IBlockDefinition blocks = AEAApi.instance().blocks();

        final Optional<ItemStack> maybeItem;
        switch (((TileCraftingTile) obj).getStorageBytes() / KILO_SCALAR) {
            case 256:
                maybeItem = blocks.craftingStorage256().maybeStack(1);
                break;
            case 1024:
                maybeItem = blocks.craftingStorage1024().maybeStack(1);
                break;
            case 4096:
                maybeItem = blocks.craftingStorage4096().maybeStack(1);
                break;
            case 16384:
                maybeItem = blocks.craftingStorage16384().maybeStack(1);
                break;
            default:
                maybeItem = Optional.empty();
                break;
        }

        return maybeItem.orElseGet(() -> super.getItemFromTile(obj));
    }

    @Override
    public int getStorageBytes() {
        if (this.world == null || this.notLoaded() || this.isInvalid()) {
            return 0;
        }

        final IBlockState blockState = this.world.getBlockState(this.pos);
        final net.minecraft.block.Block block = blockState.getBlock();

        if (block == BlockEnum.UPGRADEDCRAFTINGSTORAGE256.getBlock()) {
            return 256 * KILO_SCALAR;
        }
        if (block == BlockEnum.UPGRADEDCRAFTINGSTORAGE1024.getBlock()) {
            return 1024 * KILO_SCALAR;
        }
        if (block == BlockEnum.UPGRADEDCRAFTINGSTORAGE4096.getBlock()) {
            return 4096 * KILO_SCALAR;
        }
        if (block == BlockEnum.UPGRADEDCRAFTINGSTORAGE16384.getBlock()) {
            return 16384 * KILO_SCALAR;
        }

        return 0;
    }
}
