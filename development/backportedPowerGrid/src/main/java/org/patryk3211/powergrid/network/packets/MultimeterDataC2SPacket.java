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
package org.patryk3211.powergrid.network.packets;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.patryk3211.powergrid.electricity.wire.BaseWireEntity;
import org.patryk3211.powergrid.equipment.multimeter.MultimeterItem;
import org.patryk3211.powergrid.network.SimplePacket;

import java.util.function.Supplier;

public class MultimeterDataC2SPacket implements SimplePacket {
    private final Vec3 point; // Port: joml Vector3f (1.20) -> Vec3, sent as three floats
    private final int wire;

    public MultimeterDataC2SPacket(Vec3 point, BaseWireEntity wire) {
        this.point = point;
        this.wire = wire.getId();
    }

    public MultimeterDataC2SPacket(FriendlyByteBuf buf) {
        point = new Vec3(buf.readFloat(), buf.readFloat(), buf.readFloat());
        wire = buf.readInt();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat((float) point.x);
        buf.writeFloat((float) point.y);
        buf.writeFloat((float) point.z);
        buf.writeInt(wire);
    }

    @Override
    public void handle(Supplier<NetworkManager.PacketContext> context) {
        var ctx = context.get();
        ctx.queue(() -> {
            var player = ctx.getPlayer();
            var stack = player.getMainHandItem();
            if(!(stack.getItem() instanceof MultimeterItem multimeter))
                return;
            var level = player.level;
            var entity = level.getEntity(wire);
            if(entity == null)
                return;
            if(multimeter.getMode(stack) != 1)
                multimeter.setMode(stack, 1);
            var data = multimeter.getModeData(stack);
            data.putFloat("X", (float) point.x);
            data.putFloat("Y", (float) point.y);
            data.putFloat("Z", (float) point.z);
            data.putUUID("UUID", entity.getUUID());
        });
    }
}
