package appeng.ext.wut.recipe;

import appeng.api.AEApi;
import appeng.ext.wut.ItemWirelessUniversalTerminal;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import java.util.ArrayList;
import java.util.List;

/**
 * 合成配方：将任意2个不同的无线终端合成为带对应模式的无线通用终端
 */
public class RecipeCombineWUT extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        return !getCraftingResult(inv).isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        List<ItemStack> terminals = new ArrayList<>();

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            // 只允许无线终端
            if (isWirelessTerminal(stack)) {
                terminals.add(stack);
            } else {
                return ItemStack.EMPTY; // 不允许其他物品
            }
        }

        // 必须恰好2个终端
        if (terminals.size() != 2) {
            return ItemStack.EMPTY;
        }

        byte mode1 = getTerminalMode(terminals.get(0));
        byte mode2 = getTerminalMode(terminals.get(1));

        if (mode1 < 0 || mode2 < 0) {
            return ItemStack.EMPTY;
        }

        // 两个终端不能是同一种类型
        if (mode1 == mode2) {
            return ItemStack.EMPTY;
        }

        // 创建带两个模式的WUT
        ItemStack output = new ItemStack(AEApi.instance().definitions().items().wirelessUniversalTerminal().maybeItem().get());
        net.minecraft.nbt.NBTTagCompound tag = appeng.util.Platform.openNbtData(output);
        tag.setIntArray("modes", new int[]{ mode1, mode2 });
        tag.setByte("mode", mode1);
        return output;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= 2 && height >= 1;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        return ForgeHooks.defaultRecipeGetRemainingItems(inv);
    }

    private boolean isWirelessTerminal(ItemStack stack) {
        return AEApi.instance().definitions().items().wirelessTerminal().isSameAs(stack) ||
               AEApi.instance().definitions().items().wirelessCraftingTerminal().isSameAs(stack) ||
               AEApi.instance().definitions().items().wirelessFluidTerminal().isSameAs(stack) ||
               AEApi.instance().definitions().items().wirelessPatternTerminal().isSameAs(stack) ||
               AEApi.instance().definitions().items().wirelessInterfaceTerminal().isSameAs(stack);
    }

    private byte getTerminalMode(ItemStack stack) {
        if (AEApi.instance().definitions().items().wirelessTerminal().isSameAs(stack)) {
            return ItemWirelessUniversalTerminal.MODE_ITEM;
        } else if (AEApi.instance().definitions().items().wirelessCraftingTerminal().isSameAs(stack)) {
            return ItemWirelessUniversalTerminal.MODE_CRAFTING;
        } else if (AEApi.instance().definitions().items().wirelessFluidTerminal().isSameAs(stack)) {
            return ItemWirelessUniversalTerminal.MODE_FLUID;
        } else if (AEApi.instance().definitions().items().wirelessPatternTerminal().isSameAs(stack)) {
            return ItemWirelessUniversalTerminal.MODE_PATTERN;
        } else if (AEApi.instance().definitions().items().wirelessInterfaceTerminal().isSameAs(stack)) {
            return ItemWirelessUniversalTerminal.MODE_INTERFACE;
        }
        return -1;
    }
}
