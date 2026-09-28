# Sourced by tools/test_*.sh: sets CP to the classpath for headless patcherMod tests (dev/Mojmap names:
# the mapped Forge jar + patcherMod/build/classes + what Bootstrap.bootStrap() needs). Expects ROOT, L, C.
MAPPED="$HOME/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.19.2-43.3.9_mapped_official_1.19.2/forge-1.19.2-43.3.9_mapped_official_1.19.2.jar"
CP="$MAPPED:$ROOT/patcherMod/build/classes/java/main"
for a in com.mojang/datafixerupper it.unimi.dsi/fastutil com.google.guava/guava com.google.code.gson/gson org.apache.logging.log4j/log4j-api \
         org.slf4j/slf4j-api com.mojang/logging com.mojang/brigadier com.google.guava/failureaccess org.apache.commons/commons-lang3; do
  CP="$CP:$(find "$C/$a" -name '*.jar' ! -name '*sources*' | sort | tail -1)"
done
for j in io/netty/netty-{buffer,codec,common,handler,resolver,transport}/4.1.77.Final com/mojang/javabridge/1.2.24 cpw/mods/securejarhandler/2.1.4 \
         cpw/mods/modlauncher/10.0.9 net/minecraftforge/coremods/5.0.1 com/electronwill/night-config/core/3.6.4 com/electronwill/night-config/toml/3.6.4 \
         org/apache/maven/maven-artifact/3.8.5 net/minecraftforge/unsafe/0.2.0 net/minecraftforge/eventbus/6.0.5 net/jodah/typetools/0.8.3 \
         org/ow2/asm/asm/9.3 org/ow2/asm/asm-tree/9.3 org/ow2/asm/asm-commons/9.3 com/mojang/authlib/3.11.49 net/minecraftforge/forgespi/6.0.0 \
         net/minecraftforge/{fmlcore,fmlloader,javafmllanguage,lowcodelanguage,mclanguage}/1.19.2-43.3.9; do
  CP="$CP:$(ls "$L/$j"/*.jar | grep -v sources | head -1)"
done
