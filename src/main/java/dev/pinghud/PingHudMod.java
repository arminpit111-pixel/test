package dev.pinghud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingHudMod implements ClientModInitializer {

	public static final String MOD_ID = "pinghud";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		PingConfig.load();

		ClientTickEvents.END_CLIENT_TICK.register(PingTracker::tick);

		HudElementRegistry.attachElementAfter(
				VanillaHudElements.MISC_OVERLAYS,
				Identifier.of(MOD_ID, "ping_hud"),
				PingHud::render);

		// /pinghud            -> Einstellungsmenü
		// /pinghud toggle     -> HUD an/aus
		// /pinghud reset      -> Einstellungen zurücksetzen
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommandManager.literal("pinghud")
						.executes(ctx -> {
							MinecraftClient mc = MinecraftClient.getInstance();
							mc.send(() -> mc.setScreen(new PingConfigScreen(null)));
							return 1;
						})
						.then(ClientCommandManager.literal("toggle").executes(ctx -> {
							PingConfig.INSTANCE.enabled = !PingConfig.INSTANCE.enabled;
							PingConfig.save();
							ctx.getSource().sendFeedback(Text.literal(
									"Ping HUD: " + (PingConfig.INSTANCE.enabled ? "an" : "aus")));
							return 1;
						}))
						.then(ClientCommandManager.literal("reset").executes(ctx -> {
							PingConfig.reset();
							ctx.getSource().sendFeedback(Text.literal("Ping HUD: Einstellungen zurückgesetzt"));
							return 1;
						}))));

		LOGGER.info("Ping HUD geladen");
	}
}
