package dev.mrturtle.spatial.config;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class SpatialConfig {
	public String _c1 = "Overrides for item shapes, see default value for example of format";
	@SerializedName("shape_overrides")
	public HashMap<String, String> shapeOverrides = new HashMap<>();
	public String _c2 = "When enabled all items don't stack, are you sure you want to do this?";
	@SerializedName("brutal_mode")
	public boolean brutalMode = false;
}
