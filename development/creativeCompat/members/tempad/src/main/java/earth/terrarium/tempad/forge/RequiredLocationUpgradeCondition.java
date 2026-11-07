// Port glue (ours, GPLv3), TP-D5: stands in for upstream Tempad 3.0.4's feature flag "required_location_upgrade" on
// recipes. Upstream marks the New Location Upgrade item requiredFeatures(flag), so its recipes load only while the flag
// is on; here the translator adds this condition to them and it reads the config require_location_upgrade.
package earth.terrarium.tempad.forge;

import com.google.gson.JsonObject;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.common.config.CommonConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public final class RequiredLocationUpgradeCondition implements ICondition {
    public static final ResourceLocation ID = Tempad.id("required_location_upgrade");
    public static final RequiredLocationUpgradeCondition INSTANCE = new RequiredLocationUpgradeCondition();

    private RequiredLocationUpgradeCondition() {
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return CommonConfig.SPEC.isLoaded() && CommonConfig.requireLocationUpgrade();
    }

    public static final IConditionSerializer<RequiredLocationUpgradeCondition> SERIALIZER = new IConditionSerializer<>() {
        @Override
        public void write(JsonObject json, RequiredLocationUpgradeCondition value) {
        }

        @Override
        public RequiredLocationUpgradeCondition read(JsonObject json) {
            return INSTANCE;
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    };
}
