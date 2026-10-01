# CokeChat 1.5.0 — GUI-Redesign

## Bestandsaufnahme

Die bestehende Oberfläche bestand aus acht Screens und vier unterstützenden GUI-Komponenten. Der Hauptscreen hatte 91 Einstellungen/Aktionen in elf Kategorien. Diese 91 Einträge sind nach dem Umbau vollständig vorhanden.

| Bereich | Erhaltene Funktionen und Zustände |
| --- | --- |
| Einstellungen | Aktivierung, Appearance, Input, Animation, Compact Chat, History, Visual Words, Emojis, Peek, Message Actions, Advanced; globale Suche, Zahlen-Slider, genaue Eingabe, Auswahl, Text, Farben, Einzel-Reset, Speichern und Vorschau |
| Emoji-Bibliothek | Suche nach Namen/Emoji, Favoriten, Seiten, Hinzufügen, Bearbeiten, Nachladen, leere Ergebnisse |
| Emoji-Editor | ID, Aliase, Unicode, Bitmap-Font und Glyph; Speichern, lokale Overrides entfernen, Validierungs-/Dateifehler |
| Visual Words | Neue Regeln, Löschen, Auswahl, Reihenfolge, Aktivierung, Groß-/Kleinschreibung, Ganzwortsuche, Suchtext und Ersatz |
| Farbeditor | Farbrad, HEX/RGBA, RGB, Helligkeit, Alpha, Live-Vorschau, Apply, Cancel, Standardwerte |
| Genaue Eingabe | Direkte Werteingabe, Bereichs-/Formatprüfung, sofortige gültige Änderungen |
| Auswahl | Alle bisherigen Optionslisten; Rückkehr zum aufrufenden Screen |
| Vorschau | Öffnungsanimation, Nachrichten und Duplikate erzeugen, Scrollen, Kopieren, Ausblenden, Eingabefeld-Darstellung |

Die größten Probleme waren starre Koordinaten, uneinheitliche Bedienelemente, abgeschnittene Labels ohne klare Hierarchie, übergroße Optionslisten, schlechte Platznutzung kleiner Fenster und sofort ausgeführte Löschaktionen.

## Navigation und Aufbau

Die Reihenfolge ist jetzt: Chat → Appearance → Chat Input → Chat Animation → Compact Chat → History → Visual Words → Emojis → Chat Peek → Message Actions → Advanced. Gestaltung und Eingabe stehen direkt nebeneinander.

Ab 640 logischen Pixeln Breite erscheint eine Seitenleiste. Darunter ersetzt eine Kategorieauswahl die Seitenleiste. Sehr niedrige breite Fenster erhalten navigierbare Seiten für die Kategorien. Die globale Suche bleibt oben sichtbar und führt zum exakten Einstellungsfeld. Die Vollbildvorschau und Done stehen immer im Footer. Auf großen Fenstern erscheint zusätzlich die eingebettete Vorschau; auf kleinen Fenstern bleibt der Platz für Einstellungen frei.

Einstellungen liegen auf ruhigen Karten. Große Ansichten zeigen Label, Erklärung und Bedienfeld nebeneinander. Kleine Ansichten ordnen Label und Bedienfeld übereinander; die vollständige Erklärung steht am Feld als Tooltip. Optionslisten sind durchsuchbar und paginiert. Die Emoji-Definition verteilt sich auf die Tabs Identity und Bitmap font. Wortregeln können nun direkt nach oben und unten verschoben werden.

## Designsystem und Bedienung

- Hintergrund `#10151D`, Karten `#19212C`, Controls `#263240`, Text `#EAF1F5`, Sekundärtext `#ACBAC7`, Cyan-Akzent `#83DCE5`. Rot wird nur für Fehler und destruktive Aktionen eingesetzt.
- Einheitliche Abstände, 4–6 Pixel Rundung, 20–21 Pixel hohe Controls und vergrößerte Seitentitel. Das bestehende CokeChat-Logo bleibt erhalten.
- Gemeinsame Buttons, Slider, Textfelder, Seitengerüste, Tooltips und Bestätigungsdialoge. Eingabe/Cursor/Selektion/Narration bleiben auf Minecrafts EditBox aufgebaut; nur die Darstellung wurde ersetzt.
- Aktive Kategorie, Auswahl, Hover, Fokus und deaktivierte Buttons sind unterscheidbar. Hover-Übergänge dauern 80 ms und verzögern keine Aktion.
- Lange Labels werden begrenzt und bleiben vollständig in Tooltips beziehungsweise den fokussierbaren Controls verfügbar. Lange Auswahltexte werden paginiert; Fehlermeldungen erhalten einen eigenen Bereich.
- Tab/Shift+Tab, Enter/Space und die vorhandene Slider-Tastaturbedienung bleiben nutzbar. In der Vorschau wählen Page Up/Page Down eine Nachricht; Copy/Hide sind fokussierbare Buttons. Reset sample stellt ausgeblendete Beispielnachrichten wieder her.
- Löschen der History, Löschen einer Wortregel, Entfernen eines Emoji-Overrides und Verwerfen ungespeicherter Emoji-Änderungen erfordern eine Bestätigung. Escape bricht die Bestätigung ab.
- Der Farbeditor bleibt transaktional: Apply übernimmt, Cancel/Escape stellt den Ausgangswert wieder her. Ungültige Farbtexte deaktivieren Apply. Gültige andere Einstellungen bleiben wie bisher unmittelbar gespeichert.
- Dateioperationen sind lokal und synchron; hierfür wurde kein künstlicher Ladebildschirm eingeführt. Leere Suchlisten, ungültige Werte, Speicherstatus und deaktivierte Navigation sind explizite Zustände.

