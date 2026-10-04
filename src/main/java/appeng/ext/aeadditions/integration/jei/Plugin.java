package appeng.ext.aeadditions.integration.jei;

import java.util.Iterator;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import appeng.ext.aeadditions.integration.Integration;
import appeng.ext.aeadditions.registries.BlockEnum;
import appeng.ext.aeadditions.registries.ItemEnum;
import appeng.ext.aeadditions.util.CreativeTabEC;

/** Ported from Plugin.kt. */
@JEIPlugin
public class Plugin implements IModPlugin {

    @Override
    public void register(final IModRegistry registry) {
        if (!Integration.Mods.JEI.isEnabled()) {
            return;
        }

        Jei.registry = registry;

        for (final net.minecraftforge.fluids.FluidStack item : Jei.fluidBlackList) {
            registry.getJeiHelpers().getIngredientBlacklist().addIngredientToBlacklist(item);
        }

        this.hideItem(new ItemStack(ItemEnum.FLUIDITEM.getItem()), registry);
        this.hideItem(new ItemStack(ItemEnum.GASITEM.getItem()), registry);
        this.hideItem(new ItemStack(ItemEnum.CRAFTINGPATTERN.getItem()), registry);

        for (final ItemEnum item : ItemEnum.values()) {
            if ((item.getMod() != null && !item.getMod().isEnabled()) || !item.isEnabled()) {
                final net.minecraft.item.Item i = item.getItem();

                final NonNullList<ItemStack> list = NonNullList.create();

                i.getSubItems(CreativeTabEC.INSTANCE, list);

                final Iterator<ItemStack> iterator = list.iterator();
                while (iterator.hasNext()) {
                    try {
                        this.hideItem(iterator.next(), registry);
                    } catch (final Throwable e) {
                        continue;
                    }
                }
            }
        }

        for (final BlockEnum block : BlockEnum.values()) {
            if ((block.getMod() != null && !block.getMod().isEnabled()) || !block.getEnabled()) {
                final net.minecraft.block.Block b = block.getBlock();

                final NonNullList<ItemStack> list = NonNullList.create();

                b.getSubBlocks(CreativeTabEC.INSTANCE, list);

                final Iterator<ItemStack> iterator = list.iterator();
                while (iterator.hasNext()) {
                    try {
                        this.hideItem(iterator.next(), registry);
                    } catch (final Throwable e) {
                        continue;
                    }
                }
            }
        }
    }

    private void hideItem(final ItemStack item, final IModRegistry registry) {
        registry.getJeiHelpers().getIngredientBlacklist().addIngredientToBlacklist(item);
    }
}
