package com.SaintAlfonzo.solraspberrypie.mixin.client;

import com.SaintAlfonzo.solraspberrypie.client.ClientState;
import com.SaintAlfonzo.solraspberrypie.client.DietLogPages;
import com.SaintAlfonzo.solraspberrypie.client.FitLabel;
import com.SaintAlfonzo.solraspberrypie.client.Icons;
import com.tarinoita.solsweetpotato.client.gui.DiversityPage;
import com.tarinoita.solsweetpotato.client.gui.elements.UIElement;
import com.tarinoita.solsweetpotato.client.gui.elements.UIImage;
import com.tarinoita.solsweetpotato.client.gui.elements.UILabel;
import com.tarinoita.solsweetpotato.client.gui.elements.UIStack;
import com.tarinoita.solsweetpotato.lib.Localization;
import net.minecraft.client.resources.language.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Apple Pie's diversity page says "Need to eat N more times" / "to activate benefits"; with the unique-food gate
 * (12.4) it reads "N more unique food(s)" / "to activate diet" instead (plan_diet_books.md 1.9). The count itself
 * comes from FoodBookScreenMixin.
 * <p>
 * plan_owner_notes_1119.md rulings 8 and 12: the page's top spacer becomes the last meals grid
 * ({@link DietLogPages#mealsGrid}), the spacer above the warning lines is dropped to make room, and the image left of
 * the diversity value becomes a food matching the diversity ({@link DietLogPages#diversityIcon}).
 * <p>
 * plan_diet_books_r2.md A3, A9: the diversity value printed with two decimals rounded down; "Current Food Diversity"
 * and the gate lines drawn at the largest text scale (at most 0.85) that fits the page, each on its own line.
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

	/** Apple Pie's "%.2f" (rounds half up) -> Raspberry Pie's two decimals rounded down. */
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/lang/String;format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"))
	private String solrp$diversityValue(String format, Object[] args) {
		if (args.length == 1 && args[0] instanceof Double d) return Icons.diversity(d);
		return String.format(format, args);
	}

	/** Side margin (book pixels) kept free of text on each side of the page. */
	private static final int FIT_MARGIN = 20;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void solrp$fitLabels(CallbackInfo ci) {
		UIElement page = (UIElement) (Object) this;
		UIStack main = null;
		for (UIElement e : ((UIElementAccessor) page).solrp$children()) if (e instanceof UIStack st) main = st;
		if (main == null) return;
		// The labels to fit: every direct label after the header, and the name label of the diversity stat.
		List<List<UIElement>> owners = new ArrayList<>();
		List<Integer> slots = new ArrayList<>();
		List<String> texts = new ArrayList<>();
		List<UIElement> top = main.getChildren();
		for (int i = 1; i < top.size(); i++) {
			UIElement e = top.get(i);
			if (e instanceof UILabel l) {
				owners.add(top);
				slots.add(i);
				texts.add(l.text);
			} else if (e instanceof UIStack stat) {
				List<UIElement> inner = stat.getChildren();
				for (int j = 0; j < inner.size(); j++) {
					if (inner.get(j) instanceof UILabel l) {
						owners.add(inner);
						slots.add(j);
						texts.add(l.text);
					}
				}
			}
		}
		if (texts.isEmpty()) return;
		float layout = DietLogPages.Layout.diversity().scale();
		int avail = Math.round((page.frame.width - 2 * FIT_MARGIN) / Math.max(0.25F, layout));
		float scale = FitLabel.fit(avail, texts);
		for (int k = 0; k < owners.size(); k++) {
			List<UIElement> list = owners.get(k);
			int idx = slots.get(k);
			list.set(idx, new FitLabel((UILabel) list.get(idx), scale));
		}
		main.updateFrames();
		main.setCenterX(page.getCenterX());
		main.setMinY(page.getMinY() + 17);
		main.updateFrames();
	}
}
