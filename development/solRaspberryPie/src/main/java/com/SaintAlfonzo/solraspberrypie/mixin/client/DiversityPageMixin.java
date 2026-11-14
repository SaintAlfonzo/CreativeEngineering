package com.SaintAlfonzo.solraspberrypie.mixin.client;

import com.SaintAlfonzo.solraspberrypie.client.ClientState;
import com.tarinoita.solsweetpotato.client.gui.DiversityPage;
import com.tarinoita.solsweetpotato.lib.Localization;
import net.minecraft.client.resources.language.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Apple Pie's diversity page says "Need to eat N more times"; with the unique-food gate (12.4) it reads "N more
 * different foods" instead. The count itself comes from FoodBookScreenMixin.
 */
@Mixin(value = DiversityPage.class, remap = false)
public abstract class DiversityPageMixin {
    // ordinal 2 = the min_warning1 call. Ordinal 0 runs before super() (needs a static handler), 1 and 3 after it, so an
    // unqualified redirect cannot match all of them and crashes the book on open.
    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lcom/tarinoita/solsweetpotato/lib/Localization;localized(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;",
            ordinal = 2))
    private String solrp$uniqueWording(String domain, String path, Object[] args) {
        if ("food_book.stats.min_warning1".equals(path) && ClientState.state != null && ClientState.state.uniqueGate && args.length > 0) {
            return I18n.get("solraspberrypie.dietlog.min_unique", args[0]);
        }
        return Localization.localized(domain, path, args);
    }
}
