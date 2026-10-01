# CokeChat 1.3.1 — Befehlsvorschläge und Fehlerhinweise

## Ursache und Fix

Minecraft 26.1.2 verankert die Vorschlagsliste ursprünglich an `screen.height - 12` und die Syntax-/Fehlerhinweise an `screen.height - 27`. Diese Werte passen nach dem Verschieben und Skalieren des Custom-Eingabefelds nicht mehr. Außerdem wurde der graue Inline-Vervollständigungstext außerhalb des skalierten Eingabefelds gezeichnet.

Bei aktiviertem CokeChat übernimmt jetzt genau ein Popup die Darstellung von Vorschlägen oder Hinweisen. Es verwendet die tatsächlichen, begrenzten Eingabefeldkoordinaten und GUI-Pixel statt unveränderter Vanilla-Bildschirmpositionen. Die ursprüngliche Popup-Zeichnung wird für dieses Eingabefeld unterdrückt; bei ausgeschaltetem CokeChat bleibt Vanilla zuständig.

- Fünf GUI-Pixel Abstand oberhalb des Eingabefelds, Begrenzung an allen Bildschirmrändern.
- Lange Vorschläge und Hinweise werden mit Minecrafts Textumbruch inklusive Formatierungen auf die verfügbare Breite umgebrochen.
- Bei zu wenig Höhe bleibt das Popup im Bildschirm; weitere Zeilen sind mit dem Mausrad erreichbar. Kleine Markierungen zeigen zusätzlichen Inhalt an.
- Direkt am oberen Rand reserviert das Eingabefeld während der Befehls-/Vorschlagsanzeige vorübergehend Platz darüber. Die gespeicherten X-/Y-Werte werden nicht geändert.
- Hintergrund, Rundung und Schrift folgen dem CokeChat-Eingabestil; die aktuelle Auswahl wird cyan hervorgehoben.
- Das Popup wird nach Verlauf und Eingabefeld mit einem eigenen, wieder aufgehobenen Clipping-Bereich gezeichnet. Der Verlauf kann es nicht abschneiden.
- Tab, Auf/Ab, Escape, ausgewählte Vorschläge, deren Ersetzungsbereiche und die Narration bleiben in Minecrafts bestehender CommandSuggestions-/Brigadier-Logik. Mausklicks verwenden die tatsächlich gezeichneten Zeilen und werden vor den Chat-Hover-Aktionen verarbeitet.
- Inline-Vervollständigung und Texteingabe sind auf das sichtbare Eingabefeld begrenzt. Bei Größen-/Schriftänderungen wird der Cursor wieder in dessen sichtbaren Ausschnitt gescrollt; große Schrift erhält die erforderliche Mindesthöhe.

## Prüfung

37 JUnit-Tests einschließlich einer Geometriematrix aus fünf Bildschirmbreiten, fünf Höhen, vier Schriftgrößen sowie verschiedenen Randpositionen. Zusätzliche Tests sichern hohe Eingabefelder und leere Popups ab.

Fabric-Client-Test unter Minecraft 26.1.2 / Java 25:

- `/`, unvollständiger Befehl, ungültiges Zahlenargument, unbekannter langer Befehl.
- Zwanzig absichtlich lange lokale Vorschläge mit mehrzeiliger Darstellung.
- Auf/Ab und Tab, Einfügen per Mausklick, Mausrad im Popup ohne Scrollen des Chatverlaufs.
- Relative und absolute Positionierung; links oben, rechts oben, rechts unten und unterer Bildschirmrand.
- Fenster 854×480, 1280×720 und 640×360; GUI-Skalierungen 1, 2 und 3 (Minecraft begrenzt die effektive Skalierung bei kleinen Fenstern selbst).
- Schriftgrößen 9, 12, 14 und 24; deaktivierter Custom Chat.
- Bestehende Tests für Emojis, Filter, History, Compact Chat, Peek, Leseposition und unveränderten ausgehenden Text.

Die Testbefehle werden nur im lokalen Test-Dispatcher registriert und nicht abgesendet. Eine Live-Hypixel-Sitzung mit sämtlichen Drittanbieter-Mods wurde nicht getestet.

Screenshots und Client-Testprotokoll liegen unter `verification/1.3.1/`. Die Screenshots wurden visuell geprüft. Der statische Netzwerk-Audit über 198 Klassen inklusive eingebetteter Fabric-Module findet keine eigenen Netzwerk-API-Verweise; dies ist kein Betriebssystem-Paketmitschnitt.

JAR SHA-256: `80686FA68E1C1CEBC79FA491A4C539E59FF3F0EC4D6CF45E31B2B00063203628`
