package com.SaintAlfonzo.solraspberrypie.mixin.client;

import com.SaintAlfonzo.solraspberrypie.RpConfig;
import com.SaintAlfonzo.solraspberrypie.client.FoodDictionarySort;
import com.SaintAlfonzo.solraspberrypie.client.Icons;
import com.cazsius.solcarrot.client.gui.FoodBookScreen;
import com.cazsius.solcarrot.client.gui.elements.UIImage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * The Food Dictionary (Spice of Life: Carrot Edition's food book, plan_diet_books.md 2.3, 2.1): a sort button on the
 * eaten-food pages, after Sophisticated Backpacks' sort-type button (one small button, an icon per state, a click
 * steps to the next state, the tooltip names the state), own code. The state is the client config
 * {@code [foodDictionary] eatenSort}; a click re-sorts the book in place. The mouse wheel flips pages (as 1.6).
 * Gated on solcarrot (RpMixinPlugin).
 */
@Mixin(value = FoodBookScreen.class, remap = false)
public abstract class CarrotBookScreenMixin extends Screen {
	@Shadow
	private UIImage background;

	@Shadow
	@Final
	@SuppressWarnings("rawtypes")
	private List pages;

	@Unique
	private int solrp$eatenFrom = -1, solrp$eatenTo = -1;

	@Unique
	private Button solrp$sortButton;

	protected CarrotBookScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "addPages", at = @At("HEAD"))
	private void solrp$eatenStart(String header, List<?> items, CallbackInfo ci) {
		if ("eaten_foods".equals(header)) solrp$eatenFrom = pages.size();
	}

	@Inject(method = "addPages", at = @At("RETURN"))
	private void solrp$eatenEnd(String header, List<?> items, CallbackInfo ci) {
		if ("eaten_foods".equals(header)) solrp$eatenTo = pages.size();
	}

	@Inject(method = "m_7856_", at = @At("TAIL"))
	private void solrp$addSortButton(CallbackInfo ci) {
		int size = 18;
		int x = background.frame.x + background.frame.width - size - 14, y = background.frame.y + 10;
		solrp$sortButton = this.addRenderableWidget(new Button(x, y, size, size, Component.empty(), b -> {
			FoodDictionarySort.cycleEaten();
			FoodBookScreen self = (FoodBookScreen) (Object) this;
			int page = self.getCurrentPageNumber();
			this.init(this.minecraft, this.width, this.height);
			self.switchToPage(page);
		}));
		solrp$updateSortButton();
	}

	@Unique
	private void solrp$updateSortButton() {
		if (solrp$sortButton == null) return;
		int page = ((FoodBookScreen) (Object) this).getCurrentPageNumber();
		solrp$sortButton.visible = page >= solrp$eatenFrom && page < solrp$eatenTo;
	}

	@Unique
	private static ItemStack solrp$icon(RpConfig.EatenSort mode) {
		return new ItemStack(switch (mode) {
			case NEWEST_UNLOCK -> Items.GOLDEN_CARROT;
			case LATEST_EATEN -> Items.CLOCK;
			case UNEATEN_ORDER -> Items.BOOK;
		});
	}

	@Inject(method = "m_6305_", at = @At("TAIL"))
	private void solrp$drawSortButton(PoseStack pose, int mouseX, int mouseY, float partial, CallbackInfo ci) {
		solrp$updateSortButton();
		if (solrp$sortButton == null || !solrp$sortButton.visible) return;
		RpConfig.EatenSort mode = FoodDictionarySort.eatenMode();
		Icons.item(solrp$icon(mode), solrp$sortButton.x + 1, solrp$sortButton.y + 1, 16);
		if (solrp$sortButton.isMouseOver(mouseX, mouseY)) {
			this.renderComponentTooltip(pose, List.of(
					Component.translatable("solraspberrypie.fooddict.sort." + mode.name().toLowerCase(java.util.Locale.ROOT)),
					Component.translatable("solraspberrypie.fooddict.sort.hint").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
		}
	}

	/** The mouse wheel flips one page per notch (reobf renames this override to its SRG name). */
	@Override
	public boolean mouseScrolled(double x, double y, double delta) {
		FoodBookScreen self = (FoodBookScreen) (Object) this;
		int target = self.getCurrentPageNumber() + (delta < 0 ? 1 : -1);
		if (delta == 0 || !self.isWithinRange(target)) return super.mouseScrolled(x, y, delta);
		self.switchToPage(target);
		if (this.minecraft != null) this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
		return true;
	}
}
