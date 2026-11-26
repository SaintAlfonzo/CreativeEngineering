// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/itemblock/ItemBlockScreen.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.itemblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yuushya.modelling.gui.compat.ButtonUtils;
import com.yuushya.modelling.Yuushya;
import com.yuushya.modelling.block.blockstate.YuushyaBlockStates;
import com.yuushya.modelling.blockentity.AbstractTransformBlock;
import com.yuushya.modelling.blockentity.BlockShape;
import com.yuushya.modelling.blockentity.itemblock.ItemBlockEntity;
import com.yuushya.modelling.blockentity.transformData.ItemTransformType;
import com.yuushya.modelling.blockentity.transformData.TransformItemData;
import com.yuushya.modelling.gui.engrave.EngraveItemResultLoader;
import com.yuushya.modelling.gui.showblock.EditScreen;
import com.yuushya.modelling.gui.validate.DividedDoubleRange;
import com.yuushya.modelling.gui.validate.DoubleRange;
import com.yuushya.modelling.gui.validate.LazyDoubleRange;
import com.yuushya.modelling.gui.widget.AbstractColorScreen;
import com.yuushya.modelling.gui.widget.ColorWidget;
import com.yuushya.modelling.gui.widget.ItemStackIconList;
import com.yuushya.modelling.gui.widget.ItemTransformComponent;
import com.yuushya.modelling.gui.widget.SizeTransformComponent;
import com.yuushya.modelling.item.YuushyaDebugStickItem;
import com.yuushya.modelling.item.showblocktool.PosTransItem;
import com.yuushya.modelling.network.ItemStackPacket;
import com.yuushya.modelling.network.ItemTransformDataOncePacket;
import com.yuushya.modelling.network.UpdateAOPacket;
import com.yuushya.modelling.utils.ClientMethod;
import com.yuushya.modelling.utils.ShareUtils;
import com.yuushya.modelling.utils.YuushyaDataTags;
import com.yuushya.modelling.utils.YuushyaUtils;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.yuushya.modelling.gui.compat.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import com.yuushya.modelling.gui.compat.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;

public class ItemBlockScreen extends AbstractColorScreen {
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
   private final ItemBlockEntity blockEntity;
   private final ItemStack newItemStack;
   private final Map<ItemTransformType, Double> storage = new HashMap<>();
   private final Map<ItemTransformType, ItemTransformComponent> panel = new LinkedHashMap<>();
   private final Map<ItemTransformType, SizeTransformComponent> panelSize = new LinkedHashMap<>();
   public EditBox colorEditBox;
   public Button colorFinishButton;
   public CycleButton<ItemBlockScreen.ApplyColor> colorApplyButton;
   private int slot;
   private ItemStack itemStack = ItemStack.EMPTY;
   private CycleButton<ItemBlockScreen.Mode> modeButton;
   private CycleButton<Boolean> shownStateButton;
   private ItemStackIconList itemStackList;
   private ColorWidget colorWidget;
   private Property<?> property;
   private Button leftPropertyButton;
   private Button rightPropertyButton;
   private Button leftStateButton;
   private Button rightStateButton;
   private CycleButton<Boolean> enableBlockButton;

   public ItemBlockScreen(ItemBlockEntity blockEntity, ItemStack newItemStack) {
      super(GameNarrator.NO_TITLE);
      this.blockEntity = blockEntity;
      this.newItemStack = newItemStack;
      if (blockEntity.getSlot() < blockEntity.getTransformData().size()) {
         this.slot = blockEntity.getSlot();
      }
   }

   private static int top(int i, int offset) {
      return 40 + 20 * i + offset;
   }

   public void setSlot(int slot) {
      for (ItemTransformType key : this.storage.keySet()) {
         ItemTransformDataOncePacket.sendToServerSide(this.blockEntity.getBlockPos(), this.slot, key, this.storage.get(key));
      }

      if (!this.itemStack.isEmpty()) {
         ItemStackPacket.sendToServer(this.blockEntity.getBlockPos(), this.slot, this.itemStack);
      }

      this.itemStack = ItemStack.EMPTY;
      this.storage.clear();
      this.slot = slot;
      this.blockEntity.setSlot(slot);

      for (ItemTransformComponent component : this.panel.values()) {
         component.setSliderInitial(this.blockEntity, this.slot);
      }

      for (SizeTransformComponent size : this.panelSize.values()) {
         size.setSliderInitial(this.blockEntity, this.slot);
      }

      double extract = ItemTransformType.COLOR.extract(this.blockEntity, slot);
      this.colorWidget.setColor((int)extract);
      this.colorEditBox.setValue(String.format("#%06X", 16777215 & (int)extract));
      this.shownStateButton.setValue(this.blockEntity.getTransformData(slot).isShown);
      boolean enableBlock = ItemTransformType.ENABLE_BLOCK.extract(this.blockEntity, slot) == 1.0;
      this.updateBlockStateButtonVisible(this.updateStateButton() && enableBlock);
      this.enableBlockButton.setValue(enableBlock);
   }

   private ItemTransformComponent choose(ItemTransformType type) {
      return this.panel.computeIfAbsent(type, ItemTransformComponent::new);
   }

   private SizeTransformComponent chooseSize(ItemTransformType type) {
      return this.panelSize.computeIfAbsent(type, SizeTransformComponent::new);
   }

   private void updateColorVisible(boolean visible) {
      this.colorWidget.visible = visible;
      this.colorEditBox.visible = visible;
      this.colorFinishButton.visible = visible;
      this.colorApplyButton.visible = visible;
   }

   private void updateBlockStateButtonVisible(boolean visible) {
      this.leftPropertyButton.visible = visible;
      this.rightPropertyButton.visible = visible;
      this.leftStateButton.visible = visible;
      this.rightStateButton.visible = visible;
   }

   @Nullable
   public BlockState getBlockState() {
      ItemStack itemStack = this.blockEntity.getTransformData(this.slot).itemStack;
      BlockState blockState = YuushyaDataTags.getBlockState(itemStack);
      return blockState == null && itemStack.getItem() instanceof BlockItem blockItem ? blockItem.getBlock().defaultBlockState() : blockState;
   }

