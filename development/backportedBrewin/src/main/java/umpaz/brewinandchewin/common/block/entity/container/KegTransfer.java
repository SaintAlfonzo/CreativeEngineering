package umpaz.brewinandchewin.common.block.entity.container;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.crafting.FluidIngredientWithAmount;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The JEI [+] button on the keg (alcohol.md 10.3; upstream's FermentingTransfer + FermentingTransferServer, rewritten).
 * One plan, worked out on copies of the keg grid, the player's inventory and a scratch tank, with the keg's own
 * one-item steps ({@link KegBlockEntity#fillFrom}, {@link KegBlockEntity#pourInto}):
 * <ol>
 *     <li>the grid (slots 0-3) goes back to the inventory;</li>
 *     <li>the ingredients move in: one set, or as many as fit for a shift-click;</li>
 *     <li>a tank holding a fluid the recipe doesn't use is drained into empty containers from the inventory;</li>
 *     <li>the recipe's base fluid is poured in from filled containers, up to one set (or as many as fit), never past
 *     a whole multiple of the recipe's amount, since the keg only ferments whole multiples;</li>
 *     <li>filled and emptied containers go back to the inventory.</li>
 * </ol>
 * The client runs it to grey out the button and name what's missing; the server runs it again on its own state and
 * applies the result in one go, so the packet carries only the recipe id. Unlike upstream, nothing happens unless the
 * whole plan works.
 */
public final class KegTransfer {
   public enum Error {
      NONE, TEMPERATURE, INVENTORY_FULL, MISSING, CANT_EMPTY, NOT_ENOUGH_FLUID
   }

   public static final class Plan {
      public Error error = Error.NONE;
      /** Indexes into the recipe's ingredient list with nothing to fill them. */
      public final List<Integer> missingIngredients = new ArrayList<>();
      public boolean missingFluid;
      final ItemStack[] grid = new ItemStack[GRID];
      List<ItemStack> inventory = new ArrayList<>();
      AbstractedFluidStack tank = AbstractedFluidStack.EMPTY;

      private Plan fail(Error error) {
         this.error = error;
         return this;
      }
   }

   private static final int GRID = 4;
   private static final int PLAYER_SLOTS = 36;
   private static final int MAX_SETS = 64;

   private KegTransfer() {
   }

   public static Plan plan(KegMenu menu, KegFermentingRecipe recipe, boolean maxTransfer) {
      Plan plan = new Plan();
      KegBlockEntity keg = menu.blockEntity;
      if (!KegBlockEntity.isValidTemp(menu.getKegTemperature(), recipe.getTemperature())) {
         return plan.fail(Error.TEMPERATURE);
      }

      List<ItemStack> inv = new ArrayList<>();
      for (int i = 0; i < PLAYER_SLOTS; i++) {
         inv.add(menu.getSlot(KegBlockEntity.INVENTORY_SIZE + i).getItem().copy());
      }
      for (int i = 0; i < GRID; i++) {
         plan.grid[i] = ItemStack.EMPTY;
         if (!stow(inv, menu.getSlot(i).getItem().copy())) {
            return plan.fail(Error.INVENTORY_FULL);
         }
      }

      // ingredients
      NonNullList<Ingredient> ingredients = recipe.getIngredients();
      int sets = 0;
      while (sets < (maxTransfer ? MAX_SETS : 1)) {
         List<ItemStack> invBefore = copy(inv);
         ItemStack[] gridBefore = plan.grid.clone();
         List<Integer> missing = new ArrayList<>();
         for (int i = 0; i < Math.min(GRID, ingredients.size()); i++) {
            Ingredient ingredient = ingredients.get(i);
            if (ingredient.isEmpty()) {
               continue;
            }
            if (!takeInto(inv, plan.grid, i, ingredient)) {
               missing.add(i);
            }
         }
         if (!missing.isEmpty()) {
            inv = invBefore;
            System.arraycopy(gridBefore, 0, plan.grid, 0, GRID);
            if (sets == 0) {
               plan.missingIngredients.addAll(missing);
            }
            break;
         }
         sets++;
      }

      // fluid
      AbstractedFluidTank tank = BrewinAndChewin.getHelper().createKegTank(menu.kegTank.getFluidCapacity(), () -> {
      });
      tank.setAbstractedFluid(menu.kegTank.getAbstractedFluid());
      Optional<FluidIngredientWithAmount> base = recipe.getFluidIngredient();
      List<ItemStack> returned = new ArrayList<>();
      AbstractedFluidStack held = tank.getAbstractedFluid();
      if (!held.isEmpty() && (base.isEmpty() || !base.get().ingredient().matches(held))) {
         for (ItemStack stack : inv) {
            while (!tank.isEmpty() && !stack.isEmpty()) {
               ItemStack filled = keg.fillFrom(tank, stack);
               if (filled == null) {
                  break;
               }
               stack.shrink(1);
               returned.add(filled);
            }
         }
         if (!tank.isEmpty()) {
            return plan.fail(Error.CANT_EMPTY);
         }
      }
      if (base.isPresent()) {
         Error fluidError = fill(keg, tank, inv, returned, base.get(), maxTransfer, plan);
         if (fluidError != Error.NONE && plan.error == Error.NONE) {
            plan.error = fluidError;
         }
      }
      if (!plan.missingIngredients.isEmpty() || plan.missingFluid) {
         plan.error = Error.MISSING;
      }
      if (plan.error != Error.NONE) {
         return plan;
      }
      for (ItemStack stack : returned) {
         if (!stow(inv, stack)) {
            return plan.fail(Error.INVENTORY_FULL);
         }
      }
      plan.inventory = inv;
      plan.tank = tank.getAbstractedFluid();
      return plan;
   }

   /**
    * Pours the base fluid in. A shift-click aims at the largest whole multiple that fits, then at smaller ones if the
    * containers on hand can't land on it exactly.
    */
   private static Error fill(KegBlockEntity keg, AbstractedFluidTank tank, List<ItemStack> inv, List<ItemStack> returned,
      FluidIngredientWithAmount base, boolean maxTransfer, Plan plan) {
      long unit = Math.max(1L, base.loaderAmount());
      long have = tank.getAbstractedFluid().amount();
      long target = maxTransfer ? tank.getFluidCapacity() / unit * unit : Math.max(unit, (have + unit - 1) / unit * unit);
      if (have > target) {
         return Error.CANT_EMPTY;
      }
      boolean anySource = false;
      while (target >= Math.max(unit, have)) {
         List<ItemStack> invTry = copy(inv);
         List<ItemStack> returnedTry = new ArrayList<>();
         AbstractedFluidTank tankTry = BrewinAndChewin.getHelper().createKegTank(tank.getFluidCapacity(), () -> {
         });
         tankTry.setAbstractedFluid(tank.getAbstractedFluid());
         long amount = have;
         for (ItemStack stack : invTry) {
            while (amount < target && !stack.isEmpty()) {
               AbstractedFluidStack fluid = keg.sourceFluid(stack);
               if (fluid.isEmpty() || !base.ingredient().matches(fluid)) {
                  break;
               }
               anySource = true;
               if (fluid.amount() > target - amount) {
                  break;
               }
               ItemStack container = keg.pourInto(tankTry, stack);
               if (container == null) {
                  break;
               }
               stack.shrink(1);
               if (!container.isEmpty()) {
                  returnedTry.add(container);
               }
               amount = tankTry.getAbstractedFluid().amount();
            }
         }
         if (amount == target) {
            inv.clear();
            inv.addAll(invTry);
            returned.addAll(returnedTry);
            tank.setAbstractedFluid(tankTry.getAbstractedFluid());
            return Error.NONE;
         }
         if (!maxTransfer) {
            break;
         }
         target -= unit; // the containers on hand overshoot or fall short of this multiple: try one set less
      }
      if (!anySource) {
         plan.missingFluid = true;
         return Error.NONE;
      }
      return Error.NOT_ENOUGH_FLUID;
   }

   /** Server: plans again on the server's own state and, if it all works, applies it. */
   public static boolean apply(ServerPlayer player, KegFermentingRecipe recipe, boolean maxTransfer) {
      if (!(player.containerMenu instanceof KegMenu menu) || !menu.stillValid(player)) {
         return false;
      }
      Plan plan = plan(menu, recipe, maxTransfer);
      if (plan.error != Error.NONE) {
         return false;
      }
      for (int i = 0; i < GRID; i++) {
         menu.getSlot(i).set(plan.grid[i]);
      }
      for (int i = 0; i < PLAYER_SLOTS; i++) {
         menu.getSlot(KegBlockEntity.INVENTORY_SIZE + i).set(plan.inventory.get(i));
      }
      menu.kegTank.setAbstractedFluid(plan.tank);
      KegBlockEntity keg = menu.blockEntity;
      keg.setChanged();
      if (keg.getLevel() != null) {
         keg.getLevel().sendBlockUpdated(keg.getBlockPos(), keg.getBlockState(), keg.getBlockState(), 2);
      }
      menu.broadcastChanges();
      return true;
   }

   /** Moves one item matching `ingredient` from the inventory into grid slot `slot`. */
   private static boolean takeInto(List<ItemStack> inv, ItemStack[] grid, int slot, Ingredient ingredient) {
      ItemStack current = grid[slot];
      for (ItemStack stack : inv) {
         if (stack.isEmpty() || !ingredient.test(stack)) {
            continue;
         }
         if (!current.isEmpty() && (!ItemStack.isSameItemSameTags(current, stack) || current.getCount() >= current.getMaxStackSize())) {
            continue;
         }
         if (current.isEmpty()) {
            grid[slot] = stack.split(1);
         } else {
            current.grow(1);
            stack.shrink(1);
         }
         return true;
      }
      return false;
   }

   /** Merges into matching stacks, then empty slots; false if it doesn't all fit. */
   private static boolean stow(List<ItemStack> inv, ItemStack stack) {
      if (stack.isEmpty()) {
         return true;
      }
      for (ItemStack slot : inv) {
         if (!slot.isEmpty() && ItemStack.isSameItemSameTags(slot, stack) && slot.getCount() < slot.getMaxStackSize()) {
            int moved = Math.min(stack.getCount(), slot.getMaxStackSize() - slot.getCount());
            slot.grow(moved);
            stack.shrink(moved);
            if (stack.isEmpty()) {
               return true;
            }
         }
      }
      for (int i = 0; i < inv.size(); i++) {
         if (inv.get(i).isEmpty()) {
            inv.set(i, stack.copy());
            stack.setCount(0);
            return true;
         }
      }
      return false;
   }

   private static List<ItemStack> copy(List<ItemStack> stacks) {
      List<ItemStack> out = new ArrayList<>(stacks.size());
      stacks.forEach(stack -> out.add(stack.copy()));
      return out;
   }
}
