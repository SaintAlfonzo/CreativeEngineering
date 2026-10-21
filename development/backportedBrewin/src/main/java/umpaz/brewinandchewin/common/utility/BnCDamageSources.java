package umpaz.brewinandchewin.common.utility;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;

/**
 * Port: 1.19.2 has no damage type registry. cardiac_arrest is a DamageSource with upstream's flags (bypasses armor,
 * shields, effects and enchantments); its message id keeps the lang keys. The `triggers_raging` damage type tag
 * (mob_attack, player_attack) is the melee check below.
 */
public final class BnCDamageSources {
   public static final String CARDIAC_ARREST_ID = "brewinandchewin.cardiacArrest";
   public static final DamageSource CARDIAC_ARREST = new DamageSource(CARDIAC_ARREST_ID).bypassArmor().bypassMagic();

   private BnCDamageSources() {
   }

   public static boolean isCardiacArrest(DamageSource source) {
      return CARDIAC_ARREST_ID.equals(source.getMsgId());
   }

   public static boolean triggersRaging(DamageSource source) {
      return source instanceof EntityDamageSource
         && !(source instanceof IndirectEntityDamageSource)
         && ("mob".equals(source.getMsgId()) || "player".equals(source.getMsgId()));
   }
}
