package appeng.ext.aeadditions.part.gas;

import java.io.IOException;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.ext.aeadditions.api.gas.IAEGasStack;
import appeng.ext.aeadditions.integration.Integration;
import appeng.ext.aeadditions.models.PartModels;
import appeng.ext.aeadditions.part.PartECBase;
import appeng.ext.aeadditions.util.AEUtils;
import appeng.ext.aeadditions.util.GasUtil;
import appeng.ext.aeadditions.util.StorageChannels;
import appeng.ext.aeadditions.util.WrenchUtil;

/** Ported from PartGasStorageMonitor.kt. */
public class PartGasStorageMonitor extends PartECBase implements IStackWatcherHost {

    public Fluid fluid = null;
    public long amount = 0L;
    public Object dspList = null;
    public boolean locked = false;
    public IStackWatcher watcher = null;

    public boolean isMekanismEnabled() {
        return Integration.Mods.MEKANISMGAS.isEnabled();
    }

    public IMEMonitor<IAEGasStack> getGasStorage() {
        final IGridNode n = this.getGridNode();
        if (n == null) {
            return null;
        }
        final IGrid g = n.getGrid();
        if (g == null) {
            return null;
        }
        final IStorageGrid storage = g.getCache(IStorageGrid.class);
        if (storage == null) {
            return null;
        }
        return storage.getInventory(StorageChannels.GAS);
    }

    @Override
    public float getCableConnectionLength(final AECableType aeCableType) {
        return 1.0f;
    }

    public void dropItems(final World world, final BlockPos pos, final ItemStack stack) {
        if (world == null) {
            return;
        }
        if (!world.isRemote) {
            final float f = 0.7f;
            final double d0 = world.rand.nextFloat() * f + (1.0f - f) * 0.5;
            final double d1 = world.rand.nextFloat() * f + (1.0f - f) * 0.5;
            final double d2 = world.rand.nextFloat() * f + (1.0f - f) * 0.5;
            final EntityItem entityitem = new EntityItem(world, pos.getX() + d0, pos.getY() + d1, pos.getZ() + d2,
                    stack);
            entityitem.setPickupDelay(10);
            world.spawnEntity(entityitem);
        }
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(2.0, 2.0, 14.0, 14.0, 14.0, 16.0);
        bch.addBox(4.0, 4.0, 13.0, 12.0, 12.0, 14.0);
        bch.addBox(5.0, 5.0, 12.0, 11.0, 11.0, 13.0);
    }

    @Override
    public double getPowerUsage() {
        return 1.0;
    }

    @Override
    public List<String> getWailaBodey(final NBTTagCompound data, final List<String> list) {
        super.getWailaBodey(data, list);
        long amount = 0L;
        Fluid fluid = null;
        if (data.hasKey("locked") && data.getBoolean("locked")) {
            list.add(I18n.translateToLocal("waila.appliedenergistics2.Locked"));
        } else {
            list.add(I18n.translateToLocal("waila.appliedenergistics2.Unlocked"));
        }
        if (data.hasKey("amount")) {
            amount = data.getLong("amount");
        }
        if (data.hasKey("fluid")) {
            final String fluidName = data.getString("fluid");
            if (!fluidName.isEmpty()) {
                fluid = FluidRegistry.getFluid(fluidName);
            }
        }
        if (fluid != null) {
            list.add(I18n.translateToLocal("appeng.ext.aeadditions.tooltip.fluid") + ": "
                    + fluid.getLocalizedName(new FluidStack(fluid, Fluid.BUCKET_VOLUME)));
            if (this.isActive()) {
                list.add(I18n.translateToLocal("appeng.ext.aeadditions.tooltip.amount") + ": " + amount + "mB");
            } else {
                list.add(I18n.translateToLocal("appeng.ext.aeadditions.tooltip.amount") + ": 0mB");
            }
        } else {
            list.add(I18n.translateToLocal("appeng.ext.aeadditions.tooltip.fluid") + ": "
                    + I18n.translateToLocal("appeng.ext.aeadditions.tooltip.empty1"));
            list.add(I18n.translateToLocal("appeng.ext.aeadditions.tooltip.amount") + ": 0mB");
        }
        return list;
    }

