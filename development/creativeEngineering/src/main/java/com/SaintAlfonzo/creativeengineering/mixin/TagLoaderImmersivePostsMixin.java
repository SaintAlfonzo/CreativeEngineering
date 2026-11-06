package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.compat.ImmersivePostsMaterials;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

/**
 * Immersive Posts lists every post and truss in its own {@code mineable/pickaxe}, {@code needs_stone_tool} and
 * {@code needs_iron_tool} files. A required entry that names an unregistered block makes 1.19.2 drop the whole
 * tag, so the materials {@link ImmersivePostsRegistrationMixin} leaves out would take those vanilla tags down
 * with them. Their entries are removed from the loaded tag files before the tags are built.
 */
@Mixin(value = TagLoader.class, remap = false)
public abstract class TagLoaderImmersivePostsMixin {

    @Inject(method = "m_144495_(Lnet/minecraft/server/packs/resources/ResourceManager;)Ljava/util/Map;", // load
            at = @At("RETURN"), remap = false)
    private void creativeengineering$dropUnregisteredPosts(CallbackInfoReturnable<Map<ResourceLocation, List<TagLoader.EntryWithSource>>> cir) {
        for (List<TagLoader.EntryWithSource> entries : cir.getReturnValue().values()) {
            // TagEntry.toString() is "#id" for a tag and ends in '?' when optional; its fields are private
            entries.removeIf(e -> ImmersivePostsMaterials.isDroppedBlock(e.entry().toString()));
        }
    }
}