## Geänderte Dateien

Alle folgenden Pfade beziehen sich auf das Projektverzeichnis:

| Dateien | Änderung |
| --- | --- |
| `src/main/java/de/cokechat/gui/UiTheme.java` | Neue gemeinsame Farben, Textregeln, Abstände und Seitendarstellung |
| `src/main/java/de/cokechat/gui/ThemedEditBox.java` | Einheitliche Textfelddarstellung bei unverändertem Vanilla-Eingabeverhalten |
| `src/main/java/de/cokechat/gui/ConfirmActionScreen.java` | Gemeinsame Bestätigung mit sicherer Cancel-Vorauswahl |
| `src/main/java/de/cokechat/gui/StyleButton.java`, `SettingSlider.java` | Einheitliche Hover-, Fokus-, Auswahl- und Deaktiviert-Zustände |
| `src/main/java/de/cokechat/gui/CokeChatSettingsScreen.java` | Responsive Navigation, Karten, globale Suche, fokuserhaltende Aktualisierung und History-Bestätigung |
| `src/main/java/de/cokechat/gui/ChoiceScreen.java`, `ValueScreen.java` | Suchbare paginierte Auswahl und konsistente genaue Werteingabe |
| `src/main/java/de/cokechat/gui/EmojiSettingsScreen.java`, `EmojiEditorScreen.java` | Bibliothek, Favoriten, Tabs, Validierung, Entfernen-/Verwerfen-Bestätigung |
| `src/main/java/de/cokechat/gui/VisualWordsScreen.java` | Klare Regeledition, Reihenfolge in beide Richtungen, deaktivierte Grenzaktionen und Löschbestätigung |
| `src/main/java/de/cokechat/gui/ColorPickerScreen.java` | Responsive Anordnung, einheitliche Inputs und gesperrtes Apply bei ungültigen Werten |
| `src/main/java/de/cokechat/gui/PreviewScreen.java`, `PreviewChat.java` | Gemeinsames Layout, Tastaturauswahl, Copy/Hide und Zurücksetzen der lokalen Beispieldaten |
| `src/gametest/java/de/cokechat/GuiRedesignTest.java` | Neue GUI-Funktions-, Tastatur-, Bounds- und Überlagerungsprüfungen |
| `src/gametest/java/de/cokechat/CokeChatGameTest.java` | Bestehende Abläufe auf neue Navigation/Vorschauposition angepasst |
| `gradle.properties`, `README.md`, `UPDATE-1.5.0.md`, `GUI-REDESIGN.md` | Version und Dokumentation |

Chat- und Netzwerkimplementierung, Dispatcher, reale Nachrichtengruppierung, persistente Datenformate und Konfigurationsfelder wurden für dieses Redesign nicht verändert. Änderungen in PreviewChat betreffen ausschließlich die fiktive GUI-Vorschau.

## Verifikation

Die GUI-Testmatrix umfasst 854×480 / GUI 2, 640×480 / GUI 2, 1280×720 / GUI 2, 1920×1080 / GUI 3, 1024×768 / GUI 1 und 1280×480 / GUI 2. Sie prüft alle elf Kategorien einschließlich Einstellungsseiten sowie alle eigenen Screens auf Widget-Grenzen und Überschneidungen.

Zusätzliche Abläufe: leere Emoji-Suche, ungültige Definition, Emoji speichern/entfernen, Abbruch von Löschungen, Wortregel hinzufügen/verschieben/entfernen, Auswahl nur über Tastatur sowie Tastaturaktionen in der Vorschau. Die bestehenden Tests für Farbeingabe/Cancel/Apply, Emoji-Favoriten, Chatvorschau, Scrollen, Befehle anderer Mods, Compact Chat und Chat Peek bleiben Bestandteil des kompletten Clienttests.

Testprotokolle, Ergebnisse und Screenshots werden in `verification/1.5.0/` abgelegt. Die Oberfläche bleibt englisch wie bisher; getestet werden lange Texte, keine neu hinzugefügten Übersetzungen. Es handelt sich um lokale Clienttests, nicht um einen authentifizierten Hypixel-Test.