   @Nullable
   public Collection<Property<?>> getProperties() {
      BlockState blockState = this.getBlockState();
      return blockState == null ? null : blockState.getProperties();
   }

   public boolean updateStateButton() {
      BlockState blockState = this.getBlockState();
      if (blockState == null) {
         return false;
      } else {
         Collection<Property<?>> collection = blockState.getBlock().getStateDefinition().getProperties();
         boolean stateButtonVisible = !collection.isEmpty();
         if (stateButtonVisible) {
            this.property = collection.iterator().next();
         }

         return stateButtonVisible;
      }
   }

   public ItemStack getItemStack() {
      return this.blockEntity.getTransformData(this.slot).itemStack;
   }

   private int leftColumnX() {
      return this.width / 4 * 3 + 10;
   }

   private int leftColumnWidth() {
      return this.width / 4 - 20;
   }

   protected void init() {
      if (this.minecraft != null) {
         Button addItemButton = ButtonUtils.builder(Component.literal("+"), btn -> {
            if (this.newItemStack != null) {
               if (this.newItemStack.isEmpty()) {
                  return;
               }

               if (this.newItemStack.getItem() instanceof BlockItem blockItem) {
                  YuushyaDataTags.setBlockState(this.newItemStack, blockItem.getBlock().defaultBlockState());
               }

               this.itemStackList.addSlot();
               this.updateItemStack(this.newItemStack);
               this.updateTransformDataClient(ItemTransformType.SHOWN, 1.0);
               this.updateStateButton();
            }
         }).tooltip(Tooltip.create(Component.translatable("gui.showBlockScreen.display.add"))).bounds(2, 10, 20, 20).build();
         Button copyItemButton = ButtonUtils.builder(Component.literal("⧉"), btn -> {
            ItemStackIconList.Entry selected = (ItemStackIconList.Entry)this.itemStackList.getSelected();
            if (selected != null) {
               this.itemStackList.addSlot();
               this.updateTransformDataClient(selected.getTransformData());
               this.itemStackList.setSelectedSlot(this.slot);
            }
         }).tooltip(Tooltip.create(Component.translatable("gui.showBlockScreen.display.copy"))).bounds(22, 10, 20, 20).build();
         Button removeItemButton = ButtonUtils.builder(Component.literal("×"), btn -> this.updateTransformDataClient(ItemTransformType.REMOVE, 0.0))
            .tooltip(Tooltip.create(Component.translatable("gui.showBlockScreen.display.remove")))
            .bounds(42, 10, 20, 20)
            .build();
         Button replaceButton = ButtonUtils.builder(Component.literal("⇄"), btn -> {
            int chosen = this.itemStackList.getChosenOne();
            if (chosen != -1 && chosen != this.slot) {
               ItemStack item = this.blockEntity.getTransformData(chosen).itemStack;
               if (item == null) {
                  return;
               }

               if (item.isEmpty()) {
                  return;
               }

               this.updateItemStack(item);
            } else if (this.newItemStack != null) {
               if (this.newItemStack.isEmpty()) {
                  return;
               }

               this.updateItemStack(this.newItemStack);
            }
         }).tooltip(Tooltip.create(Component.translatable("gui.showBlockScreen.display.replace"))).bounds(62, 10, 20, 20).build();
         this.shownStateButton = CycleButton.booleanBuilder(Component.literal("\ud83d\udd76"), Component.literal("\ud83d\udc40"))
            .displayOnlyValue()
            .withInitialValue(true)
            .withTooltip(
               on -> Tooltip.lines(on ? Component.translatable("gui.showBlockScreen.display.on") : Component.translatable("gui.showBlockScreen.display.off"))
            )
            .create(82, 10, 20, 20, Component.empty(), (btn, bl) -> this.updateTransformDataClient(ItemTransformType.SHOWN, bl ? 1.0 : 0.0));
         Button copyButton = ButtonUtils.builder(
               Component.literal("\ud83d\udce4").withStyle(ChatFormatting.BOLD),
               btn -> {
                  String res = ShareUtils.transferItems(this.blockEntity.getTransformData());
                  ClientMethod.setClipboard(res);
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
            .bounds(122, 10, 20, 20)
            .build();
         Button parseButton = ButtonUtils.builder(
               Component.literal("\ud83d\udce5").withStyle(ChatFormatting.BOLD),
               btn -> {
                  String string = ClientMethod.getClipboard();

                  try {
                     ShareUtils.ShareItemInformation shareInformation = ShareUtils.fromItems(string);
                     if (shareInformation.items().isEmpty()) {
                        this.minecraft
                           .getToasts()
                           .addToast(
                              SystemToast.multiline(
                                 this.minecraft,
                                 SystemToastIds.PACK_LOAD_FAILURE,
                                 Component.translatable("gui.showBlockScreen.workshop.error"),
                                 Component.literal("No item data found")
                              )
                           );
                        return;
                     }

                     this.checkModLack(shareInformation);
                     this.updateAllTransformData(shareInformation);
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
            .bounds(142, 10, 20, 20)
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
                                 String res = ShareUtils.transferItems(this.blockEntity.getTransformData());

                                 try {
                                    EngraveItemResultLoader.saveItem(res, string);
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
            .bounds(162, 10, 20, 20)
            .build();
         CycleButton<Boolean> ambientOcclusionButton = CycleButton.booleanBuilder(Component.literal("●"), Component.literal("☀"))
            .displayOnlyValue()
            .withInitialValue((Boolean)this.blockEntity.getBlockState().getValue(AbstractTransformBlock.ENABLE_AO))
            .withTooltip(
               on -> Tooltip.lines(
                     on ? Component.translatable("gui.itemBlockScreen.ambientOcclusion.on") : Component.translatable("gui.itemBlockScreen.ambientOcclusion.off")
                  )
            )
            .create(
               182,
               10,
               20,
               20,
               Component.empty(),
               (btn, enableAO) -> {
                  Level level = this.blockEntity.getLevel();
                  if (level != null) {
                     UpdateAOPacket.sendToServer(
                        enableAO, (Boolean)this.blockEntity.getBlockState().getValue(AbstractTransformBlock.FULL_BLOCK), this.blockEntity.getBlockPos()
                     );
                  }
               }
            );
         CycleButton<Boolean> fullBlockButton = CycleButton.booleanBuilder(Component.literal("■"), Component.literal("□"))
            .displayOnlyValue()
            .withInitialValue((Boolean)this.blockEntity.getBlockState().getValue(AbstractTransformBlock.FULL_BLOCK))
            .withTooltip(
               on -> Tooltip.lines(on ? Component.translatable("gui.itemBlockScreen.fullBlock.on") : Component.translatable("gui.itemBlockScreen.fullBlock.off"))
            )
            .create(
               202,
               10,
               20,
               20,
               Component.empty(),
               (btn, fullBlock) -> {
                  Level level = this.blockEntity.getLevel();
                  if (level != null) {
                     UpdateAOPacket.sendToServer(
                        (Boolean)this.blockEntity.getBlockState().getValue(AbstractTransformBlock.ENABLE_AO), fullBlock, this.blockEntity.getBlockPos()
                     );
                  }
               }
            );
         Button xMirror = ButtonUtils.builder(Component.literal("x"), button -> this.mirror(YuushyaUtils.MirrorFace.X))
            .bounds(2, 177, 20, 20)
            .tooltip(Tooltip.create(Component.translatable("gui.itemBlockScreen.mirror.tip", new Object[]{"X"})))
            .build();
         Button yMirror = ButtonUtils.builder(Component.literal("y"), button -> this.mirror(YuushyaUtils.MirrorFace.Y))
            .bounds(22, 177, 20, 20)
            .tooltip(Tooltip.create(Component.translatable("gui.itemBlockScreen.mirror.tip", new Object[]{"Y"})))
            .build();
         Button zMirror = ButtonUtils.builder(Component.literal("z"), button -> this.mirror(YuushyaUtils.MirrorFace.Z))
            .bounds(42, 177, 20, 20)
            .tooltip(Tooltip.create(Component.translatable("gui.itemBlockScreen.mirror.tip", new Object[]{"Z"})))
            .build();
         this.itemStackList = new ItemStackIconList(this.minecraft, 40, 137, 2, 35, 40, 45, this.blockEntity.getTransformData(), this);
         this.itemStackList.setRenderTopAndBottom(false);
         CycleButton<BlockShape> shapeButton = CycleButton.builder(BlockShape::getSymbol)
            .displayOnlyValue()
            .withValues(BlockShape.values())
            .withInitialValue(ItemTransformType.SHAPE.extractShape(this.blockEntity))
            .create(
               this.leftColumnX() - 50,
               10,
               40,
               20,
               Component.literal("shape"),
               (button, shape) -> this.updateTransformDataClient(ItemTransformType.SHAPE, (double)shape.ordinal())
            );
         this.modeButton = CycleButton.builder(ItemBlockScreen.Mode::getSymbol)
            .displayOnlyValue()
            .withValues(ItemBlockScreen.Mode.values())
            .withInitialValue(ItemBlockScreen.Mode.SLIDER)
            .withTooltip(mode -> {
               return Tooltip.lines(switch (mode) {
                  case SLIDER -> Component.translatable("gui.itemBlockScreen.mode.slider.tooltip");
                  case EDIT -> Component.translatable("gui.itemBlockScreen.mode.edit.tooltip");
                  case FINE_TUNE -> Component.translatable("gui.itemBlockScreen.mode.fine_tune.tooltip");
                  case COLOR -> Component.translatable("gui.showBlockScreen.mode.color.tooltip");
                  case CUSTOM_SIZE -> Component.translatable("gui.itemBlockScreen.mode.custom_size.tooltip");
               });
            })
            .create(this.leftColumnX(), 10, this.leftColumnWidth(), 20, Component.literal("MODE"), (btn, mode) -> {
               switch (mode) {
                  case SLIDER:
                     this.panel.values().forEach(ItemTransformComponent::setSliderStep);
                     break;
                  case EDIT:
                  case FINE_TUNE:
                     this.panel.values().forEach(ItemTransformComponent::setSliderFineTune);
               }

               switch (mode) {
                  case SLIDER:
                  case FINE_TUNE:
                     this.panel.values().forEach(it -> it.triggerVisible(true));
                     this.panelSize.values().forEach(it -> it.triggerVisible(false));
                     this.updateColorVisible(false);
                     break;
                  case EDIT:
                     this.panel.values().forEach(it -> it.triggerVisible(false));
                     this.panelSize.values().forEach(it -> it.triggerVisible(false));
                     this.updateColorVisible(false);
                     break;
                  case COLOR:
                     this.panel.values().forEach(ItemTransformComponent::setInvisible);
                     this.panelSize.values().forEach(it -> it.triggerVisible(false));
                     this.updateColorVisible(true);
                     break;
                  case CUSTOM_SIZE:
                     this.panel.values().forEach(ItemTransformComponent::setInvisible);
                     this.panelSize.values().forEach(it -> it.triggerVisible(true));
                     this.updateColorVisible(false);
               }
            });
         this.choose(ItemTransformType.SCALE_X);
         double posX = Math.max(
            PosTransItem.getMaxPos((double)this.blockEntity.getTransformData(this.slot).scales.x()),
            Math.abs(this.blockEntity.getTransformData(this.slot).pos.x)
         );
         this.choose(ItemTransformType.POS_X).sliderButton = LazyDoubleRange.buttonBuilder(
               Component.translatable("gui.yuushya.itemBlockScreen.pos_text"),
               () -> -posX,
               () -> posX,
               () -> PosTransItem.getStep(posX),
               number -> this.updateTransformDataClient(ItemTransformType.POS_X, number)
            )
            .text(
               (caption, number) -> Component.empty()
                     .append(caption)
                     .append(
                        Component.translatable("block.yuushya.itemblock.x", new Object[]{String.format("%05.1f", number)}).withStyle(ChatFormatting.DARK_RED)
                     )
            )
            .step(this.choose(ItemTransformType.POS_X).setStandardStep(0.0))
            .onMouseOver(btn -> {
               this.blockEntity.setShowAxis(Axis.X);
               this.blockEntity.setShowPosAxis();
            })
            .initial(ItemTransformType.POS_X.extract(this.blockEntity, this.slot))
            .bounds(this.leftColumnX(), top(0, 0), this.leftColumnWidth(), 20)
            .build();
         double posY = Math.max(
            PosTransItem.getMaxPos((double)this.blockEntity.getTransformData(this.slot).scales.y()),
            Math.abs(this.blockEntity.getTransformData(this.slot).pos.y)
         );
         this.choose(ItemTransformType.POS_Y).sliderButton = LazyDoubleRange.buttonBuilder(
               Component.translatable("gui.yuushya.itemBlockScreen.pos_text"),
               () -> -posY,
               () -> posY,
               () -> PosTransItem.getStep(posY),
               number -> this.updateTransformDataClient(ItemTransformType.POS_Y, number)
            )
            .text(
               (caption, number) -> Component.empty()
                     .append(caption)
                     .append(Component.translatable("block.yuushya.itemblock.y", new Object[]{String.format("%05.1f", number)}).withStyle(ChatFormatting.GREEN))
            )
            .step(this.choose(ItemTransformType.POS_Y).setStandardStep(0.0))
            .onMouseOver(btn -> {
               this.blockEntity.setShowAxis(Axis.Y);
               this.blockEntity.setShowPosAxis();
            })
            .initial(ItemTransformType.POS_Y.extract(this.blockEntity, this.slot))
            .bounds(this.leftColumnX(), top(1, 0), this.leftColumnWidth(), 20)
            .build();
         double posZ = Math.max(
            PosTransItem.getMaxPos((double)this.blockEntity.getTransformData(this.slot).scales.z()),
            Math.abs(this.blockEntity.getTransformData(this.slot).pos.z)
         );
         this.choose(ItemTransformType.POS_Z).sliderButton = LazyDoubleRange.buttonBuilder(
               Component.translatable("gui.yuushya.itemBlockScreen.pos_text"),
               () -> -posZ,
               () -> posZ,
               () -> PosTransItem.getStep(posZ),
               number -> this.updateTransformDataClient(ItemTransformType.POS_Z, number)
            )
            .text(
               (caption, number) -> Component.empty()
                     .append(caption)
                     .append(Component.translatable("block.yuushya.itemblock.z", new Object[]{String.format("%05.1f", number)}).withStyle(ChatFormatting.BLUE))
            )
            .step(this.choose(ItemTransformType.POS_Z).setStandardStep(0.0))
            .onMouseOver(btn -> {
               this.blockEntity.setShowAxis(Axis.Z);
               this.blockEntity.setShowPosAxis();
            })
            .initial(ItemTransformType.POS_Z.extract(this.blockEntity, this.slot))
            .bounds(this.leftColumnX(), top(2, 0), this.leftColumnWidth(), 20)
            .build();
         this.choose(ItemTransformType.ROT_X).sliderButton = DoubleRange.buttonBuilder(
               Component.translatable("gui.yuushya.itemBlockScreen.rot_text"),
               0.0,
               360.0,
               number -> this.updateTransformDataClient(ItemTransformType.ROT_X, number)
            )
            .text(
               (caption, number) -> Component.empty()
                     .append(caption)
                     .append(
                        Component.translatable("block.yuushya.itemblock.x", new Object[]{String.format("%05.1f", number)}).withStyle(ChatFormatting.DARK_RED)
                     )
            )
            .step(this.choose(ItemTransformType.ROT_X).setStandardStep(22.5))
            .onMouseOver(btn -> {
               this.blockEntity.setShowAxis(Axis.X);
               this.blockEntity.setShowRotAxis();
            })
            .initial(ItemTransformType.ROT_X.extract(this.blockEntity, this.slot))
            .bounds(this.leftColumnX(), top(3, 10), this.leftColumnWidth(), 20)
            .build();
         this.choose(ItemTransformType.ROT_Y).sliderButton = DoubleRange.buttonBuilder(
               Component.translatable("gui.yuushya.itemBlockScreen.rot_text"),
               0.0,
               360.0,
               number -> this.updateTransformDataClient(ItemTransformType.ROT_Y, number)
            )
            .text(
               (caption, number) -> Component.empty()
                     .append(caption)
                     .append(Component.translatable("block.yuushya.itemblock.y", new Object[]{String.format("%05.1f", number)}).withStyle(ChatFormatting.GREEN))
            )
            .step(this.choose(ItemTransformType.ROT_Y).setStandardStep(22.5))
            .onMouseOver(btn -> {
               this.blockEntity.setShowAxis(Axis.Y);
               this.blockEntity.setShowRotAxis();
            })
            .initial(ItemTransformType.ROT_Y.extract(this.blockEntity, this.slot))
            .bounds(this.leftColumnX(), top(4, 10), this.leftColumnWidth(), 20)
            .build();
         this.choose(ItemTransformType.ROT_Z).sliderButton = DoubleRange.buttonBuilder(
               Component.translatable("gui.yuushya.itemBlockScreen.rot_text"),
               0.0,
               360.0,
               number -> this.updateTransformDataClient(ItemTransformType.ROT_Z, number)
            )
            .text(
               (caption, number) -> Component.empty()
                     .append(caption)
                     .append(Component.translatable("block.yuushya.itemblock.z", new Object[]{String.format("%05.1f", number)}).withStyle(ChatFormatting.BLUE))
            )
            .step(this.choose(ItemTransformType.ROT_Z).setStandardStep(22.5))
            .onMouseOver(btn -> {
               this.blockEntity.setShowAxis(Axis.Z);
               this.blockEntity.setShowRotAxis();
            })
            .initial(ItemTransformType.ROT_Z.extract(this.blockEntity, this.slot))
            .bounds(this.leftColumnX(), top(5, 10), this.leftColumnWidth(), 20)
            .build();
         this.choose(ItemTransformType.SCALE_X).sliderButton = DividedDoubleRange.buttonBuilder(Component.empty(), 0.0, 1.0, 10.0, number -> {
               this.updateTransformDataClient(ItemTransformType.SCALE_X, number);
               this.updateTransformDataClient(ItemTransformType.SCALE_Y, number);
               this.updateTransformDataClient(ItemTransformType.SCALE_Z, number);
               this.choose(ItemTransformType.POS_X).sliderButton.setValidatedValue(this.choose(ItemTransformType.POS_X).sliderButton.getValidatedValue());
               this.choose(ItemTransformType.POS_Y).sliderButton.setValidatedValue(this.choose(ItemTransformType.POS_Y).sliderButton.getValidatedValue());
               this.choose(ItemTransformType.POS_Z).sliderButton.setValidatedValue(this.choose(ItemTransformType.POS_Z).sliderButton.getValidatedValue());
            })
            .text((caption, number) -> Component.translatable("gui.yuushya.itemBlockScreen.scale_text", new Object[]{String.format("%05.1f", number)}))
            .step(this.choose(ItemTransformType.SCALE_X).setStandardStep(0.1))
            .initial(ItemTransformType.SCALE_X.extract(this.blockEntity, this.slot))
            .bounds(this.leftColumnX(), top(6, 20), this.leftColumnWidth(), 20)
            .build();
         this.choose(ItemTransformType.LIT).sliderButton = DoubleRange.buttonBuilder(
               Component.translatable("gui.yuushya.itemBlockScreen.brightness_text"),
               0.0,
               15.0,
               number -> this.updateTransformDataClient(ItemTransformType.LIT, number)
            )
            .text(LazyDoubleRange::captionToString)
            .step(this.choose(ItemTransformType.LIT).setStandardStep(1.0))
            .initial(ItemTransformType.LIT.extract(this.blockEntity, this.slot))
            .bounds(this.leftColumnX(), top(7, 30), this.leftColumnWidth(), 20)
            .build();
         this.chooseSize(ItemTransformType.SCALE_X).sliderButton = DividedDoubleRange.buttonBuilder(Component.empty(), 0.0, 1.0, 10.0, number -> {
               this.updateTransformDataClient(ItemTransformType.SCALE_X, number);
               this.chooseSize(ItemTransformType.SCALE_X).editBox.setValue(String.valueOf(number));
               this.choose(ItemTransformType.POS_X).sliderButton.setValidatedValue(this.choose(ItemTransformType.POS_X).sliderButton.getValidatedValue());
            })
            .text((caption, number) -> Component.translatable("gui.yuushya.itemBlockScreen.scale_text", new Object[]{String.format("%05.1f", number)}))
            .step(this.chooseSize(ItemTransformType.SCALE_X).setStandardStep(0.1))
            .initial(ItemTransformType.SCALE_X.extract(this.blockEntity, this.slot))
            .bounds(this.leftColumnX(), top(0, 20), this.leftColumnWidth(), 20)
            .build();
         this.chooseSize(ItemTransformType.SCALE_Y).sliderButton = DividedDoubleRange.buttonBuilder(Component.empty(), 0.0, 1.0, 10.0, number -> {
               this.updateTransformDataClient(ItemTransformType.SCALE_Y, number);
               this.chooseSize(ItemTransformType.SCALE_Y).editBox.setValue(String.valueOf(number));
               this.choose(ItemTransformType.POS_Y).sliderButton.setValidatedValue(this.choose(ItemTransformType.POS_Y).sliderButton.getValidatedValue());
            })
            .text((caption, number) -> Component.translatable("gui.yuushya.itemBlockScreen.scale_text", new Object[]{String.format("%05.1f", number)}))
            .step(this.chooseSize(ItemTransformType.SCALE_Y).setStandardStep(0.1))
            .initial(ItemTransformType.SCALE_Y.extract(this.blockEntity, this.slot))
            .bounds(this.leftColumnX(), top(2, 20), this.leftColumnWidth(), 20)
            .build();
         this.chooseSize(ItemTransformType.SCALE_Z).sliderButton = DividedDoubleRange.buttonBuilder(Component.empty(), 0.0, 1.0, 10.0, number -> {
               this.updateTransformDataClient(ItemTransformType.SCALE_Z, number);
               this.chooseSize(ItemTransformType.SCALE_Z).editBox.setValue(String.valueOf(number));
               this.choose(ItemTransformType.POS_Z).sliderButton.setValidatedValue(this.choose(ItemTransformType.POS_Z).sliderButton.getValidatedValue());
            })
            .text((caption, number) -> Component.translatable("gui.yuushya.itemBlockScreen.scale_text", new Object[]{String.format("%05.1f", number)}))
            .step(this.chooseSize(ItemTransformType.SCALE_Z).setStandardStep(0.1))
            .initial(ItemTransformType.SCALE_Z.extract(this.blockEntity, this.slot))
            .bounds(this.leftColumnX(), top(4, 20), this.leftColumnWidth(), 20)
            .build();
         boolean enableBlock = ItemTransformType.ENABLE_BLOCK.extract(this.blockEntity, this.slot) == 1.0;
         this.enableBlockButton = CycleButton.booleanBuilder(Component.translatable("gui.textBlockScreen.block"), Component.translatable("gui.textBlockScreen.item"))
            .displayOnlyValue()
            .withInitialValue(enableBlock)
            .create(42, 50, 40, 20, Component.empty(), (button, bool) -> {
               this.updateBlockStateButtonVisible(bool);
               this.updateTransformDataClient(ItemTransformType.ENABLE_BLOCK, bool ? 1.0 : 0.0);
            });
         this.leftPropertyButton = ButtonUtils.builder(Component.literal("<"), btn -> {
            if (this.property != null) {
               this.property = YuushyaBlockStates.getRelative(this.getProperties(), this.property, true);
            }
         }).bounds(42, 70, 10, 20).build();
         this.rightPropertyButton = ButtonUtils.builder(Component.literal(">"), btn -> {
            if (this.property != null) {
               this.property = YuushyaBlockStates.getRelative(this.getProperties(), this.property, false);
            }
         }).bounds(102, 70, 10, 20).build();
         this.leftStateButton = ButtonUtils.builder(Component.literal("<"), btn -> {
            BlockState blockState = this.getBlockState();
            if (blockState != null) {
               BlockState nextBlockState = YuushyaBlockStates.cycleState(blockState, this.property, true);
               ItemStack itemStack = this.blockEntity.getTransformData(this.slot).itemStack;
               YuushyaDataTags.setBlockState(itemStack, nextBlockState);
               this.updateItemStack(itemStack);
            }
         }).bounds(42, 90, 10, 20).build();
         this.rightStateButton = ButtonUtils.builder(Component.literal(">"), btn -> {
            BlockState blockState = this.getBlockState();
            if (blockState != null) {
               BlockState nextBlockState = YuushyaBlockStates.cycleState(this.getBlockState(), this.property, false);
               ItemStack itemStack = this.blockEntity.getTransformData(this.slot).itemStack;
               YuushyaDataTags.setBlockState(itemStack, nextBlockState);
               this.updateItemStack(itemStack);
            }
         }).bounds(102, 90, 10, 20).build();
         this.updateBlockStateButtonVisible(this.updateStateButton() && enableBlock);
         this.colorWidget = new ColorWidget(
            this.leftColumnX() - 10,
            top(-2, 30),
            110,
            160,
            (int)ItemTransformType.COLOR.extract(this.blockEntity, this.slot),
            Component.translatable("gui.yuushya.itemBlockScreen.color_text"),
            this
         );
         this.colorEditBox = new EditBox(
            this.font, this.leftColumnX() - 5, top(6, 30), this.leftColumnWidth(), 20, Component.translatable("gui.yuushya.itemBlockScreen.color_text")
         );
         this.colorEditBox.setMaxLength(7);
         this.colorFinishButton = ButtonUtils.builder(Component.literal("√").withStyle(ChatFormatting.GREEN), button -> {
            String text = this.colorEditBox.getValue();
            if (text.startsWith("#")) {
               try {
                  int color = Integer.parseInt(text.substring(1), 16);
                  this.colorWidget.setColor(color);
                  this.updateTransformDataClient(ItemTransformType.COLOR, (double)color);
               } catch (NumberFormatException var4x) {
                  Yuushya.LOGGER.error("Invalid color number");
               }
            }
         }).bounds(this.leftColumnX() + this.leftColumnWidth() - 5, top(6, 30), 10, 20).build();
         this.colorApplyButton = CycleButton.builder(ItemBlockScreen.ApplyColor::getSymbol)
            .displayOnlyValue()
            .withValues(ItemBlockScreen.ApplyColor.values())
            .withInitialValue(ItemBlockScreen.ApplyColor.PRE_APPLY)
            .withTooltip(mode -> Tooltip.lines(mode.getDescription()))
            .create(this.leftColumnX() + 30, top(-2, 30) + 5, 50, 20, Component.literal("TYPE"), (button, mode) -> {
               switch (mode) {
                  case DONE:
                     int color = (int)ItemTransformType.COLOR.extract(this.blockEntity, this.slot);

                     for (int i = 0; i < this.blockEntity.getTransformData().size(); i++) {
                        if (i != this.slot) {
                           this.updateTransformDataSever(ItemTransformType.COLOR, (double)color, i);
                        }
                     }

                     this.blockEntity.getLevel().sendBlockUpdated(this.blockEntity.getBlockPos(), this.blockEntity.getBlockState(), this.blockEntity.getBlockState(), 11);
                     this.colorApplyButton.setValue(ItemBlockScreen.ApplyColor.PRE_APPLY);
                  case PRE_APPLY:
                  case APPLY:
               }
            });
         this.colorWidget.visible = false;
         this.colorEditBox.visible = false;
         this.colorFinishButton.visible = false;
         this.colorApplyButton.visible = false;

         for (ItemTransformComponent component : this.panel.values()) {
            component.initWidget(this.font);
            this.addRenderableWidget(component.sliderButton);
            this.addRenderableWidget(component.minusButton);
            this.addRenderableWidget(component.addButton);
            this.addRenderableWidget(component.editBox);
            this.addRenderableWidget(component.cancelButton);
            this.addRenderableWidget(component.finishButton);
         }

         for (SizeTransformComponent size : this.panelSize.values()) {
            size.initWidget(this.font);
            this.addRenderableWidget(size.sliderButton);
            this.addRenderableWidget(size.minusButton);
            this.addRenderableWidget(size.addButton);
            this.addRenderableWidget(size.editBox);
            this.addRenderableWidget(size.cancelButton);
            this.addRenderableWidget(size.finishButton);
            size.triggerVisible(false);
         }

         this.addRenderableWidget(this.modeButton);
         this.addRenderableWidget(shapeButton);
         this.addWidget(this.itemStackList);
         this.addRenderableWidget(addItemButton);
         this.addRenderableWidget(removeItemButton);
         this.addRenderableWidget(copyItemButton);
         this.addRenderableWidget(replaceButton);
         this.addRenderableWidget(this.shownStateButton);
         this.addRenderableWidget(copyButton);
         this.addRenderableWidget(parseButton);
         this.addRenderableWidget(saveButton);
         this.addRenderableWidget(ambientOcclusionButton);
         this.addRenderableWidget(fullBlockButton);
         this.addRenderableWidget(xMirror);
         this.addRenderableWidget(yMirror);
         this.addRenderableWidget(zMirror);
         this.addRenderableWidget(this.colorWidget);
         this.addRenderableWidget(this.colorEditBox);
         this.addRenderableWidget(this.colorFinishButton);
         this.addRenderableWidget(this.colorApplyButton);
         this.addRenderableWidget(this.enableBlockButton);
         this.addRenderableWidget(this.leftPropertyButton);
         this.addRenderableWidget(this.rightStateButton);
         this.addRenderableWidget(this.leftStateButton);
         this.addRenderableWidget(this.rightPropertyButton);
         this.itemStackList.setSelectedSlot(this.slot);
      }
   }

   private void mirror(YuushyaUtils.MirrorFace face) {
      double x = ItemTransformType.POS_X.extract(this.blockEntity, this.slot);
      double y = ItemTransformType.POS_Y.extract(this.blockEntity, this.slot);
      double z = ItemTransformType.POS_Z.extract(this.blockEntity, this.slot);
      Vector3d pos = new Vector3d(x, y, z);
      double xRot = ItemTransformType.ROT_X.extract(this.blockEntity, this.slot);
      double yRot = ItemTransformType.ROT_Y.extract(this.blockEntity, this.slot);
      double zRot = ItemTransformType.ROT_Z.extract(this.blockEntity, this.slot);
      Quaternion rot = Quaternion.fromXYZ((float)Math.toRadians(xRot), (float)Math.toRadians(yRot), (float)Math.toRadians(zRot));
      YuushyaUtils.mirror(pos, rot, face);
      this.updateTransformDataClient(ItemTransformType.POS_X, pos.x);
      this.updateTransformDataClient(ItemTransformType.POS_Y, pos.y);
      this.updateTransformDataClient(ItemTransformType.POS_Z, pos.z);
      Vector3f euler = YuushyaUtils.eulerAnglesXYZ(rot);
      this.updateTransformDataClient(ItemTransformType.ROT_X, YuushyaUtils.normalizeAngle(Math.toDegrees((double)euler.x())));
      this.updateTransformDataClient(ItemTransformType.ROT_Y, YuushyaUtils.normalizeAngle(Math.toDegrees((double)euler.y())));
      this.updateTransformDataClient(ItemTransformType.ROT_Z, YuushyaUtils.normalizeAngle(Math.toDegrees((double)euler.z())));
      this.itemStackList.setSelectedSlot(this.slot);
   }

   // Upstream draws no background behind this screen (1.19.2 Screen.render never draws one itself either).
   @Override
   public void renderBackground(PoseStack poseStack) {
   }

   @Override
   public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
      GuiGraphics guiGraphics = new GuiGraphics(poseStack);
      this.itemStackList.render(poseStack, mouseX, mouseY, partialTick);
      ItemStack itemStack = this.getItemStack();
      guiGraphics.drawString(this.font, itemStack.getDisplayName(), 45, 36, -1, false);
      if (this.modeButton.getValue() == ItemBlockScreen.Mode.EDIT) {
         for (ItemTransformComponent component : this.panel.values()) {
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

      if (ItemTransformType.ENABLE_BLOCK.extract(this.blockEntity, this.slot) == 1.0) {
         BlockState blockState = this.getBlockState();
         if (blockState != null && this.property != null) {
            String propertyName = this.property.getName();
            guiGraphics.drawString(this.font, propertyName, 82 - this.font.width(propertyName) / 2, 75, -1, false);
            String propertyValue = YuushyaDebugStickItem.getNameHelper(blockState, this.property);
            guiGraphics.drawString(this.font, propertyValue, 82 - this.font.width(propertyValue) / 2, 95, -1, false);
         }
      }

      super.render(poseStack, mouseX, mouseY, partialTick);
      Tooltip.renderHovered(this, this.children(), poseStack, mouseX, mouseY);
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void removed() {
      for (ItemTransformType key : this.storage.keySet()) {
         ItemTransformDataOncePacket.sendToServerSide(this.blockEntity.getBlockPos(), this.slot, key, this.storage.get(key));
      }

      if (!this.itemStack.isEmpty()) {
         ItemStackPacket.sendToServer(this.blockEntity.getBlockPos(), this.slot, this.itemStack);
      }

      this.itemStack = ItemStack.EMPTY;
      this.storage.clear();
      ItemTransformDataOncePacket.sendToServerSideSuccess(this.blockEntity.getBlockPos());
   }

   public void checkModLack(ShareUtils.ShareItemInformation shareInformation) {
      List<String> unLoaded = shareInformation.mods().stream().filter(id -> !ModList.get().isLoaded(id)).toList();
      if (!unLoaded.isEmpty()) {
         if (!unLoaded.contains("yuushya")) {
            Minecraft.getInstance()
               .getToasts()
               .addToast(
                  SystemToast.multiline(
                     Minecraft.getInstance(), SystemToastIds.PACK_LOAD_FAILURE, Component.literal("Mod Lack"), Component.literal(String.join(", ", unLoaded))
                  )
               );
         }
      }
   }

   private void updateAllTransformData(ShareUtils.ShareItemInformation shareInformation) {
      List<TransformItemData> dataList = this.blockEntity.getTransformData();
      BlockPos pos = this.blockEntity.getBlockPos();
      int currentSize = dataList.size();

      for (int slot = 0; slot < currentSize; slot++) {
         this.blockEntity.removeTransformData(slot);
         ItemTransformDataOncePacket.sendToServerSide(pos, slot, ItemTransformType.REMOVE, 0.0);
      }

      shareInformation.transferItems(dataList);
      int nextSize = dataList.size();
      this.blockEntity.getLevel().sendBlockUpdated(pos, this.blockEntity.getBlockState(), this.blockEntity.getBlockState(), 11);
      this.storage.clear();

      for (int slot = 0; slot < nextSize; slot++) {
         TransformItemData data = dataList.get(slot);
         this.updateTransformDataServerImmediate(data, slot);
      }

      ItemTransformDataOncePacket.sendToServerSideSuccess(pos);

      for (int slot = nextSize - 1; slot < currentSize; slot++) {
         this.blockEntity.setSlot(slot);
      }

      boolean enableBlock = ItemTransformType.ENABLE_BLOCK.extract(this.blockEntity, this.slot) == 1.0;
      this.updateBlockStateButtonVisible(this.updateStateButton() && enableBlock);
      this.enableBlockButton.setValue(enableBlock);
      this.itemStackList.updateRenderList();
   }

   private void updateTransformDataServerImmediate(TransformItemData data, int slot) {
      if (!data.itemStack.isEmpty()) {
         BlockPos pos = this.blockEntity.getBlockPos();
         ItemTransformDataOncePacket.sendToServerSide(pos, slot, ItemTransformType.POS_X, data.pos.x);
         ItemTransformDataOncePacket.sendToServerSide(pos, slot, ItemTransformType.POS_Y, data.pos.y);
         ItemTransformDataOncePacket.sendToServerSide(pos, slot, ItemTransformType.POS_Z, data.pos.z);
         ItemTransformDataOncePacket.sendToServerSide(pos, slot, ItemTransformType.ROT_X, (double)data.rot.x());
         ItemTransformDataOncePacket.sendToServerSide(pos, slot, ItemTransformType.ROT_Y, (double)data.rot.y());
         ItemTransformDataOncePacket.sendToServerSide(pos, slot, ItemTransformType.ROT_Z, (double)data.rot.z());
         ItemTransformDataOncePacket.sendToServerSide(pos, slot, ItemTransformType.SCALE_X, (double)data.scales.x());
         ItemTransformDataOncePacket.sendToServerSide(pos, slot, ItemTransformType.SCALE_Y, (double)data.scales.y());
         ItemTransformDataOncePacket.sendToServerSide(pos, slot, ItemTransformType.SCALE_Z, (double)data.scales.z());
         ItemTransformDataOncePacket.sendToServerSide(pos, slot, ItemTransformType.SHOWN, data.isShown ? 1.0 : 0.0);
         ItemTransformDataOncePacket.sendToServerSide(pos, slot, ItemTransformType.ENABLE_BLOCK, data.enableBlock ? 1.0 : 0.0);
         ItemTransformDataOncePacket.sendToServerSide(pos, slot, ItemTransformType.COLOR, (double)data.color);
         ItemStackPacket.sendToServer(pos, slot, data.itemStack);
      }
   }

   private void updateTransformDataClient(TransformItemData data) {
      this.updateTransformDataClient(ItemTransformType.POS_X, data.pos.x);
      this.updateTransformDataClient(ItemTransformType.POS_Y, data.pos.y);
      this.updateTransformDataClient(ItemTransformType.POS_Z, data.pos.z);
      this.updateTransformDataClient(ItemTransformType.ROT_X, (double)data.rot.x());
      this.updateTransformDataClient(ItemTransformType.ROT_Y, (double)data.rot.y());
      this.updateTransformDataClient(ItemTransformType.ROT_Z, (double)data.rot.z());
      this.updateTransformDataClient(ItemTransformType.SCALE_X, (double)data.scales.x());
      this.updateTransformDataClient(ItemTransformType.SCALE_Y, (double)data.scales.y());
      this.updateTransformDataClient(ItemTransformType.SCALE_Z, (double)data.scales.z());
      this.updateTransformDataClient(ItemTransformType.SHOWN, data.isShown ? 1.0 : 0.0);
      this.updateTransformDataClient(ItemTransformType.ENABLE_BLOCK, data.enableBlock ? 1.0 : 0.0);
      this.updateTransformDataClient(ItemTransformType.COLOR, (double)data.color);
      this.updateItemStack(data.itemStack);
   }

   public void updateTransformDataClient(ItemTransformType type, Double number) {
      this.storage.put(type, number);
      type.modify(this.blockEntity, this.slot, number);
      this.blockEntity.getLevel().sendBlockUpdated(this.blockEntity.getBlockPos(), this.blockEntity.getBlockState(), this.blockEntity.getBlockState(), 11);
   }

   public void updateTransformDataSever(ItemTransformType type, Double number, int slot) {
      type.modify(this.blockEntity, slot, number);
      ItemTransformDataOncePacket.sendToServerSide(this.blockEntity.getBlockPos(), slot, type, number);
   }

   private void updateItemStack(ItemStack itemStack) {
      this.itemStack = itemStack.copy();
      ItemTransformType.ITEM_STACK.modify(this.blockEntity, this.slot, itemStack);
      this.blockEntity.getLevel().sendBlockUpdated(this.blockEntity.getBlockPos(), this.blockEntity.getBlockState(), this.blockEntity.getBlockState(), 11);
   }

   @Override
   public Font getColorFont() {
      return this.font;
   }

   @Override
   public EditBox getColorEditBox() {
      return this.colorEditBox;
   }

   @Override
   public void updateColorData(int colorValue) {
      this.updateTransformDataClient(ItemTransformType.COLOR, (double)colorValue);
   }

   @Override
   public void setColorFocused(AbstractWidget widget) {
      this.setFocused(widget);
   }

   @Override
   public void setColorDragging(boolean dragging) {
      this.setDragging(dragging);
   }

   public Font getFont() {
      return this.font;
   }

   public static enum ApplyColor {
      PRE_APPLY,
      APPLY,
      DONE;

      public Component getSymbol() {
         return Component.translatable("gui.itemBlockScreen.apply." + this.name().toLowerCase());
      }

      public Component getDescription() {
         return Component.translatable("gui.itemBlockScreen.apply.tip." + this.name().toLowerCase());
      }
   }

   public static enum Mode implements StringRepresentable {
      SLIDER("slider"),
      FINE_TUNE("fine_tune"),
      EDIT("edit"),
      COLOR("color"),
      CUSTOM_SIZE("custom_size");

      private final String name;
      private final Component symbol;

      private Mode(String name) {
         this.name = name;
         this.symbol = Component.translatable("gui.itemBlockScreen.mode." + name);
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
