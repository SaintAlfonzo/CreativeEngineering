// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/showblock/ShowBlockScreen.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.showblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yuushya.modelling.gui.compat.ButtonUtils;
import com.yuushya.modelling.block.blockstate.YuushyaBlockStates;
import com.yuushya.modelling.blockentity.BlockShape;
import com.yuushya.modelling.blockentity.showblock.ShowBlockEntity;
import com.yuushya.modelling.blockentity.transformData.TransformBlockData;
import com.yuushya.modelling.blockentity.transformData.TransformType;
import com.yuushya.modelling.gui.engrave.EngraveBlockResultLoader;
import com.yuushya.modelling.gui.validate.DividedDoubleRange;
import com.yuushya.modelling.gui.validate.DoubleRange;
import com.yuushya.modelling.gui.validate.LazyDoubleRange;
import com.yuushya.modelling.gui.widget.BlockStateIconList;
import com.yuushya.modelling.gui.widget.TransformComponent;
import com.yuushya.modelling.item.YuushyaDebugStickItem;
import com.yuushya.modelling.item.showblocktool.PosTransItem;
import com.yuushya.modelling.network.TransformDataOncePacket;
import com.yuushya.modelling.utils.ShareUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import com.yuushya.modelling.gui.compat.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import com.yuushya.modelling.gui.compat.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

public class ShowBlockScreen extends Screen {
   public static final int PER_HEIGHT = 20;
   public static final int SMALL_BUTTON_WIDTH = 10;
   private static final int TOP = 10;
   private static final int RIGHT_COLUMN_X = 2;
   private static final int RIGHT_BAR_WIDTH = 20;
   private static final int RIGHT_LIST_WIDTH = 40;
   private static final int RIGHT_LIST_PER_HEIGHT = 45;
   private static final int RIGHT_LIST_TOP = 35;
   private static final int RIGHT_LIST_HEIGHT = 137;
   private static final int RIGHT_LIST_BOTTOM = 172;
   private static final int RIGHT_STATE_PANEL_Y = 177;
   private static final int RIGHT_STATE_INFORM_X = 45;
   private static final RandomSource random = RandomSource.create();
   private final ShowBlockEntity blockEntity;
   private final BlockState newBlockState;
   private final Map<TransformType, Double> storage = new HashMap<>();
   private final Map<TransformType, TransformComponent> panel = new LinkedHashMap<>();
   private final Map<TransformType, EditBox> editBoxes = new HashMap<>();
   private long faceCount = 0L;
   private int slot;
   private CycleButton<ShowBlockScreen.Mode> modeButton;
   private CycleButton<Boolean> shownStateButton;
   private BlockStateIconList blockStateList;
   private Button leftPropertyButton;
   private Property<?> property;
   private Button rightPropertyButton;
   private Button leftStateButton;
   private Button rightStateButton;

   public ShowBlockScreen(ShowBlockEntity blockEntity, BlockState newBlockState) {
      super(GameNarrator.NO_TITLE);
      this.blockEntity = blockEntity;
      this.newBlockState = newBlockState;
      if (blockEntity.getSlot() < blockEntity.getTransformData().size()) {
         this.slot = blockEntity.getSlot();
      }
   }

   private static int top(int i, int offset) {
      return 40 + 20 * i + offset;
   }

   public static long calculateFaces(ShowBlockEntity showBlockEntity) {
      BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
      ArrayList<Direction> directions = new ArrayList<>(Arrays.asList(Direction.values()));
      directions.add(null);
      return (long)showBlockEntity.getTransformData().stream().mapToInt(data -> {
         int countFace = 0;
         BlockState blockState = data.blockState;
         BakedModel blockModel = blockRenderDispatcher.getBlockModel(blockState);

         for (Direction value : directions) {
            List<BakedQuad> blockModelQuads = blockModel.getQuads(blockState, value, random);
            countFace += blockModelQuads.size();
         }

         return countFace;
      }).sum();
   }

