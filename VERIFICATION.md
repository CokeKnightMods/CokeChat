# Verification — 2026-09-22

Release: CokeChat 1.2.0 / Minecraft 26.1.2 / Fabric Loader 0.19.5 / Java 25.

## Results

- Major implementation phases compiled successfully.
- Latest full `build runClientGameTest` completed successfully in 40 seconds.
- Final build after bounds/unbound-key guards completed successfully in 6 seconds.
- JUnit: 28 tests, zero failures/errors/skips (13 core, 3 compact, 2 favorites, 7 motion/state, 3 SkyBlock).
- Actual Minecraft launched and ran the integrated-world client test.
- Final static network audit: 183 compiled classes including nested Fabric modules, no audited network API references.
- Source scan: no networking/send APIs added. The release has no game-test code or full Fabric/networking API module.

## New integration coverage in Minecraft

- Multiple simultaneous messages animate; compact duplicates retain the first message's animation identity and a correct count of five.
- Long-duration (2,000 ms) message entry rendered and captured; disabled animations also exercised.
- Hover copy uses the complete original text of a single message, including literal emoji codes, without the duplicate counter.
- Hover delete hides the full duplicate group while preserving all 65 raw messages.
- Normal wheel scroll targets seven lines. Shift precision adds 1.75 lines. Releasing Shift retains the transition.
- Peek opens, stays visible while held, and fades out after release. Repeated quick open/close succeeds.
- Peek works over the existing ChatScreen and directly in gameplay, without creating/replacing a screen.
- Normal chat draft and exact scroll target (8.75 lines) remain unchanged after using and closing Peek.
- Peek copy hits the displayed row; Peek uses the same seven-line normal and 1.75-line precision scroll behavior. The Right Shift hold key does not itself trigger the precision modifier.
- Wrapped text at enlarged font/emoji size and changed chat position still copies the full original message correctly.
- Delete from the differently sized Peek hides an entire wrapped duplicate group while leaving all 68 original messages intact.
- New Animations, Chat Peek and Message Actions settings render.
- Preview hover actions work: Copy returns the sample original; Delete removes the sample group only. Actual game chat remains unchanged. The user's prior clipboard text is restored at test completion.

## Regression coverage

The existing tests still cover the full standard-color emoji catalog and every bitmap font, emoji favorites, search navigation, counter selection, RGB color picker, custom bitmap emoji, missing-font fallback, Visual Words, scaling, history scrolling, limits, and clearing. Actual Enter sends `gg :sob:` unchanged through Minecraft's vanilla path. Emoji completion inserts `:fire:` without sending a message.

Dungeon/Kuudra filtering retains Party Chat and raw history. OFF restores the display. Duplicate counts of three, ten and 100 (after eviction) are correct. Removing automatic detection from AUTO changes it to OFF.

## Core motion/state coverage

Easing endpoints and short/long/zero durations; continuous rapid retargeting; normal/slow scrolling; releasing the modifier; both scroll boundaries; shrinking history during movement; identity-stable duplicate groups; group-only local deletion; retained raw objects; independent animation starts for 2,000 messages; invalid/missing new config values.

## Visual review

Current screenshots in `verification/` were inspected, including Peek, scaled/wrapped message actions, hover tooltips, settings and interactive preview before/after local deletion. The supplied CokeChat logo and Apple emojis remain included unchanged.

## Scope and limits

No live authenticated Hypixel run or third-party chat overhaul compatibility matrix was tested. The network audit is static bytecode/source inspection, not OS packet capture. Normal test-launcher Realms authentication/performance-counter warnings occur; the successful runs contain no CokeChat mixin or rendering errors.

Local Delete is display-only for the current chat state. Raw messages, signatures, server communication and the optional raw history archive remain unchanged. Preview samples never enter the game's chat queue. No Chatting code, assets, UI resources or dependency were used.

SHA-256 of `cokechat-1.2.0.jar`:

```text
1EF24EEAC570EFE7A3BB2DD161C2ECE156342064C77737D963235A0DDA984DAD
```
