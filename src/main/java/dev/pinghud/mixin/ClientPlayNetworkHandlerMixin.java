package dev.pinghud.mixin;

import dev.pinghud.PingTracker;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Der Server antwortet auf unsere Stats-Anfrage mit einem StatisticsS2CPacket.
 * Sobald die Antwort ankommt, stoppen wir die Uhr.
 */
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

	@Inject(method = "onStatistics", at = @At("HEAD"))
	private void pinghud$onStatistics(StatisticsS2CPacket packet, CallbackInfo ci) {
		PingTracker.onResponse();
	}
}