   public void setSlot(int slot) {
      for (TransformType key : this.storage.keySet()) {
         TransformDataOncePacket.sendToServerSide(this.blockEntity.getBlockPos(), this.slot, key, this.storage.get(key));
      }

      this.storage.clear();
      this.slot = slot;
      this.blockEntity.setSlot(slot);

      for (TransformComponent component : this.panel.values()) {
         component.setSliderInitial(this.blockEntity, this.slot);
      }

      this.shownStateButton.setValue(this.blockEntity.getTransformData(slot).isShown);
      this.updateStateButtonVisible(true);
   }

   private TransformComponent choose(TransformType type) {
      return this.panel.computeIfAbsent(type, TransformComponent::new);
   }

   public boolean updateStateButtonVisible(boolean force) {
      Collection<Property<?>> collection = this.blockStateList.updateRenderProperties(this.getBlockState());
      boolean stateButtonVisible = !collection.isEmpty();
      if (stateButtonVisible && (this.property == null || force)) {
         this.property = collection.iterator().next();
      }

      this.leftStateButton.visible = stateButtonVisible;
      this.rightStateButton.visible = stateButtonVisible;
      this.leftPropertyButton.visible = stateButtonVisible;
      this.rightPropertyButton.visible = stateButtonVisible;
      return stateButtonVisible;
   }

   public BlockState getBlockState() {
      return this.blockEntity.getTransformData(this.slot).blockState;
   }

   private int leftColumnX() {
      return this.width / 4 * 3 + 10;
   }

   private int leftColumnWidth() {
      return this.width / 4 - 20;
   }

