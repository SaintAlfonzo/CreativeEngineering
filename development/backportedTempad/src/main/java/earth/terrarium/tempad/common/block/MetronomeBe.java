// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/block/MetronomeBe.kt, with the metronome
// entries of Tempad.kt's RegisterCapabilitiesEvent block (chronons: BlockChrononContent.metronome while booting, then
// MultiversalChrononHandler(owner), or BlockChrononContent.metronomeClient when no server is running; item handler: the
// inventory). Translated to Java for Forge 1.19.2:
// - Resourceful ContentMenuProvider -> MenuProvider + openMenu (NetworkHooks.openScreen, the Optional content written to the
//   open-screen buffer as upstream's MenuContentHelper does).
// - NeoForge block capabilities -> getCapability here; NeoForge ItemStackHandler -> Forge's ("Inventory" key).
// - GAME_PROFILE_CODEC under "Owner" -> CodecUtils game profile {id, name} under "Owner".
// - metronomeEnergy (overworld attachment) -> MetronomeData.get() (overworld SavedData).
package earth.terrarium.tempad.common.block;

import com.mojang.authlib.GameProfile;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.api.tva_device.impl.BlockChrononContent;
import earth.terrarium.tempad.api.tva_device.impl.MultiversalChrononHandler;
import earth.terrarium.tempad.common.config.CommonConfig;
import earth.terrarium.tempad.common.data.MetronomeData;
import earth.terrarium.tempad.common.menu.MetronomeMenu;
import earth.terrarium.tempad.common.menu.MetronomeMenuData;
import earth.terrarium.tempad.common.registries.ModBlocks;
import earth.terrarium.tempad.common.utils.CodecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MetronomeBe extends BlockEntity implements MenuProvider {
    @Nullable
    public GameProfile owner = null;
    public boolean locked = true;
    public int initialChronons = 0;
    public int bootChronons = 0;
    public int localChronons = 0;
    public int localCapacity = 0;
    public int bootTime = 100;
    public final MetronomeItemHandler inventory = new MetronomeItemHandler();
    private LazyOptional<IItemHandler> inventoryCap = LazyOptional.of(() -> inventory);

    public MetronomeBe(BlockPos pos, BlockState state) {
        super(ModBlocks.metronomeBe.get(), pos, state);
    }

    public void tick() {
        if (!(level instanceof ServerLevel)) return;
        BlockPos blockPos = getBlockPos();
        BlockState blockState = getBlockState();
        if (bootTime > 0) {
            if (bootChronons >= CommonConfig.Metronome.jumpStartAmount()) {
                bootTime--;
                if (bootTime <= 0) {
                    bootChronons = 0;
                    MetronomeData energy = MetronomeData.get();
                    if (energy != null && owner != null && owner.getId() != null) {
                        energy.add(owner.getId(), GlobalPos.of(level.dimension(), blockPos), initialChronons);
                        this.initialChronons = 0;
                    }
                }
            } else {
                bootTime = 100;
            }
            level.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_ALL);
            this.setChanged();
            return;
        }

        if (level.getServer() != null && (level.getServer().getTickCount() + 1) % CommonConfig.Metronome.generationRate() == 0) {
            level.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_ALL);
            this.setChanged();
        }
        ChrononHandler metronome = ChrononHandler.of(this);
        if (metronome == null) return;
        int rate = CommonConfig.Metronome.transferRate();
        for (int i = 0; i <= 7; i++) {
            ChrononHandler target = ChrononHandler.of(inventory.getStackInSlot(i));
            if (target == null) continue;
            if (i < 4) {
                ChrononHandler.move(target, metronome, rate);
            } else {
                ChrononHandler.move(metronome, target, rate);
            }
        }
        for (Direction direction : Direction.values()) {
            ChrononHandler target = ChrononHandler.of(level.getBlockEntity(blockPos.relative(direction)));
            if (target != null) ChrononHandler.move(metronome, target, rate);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (owner != null) tag.put("Owner", CodecUtils.saveGameProfile(owner));
        tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("BootTime", bootTime);
        tag.putInt("BootChronons", bootChronons);
        tag.putInt("InitialChronons", initialChronons);
        tag.putBoolean("Locked", locked);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        owner = tag.contains("Owner", Tag.TAG_COMPOUND) ? CodecUtils.loadGameProfile(tag.getCompound("Owner")) : null;
        bootTime = tag.contains("BootTime") ? tag.getInt("BootTime") : 100;
        locked = tag.getBoolean("Locked");
        initialChronons = tag.getInt("InitialChronons");
        bootChronons = tag.getInt("BootChronons");
        localChronons = tag.getInt("LocalChronons");
        localCapacity = tag.getInt("LocalCapacity");
        inventory.deserializeNBT(tag.getCompound("Inventory"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        MetronomeData energy = MetronomeData.get();
        if (energy != null && owner != null) {
            tag.putInt("LocalChronons", energy.getStored(owner.getId()));
            tag.putInt("LocalCapacity", energy.getCapacity(owner.getId()));
        }
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /** Upstream {@code createContent}: null (Optional.empty on the client) without an owner. */
    @Nullable
    public MetronomeMenuData createContent() {
        if (owner == null || level == null) return null;
        return new MetronomeMenuData(owner.getId(), GlobalPos.of(level.dimension(), getBlockPos()), locked);
    }

    /** Upstream {@code ContentMenuProvider.openMenu}. */
    public void openMenu(ServerPlayer player) {
        MetronomeMenuData content = createContent();
        NetworkHooks.openScreen(player, this, buf -> buf.writeOptional(Optional.ofNullable(content), (b, data) -> data.encode(b)));
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MetronomeMenu(containerId, playerInventory, inventory,
                owner != null && owner.getId() != null ? new MetronomeDataContainer(owner.getId()) : new SimpleContainerData(2),
                createContent());
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ChrononHandler.CAPABILITY) {
            ChrononHandler handler = chrononHandler();
            if (handler == null) return LazyOptional.empty();
            return LazyOptional.of(() -> handler).cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) return inventoryCap.cast();
        return super.getCapability(cap, side);
    }

    @Nullable
    private ChrononHandler chrononHandler() {
        if (owner == null) return null;
        if (bootTime > 0) return BlockChrononContent.metronome(this, CommonConfig.Metronome.jumpStartAmount());
        if (Tempad.server() == null) return BlockChrononContent.metronomeClient(this);
        return new MultiversalChrononHandler(owner.getId());
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inventoryCap.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        inventoryCap = LazyOptional.of(() -> inventory);
    }
}
