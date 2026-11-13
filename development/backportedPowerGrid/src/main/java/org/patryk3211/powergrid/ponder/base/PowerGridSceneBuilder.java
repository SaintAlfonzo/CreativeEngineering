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
// Upstream extends Create 6's CreateSceneBuilder (accessor methods world(), overlay(), effects()). Create 0.5.1's
// SceneBuilder is a class with public instruction fields, so this is a wrapper around it with the Create 6 accessors the
// scenes call; the scenes keep upstream's text and calls. Create 6 API without a 0.5.1 counterpart: setNextUpEnabled
// (no "next up" list in 0.5.1, no-op). Create 6 showControls(Vec3, Pointing, int) returns the input element for chained
// rightClick() / withItem(); here it schedules a 0.5.1 InputWindowElement and returns it (same chain methods).
package org.patryk3211.powergrid.ponder.base;

import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.TextWindowElement;
import com.simibubi.create.foundation.ponder.instruction.EmitParticlesInstruction;
import com.simibubi.create.foundation.ponder.instruction.PonderInstruction;
import com.simibubi.create.foundation.ponder.instruction.ShowInputInstruction;
import com.simibubi.create.foundation.utility.Pointing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class PowerGridSceneBuilder {
    private final SceneBuilder base;
    private final ElectricInstructions electric;
    private final Overlay overlay;
    private final Effects effects;
    private boolean electricUsed = false;

    public PowerGridSceneBuilder(SceneBuilder baseSceneBuilder) {
        this.base = baseSceneBuilder;
        electric = new ElectricInstructions(baseSceneBuilder);
        overlay = new Overlay();
        effects = new Effects();
    }

    public ElectricInstructions electric() {
        electricUsed = true;
        return electric;
    }

    public SceneBuilder base() {
        return base;
    }

    public SceneBuilder.WorldInstructions world() {
        return base.world;
    }

    public Overlay overlay() {
        return overlay;
    }

    public Effects effects() {
        return effects;
    }

    public SceneBuilder.SpecialInstructions special() {
        return base.special;
    }

    public SceneBuilder.DebugInstructions debug() {
        return base.debug;
    }

    public void title(String sceneId, String title) {
        base.title(sceneId, title);
    }

    public void configureBasePlate(int xOffset, int zOffset, int basePlateSize) {
        base.configureBasePlate(xOffset, zOffset, basePlateSize);
    }

    public void scaleSceneView(float factor) {
        base.scaleSceneView(factor);
    }

    public void removeShadow() {
        base.removeShadow();
    }

    public void setSceneOffsetY(float yOffset) {
        base.setSceneOffsetY(yOffset);
    }

    public void showBasePlate() {
        base.showBasePlate();
    }

    public void addInstruction(PonderInstruction instruction) {
        base.addInstruction(instruction);
    }

    public void addInstruction(Consumer<PonderScene> callback) {
        base.addInstruction(callback);
    }

    public void idle(int ticks) {
        base.idle(ticks);
    }

    public void idleSeconds(int seconds) {
        base.idleSeconds(seconds);
    }

    public void rotateCameraY(float degrees) {
        base.rotateCameraY(degrees);
    }

    public void addKeyframe() {
        base.addKeyframe();
    }

    public void addLazyKeyframe() {
        base.addLazyKeyframe();
    }

    /** Create 6 only (the "next up" scene list); Create 0.5.1 has none. */
    public void setNextUpEnabled(boolean enabled) {
    }

    public void markAsFinished() {
        base.markAsFinished();
        if(electricUsed)
            electric.unload();
    }

    public class Overlay {
        public TextWindowElement.Builder showText(int duration) {
            return base.overlay.showText(duration);
        }

        public TextWindowElement.Builder showSelectionWithText(Selection selection, int duration) {
            return base.overlay.showSelectionWithText(selection, duration);
        }

        /** Create 6 name of 0.5.1 showSelectionWithText. */
        public TextWindowElement.Builder showOutlineWithText(Selection selection, int duration) {
            return base.overlay.showSelectionWithText(selection, duration);
        }

        public InputWindowElement showControls(Vec3 sceneSpace, Pointing direction, int duration) {
            var element = new InputWindowElement(sceneSpace, direction);
            base.addInstruction(new ShowInputInstruction(element, duration));
            return element;
        }

        public void showControls(InputWindowElement element, int duration) {
            base.overlay.showControls(element, duration);
        }

        public void chaseBoundingBoxOutline(PonderPalette color, Object slot, AABB boundingBox, int duration) {
            base.overlay.chaseBoundingBoxOutline(color, slot, boundingBox, duration);
        }

        public void showCenteredScrollInput(BlockPos pos, Direction side, int duration) {
            base.overlay.showCenteredScrollInput(pos, side, duration);
        }

        public void showScrollInput(Vec3 location, Direction side, int duration) {
            base.overlay.showScrollInput(location, side, duration);
        }

        public void showRepeaterScrollInput(BlockPos pos, int duration) {
            base.overlay.showRepeaterScrollInput(pos, duration);
        }

        public void showFilterSlotInput(Vec3 location, Direction side, int duration) {
            base.overlay.showFilterSlotInput(location, side, duration);
        }

        public void showLine(PonderPalette color, Vec3 start, Vec3 end, int duration) {
            base.overlay.showLine(color, start, end, duration);
        }

        public void showBigLine(PonderPalette color, Vec3 start, Vec3 end, int duration) {
            base.overlay.showBigLine(color, start, end, duration);
        }

        public void showOutline(PonderPalette color, Object slot, Selection selection, int duration) {
            base.overlay.showOutline(color, slot, selection, duration);
        }
    }

    public class Effects {
        public void emitParticles(Vec3 location, EmitParticlesInstruction.Emitter emitter, float amountPerCycle, int cycles) {
            base.effects.emitParticles(location, emitter, amountPerCycle, cycles);
        }

        /** Create 6 EffectInstructions.simpleParticleEmitter -> 0.5.1 Emitter.simple. */
        public <T extends ParticleOptions> EmitParticlesInstruction.Emitter simpleParticleEmitter(T data, Vec3 motion) {
            return EmitParticlesInstruction.Emitter.simple(data, motion);
        }

        /** Create 6 EffectInstructions.particleEmitterWithinBlockSpace -> 0.5.1 Emitter.withinBlockSpace. */
        public <T extends ParticleOptions> EmitParticlesInstruction.Emitter particleEmitterWithinBlockSpace(T data, Vec3 motion) {
            return EmitParticlesInstruction.Emitter.withinBlockSpace(data, motion);
        }

        public void superGlue(BlockPos pos, Direction side, boolean fullBlock) {
            base.effects.superGlue(pos, side, fullBlock);
        }

        public void rotationSpeedIndicator(BlockPos pos) {
            base.effects.rotationSpeedIndicator(pos);
        }

        public void rotationDirectionIndicator(BlockPos pos) {
            base.effects.rotationDirectionIndicator(pos);
        }

        public void indicateRedstone(BlockPos pos) {
            base.effects.indicateRedstone(pos);
        }

        public void indicateSuccess(BlockPos pos) {
            base.effects.indicateSuccess(pos);
        }

        public void createRedstoneParticles(BlockPos pos, int color, int amount) {
            base.effects.createRedstoneParticles(pos, color, amount);
        }
    }
}