   protected void init() {
      if (this.minecraft != null) {
         Button addStateButton = ButtonUtils.builder(Component.literal("+"), btn -> {
            int chosen = this.blockStateList.getChosenOne();
            if (chosen != -1) {
               this.blockStateList.addSlot();
               this.blockEntity.getTransformData().add(new TransformBlockData());
               this.updateTransformDataServerImmediate(this.blockEntity.getTransformData(chosen), this.slot);
               TransformDataOncePacket.sendToServerSideSuccess(this.blockEntity.getBlockPos());
               this.updateStateButtonVisible(true);
            } else if (this.newBlockState != null) {
               this.blockStateList.addSlot();
               this.updateTransformData(TransformType.BLOCK_STATE, (double)Block.getId(this.newBlockState));
               this.updateTransformData(TransformType.SHOWN, 1.0);
               this.updateStateButtonVisible(true);
            }
         }).tooltip(Tooltip.create(Component.translatable("gui.showBlockScreen.display.add"))).bounds(2, 10, 20, 20).build();
         Button removeStateButton = ButtonUtils.builder(Component.literal("×"), btn -> {
            this.updateTransformData(TransformType.REMOVE, 0.0);
            this.updateStateButtonVisible(true);
         }).tooltip(Tooltip.create(Component.translatable("gui.showBlockScreen.display.remove"))).bounds(22, 10, 20, 20).build();
         Button replaceButton = ButtonUtils.builder(Component.literal("⇄"), btn -> {
            int chosen = this.blockStateList.getChosenOne();
            if (chosen != -1 && chosen != this.slot) {
               this.updateTransformData(TransformType.BLOCK_STATE, (double)Block.getId(this.blockEntity.getTransformData(chosen).blockState));
               this.updateStateButtonVisible(true);
            } else {
               this.updateTransformData(TransformType.BLOCK_STATE, (double)Block.getId(this.newBlockState));
               this.updateStateButtonVisible(true);
            }
         }).tooltip(Tooltip.create(Component.translatable("gui.showBlockScreen.display.replace"))).bounds(42, 10, 20, 20).build();
         this.shownStateButton = CycleButton.booleanBuilder(Component.literal("\ud83d\udd76"), Component.literal("\ud83d\udc40"))
            .displayOnlyValue()
            .withInitialValue(true)
            .withTooltip(
               on -> Tooltip.lines(on ? Component.translatable("gui.showBlockScreen.display.on") : Component.translatable("gui.showBlockScreen.display.off"))
            )
            .create(62, 10, 20, 20, Component.empty(), (btn, bl) -> this.updateTransformData(TransformType.SHOWN, bl ? 1.0 : 0.0));
         Button copyButton = ButtonUtils.builder(
               Component.literal("\ud83d\udce4").withStyle(ChatFormatting.BOLD),
               btn -> {
                  String res = ShareUtils.transfer(this.blockEntity.getTransformData());
                  this.setClipboard(res);
                  this.minecraft
                     .getToasts()
                     .addToast(
                        SystemToast.multiline(
                           this.minecraft,
                           SystemToastIds.NARRATOR_TOGGLE,
                           Component.translatable("gui.showBlockScreen.workshop.copy_pass"),
                           Component.translatable("gui.showBlockScreen.workshop.share_hint")
                        )
                     );
               }
            )
            .tooltip(Tooltip.create(Component.translatable("gui.showBlockScreen.workshop.copy")))
            .bounds(102, 10, 20, 20)
            .build();
         Button parseButton = ButtonUtils.builder(
               Component.literal("\ud83d\udce5").withStyle(ChatFormatting.BOLD),
               btn -> {
                  String string = this.getClipboard();

                  try {
                     ShareUtils.ShareBlockInformation shareBlockInformation = ShareUtils.from(string);
                     if (shareBlockInformation.blocks().isEmpty()) {
                        this.minecraft
                           .getToasts()
                           .addToast(
                              SystemToast.multiline(
                                 this.minecraft,
                                 SystemToastIds.PACK_LOAD_FAILURE,
                                 Component.translatable("gui.showBlockScreen.workshop.error"),
                                 Component.literal("No block data found")
                              )
                           );
                        return;
                     }

                     this.checkModLack(shareBlockInformation);
                     this.updateAllTransformData(shareBlockInformation);
                     this.updateStateButtonVisible(true);
                     this.minecraft
                        .getToasts()
                        .addToast(new SystemToast(SystemToastIds.NARRATOR_TOGGLE, Component.translatable("gui.showBlockScreen.workshop.paste_pass"), null));
                  } catch (Exception var4x) {
                     this.minecraft
                        .getToasts()
                        .addToast(
                           SystemToast.multiline(
                              this.minecraft,
                              SystemToastIds.PACK_LOAD_FAILURE,
                              Component.translatable("gui.showBlockScreen.workshop.error"),
                              Component.literal(var4x.getMessage())
                           )
                        );
                  }
               }
            )
            .tooltip(Tooltip.create(Component.translatable("gui.showBlockScreen.workshop.paste")))
            .bounds(122, 10, 20, 20)
            .build();
         Button saveButton = ButtonUtils.builder(
               Component.literal("\ud83d\udcbe").withStyle(ChatFormatting.BOLD),
               btn -> this.minecraft
                     .setScreen(
                        new EditScreen(
                           this,
                           Component.translatable("gui.showBlockScreen.workshop.save"),
                           Component.translatable("gui.showBlockScreen.workshop.save.tip"),
                           string -> {
                              if (string != null) {
                                 String res = ShareUtils.transfer(this.blockEntity.getTransformData());

                                 try {
                                    EngraveBlockResultLoader.saveBlock(res, string);
                                    this.minecraft
                                       .getToasts()
                                       .addToast(
                                          SystemToast.multiline(
                                             this.minecraft,
                                             SystemToastIds.NARRATOR_TOGGLE,
                                             Component.translatable("gui.showBlockScreen.workshop.save_pass"),
                                             Component.translatable("gui.showBlockScreen.workshop.share_hint")
                                          )
                                       );
                                 } catch (IOException var4x) {
                                    this.minecraft
                                       .getToasts()
                                       .addToast(
                                          SystemToast.multiline(
                                             this.minecraft,
                                             SystemToastIds.PACK_LOAD_FAILURE,
                                             Component.translatable("gui.showBlockScreen.workshop.save_error"),
                                             Component.literal(var4x.getMessage())
                                          )
                                       );
                                 }

                                 this.minecraft.setScreen(this);
                              } else {
                                 this.minecraft.setScreen(this);
                              }
                           },
                           string -> true
                        )
                     )
            )
            .tooltip(Tooltip.create(Component.translatable("gui.showBlockScreen.workshop.save")))
            .bounds(142, 10, 20, 20)
            .build();
         this.blockStateList = new BlockStateIconList(this.minecraft, 40, 137, 2, 35, 40, 45, this.blockEntity.getTransformData(), this);
         this.blockStateList.setRenderTopAndBottom(false);
         Button statisticButton = ButtonUtils.builder(Component.literal("\ud83d\udcca"), btn -> this.faceCount = calculateFaces(this.blockEntity))
            .tooltip(Tooltip.create(Component.translatable("gui.showBlockScreen.calculate")))
            .bounds(182, 10, 20, 20)
            .build();
         this.leftPropertyButton = ButtonUtils.builder(
               Component.literal("<"),
               btn -> this.property = YuushyaBlockStates.getRelative(this.blockStateList.updateRenderProperties(this.getBlockState()), this.property, true)
            )
            .bounds(2, 177, 10, 20)
            .build();
         this.rightPropertyButton = ButtonUtils.builder(
               Component.literal(">"),
               btn -> this.property = YuushyaBlockStates.getRelative(this.blockStateList.updateRenderProperties(this.getBlockState()), this.property, false)
            )
            .bounds(62, 177, 10, 20)
            .build();
         this.leftStateButton = ButtonUtils.builder(Component.literal("<"), btn -> {
            BlockState nextBlockState = YuushyaBlockStates.cycleState(this.getBlockState(), this.property, true);
            this.updateTransformData(TransformType.BLOCK_STATE, (double)Block.getId(nextBlockState));
         }).bounds(2, 197, 10, 20).build();
         this.rightStateButton = ButtonUtils.builder(Component.literal(">"), btn -> {
            BlockState nextBlockState = YuushyaBlockStates.cycleState(this.getBlockState(), this.property, false);
            this.updateTransformData(TransformType.BLOCK_STATE, (double)Block.getId(nextBlockState));
         }).bounds(62, 197, 10, 20).build();
         CycleButton<BlockShape> shapeButton = CycleButton.builder(BlockShape::getSymbol)
            .displayOnlyValue()
            .withValues(BlockShape.values())
            .withInitialValue(TransformType.SHAPE.extractShape(this.blockEntity))
            .create(
               this.leftColumnX() - 50,
               10,
               40,
               20,
               Component.literal("shape"),
               (button, shape) -> this.updateTransformData(TransformType.SHAPE, (double)shape.ordinal())
            );
         this.modeButton = CycleButton.builder(ShowBlockScreen.Mode::getSymbol)
            .displayOnlyValue()
            .withValues(ShowBlockScreen.Mode.values())
            .withInitialValue(ShowBlockScreen.Mode.SLIDER)
            .withTooltip(mode -> {
               return Tooltip.lines(switch (mode) {
                  case SLIDER -> Component.translatable("gui.showBlockScreen.mode.slider.tooltip");
                  case EDIT -> Component.translatable("gui.showBlockScreen.mode.edit.tooltip");
                  case FINE_TUNE -> Component.translatable("gui.showBlockScreen.mode.fine_tune.tooltip");
               });
            })
            .create(this.leftColumnX(), 10, this.leftColumnWidth(), 20, Component.literal("MODE"), (btn, mode) -> {
               switch (mode) {
                  case SLIDER:
                     this.panel.values().forEach(TransformComponent::setSliderStep);
                     break;
                  case EDIT:
                  case FINE_TUNE:
                     this.panel.values().forEach(TransformComponent::setSliderFineTune);
               }

               switch (mode) {
                  case SLIDER:
                  case FINE_TUNE:
                     this.panel.values().forEach(it -> it.triggerVisible(true));
                     break;
                  case EDIT:
                     this.panel.values().forEach(it -> it.triggerVisible(false));
               }
            });
         this.choose(TransformType.SCALE_X);
         double posX = Math.max(
            PosTransItem.getMaxPos((double)this.blockEntity.getTransformData(this.slot).scales.x()), Math.abs(this.blockEntity.getTransformData(this.slot).pos.x)
         );
         this.choose(TransformType.POS_X).sliderButton = LazyDoubleRange.buttonBuilder(
               Component.translatable("gui.yuushya.showBlockScreen.pos_text"),
               () -> -posX,
               () -> posX,
               () -> PosTransItem.getStep(posX),
               number -> this.updateTransformData(TransformType.POS_X, number)
            )
            .text(
               (caption, number) -> Component.empty()
                     .append(caption)
                     .append(
                        Component.translatable("block.yuushya.showblock.x", new Object[]{String.format("%05.1f", number)}).withStyle(ChatFormatting.DARK_RED)
                     )
            )
            .step(this.choose(TransformType.POS_X).setStandardStep(0.0))
            .onMouseOver(btn -> {
               this.blockEntity.setShowAxis(Axis.X);
               this.blockEntity.setShowPosAxis();
            })
            .initial(TransformType.POS_X.extract(this.blockEntity, this.slot))
            .bounds(this.leftColumnX(), top(0, 0), this.leftColumnWidth(), 20)
            .build();
         double posY = Math.max(
            PosTransItem.getMaxPos((double)this.blockEntity.getTransformData(this.slot).scales.y()), Math.abs(this.blockEntity.getTransformData(this.slot).pos.y)
         );
         this.choose(TransformType.POS_Y).sliderButton = LazyDoubleRange.buttonBuilder(
               Component.translatable("gui.yuushya.showBlockScreen.pos_text"),
               () -> -posY,
               () -> posY,
               () -> PosTransItem.getStep(posY),
               number -> this.updateTransformData(TransformType.POS_Y, number)
            )
            .text(
               (caption, number) -> Component.empty()
                     .append(caption)
                     .append(Component.translatable("block.yuushya.showblock.y", new Object[]{String.format("%05.1f", number)}).withStyle(ChatFormatting.GREEN))
            )
            .step(this.choose(TransformType.POS_Y).setStandardStep(0.0))
            .onMouseOver(btn -> {
               this.blockEntity.setShowAxis(Axis.Y);
               this.blockEntity.setShowPosAxis();
            })
            .initial(TransformType.POS_Y.extract(this.blockEntity, this.slot))
            .bounds(this.leftColumnX(), top(1, 0), this.leftColumnWidth(), 20)
            .build();
         double posZ = Math.max(
            PosTransItem.getMaxPos((double)this.blockEntity.getTransformData(this.slot).scales.z()), Math.abs(this.blockEntity.getTransformData(this.slot).pos.z)
         );
         this.choose(TransformType.POS_Z).sliderButton = LazyDoubleRange.buttonBuilder(
               Component.translatable("gui.yuushya.showBlockScreen.pos_text"),
               () -> -posZ,
               () -> posZ,
               () -> PosTransItem.getStep(posZ),
               number -> this.updateTransformData(TransformType.POS_Z, number)
            )
            .text(
               (caption, number) -> Component.empty()
                     .append(caption)
                     .append(Component.translatable("block.yuushya.showblock.z", new Object[]{String.format("%05.1f", number)}).withStyle(ChatFormatting.BLUE))
            )
            .step(this.choose(TransformType.POS_Z).setStandardStep(0.0))
            .onMouseOver(btn -> {
               this.blockEntity.setShowAxis(Axis.Z);
               this.blockEntity.setShowPosAxis();
            })
            .initial(TransformType.POS_Z.extract(this.blockEntity, this.slot))
            .bounds(this.leftColumnX(), top(2, 0), this.leftColumnWidth(), 20)
            .build();
         this.choose(TransformType.ROT_X).sliderButton = DoubleRange.buttonBuilder(
               Component.translatable("gui.yuushya.showBlockScreen.rot_text"), 0.0, 360.0, number -> this.updateTransformData(TransformType.ROT_X, number)
            )
            .text(
               (caption, number) -> Component.empty()
                     .append(caption)
                     .append(
                        Component.translatable("block.yuushya.showblock.x", new Object[]{String.format("%05.1f", number)}).withStyle(ChatFormatting.DARK_RED)
                     )
            )
            .step(this.choose(TransformType.ROT_X).setStandardStep(22.5))
            .onMouseOver(btn -> {
               this.blockEntity.setShowAxis(Axis.X);
               this.blockEntity.setShowRotAxis();
            })
            .initial(TransformType.ROT_X.extract(this.blockEntity, this.slot))
            .bounds(this.leftColumnX(), top(3, 10), this.leftColumnWidth(), 20)
            .build();
         this.choose(TransformType.ROT_Y).sliderButton = DoubleRange.buttonBuilder(
               Component.translatable("gui.yuushya.showBlockScreen.rot_text"), 0.0, 360.0, number -> this.updateTransformData(TransformType.ROT_Y, number)
            )
            .text(
               (caption, number) -> Component.empty()
                     .append(caption)
                     .append(Component.translatable("block.yuushya.showblock.y", new Object[]{String.format("%05.1f", number)}).withStyle(ChatFormatting.GREEN))
            )
            .step(this.choose(TransformType.ROT_Y).setStandardStep(22.5))
            .onMouseOver(btn -> {
               this.blockEntity.setShowAxis(Axis.Y);
               this.blockEntity.setShowRotAxis();
            })
            .initial(TransformType.ROT_Y.extract(this.blockEntity, this.slot))
            .bounds(this.leftColumnX(), top(4, 10), this.leftColumnWidth(), 20)
            .build();
         this.choose(TransformType.ROT_Z).sliderButton = DoubleRange.buttonBuilder(
               Component.translatable("gui.yuushya.showBlockScreen.rot_text"), 0.0, 360.0, number -> this.updateTransformData(TransformType.ROT_Z, number)
            )
            .text(
               (caption, number) -> Component.empty()
                     .append(caption)
                     .append(Component.translatable("block.yuushya.showblock.z", new Object[]{String.format("%05.1f", number)}).withStyle(ChatFormatting.BLUE))
            )
            .step(this.choose(TransformType.ROT_Z).setStandardStep(22.5))
            .onMouseOver(btn -> {
               this.blockEntity.setShowAxis(Axis.Z);
               this.blockEntity.setShowRotAxis();
            })
            .initial(TransformType.ROT_Z.extract(this.blockEntity, this.slot))
            .bounds(this.leftColumnX(), top(5, 10), this.leftColumnWidth(), 20)
            .build();
         this.choose(TransformType.SCALE_X).sliderButton = DividedDoubleRange.buttonBuilder(Component.empty(), 0.0, 1.0, 10.0, number -> {
               this.updateTransformData(TransformType.SCALE_X, number);
               this.updateTransformData(TransformType.SCALE_Y, number);
               this.updateTransformData(TransformType.SCALE_Z, number);
               this.choose(TransformType.POS_X).sliderButton.setValidatedValue(this.choose(TransformType.POS_X).sliderButton.getValidatedValue());
               this.choose(TransformType.POS_Y).sliderButton.setValidatedValue(this.choose(TransformType.POS_Y).sliderButton.getValidatedValue());
               this.choose(TransformType.POS_Z).sliderButton.setValidatedValue(this.choose(TransformType.POS_Z).sliderButton.getValidatedValue());
            })
            .text((caption, number) -> Component.translatable("gui.yuushya.showBlockScreen.scale_text", new Object[]{String.format("%05.1f", number)}))
            .step(this.choose(TransformType.SCALE_X).setStandardStep(0.1))
            .initial(TransformType.SCALE_X.extract(this.blockEntity, this.slot))
            .bounds(this.leftColumnX(), top(6, 20), this.leftColumnWidth(), 20)
            .build();
         this.choose(TransformType.LIT).sliderButton = DoubleRange.buttonBuilder(
               Component.translatable("gui.yuushya.showBlockScreen.brightness_text"), 0.0, 15.0, number -> this.updateTransformData(TransformType.LIT, number)
            )
            .text(LazyDoubleRange::captionToString)
            .step(this.choose(TransformType.LIT).setStandardStep(1.0))
            .initial(TransformType.LIT.extract(this.blockEntity, this.slot))
            .bounds(this.leftColumnX(), top(7, 30), this.leftColumnWidth(), 20)
            .build();

         for (TransformComponent component : this.panel.values()) {
            component.initWidget(this.font);
            this.addRenderableWidget(component.sliderButton);
            this.addRenderableWidget(component.minusButton);
            this.addRenderableWidget(component.addButton);
            this.addRenderableWidget(component.editBox);
            this.addRenderableWidget(component.cancelButton);
            this.addRenderableWidget(component.finishButton);
         }

         this.addRenderableWidget(this.modeButton);
         this.addRenderableWidget(shapeButton);
         this.addWidget(this.blockStateList);
         this.addRenderableWidget(addStateButton);
         this.addRenderableWidget(removeStateButton);
         this.addRenderableWidget(replaceButton);
         this.addRenderableWidget(this.shownStateButton);
         this.addRenderableWidget(this.leftPropertyButton);
         this.addRenderableWidget(this.rightPropertyButton);
         this.addRenderableWidget(this.leftStateButton);
         this.addRenderableWidget(this.rightStateButton);
         this.addRenderableWidget(copyButton);
         this.addRenderableWidget(parseButton);
         this.addRenderableWidget(saveButton);
         this.addRenderableWidget(statisticButton);
         this.blockStateList.setSelectedSlot(this.slot);
      }
   }

