// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/data/client/ModLang.kt: the guide entries (bookLang and the
// companion values the guide reads). Translated to Java for Forge 1.19.2. Only the keys and arguments are kept: the
// English text is upstream's datagen output, served at runtime from the upstream jar's lang (TP-D1). Upstream builds
// each value once (val) or on every read (get(), for those with config numbers or late items); every method here
// builds on every call, which reads the same text.
package earth.terrarium.tempad.client.screen.guide;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.common.config.CommonConfigCache;
import earth.terrarium.tempad.common.registries.ModItems;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.text.NumberFormat;

public final class GuideLang {
    private GuideLang() {
    }

    /** Upstream {@code bookLang(key, value, args)}: items link to their page (CHANGE_PAGE), strings are highlighted. */
    public static Component bookLang(String key, Object... args) {
        String finalKey = ModItems.knowledgeProjector.get().getDescriptionId() + "." + key;
        Object[] converted = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof RegistryObject<?> holder) arg = holder.get();
            if (arg instanceof Item item) {
                converted[i] = Component.translatable(item.getDescriptionId()).withStyle(Style.EMPTY
                        .withColor(Tempad.HIGHLIGHTED_ORANGE.getValue())
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, String.valueOf(ForgeRegistries.ITEMS.getKey(item)))));
            } else if (arg instanceof String string) {
                converted[i] = Component.literal(string).withStyle(style -> style.withColor(Tempad.HIGHLIGHTED_ORANGE.getValue()));
            } else {
                converted[i] = arg instanceof Component ? arg : null;
            }
        }
        return Component.translatable(finalKey, converted);
    }

    /** Upstream {@code Number.format()}. */
    private static String format(Number number) {
        return NumberFormat.getInstance().format(number);
    }

    public static Component credits() { return bookLang("credits"); }
    public static Component travelAdvisory() { return bookLang("travel_advisory"); }
    public static Component iron() { return bookLang("iron"); }
    public static Component steel() { return bookLang("steel"); }
    public static Component overview() { return bookLang("overview"); }
    public static Component crafting() { return bookLang("crafting"); }
    public static Component usage() { return bookLang("usage"); }

    // chronon cell
    public static Component cellOverview() { return bookLang("cell_overview"); }
    public static Component cellCrafting() { return bookLang("cell_crafting"); }
    public static Component cellUsage() {
        return bookLang("cell_usage", ModItems.chrononGenerator, ModItems.chronometer, ModItems.metronome, format(CommonConfigCache.ChrononCell.capacity()));
    }

    // chronon generator
    public static Component chrononGenOverview() { return bookLang("chronongen_overview"); }
    public static Component chrononGenCrafting() { return bookLang("chronongen_crafting", ModItems.chrononCell); }
    public static Component chrononGenUsage() { return bookLang("chronongen_usage", ModItems.chronometer); }
    public static Component chrononGenStorage() {
        return bookLang("chronon_generator_storage", format(CommonConfigCache.ChrononGenerator.capacity()));
    }

    // location card
    public static Component cardOverview() { return bookLang("card_overview"); }
    public static Component cardCrafting() { return bookLang("card_crafting", ModItems.timeSteel); }
    public static Component cardUsage() {
        return bookLang("card_usage", ModItems.timedoorMarker, ModItems.chronomark, ModItems.tempad, ModItems.timedoorMarker, ModItems.chronomark, ModItems.tempad);
    }
    public static Component cardUsage2() {
        return bookLang("card_usage_2", ModItems.timedoorProjector, ModItems.tempad, ModItems.tempad, ModItems.cardWallet, ModItems.timedoorProjector);
    }
    public static Component cardUsage3() { return bookLang("card_usage_3"); }

    // card wallet
    public static Component walletOverview() { return bookLang("wallet_overview"); }
    public static Component walletCrafting() { return bookLang("wallet_crafting", ModItems.locationCard); }
    public static Component walletUsage() { return bookLang("wallet_usage", ModItems.locationCard); }
    public static Component walletUsage2() {
        return bookLang("wallet_usage_2", ModItems.tempad, ModItems.tempad, ModItems.timedoorMarker, ModItems.chronomark, ModItems.tempad, ModItems.tempad);
    }
    public static Component walletUsage3() { return bookLang("wallet_usage_3", ModItems.tempad); }

    // timedoor marker
    public static Component markerOverview() { return bookLang("marker_overview"); }
    public static Component markerCrafting() { return bookLang("marker_crafting"); }
    public static Component markerUsage() {
        return bookLang("marker_usage", ModItems.locationCard, ModItems.locationCard, ModItems.timedoorProjector, ModItems.tempad, ModItems.chronomark, ModItems.locationCard);
    }
    public static Component markerUsage2() { return bookLang("marker_usage_2"); }

    // timedoor projector
    public static Component projectorOverview() { return bookLang("projector_overview", ModItems.tempad); }
    public static Component projectorCrafting() { return bookLang("projector_crafting", ModItems.chrononCell); }
    public static Component projectorUsage() { return bookLang("projector_usage", ModItems.locationCard); }
    public static Component projectorUsage2() { return bookLang("projector_usage_2"); }
    public static Component projectorTimedoors() { return bookLang("projector_timedoors"); }
    public static Component projectorTimedoors2() { return bookLang("projector_timedoors_2"); }

    // location broadcaster
    public static Component locationBroadcastersOverview() { return bookLang("location_broadcasters_overview", ModItems.tempad); }
    public static Component locationBroadcastersCrafting() { return bookLang("location_broadcasters_crafting"); }
    public static Component locationBroadcastersUsage() { return bookLang("location_broadcasters_usage", ModItems.tempad); }
    public static Component locationBroadcastersUsage2() { return bookLang("location_broadcasters_toggle"); }

    // time steel
    public static Component timeSteelOverview() { return bookLang("time_steel_overview"); }
    public static Component timeSteelCrafting() { return bookLang("time_steel_crafting"); }
    public static Component timeSteelCrafting2() { return bookLang("time_steel_tech_discovery"); }

    // chronon battery
    public static Component chrononBatteryOverview() { return bookLang("chronon_battery_overview", ModItems.chrononCell); }
    public static Component chrononBatteryCrafting() { return bookLang("chronon_battery_crafting", ModItems.timeSteel, ModItems.chrononCell); }
    public static Component chrononBatteryUsage() {
        return bookLang("chronon_battery_usage", ModItems.chrononGenerator, ModItems.chronometer, ModItems.metronome, format(CommonConfigCache.Battery.capacity()));
    }

    // chronometer
    public static Component chronometerOverview() { return bookLang("chronometer_overview"); }
    public static Component chronometerCrafting() { return bookLang("chronometer_crafting", ModItems.timeSteel, ModItems.chrononBattery); }
    public static Component chronometerUsage() { return bookLang("chronometer_usage", ModItems.chrononGenerator); }
    public static Component chronometerInternalStorage() {
        return bookLang("chronometer_internal_storage", format(CommonConfigCache.Chronometer.capacity()));
    }

    // chronomark
    public static Component chronomarkOverview() {
        return bookLang("chronomark_overview", ModItems.timedoorMarker, format(CommonConfigCache.Chronomark.maxOffset()));
    }
    public static Component chronomarkCrafting() { return bookLang("chronomark_crafting", ModItems.timeSteel); }
    public static Component chronomarkUsage() {
        return bookLang("chronomark_usage", ModItems.locationCard, ModItems.locationCard, ModItems.timedoorProjector, ModItems.tempad, ModItems.timedoorMarker, ModItems.locationCard);
    }
    public static Component chronomarkUsage2() { return bookLang("chronomark_usage_2"); }

    // tempad
    public static Component tempadOverview() { return bookLang("tempad_overview", ModItems.timeTwister); }
    public static Component tempadCrafting() { return bookLang("tempad_crafting", ModItems.timeSteel, ModItems.chrononBattery); }
    public static Component tempadUsage() { return bookLang("tempad_usage"); }
    public static Component tempadUsage2() { return bookLang("tempad_usage_2", ModItems.locationCard, ModItems.cardWallet); }
    public static Component tempadTimedoors() { return bookLang("tempad_timedoors"); }
    public static Component tempadTimedoors2() { return bookLang("tempad_timedoors_2", ModItems.workstation); }
    public static Component tempadApps() { return bookLang("tempad_apps"); }
    public static Component tempadAppTeleport() { return bookLang("tempad_app_teleport", ModItems.timedoorMarker, ModItems.chronomark); }
    public static Component tempadAppTeleport2() { return bookLang("tempad_app_teleport_2"); }
    public static Component tempadAppTeleport3() { return bookLang("tempad_app_teleport_3", ModItems.locationCard); }
    public static Component tempadAppNewLocation() { return bookLang("tempad_app_new_location"); }
    public static Component tempadAppTravelTimeline() { return bookLang("tempad_app_travel_timeline", ModItems.timeTwister, ModItems.timeTwister); }
    public static Component tempadAppSettings() { return bookLang("tempad_app_settings"); }
    public static Component tempadAppPortalSetup() {
        return bookLang("tempad_app_portal_setup", ModItems.workstation, format(CommonConfigCache.Tempad.maxOffset()), ModItems.workstation);
    }

    // workstation
    public static Component workstationOverview() { return bookLang("workstation_overview", ModItems.tempad); }
    public static Component workstationCrafting() { return bookLang("workstation_crafting", ModItems.timeSteel); }
    public static Component workstationUsage() { return bookLang("workstation_usage", ModItems.tempad, ModItems.tempad); }
    public static Component workstationUsage2() { return bookLang("workstation_usage_2", ModItems.tempad, ModItems.timedoorProjector); }
    public static Component workstationUsage3() {
        return bookLang("workstation_usage_3", ModItems.tempad, format(CommonConfigCache.Tempad.maxOffset()));
    }

    // time twister
    public static Component timeTwisterOverview() { return bookLang("time_twister_overview"); }
    public static Component timeTwisterCrafting() { return bookLang("time_twister_crafting", ModItems.timeSteel, ModItems.chrononBattery); }
    public static Component timeTwisterUsage() { return bookLang("time_twister_usage"); }
    public static Component timeTwisterUsage2() { return bookLang("time_twister_usage_2"); }
    public static Component timeTwisterUsage3() { return bookLang("time_twister_usage_3", ModItems.tempad, ModItems.tempad, ModItems.tempad); }

    // screening device
    public static Component screeningDeviceOverview() { return bookLang("screening_device_overview", ModItems.locationBroadcaster); }
    public static Component screeningDeviceCrafting() { return bookLang("screening_device_crafting", ModItems.timeSteel); }
    public static Component screeningDeviceUsage() {
        return bookLang("screening_device_usage", ModItems.locationBroadcaster, ModItems.tempad, ModItems.screeningDevice);
    }

    // metronome
    public static Component metronomesOverview() { return bookLang("metronomes_overview"); }
    public static Component metronomesCrafting() { return bookLang("metronomes_crafting", ModItems.timeSteel); }
    public static Component metronomesUsage() { return bookLang("metronomes_usage", ModItems.chrononCell, ModItems.chrononBattery); }
    public static Component metronomesUsage2() { return bookLang("metronomes_booted", format(CommonConfigCache.Metronome.capacity())); }
    public static Component metronomesMultiCharge() { return bookLang("metronomes_multi_charge"); }
    public static Component metronomesNoMultiCharge() { return bookLang("metronomes_no_multi_charge"); }
}
