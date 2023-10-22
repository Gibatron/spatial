package dev.mrturtle.spatial.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.mrturtle.spatial.Spatial;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;

public class ConfigManager {
	private static final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("spatial.json");

	public static SpatialConfig config;

	public static void loadConfig() {
		try {
			File configFile = configPath.toFile();
			if (configFile.exists()) {
				Gson gson = new Gson();
				FileReader reader = new FileReader(configFile);
				config = gson.fromJson(reader, SpatialConfig.class);
				reader.close();
			} else {
				createDefaultConfig();
			}
		} catch (Exception e) {
			Spatial.LOGGER.error("Something went wrong while loading config file!");
			e.printStackTrace();
		}
	}

	public static void createDefaultConfig() {
		File configFile = configPath.toFile();
		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			FileWriter writer = new FileWriter(configFile);
			config = getDefaultConfig();
			writer.write(gson.toJson(config));
			writer.close();
		} catch (Exception e) {
			Spatial.LOGGER.error("Something went wrong while saving config file!");
			e.printStackTrace();
		}
	}

	public static SpatialConfig getDefaultConfig() {
		SpatialConfig config = new SpatialConfig();
		config.shapeOverrides.put("hopefully_this_is_not_a_real_mod_id:example_item", "xxx\n x \nxxx");
		return config;
	}
}