   // Upstream draws no background behind this screen (1.19.2 Screen.render never draws one itself either).
   @Override
   public void renderBackground(PoseStack poseStack) {
   }

   @Override
   public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
      GuiGraphics guiGraphics = new GuiGraphics(poseStack);
      this.blockStateList.render(poseStack, mouseX, mouseY, partialTick);
      BlockState blockState = this.getBlockState();
      guiGraphics.drawString(this.font, this.blockStateList.updateRenderDisplayName(blockState), 45, 36, -1, false);
      List<String> properties = this.blockStateList.updateRenderBlockStateProperties(blockState);

      for (int i = 0; i < properties.size(); i++) {
         MutableComponent displayBlockState = Component.literal(properties.get(i));
         guiGraphics.drawString(this.font, displayBlockState, 45, 36 + 9 * (i + 1) + 1, 16772038, false);
      }

      if (this.updateStateButtonVisible(false)) {
         guiGraphics.drawString(this.font, this.property.getName(), 22, 182, -1, false);
         guiGraphics.drawString(this.font, YuushyaDebugStickItem.getNameHelper(blockState, this.property), 22, 202, -1, false);
      }

      if (this.modeButton.getValue() == ShowBlockScreen.Mode.EDIT) {
         for (TransformComponent component : this.panel.values()) {
            guiGraphics.drawString(
               this.font,
               component.editBox.getMessage(),
               component.editBox.x + component.editBox.getWidth() / 2,
               component.editBox.y + component.editBox.getHeight() / 3,
               7368816
            );
            component.editBox.render(poseStack, mouseX, mouseY, partialTick);
         }
      }

