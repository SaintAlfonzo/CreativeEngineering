/*
 * Copyright 2025 patryk3211
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Modified for the Forge 1.19.2 / Create 0.5.1 port (backportedPowerGrid, 2026); changes listed in PORT_NOTES.md.
package org.patryk3211.powergrid.equipment.portablebattery;

import com.simibubi.create.AllEnchantments;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.patryk3211.powergrid.collections.ModdedConfigs;
import org.patryk3211.powergrid.collections.ModdedSoundEvents;
import org.patryk3211.powergrid.equipment.ItemBoostUtils;
import org.patryk3211.powergrid.utility.ClientSideAccess;
import org.patryk3211.powergrid.utility.Lang;

public class BatteryUtils {
    public static int getMaxCharge(ItemStack stack) {
        return getMaxCharge(EnchantmentHelper.getItemEnchantmentLevel(AllEnchantments.CAPACITY.get(), stack));
    }

    public static int getMaxCharge(int level) {
        return ModdedConfigs.server().equipment.portableBatteryBaseCapacity.get() +
                ModdedConfigs.server().equipment.portableBatteryEnchantCapacity.get() * level;
    }

    public static int getCurrentCharge(ItemStack stack) {
        if(!stack.hasTag())
            return 0;
        var nbt = stack.getTag();
        return nbt.getInt("Charge");
    }

    public static float drawEnergy(Player player, int energy) {
        if(player.getAbilities().instabuild)
            return 1.0f;
        var stack = player.getItemBySlot(EquipmentSlot.CHEST);
        if(stack.isEmpty() || !(stack.getItem() instanceof PortableBatteryItem))
            return 0.0f;
        var charge = getCurrentCharge(stack);
        float chargePercent = (float) getCurrentCharge(stack) / getMaxCharge(stack);
        float outputPercent = 1.0f;
        if(chargePercent < 0.5f) {
            // High ESR causing lower energy output
            outputPercent = chargePercent / 0.5f;
            if(outputPercent < 0.25f)
                outputPercent = 0.25f;
        }
        var tag = stack.getTag();
        if(energy == 0 || tag == null)
            return 0.0f;
        // Port fix B20 (power_grid.md 3.5): upstream zeroed the charge and returned 0 when the charge was below the
        // request, so a request larger than the charge lost the charge and delivered nothing; a draw that cannot be
        // met now leaves the charge as it is (callers ask at most the charge: PortableBatteryItemImpl.tryTransferPower)
        if(charge < energy)
            return 0.0f;
        tag.putInt("Charge", charge - energy);
        if(player instanceof ServerPlayer serverPlayer) {
            float maxCharge = getMaxCharge(stack);
            sendWarning(serverPlayer, charge, charge - energy, (maxCharge / 10));
            sendWarning(serverPlayer, charge, charge - energy, 25);
        }
        return outputPercent;
    }

    /**
     * Energy a {@link #drawEnergy} call can take from the worn battery now: its charge, or no limit in creative
     * (where drawEnergy draws nothing). Port (B20): the worn battery's FE transfer caps its request with this.
     */
    public static int availableEnergy(Player player) {
        if(player.getAbilities().instabuild)
            return Integer.MAX_VALUE;
        var stack = getBattery(player);
        return stack == null ? 0 : getCurrentCharge(stack);
    }

    public static ItemStack getBattery(Player player) {
        var stack = player.getItemBySlot(EquipmentSlot.CHEST);
        if(!(stack.getItem() instanceof PortableBatteryItem))
            return null;
        return stack;
    }

    public static float tryDrawEnergy(ItemStack battery, int energy) {
        var charge = getCurrentCharge(battery);
        float chargePercent = (float) getCurrentCharge(battery) / getMaxCharge(battery);
        float outputPercent = 1.0f;
        if(chargePercent < 0.5f) {
            // High ESR causing lower energy output
            outputPercent = chargePercent / 0.5f;
            if(outputPercent < 0.25f)
                outputPercent = 0.25f;
        }
        var tag = battery.getTag();
        if(energy == 0 || tag == null)
            return 0.0f;
        if(charge < energy)
            return 0.0f;
        return outputPercent;
    }

    private static void sendWarning(ServerPlayer player, float charge, float newCharge, float threshold) {
        if (newCharge > threshold)
            return;
        if (charge <= threshold)
            return;

        boolean depleted = threshold <= 25;
        MutableComponent component = Lang.translateDirect(depleted ? "gui.portable.battery.depleted" : "gui.portable.battery.low");

        ModdedSoundEvents.UI_FAIL.play(player.level, null, player.blockPosition(), .75f, 1);
        ModdedSoundEvents.WIRE_BURNED.play(player.level, null, player.blockPosition(), 1, .5f);

        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 40, 10));
        player.connection.send(new ClientboundSetSubtitleTextPacket(
                Component.literal("\u26A0 ").withStyle(depleted ? ChatFormatting.RED : ChatFormatting.GOLD)
                        .append(component.withStyle(ChatFormatting.GRAY))));
        player.connection.send(new ClientboundSetTitleTextPacket(CommonComponents.EMPTY));
    }

    public static boolean isBarVisible(ItemStack stack, int energyPerUse, float minPower) {
        if(energyPerUse == 0)
            return false;
        return EnvExecutor.getInEnv(Env.CLIENT, () -> ClientSideAccess::player)
                .map(player -> {
                    var battery = getBattery(player);
                    if(battery != null && tryDrawEnergy(battery, energyPerUse) > minPower)
                        return true;
                    return stack.isDamaged();
                }).orElse(false);
    }

    public static int getBarWidth(ItemStack stack, int energyPerUse, float minPower) {
        if(energyPerUse == 0)
            return 13;
        return EnvExecutor.getInEnv(Env.CLIENT, () -> ClientSideAccess::player)
                .map(player -> {
                    var battery = getBattery(player);
                    if(battery == null || tryDrawEnergy(battery, energyPerUse) <= minPower)
                        return Math.round(13.0F - (float) stack.getDamageValue() / stack.getMaxDamage() * 13.0F);
                    return battery.getBarWidth();
                }).orElse(13);
    }

    public static int getBarColor(ItemStack stack, int energyPerUse, float minPower) {
        if(energyPerUse == 0)
            return 0;
        return EnvExecutor.getInEnv(Env.CLIENT, () -> ClientSideAccess::player)
                .map(player -> {
                    var battery = getBattery(player);
                    if(battery == null || tryDrawEnergy(battery, energyPerUse) <= minPower)
                        return Mth.hsvToRgb(Math.max(0.0F, 1.0F - (float) stack.getDamageValue() / stack.getMaxDamage()) / 3.0F, 1.0F, 1.0F);
                    if(ItemBoostUtils.isBoosted(stack))
                        return 0x34a8eb;
                    return battery.getBarColor();
                }).orElse(0);
    }

    public static boolean isBarVisible(ItemStack stack, int energyPerUse) {
        return isBarVisible(stack, energyPerUse, 0);
    }

    public static int getBarWidth(ItemStack stack, int energyPerUse) {
        return getBarWidth(stack, energyPerUse, 0);
    }

    public static int getBarColor(ItemStack stack, int energyPerUse) {
        return getBarColor(stack, energyPerUse, 0);
    }
}
