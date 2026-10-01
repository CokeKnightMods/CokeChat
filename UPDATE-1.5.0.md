# CokeChat 1.5.0 — Neue Oberfläche

- Gesamte Einstellungs-GUI mit einheitlichem dunklem Design, Cyan-Akzent und dem vorhandenen CokeChat-Logo überarbeitet.
- Responsive Seitenleiste auf großen Fenstern; kompakte Kategorieauswahl auf kleinen Fenstern.
- Chat-Design, Eingabefeld und Animationen in der Navigation zusammengeführt. Globale Suche führt direkt zur gesuchten Einstellung.
- Einheitliche Karten, Buttons, Textfelder, Slider, Tooltips, Fokus- und Deaktiviert-Zustände; dezente Hover-Animationen.
- Auswahlmenüs sind durchsuchbar und paginiert. Emoji-Editor mit Identity-/Bitmap-font-Tabs, klarer Validierung und Bestätigung vor Entfernen oder Verwerfen.
- Wortregeln übersichtlicher bearbeiten, nach oben/unten verschieben und vor dem Löschen bestätigen.
- Farbeditor mit konsistenter Eingabe und deaktiviertem Apply bei ungültigen Werten. Apply/Cancel/Reset behalten ihre bisherigen Funktionen.
- Lokale Vorschau mit Tastaturauswahl über Page Up/Page Down sowie Copy, Hide und Reset sample.
- History-Löschen benötigt eine Bestätigung. Alle 91 bestehenden Einstellungen/Aktionen bleiben verfügbar.
- Chatlogik, Netzwerklogik, Konfigurationsfelder und Datenformate unverändert.

Die vollständige Dateiübersicht, Bedienstruktur und Prüfmatrix stehen in [GUI-REDESIGN.md](GUI-REDESIGN.md).

Prüfung: Build, 44 Unit-Tests und der vollständige Minecraft-Clienttest mit Athen/CokeKnightAddons erfolgreich. Sechs Fenster-/GUI-Skalierungskombinationen, alle eigenen Screens, Einstellungsseiten, Tastaturabläufe, Löschbestätigungen und bestehende Chat-Regressionen geprüft. Netzwerk-Audit: 201 Klassen ohne zusätzliche Netzwerk-API-Referenzen.

Release-JAR SHA-256: `C05820978D38236EEA3E844AD595DEB0C8578DDABD98D2ECC26E84D67DB7611C`.
