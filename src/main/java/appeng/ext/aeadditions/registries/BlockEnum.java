package appeng.ext.aeadditions.registries;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

import appeng.api.config.Upgrades;
import appeng.block.crafting.BlockCraftingUnit;
import appeng.ext.aeadditions.Constants;
import appeng.ext.aeadditions.block.BlockCertusTank;
import appeng.ext.aeadditions.block.BlockCraftingStorage;
import appeng.ext.aeadditions.block.BlockFluidCrafter;
import appeng.ext.aeadditions.block.BlockFluidFiller;
import appeng.ext.aeadditions.block.BlockGasInterface;
import appeng.ext.aeadditions.block.BlockHardMEDrive;
import appeng.ext.aeadditions.block.BlockVibrationChamberFluid;
import appeng.ext.aeadditions.integration.Integration;
import appeng.ext.aeadditions.item.block.ItemBlockCertusTank;
import appeng.ext.aeadditions.item.block.ItemBlockFluidFiller;
import appeng.ext.aeadditions.item.block.ItemBlockGasInterface;
import appeng.ext.aeadditions.util.CreativeTabEC;

/** Ported from BlockEnum.kt. */
public enum BlockEnum {

    CERTUSTANK("certustank", new BlockCertusTank(), b -> new ItemBlockCertusTank(b)),
    FLUIDCRAFTER("fluidcrafter", new BlockFluidCrafter(), Pair.of(Upgrades.SPEED, 5), Pair.of(Upgrades.CAPACITY, 2)),
    FILLER("fluidfiller", new BlockFluidFiller(), b -> new ItemBlockFluidFiller(b)),
    BLASTRESISTANTMEDRIVE("hardmedrive", new BlockHardMEDrive()),
    VIBRANTCHAMBERFLUID("vibrantchamberfluid", new BlockVibrationChamberFluid()),
    GASINTERFACE("gas_interface", new BlockGasInterface(), b -> new ItemBlockGasInterface(b),
            Integration.Mods.MEKANISMGAS),
    UPGRADEDCRAFTINGSTORAGE256("crafting_storage_256",
            new BlockCraftingStorage(BlockCraftingUnit.CraftingUnitType.STORAGE_1K), false),
    UPGRADEDCRAFTINGSTORAGE1024("crafting_storage_1024",
            new BlockCraftingStorage(BlockCraftingUnit.CraftingUnitType.STORAGE_4K), false),
    UPGRADEDCRAFTINGSTORAGE4096("crafting_storage_4096",
            new BlockCraftingStorage(BlockCraftingUnit.CraftingUnitType.STORAGE_16K), false),
    UPGRADEDCRAFTINGSTORAGE16384("crafting_storage_16384",
            new BlockCraftingStorage(BlockCraftingUnit.CraftingUnitType.STORAGE_64K), false);

    private final String internalName;
    private final Block block;
    private final ItemBlock item;
    private final Integration.Mods mod;
    private final Map<Upgrades, Integer> upgrades = new LinkedHashMap<>();
    private boolean enabled = true;

    /**
     * The primary constructor. Mirrors the Kotlin {@code init} block, which runs before the vararg
     * constructors populate {@link #upgrades}.
     */
    BlockEnum(final String internalName, final Block block, final Function<Block, ItemBlock> factory,
            final Integration.Mods mod) {
        this.internalName = internalName;
        this.block = block;
        this.mod = mod;

        block.setTranslationKey("appeng.ext.aeadditions.block." + internalName);
        block.setRegistryName(Constants.MOD_ID, internalName);
        this.item = factory.apply(block);
        this.item.setRegistryName(block.getRegistryName());
        if (mod == null || mod.isEnabled()) {
            block.setCreativeTab(CreativeTabEC.INSTANCE);
        }
    }

    BlockEnum(final String internalName, final Block block, final Function<Block, ItemBlock> factory) {
        this(internalName, block, factory, null);
    }

    BlockEnum(final String internalName, final Block block) {
        this(internalName, block, b -> new ItemBlock(b), null);
    }

    BlockEnum(final String internalName, final Block block, final boolean enabled) {
        this(internalName, block, b -> new ItemBlock(b), null);
        this.enabled = enabled;
    }

    @SafeVarargs
    BlockEnum(final String internalName, final Block block, final Pair<Upgrades, Integer>... upgrades) {
        this(internalName, block, b -> new ItemBlock(b), null);
        for (final Pair<Upgrades, Integer> pair : upgrades) {
            this.upgrades.put(pair.getKey(), pair.getValue());
        }
    }

    public String getInternalName() {
        return this.internalName;
    }

    public Block getBlock() {
        return this.block;
    }

    public ItemBlock getItem() {
        return this.item;
    }

    public Integration.Mods getMod() {
        return this.mod;
    }

    public Map<Upgrades, Integer> getUpgrades() {
        return this.upgrades;
    }

    public boolean getEnabled() {
        return this.enabled;
    }

    public String getStatName() {
        return I18n.translateToLocal(this.block.getTranslationKey() + ".name");
    }

    public void registerUpgrades() {
        for (final Map.Entry<Upgrades, Integer> entry : this.upgrades.entrySet()) {
            entry.getKey().registerItem(new ItemStack(this.block, 1), entry.getValue());
        }
    }

    public static BlockEnum getValueByBlockInternalName(final String internalName) {
        for (final BlockEnum value : values()) {
            if (value.internalName.equals(internalName)) {
                return value;
            }
        }

        return null;
    }
}
