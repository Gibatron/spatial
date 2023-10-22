package dev.mrturtle.spatial.config;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class SpatialConfig {
	public String _c1 = "Overrides for item shapes, see default value for example of format";
	@SerializedName("shape_overrides")
	public HashMap<String, String> shapeOverrides = new HashMap<>();
}
