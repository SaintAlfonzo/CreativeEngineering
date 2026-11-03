package com.SaintAlfonzo.baby_mobs.client.family;

/** Every 26.1 baby remodel this jar ports (squid and dolphins keep their own renderers, client/Baby*Renderer). */
public final class BabyFamilies {
    private BabyFamilies() {}

    public static void registerAll() {
        FarmBabies.register();
        PetBabies.register();
        EquineBabies.register();
        MiscBabies.register();
        UndeadBabies.register();
    }
}
