# CokeChat 1.4.2 — Compact Chat

- Gleiche Nachrichten werden innerhalb des eingestellten Zeitfensters auch dann zusammengefasst, wenn andere Nachrichten dazwischenkommen.
- Die Gruppe erscheint an der Position der neuesten Wiederholung: `Hallo → Tschüss → Hallo` wird zu `Tschüss → Hallo ×2`.
- Jede Wiederholung startet den Timer dieser Nachricht neu. Andere Nachrichten verlängern ihren Timer nicht.
- Unterschiedliche Farben, Formatierungen und relevante Nachrichten-Metadaten bleiben getrennt.
- Der ursprüngliche Animationsbeginn der Gruppe bleibt erhalten. Rohverlauf, Gruppen-Ausblenden und Scrollanker bleiben Bestandteil der bestehenden Implementierung.
- Die lokale Vorschau verwendet ebenfalls das neue Verhalten; beim Ausblenden einer Vorschaugruppe werden nur ihre tatsächlichen Mitglieder entfernt.
- Enthält die Änderungen aus 1.4.1 einschließlich des vollständig entfernten Fokus-/Chat-Filters.

Prüfung: Build, 44 Unit-Tests und vollständiger Minecraft-Clienttest mit Athen/CokeKnightAddons erfolgreich. Übergreifende Gruppierung, Neuaufbau, Timergrenzen, Zähler, Rohverlauf und bestehende Scroll-/Animationsprüfungen bestanden.

JAR SHA-256: `04037B977F00165B1FC8F1361E4BD67F7EAAF54DC0B3D09DADB9804F323FA70D`.
