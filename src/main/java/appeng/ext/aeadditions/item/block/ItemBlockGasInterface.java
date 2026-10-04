package appeng.ext.aeadditions.item.block;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

/** Ported from ItemBlockGasInterface.kt. */
public class ItemBlockGasInterface extends ItemBlock {

    public ItemBlockGasInterface(final Block block) {
        super(block);
        this.setMaxDamage(0);
    }
}
