package com.SaintAlfonzo.baby_mobs.age;

/** Added to {@code Mob} by {@code mixin/MobBabyMixin}: the server-side age of a mob this mod gives a baby form. */
public interface BabyAgeHolder {
    int babyMobs$getAge();

    void babyMobs$setAgeRaw(int age);
}
