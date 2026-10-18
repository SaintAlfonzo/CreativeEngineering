package umpaz.brewinandchewin.client.renderer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.registries.ForgeRegistries;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.renderer.texture.BnCTextureModifiers;
import umpaz.brewinandchewin.client.renderer.texture.modifier.TextureModifier;
import umpaz.brewinandchewin.common.block.CoasterBlock;
import umpaz.brewinandchewin.common.block.entity.CoasterBlockEntity;

/**
 * Up to four items on a coaster, each with its own small model from {@code assets/<ns>/brewinandchewin/coaster/*.json}
 * ({"item", "models": id | [{"model", "texture_modifiers"}]}); items without one render as a flat item.
 * <p>
 * Port: the models are registered as extra models (ModelEvent.RegisterAdditional) and their quads drawn straight into the
 * buffer with the modifier tint, in place of upstream's per-tint-index wrapped model and block colour handler.
 */
public class CoasterBlockEntityRenderer implements BlockEntityRenderer<CoasterBlockEntity> {
   private static final String DIRECTORY = "brewinandchewin/coaster";
   private static final Map<ResourceLocation, List<ModelEntry>> ITEM_TO_MODELS = new HashMap<>();
   private static final Set<ResourceLocation> ERRONEOUS_ENTRIES = new HashSet<>();
   public static final ResourceLocation COASTER_MODEL = BrewinAndChewin.asResource("block/coaster");
   public static final ResourceLocation COASTER_TRAY_MODEL = BrewinAndChewin.asResource("block/coaster_tray");

   public CoasterBlockEntityRenderer(Context context) {
   }

   public static List<ModelEntry> getModelEntries(ResourceLocation itemId) {
      return ITEM_TO_MODELS.get(itemId);
   }

   /** Reads every coaster JSON and returns the models to register; called while models load. */
   public static List<ResourceLocation> loadModelEntries(ResourceManager manager) {
      ITEM_TO_MODELS.clear();
      ERRONEOUS_ENTRIES.clear();
      List<ResourceLocation> models = new ArrayList<>();
      for (Entry<ResourceLocation, Resource> entry : manager.listResources(DIRECTORY, path -> path.getPath().endsWith(".json")).entrySet()) {
         try (Reader reader = entry.getValue().openAsReader()) {
            JsonObject json = GsonHelper.convertToJsonObject(JsonParser.parseReader(reader), "coaster model");
            ResourceLocation itemId = new ResourceLocation(GsonHelper.getAsString(json, "item"));
            List<ModelEntry> entries = parseModels(json.get("models"));
            ITEM_TO_MODELS.put(itemId, entries);
            entries.forEach(modelEntry -> models.add(modelEntry.model()));
         } catch (Exception e) {
            BrewinAndChewin.LOG.error("Unexpected error in Brewin' And Chewin' coaster model JSON \"{}\". {}", entry.getKey(), e.toString());
         }
      }
      return models;
   }

   private static List<ModelEntry> parseModels(JsonElement json) {
      if (json.isJsonPrimitive()) {
         return List.of(new ModelEntry(new ResourceLocation(json.getAsString()), List.of()));
      }
      List<ModelEntry> entries = new ArrayList<>();
      for (JsonElement element : (JsonArray)json) {
         JsonObject object = element.getAsJsonObject();
         List<TextureModifier> modifiers = new ArrayList<>();
         for (JsonElement modifierJson : GsonHelper.getAsJsonArray(object, "texture_modifiers", new JsonArray())) {
            ResourceLocation type = new ResourceLocation(GsonHelper.getAsString(modifierJson.getAsJsonObject(), "type"));
            TextureModifier modifier = BnCTextureModifiers.create(type);
            if (modifier == null) {
               throw new JsonParseException("Could not find texture modifier type " + type);
            }
            modifiers.add(modifier);
         }
         entries.add(new ModelEntry(new ResourceLocation(GsonHelper.getAsString(object, "model")), modifiers));
      }
      return entries;
   }

   private void poseUtil(PoseStack poseStack, int fullCount, int curCount, RandomSource seededRandom, boolean invisible) {
      float tx;
      float tz;
      float rotation;
      switch (fullCount) {
         case 2 -> {
            tx = curCount == 0 ? -0.2F : 0.2F;
            tz = curCount == 0 ? -0.15F : 0.15F;
            rotation = curCount == 0 ? 190.0F : 10.0F;
         }
         case 3 -> {
            tx = curCount == 0 ? 0.05F : (curCount == 1 ? -0.25F : 0.25F);
            tz = curCount == 0 ? 0.25F : (curCount == 1 ? -0.15F : -0.25F);
            rotation = curCount == 0 ? -20.0F : (curCount == 1 ? 220.0F : 100.0F);
         }
         case 4 -> {
            tx = curCount == 0 ? 0.2F : (curCount == 1 ? -0.25F : (curCount == 2 ? 0.25F : -0.2F));
            tz = curCount == 0 ? 0.25F : (curCount == 1 ? 0.2F : (curCount == 2 ? -0.2F : -0.25F));
            rotation = curCount == 0 ? -5.0F : (curCount == 1 ? 265.0F : (curCount == 2 ? 85.0F : 175.0F));
         }
         default -> {
            tx = 0.0F;
            tz = 0.0F;
            rotation = 0.0F;
         }
      }
      rotateAround(poseStack, rotation + seededRandom.nextFloat() * 20.0F - 10.0F, 0.5F + tx, 0.5F + tz);
      poseStack.translate(tx, invisible ? 0.0 : 0.0625, tz);
   }

