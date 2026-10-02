package io.github.mortuusars.exposure.forge;

import java.util.List;
import java.util.Set;
import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** exposure-forge.mixins.json: mixins into other mods ({@code forge.mixin.<modid>.*}) apply only when that mod is present. */
public class ExposureForgeMixinPlugin implements IMixinConfigPlugin {
   private static final String MIXIN_PACKAGE = "io.github.mortuusars.exposure.forge.mixin.";

   @Override
   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      if (!mixinClassName.startsWith(MIXIN_PACKAGE)) {
         return true;
      }
      String rest = mixinClassName.substring(MIXIN_PACKAGE.length());
      int dot = rest.indexOf('.');
      if (dot < 0) {
         return true; // not in a mod sub-package
      }
      String modId = rest.substring(0, dot);
      return LoadingModList.get().getModFileById(modId) != null;
   }

   @Override
   public void onLoad(String mixinPackage) {
   }

   @Override
   public String getRefMapperConfig() {
      return null;
   }

   @Override
   public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
   }

   @Override
   public List<String> getMixins() {
      return null;
   }

   @Override
   public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }

   @Override
   public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }
}
