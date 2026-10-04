package appeng.ext.aeadditions.integration.opencomputers;

import li.cil.oc.api.network.EnvironmentHost;

/** Ported from CompletionHelper.kt (the Kotlin object is named CompleteHelper). */
public final class CompleteHelper {

    private CompleteHelper() {
    }

    public static UpgradeAE getCompleteUpgradeAE(final EnvironmentHost envHost) {
        return new UpgradeAEComplete(envHost);
    }

    public static Class<? extends UpgradeAE> getCompleteUpgradeAEClass() {
        return UpgradeAEComplete.class;
    }
}
