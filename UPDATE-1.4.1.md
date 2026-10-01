# CokeChat 1.4.1 — Patchnotes

- Fokusmodus / Chat Filter vollständig entfernt: OFF/AUTO/DUNGEON/KUUDRA, automatische Run-Erkennung und die zugehörigen Einstellungen entfallen.
- Nachrichten werden nicht mehr aufgrund eines Dungeon- oder Kuudra-Runs ausgeblendet. Manuelles Ausblenden einzelner Nachrichten bleibt verfügbar.
- Alte Filterwerte in vorhandenen Konfigurationen werden ignoriert und beim nächsten Speichern entfernt. Andere Einstellungen bleiben erhalten.

Enthält außerdem die Änderungen aus 1.4.0:

- Gemeinsame Befehlsvorschläge und Syntaxprüfung für Fabric-Clientbefehle und Serverbefehle; keine Sonderbehandlung einzelner Mods.
- Kompakte Fehlerhinweise mit Abstand zum Chatverlauf; keine großen Fehlerkästen beim Tippen eines Befehlsnamens.
- Copy Chat kopiert Klartext ohne Minecraft-Farbcodes.
- Chat Peek verwendet die gleichen Animationen und Einstellungen wie Open Chat.
- Chat-Rand, Eingabefeld-Rand und Seiten-/Scrollleiste sind unabhängig schaltbar.
- Compact Chat gruppiert aufeinanderfolgende, gleich formatierte Nachrichten und startet das Zeitfenster mit jedem Duplikat neu. Scrollposition und Nachrichtenanimationen bleiben erhalten.

Geprüft: Build, 44 Unit-Tests und Minecraft-Clienttests mit Athen und CokeKnightAddons erfolgreich. Alte Filterkonfigurationen, ungefilterte Nachrichten, Menünavigation und bestehende Chat-Funktionen geprüft. Kein Live-Test auf Hypixel. Netzwerk-Audit: 198 Klassen ohne zusätzliche Netzwerk-API-Referenzen.

JAR SHA-256: `90530E9B1BC37711E408B6B308838E83B6E4F1C155ACD6CFC6AD21290C476CDB`.
