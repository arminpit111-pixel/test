package dev.pinghud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

/** Einfaches Einstellungsmenü, öffnen mit /pinghud */
public class PingConfigScreen extends Screen {

	private static final String[] ANCHORS = {"Oben links", "Oben rechts", "Unten links", "Unten rechts"};

	private final Screen parent;

	public PingConfigScreen(Screen parent) {
		super(Text.literal("Ping HUD Einstellungen"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		PingConfig cfg = PingConfig.INSTANCE;
		int left = this.width / 2 - 155;
		int right = this.width / 2 + 5;
		int w = 150;
		int y = this.height / 2 - 80;
		int step = 24;

		// Linke Spalte: Schalter
		addDrawableChild(ButtonWidget.builder(label("HUD", cfg.enabled), b -> {
			cfg.enabled = !cfg.enabled;
			b.setMessage(label("HUD", cfg.enabled));
		}).dimensions(left, y, w, 20).build());

		addDrawableChild(ButtonWidget.builder(Text.literal("Position: " + ANCHORS[cfg.anchor]), b -> {
			cfg.anchor = (cfg.anchor + 1) % ANCHORS.length;
			b.setMessage(Text.literal("Position: " + ANCHORS[cfg.anchor]));
		}).dimensions(left, y + step, w, 20).build());

		addDrawableChild(ButtonWidget.builder(label("Graph", cfg.showGraph), b -> {
			cfg.showGraph = !cfg.showGraph;
			b.setMessage(label("Graph", cfg.showGraph));
		}).dimensions(left, y + step * 2, w, 20).build());

		addDrawableChild(ButtonWidget.builder(label("Statistik", cfg.showStats), b -> {
			cfg.showStats = !cfg.showStats;
			b.setMessage(label("Statistik", cfg.showStats));
		}).dimensions(left, y + step * 3, w, 20).build());

		addDrawableChild(ButtonWidget.builder(label("Hintergrund", cfg.background), b -> {
			cfg.background = !cfg.background;
			b.setMessage(label("Hintergrund", cfg.background));
		}).dimensions(left, y + step * 4, w, 20).build());

		addDrawableChild(ButtonWidget.builder(label("Textschatten", cfg.shadow), b -> {
			cfg.shadow = !cfg.shadow;
			b.setMessage(label("Textschatten", cfg.shadow));
		}).dimensions(left, y + step * 5, w, 20).build());

		// Rechte Spalte: Slider
		addDrawableChild(new ConfigSlider(right, y, w, "Abstand X", 0, 300, cfg.offsetX, v -> cfg.offsetX = (int) v, true));
		addDrawableChild(new ConfigSlider(right, y + step, w, "Abstand Y", 0, 300, cfg.offsetY, v -> cfg.offsetY = (int) v, true));
		addDrawableChild(new ConfigSlider(right, y + step * 2, w, "Größe", 0.5, 3.0, cfg.scale, v -> cfg.scale = (float) v, false));
		addDrawableChild(new ConfigSlider(right, y + step * 3, w, "Intervall ms", 500, 5000, cfg.intervalMs, v -> cfg.intervalMs = (int) v, true));
		addDrawableChild(new ConfigSlider(right, y + step * 4, w, "Grün bis ms", 20, 600, cfg.goodMax, v -> cfg.goodMax = (int) v, true));
		addDrawableChild(new ConfigSlider(right, y + step * 5, w, "Gelb bis ms", 20, 800, cfg.okMax, v -> cfg.okMax = (int) v, true));

		addDrawableChild(ButtonWidget.builder(Text.literal("Fertig"), b -> close())
				.dimensions(this.width / 2 - 100, y + step * 6 + 6, 200, 20).build());
	}

	private static Text label(String name, boolean on) {
		return Text.literal(name + ": " + (on ? "an" : "aus"));
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 105, 0xFFFFFFFF);
		ctx.drawCenteredTextWithShadow(this.textRenderer,
				Text.literal("Orange/Rot-Grenzen und Farben: config/pinghud.json"),
				this.width / 2, this.height / 2 + 82, 0xFFAAAAAA);
	}

	@Override
	public void close() {
		PingConfig.save();
		if (this.client != null) this.client.setScreen(parent);
	}

	private static class ConfigSlider extends SliderWidget {
		private final String name;
		private final double min;
		private final double max;
		private final boolean integer;
		private final Consumer<Double> setter;

		ConfigSlider(int x, int y, int w, String name, double min, double max, double current,
					 Consumer<Double> setter, boolean integer) {
			super(x, y, w, 20, Text.empty(), (current - min) / (max - min));
			this.name = name;
			this.min = min;
			this.max = max;
			this.integer = integer;
			this.setter = setter;
			updateMessage();
		}

		private double actual() {
			return min + this.value * (max - min);
		}

		@Override
		protected void updateMessage() {
			if (name == null) return; // wird vom Super-Konstruktor einmal zu früh aufgerufen
			double v = actual();
			String text = integer ? String.valueOf((int) Math.round(v)) : String.format("%.1f", v);
			setMessage(Text.literal(name + ": " + text));
		}

		@Override
		protected void applyValue() {
			double v = actual();
			setter.accept(integer ? (double) Math.round(v) : Math.round(v * 10.0) / 10.0);
		}
	}
}
