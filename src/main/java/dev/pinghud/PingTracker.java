package dev.pinghud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Misst den Ping selbst: Wir schicken alle X ms eine Stats-Anfrage an den Server
 * und messen, wie lange die Antwort braucht. Diese Anfrage läuft durch den Proxy
 * bis zum echten Spielserver - deshalb zeigt sie den echten EU-Ping und nicht
 * den Tab-Listen-Wert (der bei Proxys oft nur Spieler->Proxy ist).
 */
public final class PingTracker {

	public static final int HISTORY = 60;
	private static final long TIMEOUT_NS = 5_000_000_000L;

	private static final AtomicLong pendingSince = new AtomicLong(0);
	private static long lastSendNs = 0;
	private static int timeouts = 0;

	private static final Object LOCK = new Object();
	private static final int[] ring = new int[HISTORY];
	private static int count = 0;
	private static int head = 0;

	private static volatile int lastPing = -1;
	private static volatile int maxPing = 0;
	private static volatile int spikes = 0;
	private static volatile long lastSpikeMs = 0;
	private static volatile boolean usingFallback = false;

	private PingTracker() {}

	/** Jeden Client-Tick aufrufen. */
	public static void tick(MinecraftClient client) {
		if (client.player == null || client.getNetworkHandler() == null) {
			reset();
			return;
		}

		long now = System.nanoTime();
		long pending = pendingSince.get();

		if (pending != 0 && now - pending > TIMEOUT_NS) {
			pendingSince.compareAndSet(pending, 0);
			timeouts++;
			pending = 0;
		}

		if (pending == 0 && now - lastSendNs >= PingConfig.INSTANCE.intervalMs * 1_000_000L) {
			lastSendNs = now;
			pendingSince.set(now);
			client.getNetworkHandler().sendPacket(
					new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.REQUEST_STATS));
		}

		// Falls der Server gar nicht auf Stats antwortet: Tab-Listen-Wert als Notlösung
		if (timeouts >= 3) {
			PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
			if (entry != null && entry.getLatency() > 0) {
				usingFallback = true;
				lastPing = entry.getLatency();
			}
		}
	}

	/** Wird vom Mixin aufgerufen, wenn die Server-Antwort ankommt (kann auf dem Netzwerk-Thread sein). */
	public static void onResponse() {
		long sent = pendingSince.getAndSet(0);
		if (sent == 0) return; // Antwort, auf die wir nicht gewartet haben

		int ms = (int) Math.round((System.nanoTime() - sent) / 1_000_000.0);
		timeouts = 0;
		usingFallback = false;
		add(ms);
	}

	private static void add(int ms) {
		synchronized (LOCK) {
			boolean spike = isSpike(ms);
			ring[head] = ms;
			head = (head + 1) % HISTORY;
			if (count < HISTORY) count++;
			lastPing = ms;
			if (ms > maxPing) maxPing = ms;
			if (spike) {
				spikes++;
				lastSpikeMs = System.currentTimeMillis();
			}
		}
	}

	/** Spike = deutlich über dem normalen Niveau der letzten Messungen (Median). */
	private static boolean isSpike(int ms) {
		if (count < 10) return false;
		int[] copy = snapshotUnlocked();
		int[] sorted = Arrays.copyOf(copy, copy.length);
		Arrays.sort(sorted);
		int median = sorted[sorted.length / 2];
		return ms > median * 1.5 + 30;
	}

	private static int[] snapshotUnlocked() {
		int[] out = new int[count];
		int start = (head - count + HISTORY) % HISTORY;
		for (int i = 0; i < count; i++) out[i] = ring[(start + i) % HISTORY];
		return out;
	}

	/** Messwerte, älteste zuerst. */
	public static int[] snapshot() {
		synchronized (LOCK) {
			return snapshotUnlocked();
		}
	}

	public static int last() { return lastPing; }
	public static int max() { return maxPing; }
	public static int spikes() { return spikes; }
	public static boolean spikeRecently() { return System.currentTimeMillis() - lastSpikeMs < 3000 && lastSpikeMs != 0; }
	public static boolean fallback() { return usingFallback; }
	public static int timeouts() { return timeouts; }

	public static int average() {
		int[] s = snapshot();
		if (s.length == 0) return -1;
		long sum = 0;
		for (int v : s) sum += v;
		return (int) (sum / s.length);
	}

	public static void reset() {
		pendingSince.set(0);
		lastSendNs = 0;
		timeouts = 0;
		synchronized (LOCK) {
			count = 0;
			head = 0;
		}
		lastPing = -1;
		maxPing = 0;
		spikes = 0;
		lastSpikeMs = 0;
		usingFallback = false;
	}
}