      if (this.faceCount > 0L) {
         String text = String.valueOf(this.faceCount);
         guiGraphics.fill(207, 10, 217 + this.font.width(text), 30, Integer.MIN_VALUE);
         guiGraphics.drawString(this.font, Component.literal(text), 212, 15, -1, false);
      }

      super.render(poseStack, mouseX, mouseY, partialTick);
      Tooltip.renderHovered(this, this.children(), poseStack, mouseX, mouseY);
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void removed() {
      for (TransformType key : this.storage.keySet()) {
         TransformDataOncePacket.sendToServerSide(this.blockEntity.getBlockPos(), this.slot, key, this.storage.get(key));
      }

      this.storage.clear();
      TransformDataOncePacket.sendToServerSideSuccess(this.blockEntity.getBlockPos());
   }

   public void checkModLack(ShareUtils.ShareBlockInformation shareBlockInformation) {
      List<String> unLoaded = shareBlockInformation.mods().stream().filter(id -> !ModList.get().isLoaded(id)).toList();
      Minecraft.getInstance()
         .getToasts()
         .addToast(
            SystemToast.multiline(
               Minecraft.getInstance(), SystemToastIds.PACK_LOAD_FAILURE, Component.literal("Mod Lack"), Component.literal(String.join(", ", unLoaded))
            )
         );
   }

