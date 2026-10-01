# CokeChat 1.3.2 – Client- und Serverbefehle

## Verhalten und Ursache

CokeChat verwendet keine Vanilla-Befehlsliste. Minecraft liefert den aktuellen Server-Befehlsbaum; Fabric ergänzt registrierte Client-Befehle. Der Test einer Namenskollision hat jedoch gezeigt: Wenn ein Server denselben Wurzelbefehl registriert, können beim Zusammenführen die Unterbefehle des Client-Befehls fehlen. Die lokale Ausführung kennt sie weiterhin, während die gemeinsame Syntaxprüfung sie ablehnt.

Bei aktiviertem Custom Chat wird für einen exakt erkannten, nutzbaren Client-Befehlsnamen deshalb der aktive originale Fabric-Client-Dispatcher verwendet. Für Wurzelvervollständigung und andere Befehle bleibt der aktuelle gemeinsame Verbindungs-Dispatcher zuständig. Argumente, Redirects und dynamische Suggestion-Provider werden direkt übernommen. Syntaxfarben und Gebrauchshinweise verwenden denselben ausgewählten Dispatcher. Beim Dispatcherwechsel wird der Parse-Cache verworfen; beim Öffnen des Custom-Eingabefelds wird die bereits vorhandene Eingabe erneut geprüft.

Die Fabric-Command-API bleibt optional. Ohne sie wird ausschließlich der vorhandene Verbindungs-Dispatcher verwendet. CokeChat registriert keine Ersatzbefehle, erfindet keine Metadaten und greift nicht in den Ausführungs-/Sendepfad ein. Ein Mod ohne veröffentlichte Syntax kann weiterhin einen Vanilla-Unbekannt-Hinweis auslösen; CokeChat blockiert oder verändert seine Eingabe nicht.

## Überprüfung am 22.09.2026

- 37 JUnit-Tests erfolgreich; vollständiger Fabric-Client-Test unter Minecraft 26.1.2 erfolgreich.
- Tatsächliche installierte `CokeKnightAddons-0.16.0-MC26.1.2.jar` im isolierten Testclient geladen: `/cs` in der Vervollständigung, kein Syntaxfehler für `/cs`, Enter öffnet `lotus.arsenal.LoadoutScreen`.
- Zusätzliche Befehle über echte Fabric-Registrierungsereignisse: Unterbefehle, Argumente, Redirect/Alias und dynamisch wechselnde Client-Vorschläge.
- Regression für gleichnamige Client-/Serverwurzel: lokale Unterbefehle bleiben gültig; Ausführung der gemeinsamen Wurzel erreicht nur den Client-Handler.
- Testserver registriert Hypixel-ähnliche `/party invite`, `/warp hub`, `/play sb`. Sein Baum wird über die normale Verbindung empfangen. Benutzerdefinierte Server-Vorschläge werden über Minecrafts normale asynchrone Anfrage/Antwort abgefragt und nach Änderung erneut aktualisiert. Tab fügt die Originalantwort ein, Enter erreicht den Server-Handler unverändert.
- Ein Testmod ohne Brigadier-Metadaten erhält `cc_opaque arbitrary :fire: payload` unverändert über das Fabric-Eingabeereignis.
- Bestehende Popup-Prüfungen für `/`, ungültige/unvollständige Befehle, lange Vorschläge, Tab/Pfeile/Maus/Scrollen, GUI-Skalierungen, Fenstergrößen und Eingabepositionen ebenfalls erfolgreich.
- Screenshots unter `verification/1.3.2/` visuell geprüft; Client-Protokoll dort als `client-test.log`.
- Statischer Netzwerk-Audit: 200 Klassen inklusive eingebetteter Module, keine eigenen Netzwerk-API-Verweise. Die Testmod und fremde Mods sind nicht in der Release-JAR enthalten.

Ein Live-Test auf Hypixel wurde nicht durchgeführt. Die genannten Hypixel-ähnlichen Befehle sind ausschließlich Testserver-Fixtures; ihr Verhalten belegt den normalen Server-Befehls-/Provider-Pfad, nicht Hypixels aktuelles Befehlsangebot. Fehlende Server-Metadaten kann CokeChat nicht ergänzen. Der Testclient aktiviert für die übrigen Darstellungstests nur ausgewählte Ressourcenpakete; das im `/cs`-Screenshot leere Loadout ist deshalb kein Test der Waffenressourcen.

## Reproduktion

`gradlew test runClientGameTest` führt die Suite mit einer isolierten `/cs`-Fixture aus. Mit `-PcommandCompatibilityMods=<Ordner>` lädt der Test zusätzlich die dort abgelegten Mod-JARs. Für den dokumentierten Lauf enthielt der Ordner die installierte CokeKnightAddons-JAR sowie Fabric Language Kotlin 1.13.13. Bei vorhandenen CokeKnightAddons wird kein `/cs`-Ersatz registriert und die Suite verlangt das echte Loadout-Menü.

Die Versionsnummer ist als Eingabe des Ressourcen-Builds registriert, damit `fabric.mod.json` bei Versionswechseln zuverlässig aktualisiert wird. Die korrigierte Release-JAR meldet auch in den Mod-Metadaten 1.3.2.

Release SHA-256: `E8F6A311B408EFF14E62C2819F9279DFFB4C5F92CB21987E6CB296ADCCECE296`