   private static void rotateAround(PoseStack poseStack, float degrees, float x, float z) {
      poseStack.translate(x, 0.0, z);
      poseStack.mulPose(Vector3f.YP.rotationDegrees(degrees));
      poseStack.translate(-x, 0.0, -z);
   }

   @Override
   public void render(CoasterBlockEntity entity, float tickDelta, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
      int count = (int)entity.getItems().stream().filter(ix -> !ix.isEmpty()).count();
      rotateAround(poseStack, -22.5F * (float)entity.getBlockState().getValue(CoasterBlock.ROTATION), 0.5F, 0.5F);
      RandomSource random = new LegacyRandomSource(entity.getBlockPos().asLong());
      boolean invisible = entity.getBlockState().getValue(CoasterBlock.INVISIBLE);
      if (!invisible || count == 0) {
         ResourceLocation modelId = entity.getBlockState().getValue(CoasterBlock.SIZE) > 1 ? COASTER_TRAY_MODEL : COASTER_MODEL;
         renderModel(modelId, poseStack, buffer.getBuffer(RenderType.cutout()), random, -1, combinedLight, combinedOverlay);
      }

      for (int i = 0; i < count; i++) {
         ItemStack stack = entity.getItems().get(i);
         List<ModelEntry> modelEntries = getModelEntries(ForgeRegistries.ITEMS.getKey(stack.getItem()));
         poseStack.pushPose();
         this.poseUtil(poseStack, count, i, random, invisible);
         if (modelEntries == null) {
            poseStack.translate(0.51, 0.05, 0.5);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            poseStack.scale(0.5F, 0.5F, 0.5F);
            Minecraft.getInstance().getItemRenderer()
               .renderStatic(stack, ItemTransforms.TransformType.FIXED, combinedLight, combinedOverlay, poseStack, buffer, (int)entity.getBlockPos().asLong());
         } else {
            for (ModelEntry modelEntry : modelEntries) {
               if (!checkModel(modelEntry.model())) {
                  continue;
               }
               int color = -1;
               RenderType renderType = RenderType.cutout();
               for (TextureModifier modifier : modelEntry.modifiers()) {
                  color = modifier.color(entity.getLevel(), entity.getBlockState(), entity.getBlockPos(), stack, color);
                  renderType = modifier.renderType(entity.getLevel(), entity.getBlockState(), entity.getBlockPos(), stack, renderType);
               }
               renderModel(modelEntry.model(), poseStack, buffer.getBuffer(renderType), random, color, combinedLight, combinedOverlay);
            }
         }

         poseStack.popPose();
      }
   }

   private static void renderModel(ResourceLocation modelId, PoseStack poseStack, VertexConsumer consumer, RandomSource random, int color, int light, int overlay) {
      BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelId);
      float red = color == -1 ? 1.0F : (float)(color >> 16 & 0xFF) / 255.0F;
      float green = color == -1 ? 1.0F : (float)(color >> 8 & 0xFF) / 255.0F;
      float blue = color == -1 ? 1.0F : (float)(color & 0xFF) / 255.0F;
      PoseStack.Pose pose = poseStack.last();
      for (Direction direction : Direction.values()) {
         random.setSeed(42L);
         for (BakedQuad quad : model.getQuads(null, direction, random, ModelData.EMPTY, null)) {
            consumer.putBulkData(pose, quad, red, green, blue, light, overlay);
         }
      }
      random.setSeed(42L);
      for (BakedQuad quad : model.getQuads(null, null, random, ModelData.EMPTY, null)) {
         consumer.putBulkData(pose, quad, red, green, blue, light, overlay);
      }
   }

   public static boolean checkModel(ResourceLocation path) {
      BakedModel model = Minecraft.getInstance().getModelManager().getModel(path);
      if (model == Minecraft.getInstance().getModelManager().getMissingModel()) {
         if (ERRONEOUS_ENTRIES.add(path)) {
            BrewinAndChewin.LOG.error("Failed to get model '{}'", path);
         }
         return false;
      }
      return true;
   }

   public record ModelEntry(ResourceLocation model, List<? extends TextureModifier> modifiers) {
   }
}
