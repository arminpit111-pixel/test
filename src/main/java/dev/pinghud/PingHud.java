package dev.pinghud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/** Zeigt nur "123 ms" an, Farbe nach Höhe des Pings. */
public final class PingHud {

	// Farben (ARGB)
	private static final int GREEN = 0xFF55FF55;  // 0-99 ms
	private static final int YELLOW = 0xFFFFFF55; // 100-199 ms
	private static final int RED = 0xFFFF3333;    // ab 200 ms

	private PingHud() {}

	private static int colorFor(int ping) {
		if (ping < 100) return GREEN;
		if (ping < 200) return YELLOW;
		return RED;
	}

	public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
		MinecraftClient mc = MinecraftClient.getInstance();
		PingConfig cfg = PingConfig.INSTANCE;
		if (!cfg.enabled || mc.player == null || mc.options.hudHidden) return;

		TextRenderer font = mc.textRenderer;
		int last = PingTracker.last();

		String text = last < 0 ? "-- ms" : last + " ms";
		int color = last < 0 ? 0xFFAAAAAA : colorFor(last);

		int boxW = font.getWidth(text) + 2;
		int boxH = font.fontHeight + 2;

		float s = cfg.scale;
		int sw = ctx.getScaledWindowWidth();
		int sh = ctx.getScaledWindowHeight();
		float px = (cfg.anchor == 1 || cfg.anchor == 3) ? sw - cfg.offsetX - boxW * s : cfg.offsetX;
		float py = (cfg.anchor == 2 || cfg.anchor == 3) ? sh - cfg.offsetY - boxH * s : cfg.offsetY;

		ctx.getMatrices().pushMatrix();
		ctx.getMatrices().translate(px, py);
		ctx.getMatrices().scale(s, s);
		ctx.drawText(font, text, 1, 1, color, cfg.shadow);
		ctx.getMatrices().popMatrix();
	}
}
