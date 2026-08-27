package yesman.epicfight.data;

import net.neoforged.neoforge.data.event.GatherDataEvent;

import yesman.epicfight.main.EpicFightMod;

/// Data generation event handler.
/// On Fabric, data generation uses a different system (FabricDataGenerator).
/// The pre-generated data files are included in src/main/resources.
/// This handler is a no-op stub to maintain source parity with NeoForge.
public final class DataEvents {
	private DataEvents() {}

	public static void epicfight$gatherData(GatherDataEvent event) {
		// Fabric does not use NeoForge's GatherDataEvent for data generation.
		// Pre-generated data files are included in src/main/resources.
	}
}
