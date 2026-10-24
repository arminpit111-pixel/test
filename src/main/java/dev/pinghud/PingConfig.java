package dev.pinghud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Einstellungen, gespeichert in .minecraft/config/pinghud.json */
public class PingConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("pinghud.json");

	public static PingConfig INSTANCE = new PingConfig();

	// Anzeige
	public boolean enabled = true;
	/** 0 = oben links, 1 = oben rechts, 2 = unten links, 3 = unten rechts */
	public int anchor = 0;
	public int offsetX = 4;
	public int offsetY = 4;
	public float scale = 1.0f;
	public boolean background = true;
	public boolean shadow = true;
	public boolean showGraph = true;
	public boolean showStats = true;

	// Messung
	/** Abstand zwischen zwei Messungen in Millisekunden */
	public int intervalMs = 1000;

	// Farben: Grenzwerte in ms (Ping <= Grenze => Farbe)
	public int goodMax = 200;
	public int okMax = 300;
	public int badMax = 450;
	public String colorGood = "#55FF55";
	public String colorOk = "#FFFF55";
	public String colorBad = "#FFAA00";
	public String colorTerrible = "#FF5555";

	public int colorFor(int ping) {
		String hex;
		if (ping <= goodMax) hex = colorGood;
		else if (ping <= okMax) hex = colorOk;
		else if (ping <= badMax) hex = colorBad;
		else hex = colorTerrible;
		return 0xFF000000 | parse(hex);
	}

	private static int parse(String hex) {
		try {
			return Integer.parseInt(hex.replace("#", "").trim(), 16) & 0xFFFFFF;
		} catch (Exception e) {
			return 0xFFFFFF;
		}
	}

	public static void load() {
		try {
			if (Files.exists(FILE)) {
				PingConfig loaded = GSON.fromJson(Files.readString(FILE), PingConfig.class);
				if (loaded != null) INSTANCE = loaded;
			}
		} catch (Exception e) {
			PingHudMod.LOGGER.warn("Konnte pinghud.json nicht lesen, nutze Standardwerte", e);
		}
		save();
	}

	public static void save() {
		try {
			Files.writeString(FILE, GSON.toJson(INSTANCE));
		} catch (IOException e) {
			PingHudMod.LOGGER.warn("Konnte pinghud.json nicht speichern", e);
		}
	}

	public static void reset() {
		INSTANCE = new PingConfig();
		save();
	}
}
