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
package org.patryk3211.powergrid.mixin;

import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.patryk3211.powergrid.collections.ModdedItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

// Port: remap off (Create's own methods); fix B25: an output that is not a belt is skipped instead of ending the scan.
@Mixin(value = ArmBlockEntity.class, remap = false)
public abstract class ArmBlockEntityMixin {
    @Shadow List<ArmInteractionPoint> outputs;

    @Inject(method = "tick()V", at = @At(
            value = "INVOKE",
            shift = At.Shift.AFTER,
            target = "Lcom/simibubi/create/content/kinetics/mechanicalArm/ArmBlockEntity;tickMovementProgress()Z"))
    private void powerGrid$tickLockCircuitOnBelt(CallbackInfo ci) {
        for(var point : outputs) {
            var beltBE = BeltHelper.getSegmentBE(point.getLevel(), point.getPos());
            if (beltBE == null)
                continue;
            var transport = beltBE.getBehaviour(TransportedItemStackHandlerBehaviour.TYPE);
            if (transport == null)
                continue;
            var found = new MutableBoolean(false);
            transport.handleCenteredProcessingOnAllItems(0.05f, tis -> {
                if (found.isTrue())
                    return TransportedItemStackHandlerBehaviour.TransportedResult.doNothing();
                if (ModdedItems.INCOMPLETE_CIRCUIT.isIn(tis.stack)) {
                    tis.lockedExternally = true;
                    found.setTrue();
                }
                return TransportedItemStackHandlerBehaviour.TransportedResult.doNothing();
            });
        }
    }
}
