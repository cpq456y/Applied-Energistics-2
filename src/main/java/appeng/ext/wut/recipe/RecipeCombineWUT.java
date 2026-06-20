package appeng.ext.wut.recipe;

import appeng.api.AEApi;
import appeng.ext.wut.ItemWirelessUniversalTerminal;
import appeng.ext.wut.WirelessUniversalTerminalHandler;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

/**
 * 合成配方：将4个基础无线终端合成为基础无线通用终端
 */
public class RecipeCombineWUT extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        return !getCraftingResult(inv).isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        // 检查是否有4个基础终端
        int itemCount = 0;
        int craftingCount = 0;
        int fluidCount = 0;
        int patternCount = 0;
        
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            
            // 只允许基础终端
            if (AEApi.instance().definitions().items().wirelessTerminal().isSameAs(stack)) {
                itemCount++;
            } else if (AEApi.instance().definitions().items().wirelessCraftingTerminal().isSameAs(stack)) {
                craftingCount++;
            } else if (AEApi.instance().definitions().items().wirelessFluidTerminal().isSameAs(stack)) {
                fluidCount++;
            } else if (AEApi.instance().definitions().items().wirelessPatternTerminal().isSameAs(stack)) {
                patternCount++;
            } else {
                return ItemStack.EMPTY; // 不允许其他物品
            }
        }
        
        // 必须每种基础终端各一个
        if (itemCount == 1 && craftingCount == 1 && fluidCount == 1 && patternCount == 1) {
            // 创建基础WUT（包含4个基础终端模式）
            ItemStack output = new ItemStack(AEApi.instance().definitions().items().wirelessUniversalTerminal().maybeItem().get());
            net.minecraft.nbt.NBTTagCompound tag = appeng.util.Platform.openNbtData(output);
            tag.setIntArray("modes", new int[]{
                ItemWirelessUniversalTerminal.MODE_ITEM,
                ItemWirelessUniversalTerminal.MODE_CRAFTING,
                ItemWirelessUniversalTerminal.MODE_FLUID,
                ItemWirelessUniversalTerminal.MODE_PATTERN
            });
            tag.setByte("mode", ItemWirelessUniversalTerminal.MODE_ITEM);
            return output;
        }
        
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        return ForgeHooks.defaultRecipeGetRemainingItems(inv);
    }
}
