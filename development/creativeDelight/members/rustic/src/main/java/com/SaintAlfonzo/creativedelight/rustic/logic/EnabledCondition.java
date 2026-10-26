package com.SaintAlfonzo.creativedelight.rustic.logic;

import com.SaintAlfonzo.creativedelight.rustic.RusticDelightMod;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

/**
 * Recipe condition {@code rusticdelight:config_boolean}. Upstream switches whole food families off from its config; the
 * port has no such config (the pack removes what it does not want through its scripts), so every family is on.
 */
public final class EnabledCondition implements ICondition {
    public static final ResourceLocation ID = RusticDelightMod.path("config_boolean");
    private final String setting;

    private EnabledCondition(String setting) {
        this.setting = setting;
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return true;
    }

    public static final class Serializer implements IConditionSerializer<EnabledCondition> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, EnabledCondition value) {
            json.addProperty("settingId", value.setting);
        }

        @Override
        public EnabledCondition read(JsonObject json) {
            return new EnabledCondition(json.has("settingId") ? json.get("settingId").getAsString() : "");
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