   private void updateAllTransformData(ShareUtils.ShareBlockInformation shareBlockInformation) {
      List<TransformBlockData> dataList = this.blockEntity.getTransformData();
      BlockPos pos = this.blockEntity.getBlockPos();
      int currentSize = dataList.size();

      for (int slot = 0; slot < currentSize; slot++) {
         this.blockEntity.removeTransformData(slot);
         TransformDataOncePacket.sendToServerSide(pos, slot, TransformType.REMOVE, 0.0);
      }

      shareBlockInformation.transfer(dataList);
      int nextSize = dataList.size();
      this.blockEntity.getLevel().sendBlockUpdated(pos, this.blockEntity.getBlockState(), this.blockEntity.getBlockState(), 11);
      this.storage.clear();

      for (int slot = 0; slot < nextSize; slot++) {
         TransformBlockData data = dataList.get(slot);
         this.updateTransformDataServerImmediate(data, slot);
      }

      TransformDataOncePacket.sendToServerSideSuccess(pos);

      for (int slot = nextSize - 1; slot < currentSize; slot++) {
         this.blockEntity.setSlot(slot);
      }

      this.blockStateList.updateRenderList();
   }

   private void updateTransformDataServerImmediate(TransformBlockData data, int slot) {
      BlockPos pos = this.blockEntity.getBlockPos();
      TransformDataOncePacket.sendToServerSide(pos, slot, TransformType.POS_X, data.pos.x);
      TransformDataOncePacket.sendToServerSide(pos, slot, TransformType.POS_Y, data.pos.y);
      TransformDataOncePacket.sendToServerSide(pos, slot, TransformType.POS_Z, data.pos.z);
      TransformDataOncePacket.sendToServerSide(pos, slot, TransformType.ROT_X, (double)data.rot.x());
      TransformDataOncePacket.sendToServerSide(pos, slot, TransformType.ROT_Y, (double)data.rot.y());
      TransformDataOncePacket.sendToServerSide(pos, slot, TransformType.ROT_Z, (double)data.rot.z());
      TransformDataOncePacket.sendToServerSide(pos, slot, TransformType.SCALE_X, (double)data.scales.x());
      TransformDataOncePacket.sendToServerSide(pos, slot, TransformType.SCALE_Y, (double)data.scales.y());
      TransformDataOncePacket.sendToServerSide(pos, slot, TransformType.SCALE_Z, (double)data.scales.z());
      TransformDataOncePacket.sendToServerSide(pos, slot, TransformType.BLOCK_STATE, (double)Block.getId(data.blockState));
      TransformDataOncePacket.sendToServerSide(pos, slot, TransformType.SHOWN, data.isShown ? 1.0 : 0.0);
   }

   private void updateTransformData(TransformType type, Double number) {
      this.storage.put(type, number);
      type.modify(this.blockEntity, this.slot, number);
      this.blockEntity.getLevel().sendBlockUpdated(this.blockEntity.getBlockPos(), this.blockEntity.getBlockState(), this.blockEntity.getBlockState(), 11);
   }

   private String getClipboard() {
      return this.minecraft != null ? TextFieldHelper.getClipboardContents(this.minecraft) : "";
   }

   private void setClipboard(String clipboardValue) {
      if (this.minecraft != null) {
         TextFieldHelper.setClipboardContents(this.minecraft, clipboardValue);
      }
   }

   public static enum Mode implements StringRepresentable {
      SLIDER("slider"),
      FINE_TUNE("fine_tune"),
      EDIT("edit");

      private final String name;
      private final Component symbol;

      private Mode(String name) {
         this.name = name;
         this.symbol = Component.translatable("gui.showBlockScreen.mode." + name);
      }

      @NotNull
      public String getSerializedName() {
         return this.name;
      }

      public Component getSymbol() {
         return this.symbol;
      }
   }
}
