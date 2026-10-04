package appeng.ext.aeadditions.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.ext.aeadditions.integration.opencomputers.UpgradeItemAEBase;
import appeng.ext.aeadditions.models.ModelManager;

/** Ported from ItemOCUpgrade.kt (Kotlin {@code object}, kept as a singleton). */
public final class ItemOCUpgrade extends UpgradeItemAEBase {

    public static final ItemOCUpgrade INSTANCE = new ItemOCUpgrade();

    private ItemOCUpgrade() {
        this.setHasSubtypes(true);
    }

    @Override
    public String getTranslationKey() {
        return super.getTranslationKey().replace("item.appeng.ext.aeadditions", "appeng.ext.aeadditions.item");
    }

    @Override
    public String getTranslationKey(final ItemStack stack) {
        return this.getTranslationKey();
    }

    @Override
    public String getItemStackDisplayName(final ItemStack stack) {
        final int tier = 3 - stack.getItemDamage();

        return super.getItemStackDisplayName(stack) + " (Tier " + tier + ")";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) {
            return;
        }

        items.add(new ItemStack(this, 1, 2));
        items.add(new ItemStack(this, 1, 1));
        items.add(new ItemStack(this, 1, 0));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModel(final Item item, final ModelManager manager) {
        for (int i = 0; i < 3; i++) {
            if (manager != null) {
                manager.registerItemModel(item, i);
            }
        }
    }
}
