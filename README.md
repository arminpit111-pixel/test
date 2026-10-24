# Ping HUD (Fabric, Minecraft 1.21.11)

Zeigt den **echten Ping zum Spielserver** als HUD – auch auf EU-Servern, wenn du aus Asien spielst.

## Warum die anderen Mods dir immer ~30 ms zeigen
Die meisten Ping-Mods lesen den Ping aus der Tab-Liste. Den meldet der Server bzw. der Proxy selbst –
bei dir ist das offenbar nur die Strecke zum nächsten (asiatischen) Proxy-Knoten und ändert sich kaum.

Diese Mod misst selbst: Sie schickt jede Sekunde eine kleine Anfrage (dieselbe, die das Statistik-Menü
sendet) an den Server und stoppt die Zeit, bis die Antwort da ist. Diese Anfrage läuft durch den Proxy
bis zum echten Spielserver. Spikes siehst du dadurch live.

Hinweis: Der Server bearbeitet die Anfrage beim nächsten Server-Tick (0–50 ms Wartezeit). Der Wert
liegt daher im Schnitt ca. 25 ms über einem reinen Netzwerk-Ping, Schwankungen von bis zu 50 ms
können allein daher kommen. Echte Spikes (100+ ms) sind trotzdem klar erkennbar.

## Bedienung
- `/pinghud` – Einstellungsmenü (Position, Größe, Abstand, Graph, Statistik, Intervall, Farbgrenzen)
- `/pinghud toggle` – HUD an/aus
- `/pinghud reset` – Standardwerte
- Weitere Einstellungen (genaue Farben, Grenzen für Orange/Rot): `.minecraft/config/pinghud.json`

Standardfarben: grün bis 200 ms, gelb bis 300, orange bis 450, rot darüber.
Spike = Messwert deutlich über dem Median der letzten Messungen → blinkendes „SPIKE!“ und Zähler.

## Bauen
Variante A (ohne Programmierumgebung): Ordner als GitHub-Repo hochladen → Reiter *Actions* →
Workflow *build* → die `.jar` unter *Artifacts* herunterladen.

Variante B (lokal, Java 21): `./gradlew build` → `build/libs/pinghud-1.0.0.jar`

## Installieren
`pinghud-1.0.0.jar` (nicht die `-sources.jar`) zusammen mit der Fabric API für 1.21.11 in den
`mods`-Ordner deines Fabric-Profils legen.

## Falls der Build meckert
Ich konnte das Projekt nicht selbst kompilieren. Falls Gradle eine Version nicht findet, die
aktuellen Werte für 1.21.11 auf https://fabricmc.net/develop eintragen (`gradle.properties`).
