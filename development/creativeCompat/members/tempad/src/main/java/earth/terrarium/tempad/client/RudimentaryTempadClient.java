// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/RudimentaryTempadClient.kt and
// ModEnums.kt. Translated to Java for Forge 1.19.2: NeoForge's EnumProxy (enum extension at load) -> Forge's
// IExtensibleEnum HumanoidModel.ArmPose.create at class init; RegisterClientExtensionsEvent -> the item's
// initializeClient (RudimentaryTempadItem), which runs only on the client, so this class never loads on a server.
package earth.terrarium.tempad.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class RudimentaryTempadClient implements IClientItemExtensions {
    /** Upstream ModEnums.rudimentaryTempadPos: both arms raised to the head's pitch, the off arm pulled in. */
    public static final HumanoidModel.ArmPose rudimentaryTempadPos = HumanoidModel.ArmPose.create("TEMPAD_RUDIMENTARY_TEMPAD", true, (model, entity, arm) -> {
        float it = Mth.clamp(model.head.xRot, -1.2f, 1.2f) - 1f;
        model.rightArm.xRot = it;
        model.leftArm.xRot = it;
        if (arm == HumanoidArm.RIGHT) {
            model.leftArm.xRot -= 0.55f;
            model.leftArm.yRot += 0.5f;
        } else {
            model.rightArm.xRot -= 0.55f;
            model.rightArm.yRot -= 0.5f;
        }
    });

    public static final RudimentaryTempadClient INSTANCE = new RudimentaryTempadClient();

    private RudimentaryTempadClient() {
    }

    /** Typed as the Forge interface so callers in common code do not resolve this class. */
    public static IClientItemExtensions get() {
        return INSTANCE;
    }

    @Override
    public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
        return rudimentaryTempadPos;
    }
}
