package com.SaintAlfonzo.solraspberrypie.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/** The Liquid Diet Log (7): every unique drink remembered (last 256), most recent first. History only. */
public class LiquidDietLogScreen extends Screen {
    private static final int W = 196, H = 196, COLS = 10, ROWS = 8, PER_PAGE = COLS * ROWS;
    private int page;
    private int left, top;

    public LiquidDietLogScreen() {
        super(Component.translatable("item.solraspberrypie.liquid_diet_log"));
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new LiquidDietLogScreen());
    }

    private List<ItemStack> drinks() {
        List<ItemStack> out = new ArrayList<>();
        if (ClientState.state == null) return out;
        for (ResourceLocation id : ClientState.state.drinks) {
            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item != null) out.add(new ItemStack(item));
        }
        return out;
    }

    private int pages() {
        return Math.max(1, (drinks().size() + PER_PAGE - 1) / PER_PAGE);
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;
        addRenderableWidget(new Button(left + 8, top + H - 26, 20, 20, Component.literal("<"), b -> page = Math.max(0, page - 1)));
        addRenderableWidget(new Button(left + 32, top + H - 26, 20, 20, Component.literal(">"), b -> page = Math.min(pages() - 1, page + 1)));
        addRenderableWidget(new Button(left + W - 64, top + H - 26, 56, 20, Component.translatable("gui.done"), b -> onClose()));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partial) {
        renderBackground(pose);
        fill(pose, left - 1, top - 1, left + W + 1, top + H + 1, 0xFF4A6A8A);
        fill(pose, left, top, left + W, top + H, 0xFFF2EAD3);
        font.draw(pose, title, left + 8, top + 8, 0x2B2B2B);
        List<ItemStack> list = drinks();
        font.draw(pose, Component.translatable("solraspberrypie.liquid.count", list.size()), left + 8, top + 20, 0x555555);
        if (list.isEmpty()) font.draw(pose, Component.translatable("solraspberrypie.liquid.empty"), left + 8, top + 40, 0x777777);
        ItemStack hovered = null;
        int start = page * PER_PAGE;
        for (int i = start; i < Math.min(list.size(), start + PER_PAGE); i++) {
            int x = left + 9 + ((i - start) % COLS) * 18;
            int y = top + 34 + ((i - start) / COLS) * 18;
            itemRenderer.renderAndDecorateItem(list.get(i), x, y);
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) hovered = list.get(i);
        }
        font.draw(pose, (page + 1) + "/" + pages(), left + 58, top + H - 20, 0x555555);
        super.render(pose, mouseX, mouseY, partial);
        if (hovered != null) renderTooltip(pose, hovered, mouseX, mouseY);
    }
}
