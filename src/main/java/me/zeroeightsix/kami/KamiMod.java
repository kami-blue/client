package me.zeroeightsix.kami;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
	modid = KamiMod.MODID,
	name = KamiMod.MODNAME,
	version = KamiMod.MODVER,
	updateJSON = KamiMod.UPDATE_JSON
)
public class KamiMod {
	// private constants
	private static final Logger log = LogManager.getLogger("KAMI Blue");
	private static final String UPDATE_JSON = "https://raw.githubusercontent.com/S-B99/kamiblue/assets/assets/updateChecker.json";
	private static final String MODNAME = "KAMI Blue";
	private static final String MODID = "kamiblue";
	private static final String MODVER = "v1.1.2-beta";
	// public constants
	public static final String APP_ID = "638403216278683661";

	// static instance
	@Mod.Instance
	private static KamiMod INSTANCE;

	// event handlers
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) { }

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) { }

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		KamiMod.log.info(MODNAME + " Mod initialized!\n");
	}

	/**
	 * @return Kami instance.
	 */
	public static KamiMod getInstance() {
		return INSTANCE;
	}
}
