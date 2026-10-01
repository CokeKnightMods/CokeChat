# CokeChat 1.4.0 — Befehle, Darstellung und Compact Chat

Update vom 1. Oktober 2026 am bestehenden Projekt. Minecraft 26.1.2, Fabric Loader 0.19.5, Fabric API 0.155.3+26.1.2, offizielle Minecraft-Namen und Java 25.

## Änderungen

- **Befehle anderer Mods:** Custom Chat analysiert den aktiven Fabric-Client-Dispatcher über `ClientCommands.getActiveDispatcher()` und den aktuellen Verbindungs-Dispatcher unabhängig. Ein in einer Quelle gültiger Befehl wird nicht wegen der anderen Quelle als fehlerhaft dargestellt. Es gibt keine Sonderbehandlung einzelner Mods oder Befehlsnamen im Produktionscode.
- **Gemeinsame Vervollständigung:** Unterbefehle und dynamische Vorschläge beider Quellen erscheinen in einer Liste. Brigadier-Ersetzungsbereiche, Tooltips und Weiterleitungen bleiben erhalten. Lokale Ergebnisse erscheinen auch vor einer langsamen Serverantwort; veraltete Antworten überschreiben keine neuere Eingabe. Gleiche Einfügungen werden zusammengefasst.
- **Ruhigere Hinweise:** Beim Tippen eines Root-Befehls erscheint kein Fehlerkasten. Unbekannte Roots, unvollständige Befehle und Argumentfehler werden unterschieden. Tatsächliche Argumentfehler verwenden eine kurze Meldung ohne wiederholte Befehlskette. Der Verlauf reserviert oberhalb solcher Hinweise Platz; Vorschläge bleiben eine normale darüberliegende Auswahlliste.
- **Positionierung:** Vorschläge und Hinweise hängen am tatsächlichen Custom-Eingabefeld, werden auf den Bildschirm begrenzt und außerhalb des Verlaufs-Clippings gezeichnet. Lange Hinweise brechen um; der sichtbare Bereich ist begrenzt und scrollbar. Tab, Pfeiltasten, Mausauswahl und Einfügen bleiben erhalten.
- **Copy Chat:** Kopiert lesbaren Text ohne Minecraft-Formatcodes, auch bei wörtlichen `§`-Codes. Rohdaten und visuelle Formatierung bleiben unverändert.
- **Chat Peek:** Verwendet dieselbe `ChatOpening`-Implementierung und dieselben Open-Chat-Einstellungen für Distanz, Dauer, Easing, Nachrichten-Fade, Verzögerung und Staffelung. Der bisherige separate Peek-Dauerwert wird nicht mehr verwendet. Mausverhalten und eigener Peek-Scrollstand bleiben erhalten.
- **Appearance:** Unabhängige Schalter **Chat Border** und **Input Field Border**. Der vorhandene Schalter **Chat Side Bar / Scroll Bar** steuert zusätzlich den schmalen linken Nachrichtenstreifen. Nur die sichtbaren Ränder/Leisten verschwinden; Hintergrund, Eingabe und Scrollen bleiben aktiv. Neue Felder erhalten bei alten Konfigurationen Standardwerte.
- **Compact Chat:** Nur unmittelbar aufeinanderfolgende gleiche Komponenten mit passender Quelle/Markierung werden gruppiert. Unterschiede in Farbe und Formatierung bleiben relevant. Jedes Duplikat startet das Zeitfenster neu, während der ursprüngliche Animationsbeginn erhalten bleibt. Die Vorschau verwendet dasselbe rollende Zeitfenster. Nicht aufeinanderfolgende Nachrichten werden nicht zusammengezogen.

## Ursache und erhaltenes Verhalten

Die vorherige Dispatcher-Auswahl entschied sich für genau eine Quelle. Sie konnte daher weder beide Bäume unabhängig bewerten noch deren Vorschläge vollständig zusammenführen. Die Vanilla-Fehlerdarstellung übernahm außerdem ausführliche Brigadier-Kontexte. Die neue Integration verwendet beide Original-Dispatcher nur zum Lesen von Metadaten und erhält Slash-/Cursorpositionen. Validierung blockiert kein Absenden. Die vorhandene Minecraft-/Fabric-Ausführung einschließlich lokaler Priorität und Server-Routing wurde nicht ersetzt; es wurde kein neuer Netzwerkcode eingeführt.

## Prüfung

- `gradlew test runClientGameTest -PcommandCompatibilityMods=…`: erfolgreich; **46 Unit-Tests**, keine Fehler.
- `gradlew build`: erfolgreich. Release-Metadaten und Settings-Anzeige: **1.4.0**.
- Minecraft-Clienttest mit den installierten Mod-JARs **Athen 0.3.4+26.1**, **CokeKnightAddons 0.16.0-MC26.1.2** und **Fabric Language Kotlin 1.14.1+kotlin.2.4.20**.
- Tatsächliches `/athen` und `/ath_` ohne Fehlkasten, `/ath`-Vervollständigung, Unterbefehle und Öffnen der Athen-Konfiguration. Tatsächliches `/cs` erkannt und lokal geöffnet.
- Gemeinsame Client-/Server-Roots, dynamische Provider, Aliase, asynchrone Antwortreihenfolge, unveränderte Eingabe ohne Metadaten und Client-/Server-Ausführung geprüft.
- Server-Testbefehle `/party`, `/warp` und `/play` stammen aus einem integrierten Testserver. **Kein authentifizierter Live-Test auf Hypixel**; tatsächlich verfügbare Hypixel-Metadaten hängen weiterhin vom Server ab.
- GUI-Skalierungen, Popup-Grenzen, Eingabepositionen, Umbruch, Maus/Tab, Emoji-Regressionen, Scrollanker, Nachrichtenserien und Peek geprüft.
- Compact-Timer-Neustart über mehrere Duplikate, Ablauf, Formatunterschiede und stabiler Animationsbeginn geprüft. Echtes Kopieren, alle vier Randkombinationen, ausgeblendete Seitenleiste und gemeinsame Peek-Animation im Client geprüft. Speichern/Laden und Migration der Schalter durch Unit-Tests geprüft.
- Netzwerk-Audit: **206 Klassen** einschließlich eingebetteter Fabric-Module ohne zusätzliche Netzwerk-API-Referenzen.

Screenshots, Testresultate und Logs liegen unter `verification/1.4.0/`. Der Test verwendet ein lokales Fabric-Testprofil; Authentifizierungswarnungen anderer Mods für dieses Profil sind kein erfolgreicher Onlinedienst-Test.

Release-JAR SHA-256:

```text
9B8268170314FBA029233F91B0DB8D2EE0A86B8F5D788CDDFA544877DAF75696
```
