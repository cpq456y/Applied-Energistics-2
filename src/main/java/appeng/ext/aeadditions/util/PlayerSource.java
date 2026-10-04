package appeng.ext.aeadditions.util;

import java.util.Optional;

import com.google.common.base.Preconditions;

import net.minecraft.entity.player.EntityPlayer;

import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;

/** Ported from PlayerSource.kt. */
public class PlayerSource implements IActionSource {

    private final EntityPlayer playerObj;

    public final IActionHost via;

    public PlayerSource(final EntityPlayer playerObj, final IActionHost via) {
        Preconditions.checkNotNull(playerObj);
        this.playerObj = playerObj;
        this.via = via;
    }

    public IActionHost getVia() {
        return this.via;
    }

    @Override
    public Optional<EntityPlayer> player() {
        return Optional.of(this.playerObj);
    }

    @Override
    public Optional<IActionHost> machine() {
        return Optional.ofNullable(this.via);
    }

    @Override
    public <T> Optional<T> context(final Class<T> key) {
        return Optional.empty();
    }
}