    @Override
    public NBTTagCompound getWailaTag(final NBTTagCompound tag) {
        super.getWailaTag(tag);
        tag.setBoolean("locked", this.locked);
        tag.setLong("amount", this.amount);
        if (this.fluid == null) {
            tag.setString("fluid", "");
        } else {
            tag.setString("fluid", this.fluid.getName());
        }
        return tag;
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("amount")) {
            this.amount = data.getLong("amount");
        }
        if (data.hasKey("fluid")) {
            final String name = data.getString("fluid");
            if (name.isEmpty()) {
                this.fluid = null;
            } else {
                this.fluid = FluidRegistry.getFluid(name);
            }
        }
        if (data.hasKey("locked")) {
            this.locked = data.getBoolean("locked");
        }
    }

    @Override
    public boolean readFromStream(final ByteBuf data) throws IOException {
        super.readFromStream(data);
        this.amount = data.readLong();
        final String name = ByteBufUtils.readUTF8String(data);
        if (name.isEmpty()) {
            this.fluid = null;
        } else {
            this.fluid = FluidRegistry.getFluid(name);
        }
        this.locked = data.readBoolean();
        return true;
    }

    @Override
    public boolean onActivate(final EntityPlayer player, final EnumHand hand, final Vec3d pos) {
        if (this.isMekanismEnabled()) {
            return this.onActivateGas(player, hand, pos);
        }

        return false;
    }

    @Optional.Method(modid = "mekanism")
    public boolean onActivateGas(final EntityPlayer player, final EnumHand hand, final Vec3d pos) {
        if (player == null || player.world == null || hand == null || pos == null) {
            return true;
        }

        if (player.world.isRemote) {
            return true;
        }

        final ItemStack itemStack = player.getHeldItem(hand);

        if (itemStack == null) {
            if (this.locked) {
                return false;
            }

            if (this.fluid == null) {
                return true;
            }

            if (this.watcher != null) {
                this.watcher.remove(StorageChannels.GAS.createStack(this.fluid));
            }

            this.fluid = null;
            this.amount = 0L;
            if (this.getHost() != null) {
                this.getHost().markForUpdate();
            }
            return true;
        }

        final RayTraceResult rayTraceResult = new RayTraceResult(pos, this.getFacing(),
                this.getLocation().getPos());
        final appeng.ext.aeadditions.api.IWrenchHandler wrenchHandler = WrenchUtil.getHandler(itemStack, player,
                rayTraceResult, hand);

        if (wrenchHandler != null) {
            this.locked = !this.locked;
            wrenchHandler.wrenchUsed(itemStack, player, rayTraceResult, hand);

            if (this.getHost() != null) {
                this.getHost().markForUpdate();
            }

            if (this.locked) {
                player.sendMessage(new TextComponentTranslation("chat.appliedenergistics2.isNowLocked"));
            } else {
                player.sendMessage(new TextComponentTranslation("chat.appliedenergistics2.isNowUnlocked"));
            }

            return true;
        }

        if (this.locked) {
            return false;
        }

        if (GasUtil.isFilled(itemStack)) {
            if (this.fluid != null && this.watcher != null) {
                this.watcher.remove(StorageChannels.GAS.createStack(this.fluid));
            }

            final net.minecraftforge.fluids.FluidStack gas = GasUtil.getFluidStack(
                    GasUtil.getGasFromContainer(itemStack));

            this.fluid = gas == null ? null : gas.getFluid();

            if (this.watcher != null) {
                this.watcher.add(StorageChannels.GAS.createStack(this.fluid));
            }

            if (this.getHost() != null) {
                this.getHost().markForUpdate();
            }
            this.onStackChange();
            return true;
        }

        return false;
    }

    @Override
    public void onStackChange(final IItemList<?> p0, final IAEStack<?> p1, final IAEStack<?> p2,
            final IActionSource p3, final IStorageChannel<?> p4) {
        this.onStackChange();
    }

    @Optional.Method(modid = "mekanism")
    public void onStackChange() {
        if (this.fluid != null) {
            final IGridNode node = this.getGridNode();
            if (node == null) {
                return;
            }
            final IGrid grid = node.getGrid();
            if (grid == null) {
                return;
            }

            final IStorageGrid storage = grid.getCache(IStorageGrid.class);
            if (storage == null) {
                return;
            }

            final IMEMonitor<IAEGasStack> fluids = this.getGasStorage();
            if (fluids == null) {
                return;
            }

            final Object gas = GasUtil.getGas(this.fluid);

            for (final IAEGasStack s : fluids.getStorageList()) {
                if (s.getGas() == gas) {
                    this.amount = s.getStackSize();
                    if (this.getHost() != null) {
                        this.getHost().markForUpdate();
                    }

                    return;
                }
            }

            this.amount = 0L;

            if (this.getHost() != null) {
                this.getHost().markForUpdate();
            }
        }
    }

    @Override
    public void updateWatcher(final IStackWatcher w) {
        this.watcher = w;
        if (w == null) {
            return;
        }
        if (this.fluid != null) {
            w.add(StorageChannels.GAS.createStack(this.fluid));
        }

        this.onStackChange(null, null, null, null, null);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderDynamic(final double x, final double y, final double z, final float partialTicks,
            final int destroyStage) {
        if (this.fluid == null) {
            return;
        }
        if (this.dspList == null) {
            this.dspList = GLAllocation.generateDisplayLists(1);
        }
        if (!this.isActive()) {
            return;
        }
        final IAEFluidStack aeFluidStack = AEUtils.createFluidStack(this.fluid);
        if (aeFluidStack == null) {
            return;
        }
        aeFluidStack.setStackSize(this.amount);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
        GlStateManager.glNewList((Integer) this.dspList, GL11.GL_COMPILE_AND_EXECUTE);
        final Tessellator tess = Tessellator.getInstance();
        this.renderFluid(tess, aeFluidStack);
        GlStateManager.glEndList();
        GlStateManager.popMatrix();
    }

    @SideOnly(Side.CLIENT)
    private void renderFluid(final Tessellator tess, final IAEFluidStack fluidStack) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        final EnumFacing facing = this.getSide().getFacing();
        this.moveToFace(facing);
        this.rotateToFace(facing, (byte) 1);
        GlStateManager.pushMatrix();
        try {
            final int br = 16 << 20 | 16 << 4;
            final int var11 = br % 65536;
            final int var12 = br / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, var11 * 0.8f, var12 * 0.8f);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.disableLighting();
            GlStateManager.disableRescaleNormal();
            // RenderHelper.enableGUIStandardItemLighting();
            final Minecraft mc = Minecraft.getMinecraft();
            final net.minecraft.util.ResourceLocation fluidStill = this.fluid.getStill();
            if (fluidStill != null) {
                final TextureMap textureMap = mc.getTextureMapBlocks();
                final TextureAtlasSprite fluidIcon = textureMap.getAtlasSprite(fluidStill.toString());
                if (fluidIcon != null) {
                    GL11.glTranslatef(0.0f, 0.14f, -0.24f);
                    GL11.glScalef(1.0f / 62.0f, 1.0f / 62.0f, 1.0f / 62.0f);
                    GL11.glTranslated(-8.6, -16.3, -1.2);
                    mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                    final net.minecraft.client.renderer.BufferBuilder buffer = tess.getBuffer();
                    buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                    try {
                        buffer.pos(0.0, 16.0, 0.0).tex(fluidIcon.getMinU(), fluidIcon.getMaxV())
                                .color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
                        buffer.pos(16.0, 16.0, 0.0).tex(fluidIcon.getMaxU(), fluidIcon.getMaxV())
                                .color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
                        buffer.pos(16.0, 0.0, 0.0).tex(fluidIcon.getMaxU(), fluidIcon.getMinV())
                                .color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
                        buffer.pos(0.0, 0.0, 0.0).tex(fluidIcon.getMinU(), fluidIcon.getMinV())
                                .color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
                    } finally {
                        tess.draw();
                    }
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        GlStateManager.popMatrix();
        GlStateManager.translate(0.0f, 0.14f, -0.24f);
        GlStateManager.scale(1.0f / 62.0f, 1.0f / 62.0f, 1.0f / 62.0f);
        long qty = fluidStack.getStackSize();
        if (qty > 999999999999L) {
            qty = 999999999999L;
        }
        String msg = Long.toString(qty) + "mB";
        if (qty > 1000000000) {
            msg = Long.toString(qty / 1000000000) + "MB";
        } else if (qty > 1000000) {
            msg = Long.toString(qty / 1000000) + "KB";
        } else if (qty > 9999) {
            msg = Long.toString(qty / 1000) + 'B';
        }
        final net.minecraft.client.gui.FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        final int width = fr.getStringWidth(msg);
        GlStateManager.translate(-0.5f * width, 0.0f, -1.0f);
        fr.drawString(msg, 0, 0, 0);
        GlStateManager.popAttrib();
    }

    private void moveToFace(final EnumFacing face) {
        GlStateManager.translate(face.getXOffset() * 0.77, face.getYOffset() * 0.77, face.getZOffset() * 0.77);
    }

    private void rotateToFace(final EnumFacing face, final byte spin) {
        switch (face) {
            case UP:
                GlStateManager.scale(1.0f, -1.0f, 1.0f);
                GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);
                GlStateManager.rotate(spin * 90.0f, 0f, 0f, 1f);
                break;
            case DOWN:
                GlStateManager.scale(1.0f, -1.0f, 1.0f);
                GlStateManager.rotate(-90.0f, 1.0f, 0.0f, 0.0f);
                GlStateManager.rotate(spin * -90.0f, 0f, 0f, 1f);
                break;
            case EAST:
                GlStateManager.scale(-1.0f, -1.0f, -1.0f);
                GlStateManager.rotate(-90.0f, 0.0f, 1.0f, 0.0f);
                break;
            case WEST:
                GlStateManager.scale(-1.0f, -1.0f, -1.0f);
                GlStateManager.rotate(90.0f, 0.0f, 1.0f, 0.0f);
                break;
            case NORTH:
                GlStateManager.scale(-1.0f, -1.0f, -1.0f);
                break;
            case SOUTH:
                GlStateManager.scale(-1.0f, -1.0f, -1.0f);
                GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f);
                break;
            default:
                break;
        }
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return PartModels.STORAGE_MONITOR_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return PartModels.STORAGE_MONITOR_ON;
        } else {
            return PartModels.STORAGE_MONITOR_OFF;
        }
    }

    @Override
    public boolean requireDynamicRender() {
        return true;
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        data.setLong("amount", this.amount);
        if (this.fluid == null) {
            data.setInteger("fluid", -1);
        } else {
            data.setString("fluid", this.fluid.getName());
        }
        data.setBoolean("locked", this.locked);
    }

    @Override
    public void writeToStream(final ByteBuf data) throws IOException {
        super.writeToStream(data);
        data.writeLong(this.amount);
        if (this.fluid == null) {
            ByteBufUtils.writeUTF8String(data, "");
        } else {
            ByteBufUtils.writeUTF8String(data, this.fluid.getName());
        }
        data.writeBoolean(this.locked);
    }
}
