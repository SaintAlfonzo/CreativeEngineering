package com.SaintAlfonzo.solraspberrypie.mixin.client;

import com.SaintAlfonzo.solraspberrypie.client.ClientState;
import com.SaintAlfonzo.solraspberrypie.client.DietLogPages;
import com.tarinoita.solsweetpotato.client.gui.DiversityPage;
import com.tarinoita.solsweetpotato.client.gui.elements.UIElement;
import com.tarinoita.solsweetpotato.client.gui.elements.UIImage;
import com.tarinoita.solsweetpotato.client.gui.elements.UIStack;
import com.tarinoita.solsweetpotato.lib.Localization;
import net.minecraft.client.resources.language.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Apple Pie's diversity page says "Need to eat N more times" / "to activate benefits"; with the unique-food gate
 * (12.4) it reads "N more unique food(s)" / "to activate diet" instead (plan_diet_books.md 1.9). The count itself
 * comes from FoodBookScreenMixin.
 * <p>
 * plan_owner_notes_1119.md rulings 8 and 12: the page's top spacer becomes the last meals grid
 * ({@link DietLogPages#mealsGrid}), the spacer above the warning lines is dropped to make room, and the image left of
 * the diversity value becomes a food matching the diversity ({@link DietLogPages#diversityIcon}).
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
			boolean one = args[0] instanceof Number n && n.intValue() == 1;
			return I18n.get(one ? "solraspberrypie.dietlog.min_unique_one" : "solraspberrypie.dietlog.min_unique", args[0]);
		}
		return Localization.localized(domain, path, args);
	}

	// ordinal 3 = the min_warning2 call (the second line).
	@Redirect(method = "<init>", at = @At(value = "INVOKE",
			target = "Lcom/tarinoita/solsweetpotato/lib/Localization;localized(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;",
			ordinal = 3))
	private String solrp$uniqueWording2(String domain, String path, Object[] args) {
		if ("food_book.stats.min_warning2".equals(path) && ClientState.state != null && ClientState.state.uniqueGate) {
			return I18n.get("solraspberrypie.dietlog.min_unique2");
		}
		return Localization.localized(domain, path, args);
	}

	private static final String ADD_CHILD = "Lcom/tarinoita/solsweetpotato/client/gui/elements/UIStack;addChild(Lcom/tarinoita/solsweetpotato/client/gui/elements/UIElement;)V";

	// ordinal 0 = the 1 x 35 spacer under the header.
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = ADD_CHILD, ordinal = 0))
	private void solrp$mealsGrid(UIStack stack, UIElement spacer) {
		stack.addChild(DietLogPages.mealsGrid());
	}

	// ordinal 2 = the 1 x 10 spacer above the "need to eat" lines (only added while the gate is not met).
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = ADD_CHILD, ordinal = 2))
	private void solrp$dropSpacer(UIStack stack, UIElement spacer) {
	}

	@Redirect(method = "<init>", at = @At(value = "INVOKE",
			target = "Lcom/tarinoita/solsweetpotato/client/gui/DiversityPage;icon(Lcom/tarinoita/solsweetpotato/client/gui/elements/UIImage$Image;)Lcom/tarinoita/solsweetpotato/client/gui/elements/UIImage;"))
	private UIImage solrp$diversityIcon(DiversityPage self, UIImage.Image image) {
		UIImage food = null;
		try {
			food = DietLogPages.diversityIcon(image);
		} catch (RuntimeException ignored) {
		}
		if (food != null) return food;
		UIImage own = new UIImage(image); // Apple Pie's Page.icon: the image at 11 x 11
		own.setWidth(11);
		own.setHeight(11);
		return own;
	}
}
