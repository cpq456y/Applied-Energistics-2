package appeng.ext.aeadditions.item;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.ext.aeadditions.Constants;
import appeng.ext.aeadditions.models.IItemModelRegister;
import appeng.ext.aeadditions.models.ModelManager;

/** Ported from ItemFluid.kt. */
public class ItemFluid extends Item implements IItemModelRegister {

    @Override
    public ICapabilityProvider initCapabilities(final ItemStack stack, final NBTTagCompound nbt) {
        return new ItemFluidHandlerItemStack(stack, Fluid.BUCKET_VOLUME);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModel(final Item item, final ModelManager manager) {
        manager.registerItemModel(item, new ItemMeshDefinition() {
            @Override
            public ModelResourceLocation getModelLocation(final ItemStack stack) {
                final FluidStack fluidStack = getFluid(stack);
                final String fluidName = fluidStack == null ? "" : fluidStack.getFluid().getName();
                return new ModelResourceLocation(Constants.MOD_ID + ":fluid/" + fluidName, "inventory");
            }
        });
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> subItems) {
        if (!this.isInCreativeTab(tab)) {
            return;
        }
        for (final Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
            final ItemStack itemStack = new ItemStack(this);
            setFluid(itemStack, new FluidStack(fluid, 1));
            subItems.add(itemStack);
        }
    }

    @Override
    public String getItemStackDisplayName(final ItemStack stack) {
        final FluidStack fluidStack = getFluid(stack);
        if (fluidStack == null) {
            return "null";
        }
        final Fluid fluid = fluidStack.getFluid();
        return fluid != null ? fluid.getLocalizedName(new FluidStack(fluid, Fluid.BUCKET_VOLUME)) : "null";
    }

    public static void setFluid(final ItemStack itemStack, final FluidStack fluidStack) {
        final NBTTagCompound nbtTagCompound = new NBTTagCompound();
        fluidStack.writeToNBT(nbtTagCompound);

        if (itemStack.getTagCompound() == null) {
            itemStack.setTagCompound(new NBTTagCompound());
        }

        itemStack.getTagCompound().setTag(FluidHandlerItemStack.FLUID_NBT_KEY, nbtTagCompound);
    }

    public static FluidStack getFluid(final ItemStack itemStack) {
        if (!itemStack.hasTagCompound()) {
            return null;
        }
        final NBTTagCompound tagCompound = itemStack.getTagCompound();
        return FluidStack.loadFluidStackFromNBT(tagCompound.getCompoundTag(FluidHandlerItemStack.FLUID_NBT_KEY));
    }

    public static class ItemFluidHandlerItemStack extends FluidHandlerItemStack {

        public ItemFluidHandlerItemStack(final ItemStack stack, final int capacity) {
            super(stack, capacity);
        }

        @Override
        public FluidStack drain(final int maxDrain, final boolean doDrain) {
            if (maxDrain <= 0) {
                return null;
            }

            final FluidStack contained = this.getFluid();
            if (contained == null || contained.amount <= 0 || !this.canDrainFluidType(contained)) {
                return null;
            }

            final int drainAmount = Math.min(contained.amount, maxDrain);

            final FluidStack drained = contained.copy();
            drained.amount = drainAmount;

            if (doDrain) {
                contained.amount -= drainAmount;
                if (contained.amount == 0) {
                    this.setContainerToEmpty();
                } else {
                    this.setFluid(contained);
                }
            }

            return drained;
        }
    }
}
