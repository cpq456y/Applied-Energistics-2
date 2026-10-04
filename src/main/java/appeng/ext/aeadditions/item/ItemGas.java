package appeng.ext.aeadditions.item;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.ext.aeadditions.Constants;
import appeng.ext.aeadditions.integration.Integration;
import appeng.ext.aeadditions.models.IItemModelRegister;
import appeng.ext.aeadditions.models.ModelManager;

/** Ported from ItemGas.kt (Kotlin {@code object}, kept as a singleton). */
public final class ItemGas extends Item implements IItemModelRegister {

    public static final ItemGas INSTANCE = new ItemGas();

    public static final boolean isMekanismGasEnabled = Integration.Mods.MEKANISMGAS.isEnabled();

    private ItemGas() {
    }

    public static void setGasName(final ItemStack itemStack, final String fluidName) {
        if (itemStack != null) {
            itemStack.setTagInfo("gas", new NBTTagString(fluidName));
        }
    }

    public static String getGasName(final ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasTagCompound() || itemStack.getTagCompound() == null) {
            return "";
        }

        return itemStack.getTagCompound().getString("gas");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModel(final Item item, final ModelManager manager) {
        if (manager == null) {
            return;
        }
        manager.registerItemModel(item, new ItemMeshDefinition() {
            @Override
            public ModelResourceLocation getModelLocation(final ItemStack stack) {
                if (isMekanismGasEnabled) {
                    return new ModelResourceLocation(Constants.MOD_ID + ":gas/" + getGasName(stack), "inventory");
                }

                return new ModelResourceLocation(Constants.MOD_ID + ":fluid/water", "inventory");
            }
        });
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) {
            return;
        }

        if (isMekanismGasEnabled) {
            getSubItemsGas(items);
        }
    }

    @SideOnly(Side.CLIENT)
    @Optional.Method(modid = "mekanism")
    private void getSubItemsGas(final NonNullList<ItemStack> subItems) {
        for (final Gas gas : GasRegistry.getRegisteredGasses()) {
            final ItemStack itemStack = new ItemStack(this);
            setGasName(itemStack, gas.getName());
            subItems.add(itemStack);
        }
    }

    @Override
    public String getItemStackDisplayName(final ItemStack stack) {
        if (isMekanismGasEnabled) {
            return getItemStackDisplayNameGas(stack);
        }

        return "null";
    }

    @Optional.Method(modid = "mekanism")
    private String getItemStackDisplayNameGas(final ItemStack stack) {
        final String gasName = getGasName(stack);
        if (gasName.isEmpty()) {
            return "null";
        }

        final Gas gas = GasRegistry.getGas(gasName);

        if (gas != null) {
            return gas.getLocalizedName();
        }

        return "null";
    }
}
