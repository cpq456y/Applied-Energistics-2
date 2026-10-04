package appeng.ext.aeadditions.util;

import java.util.Optional;

import net.minecraft.entity.player.EntityPlayer;

import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;

/** Ported from MachineSource.kt. */
public class MachineSource implements IActionSource {

    public final IActionHost via;

    public MachineSource(final IActionHost via) {
        this.via = via;
    }

    public IActionHost getVia() {
        return this.via;
    }

    @Override
    public Optional<EntityPlayer> player() {
        return Optional.empty();
    }

    @Override
    public Optional<IActionHost> machine() {
        return Optional.of(this.via);
    }

    @Override
    public <T> Optional<T> context(final Class<T> key) {
        return Optional.empty();
    }
}
