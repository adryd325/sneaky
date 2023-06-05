package com.adryd.sneaky;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sneaky implements ModInitializer {
	public static final String MOD_ID = "sneaky";
	public static final String MOD_NAME;
	public static final Version MOD_VERSION;
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final FabricLoader loader = FabricLoader.getInstance();

	static {
		ModMetadata metadata = loader.getModContainer(MOD_ID).orElseThrow(RuntimeException::new).getMetadata();
		MOD_NAME = metadata.getName();
		MOD_VERSION = metadata.getVersion();
	}

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		IPList.INSTANCE.loadFromFile();
	}
}
