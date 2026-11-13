// Modified for the Forge 1.19.2 / Create 0.5.1 port (backportedPowerGrid, 2026); changes listed in PORT_NOTES.md.
// Port: Create 6 registers contraption types in CreateRegistries.CONTRAPTION_TYPE; Create 0.5.1 has a plain static map
// (ContraptionType.register(String, Supplier)). The id string is the one Create 6 writes into contraption NBT
// ("powergrid:solar_panel"), so saved solar panel contraptions keep upstream's "Type". The map is a plain HashMap, so
// the type is added from the common setup's enqueueWork (main thread; PowerGridImpl.commonSetup), not during the
// parallel mod construction, and before any level loads.
package org.patryk3211.powergrid.collections;

import com.simibubi.create.content.contraptions.ContraptionType;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.electricity.solarpanel.SolarPanelBearingContraption;

public class ModdedContraptions {
    public static ContraptionType SOLAR_PANEL;

    public static void register() {
        if(SOLAR_PANEL == null)
            SOLAR_PANEL = ContraptionType.register(PowerGrid.asResource("solar_panel").toString(), SolarPanelBearingContraption::new);
    }
}
