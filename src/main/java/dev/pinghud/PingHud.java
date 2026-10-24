package dev.pinghud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public final class PingHud {

	private static final int PAD = 3;
	private static final int GRAPH_H = 24;
	private static final int BAR_W = 2;

	private PingHud() {}

	public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
		MinecraftClient mc = MinecraftClient.getInstance();
		PingConfig cfg = PingConfig.INSTANCE;
		if (!cfg.enabled || mc.player == null || mc.options.hudHidden) return;

		TextRenderer font = mc.textRenderer;
		int last = PingTracker.last();

		// Hauptzeile
		String main;
		int mainColor;
		if (last < 0) {
			main = "Ping: ...";
			mainColor = 0xFFAAAAAA;
		} else {
			main = "Ping: " + last + " ms" + (PingTracker.fallback() ? " (Tab)" : "");
			mainColor = cfg.colorFor(last);
		}
		boolean blink = PingTracker.spikeRecently() && (System.currentTimeMillis() / 250) % 2 == 0;
		String spikeTag = PingTracker.spikeRecently() ? "  SPIKE!" : "";

		int lineH = font.fontHeight + 1;
		int[] samples = PingTracker.snapshot();

		String stats = null;
		if (cfg.showStats && samples.length > 0) {
			stats = "avg " + PingTracker.average() + "  max " + PingTracker.max() + "  spikes " + PingTracker.spikes();
		}

		int w = font.getWidth(main + spikeTag);
		if (stats != null) w = Math.max(w, font.getWidth(stats));
		int graphW = PingTracker.HISTORY * BAR_W;
		if (cfg.showGraph) w = Math.max(w, graphW);

		int h = lineH;
		if (stats != null) h += lineH;
		if (cfg.showGraph) h += GRAPH_H + 2;

		int boxW = w + PAD * 2;
		int boxH = h + PAD * 2;

		float s = cfg.scale;
		int sw = ctx.getScaledWindowWidth();
		int sh = ctx.getScaledWindowHeight();
		float px = (cfg.anchor == 1 || cfg.anchor == 3) ? sw - cfg.offsetX - boxW * s : cfg.offsetX;
		float py = (cfg.anchor == 2 || cfg.anchor == 3) ? sh - cfg.offsetY - boxH * s : cfg.offsetY;

		ctx.getMatrices().pushMatrix();
		ctx.getMatrices().translate(px, py);
		ctx.getMatrices().scale(s, s);

		if (cfg.background) ctx.fill(0, 0, boxW, boxH, 0x80000000);

		int x = PAD;
		int y = PAD;
		ctx.drawText(font, main, x, y, mainColor, cfg.shadow);
		if (!spikeTag.isEmpty()) {
			int tagColor = blink ? 0xFFFF5555 : 0xFFFFFFFF;
			ctx.drawText(font, spikeTag, x + font.getWidth(main), y, tagColor, cfg.shadow);
		}
		y += lineH;

		if (stats != null) {
			ctx.drawText(font, stats, x, y, 0xFFBBBBBB, cfg.shadow);
			y += lineH;
		}

		if (cfg.showGraph) {
			y += 2;
			int maxV = 200;
			for (int v : samples) maxV = Math.max(maxV, v);
			// dünne Grundlinie
			ctx.fill(x, y + GRAPH_H, x + graphW, y + GRAPH_H + 1, 0x60FFFFFF);
			int offset = PingTracker.HISTORY - samples.length;
			for (int i = 0; i < samples.length; i++) {
				int v = samples[i];
				int bh = Math.max(1, Math.round((float) v / maxV * GRAPH_H));
				int bx = x + (offset + i) * BAR_W;
				ctx.fill(bx, y + GRAPH_H - bh, bx + BAR_W, y + GRAPH_H, cfg.colorFor(v));
			}
		}

		ctx.getMatrices().popMatrix();
	}
}
