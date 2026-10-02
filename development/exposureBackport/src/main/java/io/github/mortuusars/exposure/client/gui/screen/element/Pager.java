package io.github.mortuusars.exposure.client.gui.screen.element;

import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.util.PagingDirection;
import io.github.mortuusars.exposure.world.sound.SoundEffect;
import java.util.Optional;
import net.minecraft.Util;
import io.github.mortuusars.exposure.backport.gui.AbstractButton;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class Pager {
   protected int pagesCount;
   protected boolean cycle;
   protected int changeCooldownMs = 50;
   @Nullable
   protected SoundEffect changeSound = null;
   @Nullable
   protected AbstractButton previousPageButton = null;
   @Nullable
   protected AbstractButton nextPageButton = null;
   protected Pager.OnPageChanged onPageChanged = (oldPage, newPage) -> {
   };
   protected int currentPage;
   protected long lastChangeTime;

   public int getPagesCount() {
      return this.pagesCount;
   }

   public Pager setPagesCount(int pages) {
      this.pagesCount = pages;
      this.changePage(this.getPage());
      return this;
   }

   public boolean isCycled() {
      return this.cycle;
   }

   public Pager setCycled(boolean cycled) {
      this.cycle = cycled;
      return this;
   }

   public int getChangeCooldown() {
      return this.changeCooldownMs;
   }

   public Pager setChangeCooldown(int milliseconds) {
      this.changeCooldownMs = milliseconds;
      return this;
   }

   public Optional<SoundEffect> getChangeSound() {
      return Optional.ofNullable(this.changeSound);
   }

   public Pager setChangeSound(@Nullable SoundEffect changeSound) {
      this.changeSound = changeSound;
      return this;
   }

   public Optional<AbstractButton> getPreviousPageButton() {
      return Optional.ofNullable(this.previousPageButton);
   }

   public Pager setPreviousPageButton(@Nullable AbstractButton previousPageButton) {
      this.previousPageButton = previousPageButton;
      this.updateButtonVisibility();
      return this;
   }

   public Optional<AbstractButton> getNextPageButton() {
      return Optional.ofNullable(this.nextPageButton);
   }

   public Pager setNextPageButton(@Nullable AbstractButton nextPageButton) {
      this.nextPageButton = nextPageButton;
      this.updateButtonVisibility();
      return this;
   }

   public Pager.OnPageChanged getOnPageChanged() {
      return this.onPageChanged;
   }

   public Pager onPageChanged(Pager.OnPageChanged onPageChanged) {
      this.onPageChanged = onPageChanged;
      return this;
   }

   public int getPage() {
      return this.currentPage;
   }

   public boolean changePage(int page) {
      page = Mth.clamp(page, 0, this.pagesCount);
      int oldPage = this.currentPage;
      if (this.currentPage == page) {
         return false;
      } else {
         this.currentPage = page;
         this.pageChanged(oldPage, page);
         return true;
      }
   }

   public void setPage(int index) {
      this.currentPage = Mth.clamp(index, 0, this.pagesCount);
      this.updateButtonVisibility();
   }

   public boolean isOnCooldown() {
      return Util.getMillis() - this.lastChangeTime < (long)this.changeCooldownMs;
   }

   public void resetCooldown() {
      this.lastChangeTime = 0L;
   }

   public boolean isPagingDirectionAvailable(PagingDirection direction) {
      int newIndex = this.getPage() + direction.getValue();
      return this.pagesCount > 1 && (this.cycle || 0 <= newIndex && newIndex < this.pagesCount);
   }

   public boolean canChangePage(PagingDirection direction) {
      return this.isPagingDirectionAvailable(direction) && !this.isOnCooldown();
   }

   public boolean previousPage() {
      return this.changePage(PagingDirection.PREVIOUS);
   }

   public boolean nextPage() {
      return this.changePage(PagingDirection.NEXT);
   }

   public boolean changePage(PagingDirection direction) {
      if (!this.canChangePage(direction)) {
         return false;
      } else {
         int oldPage = this.currentPage;
         int newPage = this.getPage() + direction.getValue();
         if (this.cycle && newPage >= this.pagesCount) {
            newPage = 0;
         } else if (this.cycle && newPage < 0) {
            newPage = this.pagesCount - 1;
         }

         return oldPage == newPage ? false : this.changePage(newPage);
      }
   }

   public void pageChanged(int oldPage, int newPage) {
      this.lastChangeTime = Util.getMillis();
      this.playChangeSound();
      this.updateButtonVisibility();
      this.onPageChanged.pageChanged(oldPage, newPage);
   }

   public void updateButtonVisibility() {
      if (this.previousPageButton != null) {
         this.previousPageButton.visible = this.isPagingDirectionAvailable(PagingDirection.PREVIOUS);
      }

      if (this.nextPageButton != null) {
         this.nextPageButton.visible = this.isPagingDirectionAvailable(PagingDirection.NEXT);
      }
   }

   public void playChangeSound() {
      this.getChangeSound()
         .ifPresent(sound -> Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(sound.get(), sound.getFinalPitch(), sound.volume())));
   }

   @FunctionalInterface
   public interface OnPageChanged {
      void pageChanged(int var1, int var2);
   }
}
